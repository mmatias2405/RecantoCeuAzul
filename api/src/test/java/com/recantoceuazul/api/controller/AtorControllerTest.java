package com.recantoceuazul.api.controller;

import com.recantoceuazul.api.model.Ator;
import com.recantoceuazul.api.repository.AtorRepository;
import com.recantoceuazul.api.repository.ResidenciaRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(AtorController.class)
public class AtorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AtorRepository atorRepository;

    @MockBean
    private ResidenciaRepository residenciaRepository;

    @Test
    public void deveListarAtoresComSucesso() throws Exception {
        // Cria um ator fictício com todos os parâmetros esperados pelo construtor
        Ator ator = new Ator(
                "João Silva",
                "joao@example.com",
                "11999999999",
                "123456",
                "ADMIN",
                Set.of()
        );

        // Simula retorno do banco de dados
        when(atorRepository.findAll()).thenReturn(List.of(ator));

        // Executa a requisição e valida o resultado
        mockMvc.perform(get("/api/ator"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("João Silva"))
                .andExpect(jsonPath("$[0].email").value("joao@example.com"))
                .andExpect(jsonPath("$[0].papel").value("ADMIN"));
    }
}


