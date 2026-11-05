package com.recantoceuazul.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recantoceuazul.api.dto.AnaliseVazamentoResponseDTO;
import com.recantoceuazul.api.dto.InferenciaMlRequestDTO;
import com.recantoceuazul.api.dto.InferenciaMlResponseDTO;
import com.recantoceuazul.api.dto.LeituraTemporalDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class VazamentoService {

    public static final int MIN_LEITURAS_ANALISE = 3;
    public static final int LIMITE_JANELA_LEITURAS = 40;

    private static final double DEFAULT_MU_MED = 408.7336;
    private static final double DEFAULT_SD_MED = 53.1663;
    private static final double DEFAULT_MU_MAX = 483.7436;
    private static final double DEFAULT_SD_MAX = 62.2806;
    private static final double DEFAULT_MU_STD = 47.7795;
    private static final double DEFAULT_SD_STD = 8.2275;

    private static final ZoneId ZONA_BR = ZoneId.of("America/Sao_Paulo");
    private static final DateTimeFormatter FORMATADOR_INICIO_VAZAMENTO =
            DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm").withZone(ZONA_BR);

    private final InfluxDbService influxDbService;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    @Value("${ml.service.url:http://localhost:8000}")
    private String mlServiceUrl;

    @Value("${ml.python.bin:python3}")
    private String mlPythonBin;

    @Autowired
    public VazamentoService(InfluxDbService influxDbService, ObjectMapper objectMapper) {
        this.influxDbService = influxDbService;
        this.objectMapper = objectMapper;
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(3000);
        factory.setReadTimeout(5000);
        this.restTemplate = new RestTemplate(factory);
    }

    public VazamentoService(
            InfluxDbService influxDbService,
            ObjectMapper objectMapper,
            RestTemplate restTemplate,
            String mlServiceUrl,
            String mlPythonBin) {
        this.influxDbService = influxDbService;
        this.objectMapper = objectMapper;
        this.restTemplate = restTemplate;
        this.mlServiceUrl = mlServiceUrl;
        this.mlPythonBin = mlPythonBin;
    }

    public AnaliseVazamentoResponseDTO analisarVazamentoResidencia(Integer residenciaId) {
        Map<String, List<LeituraTemporalDTO>> leiturasTemporais;
        Map<String, List<Double>> leiturasCondominio = new LinkedHashMap<>();

        try {
            leiturasTemporais = influxDbService.buscarUltimasLeiturasTemporaisCondominio(LIMITE_JANELA_LEITURAS);
            if (leiturasTemporais != null && !leiturasTemporais.isEmpty()) {
                for (Map.Entry<String, List<LeituraTemporalDTO>> entry : leiturasTemporais.entrySet()) {
                    List<Double> valores = entry.getValue().stream()
                            .map(LeituraTemporalDTO::getVolumeLitros)
                            .collect(Collectors.toList());
                    leiturasCondominio.put(entry.getKey(), valores);
                }
            } else {
                leiturasTemporais = Collections.emptyMap();
                Map<String, List<Double>> legado = influxDbService.buscarUltimasLeiturasCondominio(LIMITE_JANELA_LEITURAS);
                if (legado != null) {
                    leiturasCondominio.putAll(legado);
                }
            }
        } catch (Exception e) {
            return AnaliseVazamentoResponseDTO.servicoIndisponivel(
                    residenciaId,
                    "Não foi possível consultar a série temporal de consumo no InfluxDB no momento."
            );
        }

        String chaveRes = String.valueOf(residenciaId);
        List<LeituraTemporalDTO> serieTemporalRes = leiturasTemporais.getOrDefault(chaveRes, List.of()).stream()
                .filter(p -> p != null && Double.isFinite(p.getVolumeLitros()) && p.getVolumeLitros() >= 0.0)
                .collect(Collectors.toList());

        List<Double> leiturasValidas;
        List<String> timestampsIso = null;
        if (!serieTemporalRes.isEmpty()) {
            leiturasValidas = serieTemporalRes.stream()
                    .map(LeituraTemporalDTO::getVolumeLitros)
                    .collect(Collectors.toList());
            if (serieTemporalRes.stream().allMatch(p -> p.getTimestamp() != null)) {
                timestampsIso = serieTemporalRes.stream()
                        .map(p -> p.getTimestamp().toString())
                        .collect(Collectors.toList());
            }
        } else {
            List<Double> brutas = leiturasCondominio.getOrDefault(chaveRes, List.of());
            leiturasValidas = brutas.stream()
                    .filter(v -> v != null && Double.isFinite(v) && v >= 0.0)
                    .collect(Collectors.toList());
        }

        if (leiturasValidas.size() < MIN_LEITURAS_ANALISE) {
            return AnaliseVazamentoResponseDTO.dadosInsuficientes(residenciaId, leiturasValidas.size());
        }

        VetorFeaturesCalculado calculado = calcularFeaturesPadronizadas(leiturasValidas, leiturasCondominio);

        InferenciaMlRequestDTO requestMl = new InferenciaMlRequestDTO(
                residenciaId,
                calculado.zMed,
                calculado.zMax,
                calculado.zStd,
                leiturasValidas,
                timestampsIso
        );

        InferenciaMlResponseDTO respostaMl;
        try {
            respostaMl = executarInferenciaMl(requestMl);
        } catch (Exception e) {
            return AnaliseVazamentoResponseDTO.servicoIndisponivel(
                    residenciaId,
                    "O motor preditivo de detecção de vazamentos está temporariamente indisponível."
            );
        }

        return montarRespostaDiagnostico(
                residenciaId,
                leiturasValidas.size(),
                calculado,
                respostaMl,
                serieTemporalRes
        );
    }

    VetorFeaturesCalculado calcularFeaturesPadronizadas(
            List<Double> leiturasResidencia,
            Map<String, List<Double>> leiturasCondominio) {

        List<EstatisticasDemanda> perfisCondominio = new ArrayList<>();
        if (leiturasCondominio != null) {
            for (List<Double> serie : leiturasCondominio.values()) {
                if (serie == null) continue;
                List<Double> filtrada = serie.stream()
                        .filter(v -> v != null && Double.isFinite(v) && v >= 0.0)
                        .collect(Collectors.toList());
                if (filtrada.size() >= MIN_LEITURAS_ANALISE) {
                    perfisCondominio.add(calcularEstatisticas(filtrada));
                }
            }
        }

        // Caso 1: Baseline empírico do condomínio disponível (>= 3 residências com dados suficientes).
        // Quando há >= 8 residências, apara os 20% maiores desvios para evitar que múltiplos vazamentos mascararem o baseline.
        if (perfisCondominio.size() >= 3) {
            EstatisticasDemanda atual = calcularEstatisticas(leiturasResidencia);
            List<EstatisticasDemanda> referencia = perfisCondominio;
            if (perfisCondominio.size() >= 8) {
                List<EstatisticasDemanda> ordenadosPorStd = new ArrayList<>(perfisCondominio);
                ordenadosPorStd.sort(Comparator.comparingDouble(p -> p.dStd));
                int corte = Math.max(3, (int) Math.ceil(ordenadosPorStd.size() * 0.80));
                referencia = ordenadosPorStd.subList(0, corte);
            }

            List<Double> listaMed = referencia.stream().map(p -> p.dMed).collect(Collectors.toList());
            List<Double> listaMax = referencia.stream().map(p -> p.dMax).collect(Collectors.toList());
            List<Double> listaStd = referencia.stream().map(p -> p.dStd).collect(Collectors.toList());

            double zMed = safeZScore(atual.dMed, media(listaMed), desvioPadraoAmostral(listaMed));
            double zMax = safeZScore(atual.dMax, media(listaMax), desvioPadraoAmostral(listaMax));
            double zStd = safeZScore(atual.dStd, media(listaStd), desvioPadraoAmostral(listaStd));
            return new VetorFeaturesCalculado(atual.dMed, atual.dMax, atual.dStd, zMed, zMax, zStd);
        }

        // Caso 2: Série longa de uma única residência (>= 12 pontos) -> janelas históricas internas
        if (leiturasResidencia.size() >= 12) {
            int tamanhoJanela = Math.max(MIN_LEITURAS_ANALISE, leiturasResidencia.size() / 4);
            int inicioAtual = leiturasResidencia.size() - tamanhoJanela;
            List<Double> janelaAtual = leiturasResidencia.subList(inicioAtual, leiturasResidencia.size());

            List<EstatisticasDemanda> janelasHistoricas = new ArrayList<>();
            for (int i = 0; i + tamanhoJanela <= inicioAtual; i += tamanhoJanela) {
                janelasHistoricas.add(calcularEstatisticas(leiturasResidencia.subList(i, i + tamanhoJanela)));
            }

            if (janelasHistoricas.size() >= 2) {
                EstatisticasDemanda atual = calcularEstatisticas(janelaAtual);
                List<Double> listaMed = janelasHistoricas.stream().map(p -> p.dMed).collect(Collectors.toList());
                List<Double> listaMax = janelasHistoricas.stream().map(p -> p.dMax).collect(Collectors.toList());
                List<Double> listaStd = janelasHistoricas.stream().map(p -> p.dStd).collect(Collectors.toList());

                double zMed = safeZScore(atual.dMed, media(listaMed), desvioPadraoAmostral(listaMed));
                double zMax = safeZScore(atual.dMax, media(listaMax), desvioPadraoAmostral(listaMax));
                double zStd = safeZScore(atual.dStd, media(listaStd), desvioPadraoAmostral(listaStd));
                return new VetorFeaturesCalculado(atual.dMed, atual.dMax, atual.dStd, zMed, zMax, zStd);
            }
        }

        // Caso 3: Poucas residências e série curta -> parâmetros históricos calibrados de referência
        EstatisticasDemanda atual = calcularEstatisticas(leiturasResidencia);
        double zMed = safeZScore(atual.dMed, DEFAULT_MU_MED, DEFAULT_SD_MED);
        double zMax = safeZScore(atual.dMax, DEFAULT_MU_MAX, DEFAULT_SD_MAX);
        double zStd = safeZScore(atual.dStd, DEFAULT_MU_STD, DEFAULT_SD_STD);
        return new VetorFeaturesCalculado(atual.dMed, atual.dMax, atual.dStd, zMed, zMax, zStd);
    }

    protected InferenciaMlResponseDTO executarInferenciaMl(InferenciaMlRequestDTO requestDto) throws Exception {
        String baseUrl = mlServiceUrl != null && mlServiceUrl.endsWith("/")
                ? mlServiceUrl.substring(0, mlServiceUrl.length() - 1)
                : mlServiceUrl;

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<InferenciaMlRequestDTO> entity = new HttpEntity<>(requestDto, headers);
            ResponseEntity<InferenciaMlResponseDTO> response = restTemplate.postForEntity(
                    baseUrl + "/predict",
                    entity,
                    InferenciaMlResponseDTO.class
            );
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            }
        } catch (Exception httpEx) {
            InferenciaMlResponseDTO fallback = tentarInferenciaCliLocal(requestDto);
            if (fallback != null) {
                return fallback;
            }
            throw httpEx;
        }

        throw new IllegalStateException("Resposta inválida do serviço de inferência ML.");
    }

    private InferenciaMlResponseDTO tentarInferenciaCliLocal(InferenciaMlRequestDTO requestDto) {
        try {
            Path scriptPath = localizarScriptPredictor();
            if (scriptPath == null) {
                return null;
            }
            String jsonInput = objectMapper.writeValueAsString(requestDto);
            ProcessBuilder pb = new ProcessBuilder(
                    mlPythonBin,
                    scriptPath.toAbsolutePath().toString(),
                    "--json",
                    jsonInput
            );
            Process process = pb.start();
            boolean finalizado = process.waitFor(8, TimeUnit.SECONDS);
            if (!finalizado) {
                process.destroyForcibly();
                return null;
            }
            if (process.exitValue() != 0) {
                return null;
            }
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String stdout = reader.lines().collect(Collectors.joining("\n")).trim();
                if (!stdout.isEmpty()) {
                    return objectMapper.readValue(stdout, InferenciaMlResponseDTO.class);
                }
            }
        } catch (Exception ignored) {
            // Se o fallback CLI não estiver disponível, preserva a exceção HTTP original
        }
        return null;
    }

    private Path localizarScriptPredictor() {
        List<Path> candidatos = List.of(
                Path.of("ml-service", "predictor.py"),
                Path.of("..", "ml-service", "predictor.py")
        );
        for (Path p : candidatos) {
            if (Files.isRegularFile(p)) {
                return p;
            }
        }
        return null;
    }

    private AnaliseVazamentoResponseDTO montarRespostaDiagnostico(
            Integer residenciaId,
            int quantidadeLeituras,
            VetorFeaturesCalculado calculado,
            InferenciaMlResponseDTO respostaMl,
            List<LeituraTemporalDTO> serieTemporal) {

        AnaliseVazamentoResponseDTO dto = new AnaliseVazamentoResponseDTO();
        dto.setResidenciaId(residenciaId);
        dto.setAnaliseDisponivel(true);
        dto.setQuantidadeLeituras(quantidadeLeituras);
        dto.setDemandaMediaLitros(arredondar(calculado.dMed, 2));
        dto.setDemandaMaximaLitros(arredondar(calculado.dMax, 2));
        dto.setDesvioPadraoLitros(arredondar(calculado.dStd, 2));

        boolean vazamento = respostaMl.isVazamentoDetectado();
        double prob = arredondar(respostaMl.getProbabilidadeVazamento(), 2);
        String risco = respostaMl.getNivelRisco() != null ? respostaMl.getNivelRisco() : "Baixo";

        dto.setVazamentoDetectado(vazamento);
        dto.setProbabilidadeVazamento(prob);
        dto.setNivelRisco(risco);
        dto.setFeaturesUtilizadas(respostaMl.getFeaturesUtilizadas());

        if (vazamento || prob >= 40.0) {
            String inicioIndicio = respostaMl.getInicioIndicioVazamento();
            if (inicioIndicio == null || inicioIndicio.isBlank()) {
                inicioIndicio = identificarInicioIndicioLocal(serieTemporal);
            }
            dto.setInicioIndicioVazamento(inicioIndicio);
        }

        if (vazamento && "Alto".equalsIgnoreCase(risco)) {
            dto.setStatus("Alerta: Provável Vazamento Detectado");
            dto.setMensagemOrientativa(
                    "Identificamos um padrão atípico acentuado na demanda de água da sua residência. "
                            + "Recomendamos fechar todas as torneiras e registros internos para verificar se o hidrômetro continua girando, "
                            + "inspecionar caixas acopladas, boias e tubulações, e acionar a manutenção caso necessário."
            );
        } else if (vazamento) {
            dto.setStatus("Alerta: Provável Vazamento Detectado");
            dto.setMensagemOrientativa(
                    "Detectamos oscilações acima do padrão habitual de consumo da residência. "
                            + "Verifique possíveis gotejamentos em torneiras, válvulas de descarga ou caixa d'água e acompanhe as próximas leituras no painel."
            );
        } else if ("Moderado".equalsIgnoreCase(risco)) {
            dto.setStatus("Rede Normal (Em Observação)");
            dto.setMensagemOrientativa(
                    "Sua rede hidráulica não apresenta vazamento confirmado, porém houve variação moderada recente no consumo. "
                            + "Mantenha o acompanhamento preventivo pelo painel."
            );
        } else {
            dto.setStatus("Rede Normal");
            dto.setMensagemOrientativa(
                    "O perfil de consumo da sua residência está dentro da normalidade esperada. "
                            + "Nenhum indício de vazamento foi identificado na rede hidráulica."
            );
        }

        return dto;
    }

    String identificarInicioIndicioLocal(List<LeituraTemporalDTO> serieTemporal) {
        if (serieTemporal == null || serieTemporal.size() < MIN_LEITURAS_ANALISE) {
            return null;
        }
        List<Double> valores = serieTemporal.stream()
                .map(LeituraTemporalDTO::getVolumeLitros)
                .collect(Collectors.toList());

        double mediana = percentil(valores, 50.0);
        List<Double> desviosAbs = valores.stream()
                .map(v -> Math.abs(v - mediana))
                .collect(Collectors.toList());
        double sigmaRob = Math.max(percentil(desviosAbs, 50.0) * 1.4826, 15.0);
        double limiar = mediana + 2.2 * sigmaRob;

        Integer onsetIdx = null;
        int toleranciaQueda = 1;
        for (int i = valores.size() - 1; i >= 0; i--) {
            if (valores.get(i) >= limiar) {
                onsetIdx = i;
                toleranciaQueda = 1;
            } else if (onsetIdx != null && toleranciaQueda > 0 && i > 0 && valores.get(i - 1) >= limiar) {
                toleranciaQueda--;
            } else if (onsetIdx != null) {
                break;
            }
        }

        if (onsetIdx == null) {
            for (int i = 0; i < valores.size(); i++) {
                if (valores.get(i) >= limiar) {
                    onsetIdx = i;
                    break;
                }
            }
        }
        if (onsetIdx == null) {
            int maxIdx = 0;
            for (int i = 1; i < valores.size(); i++) {
                if (valores.get(i) > valores.get(maxIdx)) {
                    maxIdx = i;
                }
            }
            onsetIdx = maxIdx;
        }

        Instant ts = serieTemporal.get(onsetIdx).getTimestamp();
        return ts != null ? FORMATADOR_INICIO_VAZAMENTO.format(ts) : null;
    }

    private static double percentil(List<Double> valores, double p) {
        if (valores.isEmpty()) return 0.0;
        List<Double> copia = new ArrayList<>(valores);
        Collections.sort(copia);
        double pos = (p / 100.0) * (copia.size() - 1);
        int baseIdx = (int) Math.floor(pos);
        double frac = pos - baseIdx;
        if (baseIdx + 1 < copia.size()) {
            return copia.get(baseIdx) + frac * (copia.get(baseIdx + 1) - copia.get(baseIdx));
        }
        return copia.get(baseIdx);
    }

    static EstatisticasDemanda calcularEstatisticas(List<Double> valores) {
        double med = media(valores);
        double max = valores.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
        double std = desvioPadraoAmostral(valores);
        return new EstatisticasDemanda(med, max, std);
    }

    static double media(List<Double> valores) {
        if (valores.isEmpty()) return 0.0;
        double soma = 0.0;
        for (double v : valores) {
            soma += v;
        }
        return soma / valores.size();
    }

    static double desvioPadraoAmostral(List<Double> valores) {
        if (valores.size() < 2) return 0.0;
        double m = media(valores);
        double somaQuad = 0.0;
        for (double v : valores) {
            double diff = v - m;
            somaQuad += diff * diff;
        }
        return Math.sqrt(somaQuad / (valores.size() - 1));
    }

    static double safeZScore(double valor, double mediaRef, double stdRef) {
        double denom = Math.abs(stdRef) > 1e-6 ? stdRef : 1.0;
        double z = (valor - mediaRef) / denom;
        double clamped = Math.max(-5.0, Math.min(5.0, z));
        return arredondar(clamped, 4);
    }

    private static double arredondar(double valor, int casas) {
        double fator = Math.pow(10.0, casas);
        return Math.round(valor * fator) / fator;
    }

    static class EstatisticasDemanda {
        final double dMed;
        final double dMax;
        final double dStd;

        EstatisticasDemanda(double dMed, double dMax, double dStd) {
            this.dMed = dMed;
            this.dMax = dMax;
            this.dStd = dStd;
        }
    }

    static class VetorFeaturesCalculado {
        final double dMed;
        final double dMax;
        final double dStd;
        final double zMed;
        final double zMax;
        final double zStd;

        VetorFeaturesCalculado(double dMed, double dMax, double dStd, double zMed, double zMax, double zStd) {
            this.dMed = dMed;
            this.dMax = dMax;
            this.dStd = dStd;
            this.zMed = zMed;
            this.zMax = zMax;
            this.zStd = zStd;
        }
    }
}
