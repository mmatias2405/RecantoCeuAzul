package com.recantoceuazul.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recantoceuazul.api.dto.AnaliseVazamentoResponseDTO;
import com.recantoceuazul.api.dto.InferenciaMlRequestDTO;
import com.recantoceuazul.api.dto.InferenciaMlResponseDTO;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class VazamentoService {

    public static final int MIN_LEITURAS_ANALISE = 3;
    public static final int LIMITE_JANELA_LEITURAS = 30;

    private static final double DEFAULT_MU_MED = 408.7336;
    private static final double DEFAULT_SD_MED = 53.1663;
    private static final double DEFAULT_MU_MAX = 483.7436;
    private static final double DEFAULT_SD_MAX = 62.2806;
    private static final double DEFAULT_MU_STD = 47.7795;
    private static final double DEFAULT_SD_STD = 8.2275;

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
        Map<String, List<Double>> leiturasCondominio;
        try {
            leiturasCondominio = influxDbService.buscarUltimasLeiturasCondominio(LIMITE_JANELA_LEITURAS);
        } catch (Exception e) {
            return AnaliseVazamentoResponseDTO.servicoIndisponivel(
                    residenciaId,
                    "Não foi possível consultar a série temporal de consumo no InfluxDB no momento."
            );
        }

        List<Double> brutas = leiturasCondominio.getOrDefault(String.valueOf(residenciaId), List.of());
        List<Double> leiturasValidas = brutas.stream()
                .filter(v -> v != null && Double.isFinite(v) && v >= 0.0)
                .collect(Collectors.toList());

        if (leiturasValidas.size() < MIN_LEITURAS_ANALISE) {
            return AnaliseVazamentoResponseDTO.dadosInsuficientes(residenciaId, leiturasValidas.size());
        }

        VetorFeaturesCalculado calculado = calcularFeaturesPadronizadas(leiturasValidas, leiturasCondominio);

        InferenciaMlRequestDTO requestMl = new InferenciaMlRequestDTO(
                residenciaId,
                calculado.zMed,
                calculado.zMax,
                calculado.zStd,
                leiturasValidas
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

        return montarRespostaDiagnostico(residenciaId, leiturasValidas.size(), calculado, respostaMl);
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

        // Caso 1: Baseline empírico do condomínio disponível (>= 3 residências com dados suficientes)
        if (perfisCondominio.size() >= 3) {
            EstatisticasDemanda atual = calcularEstatisticas(leiturasResidencia);
            List<Double> listaMed = perfisCondominio.stream().map(p -> p.dMed).collect(Collectors.toList());
            List<Double> listaMax = perfisCondominio.stream().map(p -> p.dMax).collect(Collectors.toList());
            List<Double> listaStd = perfisCondominio.stream().map(p -> p.dStd).collect(Collectors.toList());

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
            InferenciaMlResponseDTO respostaMl) {

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
