package com.recantoceuazul.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recantoceuazul.api.dto.MedicaoRequest;
import com.recantoceuazul.api.model.Ator;
import com.recantoceuazul.api.model.Medicao;
import com.recantoceuazul.api.model.Residencia;
import com.recantoceuazul.api.repository.MedicaoRepository;
import com.recantoceuazul.api.service.MedicaoService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MedicaoController.class)
public class MedicaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MedicaoRepository repo;

    @MockBean
    private MedicaoService medicaoService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void deveListarTodasMedicoes() throws Exception {
        Medicao m1 = new Medicao(100f, 10f, LocalDateTime.now(), new Residencia(), new Ator());
        Medicao m2 = new Medicao(200f, 20f, LocalDateTime.now(), new Residencia(), new Ator());
        List<Medicao> medicoes = Arrays.asList(m1, m2);

        when(repo.findAll()).thenReturn(medicoes);

        mockMvc.perform(get("/api/medicao"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void deveBuscarMedicaoPorId() throws Exception {
        Medicao medicao = new Medicao(150f, 15f, LocalDateTime.now(), new Residencia(), new Ator());
        when(repo.findById(1)).thenReturn(Optional.of(medicao));

        mockMvc.perform(get("/api/medicao/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.volumeAgua").value(150f));
    }

    @Test
    void deveBuscarMedicoesPorResidencia() throws Exception {
        Medicao m1 = new Medicao(120f, 12f, LocalDateTime.now(), new Residencia(), new Ator());
        when(medicaoService.listarMedicaoPorResidencia(1)).thenReturn(List.of(m1));

        mockMvc.perform(get("/api/medicao/residencia/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void deveCriarNovaMedicao() throws Exception {
        MedicaoRequest request = new MedicaoRequest(); // objeto DTO (mesmo que vazio, pois estamos apenas simulando)
        Medicao criada = new Medicao(300f, 30f, LocalDateTime.now(), new Residencia(), new Ator());

        when(medicaoService.registrarNovaMedicao(any(MedicaoRequest.class))).thenReturn(criada);

        mockMvc.perform(post("/api/medicao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.volumeAgua").value(300f));
    }

    @Test
    void deveAtualizarMedicaoExistente() throws Exception {
        Medicao existente = new Medicao(100f, 10f, LocalDateTime.now(), new Residencia(), new Ator());
        Medicao atualizada = new Medicao(200f, 20f, LocalDateTime.now(), new Residencia(), new Ator());

        when(repo.findById(1)).thenReturn(Optional.of(existente));
        when(repo.save(any(Medicao.class))).thenReturn(atualizada);

        mockMvc.perform(put("/api/medicao/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(atualizada)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.volumeAgua").value(200f));
    }

    @Test
    void deveDeletarMedicao() throws Exception {
        doNothing().when(repo).deleteById(1);

        mockMvc.perform(delete("/api/medicao/1"))
                .andExpect(status().isOk());

        verify(repo, times(1)).deleteById(1);
    }
}

