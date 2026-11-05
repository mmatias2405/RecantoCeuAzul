package com.recantoceuazul.web.controller;

import com.recantoceuazul.web.model.Ator;
import com.recantoceuazul.web.model.Residencia;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HomeController.class)
class HomeControllerVazamentoViewTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void deveRenderizarCardDeDiagnosticoDeVazamentosNoDashboardMorador() throws Exception {
        Residencia res = new Residencia();
        res.setId(1);
        res.setNumero(101);

        Ator morador = new Ator();
        morador.setId(3);
        morador.setNome("Ana Silva");
        morador.setPapel("MORAR");
        morador.setResidencias(Set.of(res));

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("usuarioLogado", morador);

        mockMvc.perform(get("/morador").param("residencia", "1").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Diagnóstico da Rede Hidráulica / Detecção de Vazamentos")))
                .andExpect(content().string(containsString("id=\"card-diagnostico-vazamento\"")))
                .andExpect(content().string(containsString("Orientação ao Morador:")));
    }
}
