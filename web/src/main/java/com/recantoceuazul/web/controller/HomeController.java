package com.recantoceuazul.web.controller;

import com.recantoceuazul.web.model.Abastecimento;
import com.recantoceuazul.web.model.Administrador;
import com.recantoceuazul.web.model.Setor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.List;

@Controller
public class HomeController {

    @Value("${api.url}")
    private String API_URL;
    private final RestTemplate restTemplate = new RestTemplate();

    @GetMapping("/")
    public String index(Model model) {
        ResponseEntity<Abastecimento[]> response = restTemplate.getForEntity(API_URL + "abastecimentos", Abastecimento[].class);
        List<Abastecimento> abastecimentos = Arrays.asList(response.getBody());
        model.addAttribute("abastecimentos", abastecimentos);
        ResponseEntity<Setor[]> responseSetor = restTemplate.getForEntity(API_URL + "setor", Setor[].class);
        List<Setor> setores = Arrays.asList(responseSetor .getBody());
        model.addAttribute("setores", setores);

        return "index";
    }

    @GetMapping("/login")
    public String login(Model model) {
        return "login";
    }

    @GetMapping("/consumo")
    public String consumo(Model model) {
        ResponseEntity<Setor[]> responseSetor = restTemplate.getForEntity(API_URL + "setor", Setor[].class);
        List<Setor> setores = Arrays.asList(responseSetor .getBody());
        model.addAttribute("setores", setores);
        return "registrarConsumo";
    }

    @GetMapping("/captacao")
    public String captacao(Model model) {
        return "registrarCaptacao";
    }

    @GetMapping("/setor")
    public String setor(Model model) {
        return "registrarSetor";
    }

    @PostMapping("/salvarconsumo")
    public String salvarConsumo(@ModelAttribute Abastecimento abastecimento, RedirectAttributes redirectAttributes) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Abastecimento> request = new HttpEntity<>(abastecimento, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(API_URL + "abastecimentos", request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                return "redirect:/"; // redireciona pra página inicial
            }
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().value() == 401) {
                redirectAttributes.addFlashAttribute("mensagem", "Houve um erro no Registro");
                return "redirect:/consumo"; // redireciona de volta pro login
            }
        }

        redirectAttributes.addFlashAttribute("mensagem", "Erro inesperado.");
        return "redirect:/consumo";
    }
    @PostMapping("/auth")
    public String auth(@ModelAttribute Administrador administrador, RedirectAttributes redirectAttributes) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Administrador> request = new HttpEntity<>(administrador, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(API_URL + "administrador/login", request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                String id = response.getBody(); // o ID retornado como string
                redirectAttributes.addFlashAttribute("mensagem", "Login bem-sucedido! ID: " + id);
                return "redirect:/login"; // redireciona pra página inicial
            }
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().value() == 401) {
                redirectAttributes.addFlashAttribute("mensagem", "Credenciais inválidas!");
                return "redirect:/login"; // redireciona de volta pro login
            }
        }

        redirectAttributes.addFlashAttribute("mensagem", "Erro inesperado.");
        return "redirect:/login";
    }

    @PostMapping("/salvar")
    public String salvar(@ModelAttribute Administrador administrador) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Administrador> request = new HttpEntity<>(administrador, headers);
        restTemplate.postForEntity(API_URL, request, Administrador.class);
        return "redirect:/";
    }
}
