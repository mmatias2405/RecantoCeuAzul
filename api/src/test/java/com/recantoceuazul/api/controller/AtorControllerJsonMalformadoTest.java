package com.recantoceuazul.api.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.recantoceuazul.api.repository.AtorRepository;
import com.recantoceuazul.api.repository.ResidenciaRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AtorController.class)
public class AtorControllerJsonMalformadoTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AtorRepository atorRepository;

    @MockBean
    private ResidenciaRepository residenciaRepository;

    @Test
    void deveRetornarBadRequestQuandoJsonMalformado() throws Exception {
        // 🔹 JSON propositalmente malformado (faltando chave de fechamento)
        String jsonInvalido = "{ \"email\": \"teste@teste.com\", \"senha\": 12345 ";

        // 🔹 Envia requisição POST com JSON inválido
        mockMvc.perform(post("/api/ator/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonInvalido))
                .andExpect(status().isBadRequest()); // Espera retorno 400 BAD REQUEST
    }
}
