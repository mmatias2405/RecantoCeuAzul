package com.recantoceuazul.api.controller;

import com.recantoceuazul.api.dto.AnaliseVazamentoResponseDTO;
import com.recantoceuazul.api.repository.ResidenciaRepository;
import com.recantoceuazul.api.service.VazamentoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VazamentoController.class)
class VazamentoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VazamentoService vazamentoService;

    @MockBean
    private ResidenciaRepository residenciaRepository;

    @Test
    void deveRetornarDiagnosticoDeVazamentoEmAmbasAsRotas() throws Exception {
        AnaliseVazamentoResponseDTO dto = new AnaliseVazamentoResponseDTO();
        dto.setResidenciaId(1);
        dto.setAnaliseDisponivel(true);
        dto.setVazamentoDetectado(true);
        dto.setStatus("Alerta: Provável Vazamento Detectado");
        dto.setProbabilidadeVazamento(91.45);
        dto.setNivelRisco("Alto");
        dto.setQuantidadeLeituras(30);
        dto.setMensagemOrientativa("Verifique registros e tubulações.");

        when(residenciaRepository.existsById(1)).thenReturn(true);
        when(vazamentoService.analisarVazamentoResidencia(1)).thenReturn(dto);

        mockMvc.perform(get("/api/residencia/1/vazamento"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.residenciaId").value(1))
                .andExpect(jsonPath("$.vazamentoDetectado").value(true))
                .andExpect(jsonPath("$.probabilidadeVazamento").value(91.45))
                .andExpect(jsonPath("$.nivelRisco").value("Alto"));

        mockMvc.perform(get("/api/residencias/1/vazamento"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("Alerta: Provável Vazamento Detectado"));
    }

    @Test
    void deveRetornarDadosInsuficientesComStatusOkAmigavel() throws Exception {
        AnaliseVazamentoResponseDTO insuficiente = AnaliseVazamentoResponseDTO.dadosInsuficientes(2, 1);

        when(residenciaRepository.existsById(2)).thenReturn(true);
        when(vazamentoService.analisarVazamentoResidencia(2)).thenReturn(insuficiente);

        mockMvc.perform(get("/api/residencias/2/vazamento"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.analiseDisponivel").value(false))
                .andExpect(jsonPath("$.status").value("Dados insuficientes para análise"))
                .andExpect(jsonPath("$.quantidadeLeituras").value(1));
    }

    @Test
    void deveRetornarNotFoundQuandoResidenciaNaoExistir() throws Exception {
        when(residenciaRepository.existsById(999)).thenReturn(false);

        mockMvc.perform(get("/api/residencias/999/vazamento"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("Residência não encontrada"));
    }
}
