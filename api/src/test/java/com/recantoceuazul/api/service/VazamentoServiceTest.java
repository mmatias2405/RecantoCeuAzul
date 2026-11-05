package com.recantoceuazul.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recantoceuazul.api.dto.AnaliseVazamentoResponseDTO;
import com.recantoceuazul.api.dto.InferenciaMlRequestDTO;
import com.recantoceuazul.api.dto.InferenciaMlResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class VazamentoServiceTest {

    private InfluxDbService influxDbService;
    private RestTemplate restTemplate;
    private VazamentoService vazamentoService;

    @BeforeEach
    void setUp() {
        influxDbService = Mockito.mock(InfluxDbService.class);
        restTemplate = Mockito.mock(RestTemplate.class);
        vazamentoService = new VazamentoService(
                influxDbService,
                new ObjectMapper(),
                restTemplate,
                "http://ml-service:8000",
                "python3"
        );
    }

    @Test
    void deveRetornarDadosInsuficientesQuandoMenosDeTresLeiturasNoInflux() {
        Map<String, List<Double>> dados = Map.of("1", List.of(410.5, 420.0));
        when(influxDbService.buscarUltimasLeiturasCondominio(VazamentoService.LIMITE_JANELA_LEITURAS))
                .thenReturn(dados);

        AnaliseVazamentoResponseDTO resp = vazamentoService.analisarVazamentoResidencia(1);

        assertFalse(resp.isAnaliseDisponivel());
        assertFalse(resp.isVazamentoDetectado());
        assertEquals("Dados insuficientes para análise", resp.getStatus());
        assertEquals(2, resp.getQuantidadeLeituras());
        verifyNoInteractions(restTemplate);
    }

    @Test
    void deveAnalisarRedeNormalComSucessoIntegrandoInfluxEMlService() {
        Map<String, List<Double>> condominio = new LinkedHashMap<>();
        condominio.put("1", List.of(390.0, 405.0, 412.0, 398.0, 401.0));
        condominio.put("2", List.of(415.0, 430.0, 422.0, 418.0, 425.0));
        condominio.put("3", List.of(375.0, 382.0, 390.0, 378.0, 385.0));

        when(influxDbService.buscarUltimasLeiturasCondominio(VazamentoService.LIMITE_JANELA_LEITURAS))
                .thenReturn(condominio);

        InferenciaMlResponseDTO mlResp = new InferenciaMlResponseDTO();
        mlResp.setVazamentoDetectado(false);
        mlResp.setClassePredita(0);
        mlResp.setProbabilidadeVazamento(8.25);
        mlResp.setNivelRisco("Baixo");
        mlResp.setFeaturesUtilizadas(Map.of("Z_D_Med", -0.02, "Z_D_Max", 0.05, "Z_D_Std", 0.12));

        when(restTemplate.postForEntity(
                eq("http://ml-service:8000/predict"),
                any(HttpEntity.class),
                eq(InferenciaMlResponseDTO.class)
        )).thenReturn(new ResponseEntity<>(mlResp, HttpStatus.OK));

        AnaliseVazamentoResponseDTO resp = vazamentoService.analisarVazamentoResidencia(1);

        assertTrue(resp.isAnaliseDisponivel());
        assertFalse(resp.isVazamentoDetectado());
        assertEquals("Rede Normal", resp.getStatus());
        assertEquals(8.25, resp.getProbabilidadeVazamento());
        assertEquals("Baixo", resp.getNivelRisco());
        assertEquals(5, resp.getQuantidadeLeituras());
        assertNotNull(resp.getDemandaMediaLitros());
    }

    @Test
    void deveRetornarAlertaQuandoModeloDetectarVazamentoDeRiscoAlto() {
        Map<String, List<Double>> condominio = Map.of(
                "1", List.of(410.0, 420.0, 415.0, 620.0, 650.0)
        );
        when(influxDbService.buscarUltimasLeiturasCondominio(VazamentoService.LIMITE_JANELA_LEITURAS))
                .thenReturn(condominio);

        InferenciaMlResponseDTO mlResp = new InferenciaMlResponseDTO();
        mlResp.setVazamentoDetectado(true);
        mlResp.setClassePredita(1);
        mlResp.setProbabilidadeVazamento(94.8);
        mlResp.setNivelRisco("Alto");
        mlResp.setFeaturesUtilizadas(Map.of("Z_D_Med", 1.75, "Z_D_Max", 2.66, "Z_D_Std", 5.0));

        when(restTemplate.postForEntity(
                eq("http://ml-service:8000/predict"),
                any(HttpEntity.class),
                eq(InferenciaMlResponseDTO.class)
        )).thenReturn(new ResponseEntity<>(mlResp, HttpStatus.OK));

        AnaliseVazamentoResponseDTO resp = vazamentoService.analisarVazamentoResidencia(1);

        assertTrue(resp.isAnaliseDisponivel());
        assertTrue(resp.isVazamentoDetectado());
        assertEquals("Alerta: Provável Vazamento Detectado", resp.getStatus());
        assertEquals("Alto", resp.getNivelRisco());
        assertEquals(94.8, resp.getProbabilidadeVazamento());
    }
}
