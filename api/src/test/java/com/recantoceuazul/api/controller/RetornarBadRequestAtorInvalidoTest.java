package com.recantoceuazul.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recantoceuazul.api.model.Ator;
import com.recantoceuazul.api.repository.AtorRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AtorController.class)
public class RetornarBadRequestAtorInvalidoTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AtorRepository atorRepository;

    @MockBean
    private com.recantoceuazul.api.repository.ResidenciaRepository residenciaRepository;

    @Test
    void deveRetornarUnauthorizedQuandoAtorInvalido() throws Exception {
        // 🔹 Simula um ator inválido (sem email e senha)
        Ator atorInvalido = new Ator();
        atorInvalido.setEmail(null);
        atorInvalido.setSenha(null);

        // 🔹 Simula que o repositório não encontra o ator
        when(atorRepository.findByEmailAndSenha(null, null)).thenReturn(java.util.Optional.empty());

        // 🔹 Envia requisição POST com ator inválido e espera 401 (não autorizado)
        mockMvc.perform(post("/api/ator/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(atorInvalido)))
                .andExpect(status().isUnauthorized());
    }
}
