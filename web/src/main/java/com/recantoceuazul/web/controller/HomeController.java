package com.recantoceuazul.web.controller;

import com.recantoceuazul.web.model.*;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.http.HttpHeaders;
import jakarta.servlet.http.HttpSession;


@Controller
public class HomeController {
    
    @Value("${api.url}")
    private String API_URL;
    private final RestTemplate restTemplate = new RestTemplate();
    
    @GetMapping("/")
    public String index(Model model) {
        return "index";
    }
    
    @GetMapping("/cadastro")
    public String cadastro(Model model) {
        return "cadastro";
    }
    @PostMapping("/cadastrar")
    public String postCadastrar(@ModelAttribute Ator novoAtor, RedirectAttributes redirectAttributes) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Ator> request = new HttpEntity<>(novoAtor, headers);
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(API_URL + "ator", request, String.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                redirectAttributes.addFlashAttribute("mensagemSucesso", "Cadastro Efetuado com sucesso");
                return "redirect:/"; // redireciona pra página inicial
            }
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().value() == 401) {
                redirectAttributes.addFlashAttribute("mensagem", "Houve um erro no Cadastro, tente novamente!");
                return "redirect:/cadastro";
            }
        }
        
        redirectAttributes.addFlashAttribute("mensagem", "Houve um erro inesperado no Cadastro, tente novamente!");
        return "redirect:/cadastro";
    }
    
    @PostMapping("/auth")
    public String auth(@ModelAttribute Ator ator, RedirectAttributes redirectAttributes, HttpSession session) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Ator> request = new HttpEntity<>(ator, headers);
        
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(API_URL + "ator/login", request, String.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                String id = response.getBody();
                ResponseEntity<Ator> responseAtor = restTemplate.getForEntity(API_URL + "ator/" + id, Ator.class);
                Ator atorLogado = responseAtor.getBody();
                if(atorLogado == null){
                    redirectAttributes.addFlashAttribute("mensagem", "Houve um erro inesperado.");
                    return "redirect:/";
                }
                session.setAttribute("usuarioLogado", atorLogado);
                if(atorLogado.getPapel() == null){
                    redirectAttributes.addFlashAttribute("mensagem", "O administrador do sistema ainda não autorizou seu acesso a plataforma, entre em contato com ele para normalizar a situação");
                    return "redirect:/";
                }
                if(atorLogado.getPapel().equals("ADMIN") || atorLogado.getPapel().equals("FISCA")){
                    return "redirect:/dashboard";
                }
                return "redirect:/morador?residencia=0";
            }
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().value() == 401) {
                redirectAttributes.addFlashAttribute("mensagem", "Usuário ou senha inválidos.");
                return "redirect:/"; 
            }
        }
        
        redirectAttributes.addFlashAttribute("mensagem", "Erro inesperado.");
        return "redirect:/";
    }
    @GetMapping("/morador")
    public String mostrarDashboardMorador(@RequestParam("residencia") Integer residenciaId, HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        Ator usuario = (Ator) session.getAttribute("usuarioLogado");
        if (usuario == null) {
            // Não está logado, redireciona para a home (login)
            redirectAttributes.addFlashAttribute("mensagem", "Faça Login para acessar essa página!");
            return "redirect:/";
        }
        if(usuario.getPapel() == null){
            redirectAttributes.addFlashAttribute("mensagem", "O administrador do sistema ainda não autorizou seu acesso a plataforma, entre em contato com ele para normalizar a situação");
            return "redirect:/";
        }
        if (usuario.getPapel().equals("ADMIN") || usuario.getPapel().equals("FISCA")){
            return "redirect:/dashboard";
        }
        // 3. Se chegou aqui, ele está logado!
        model.addAttribute("nomeUsuario", usuario.getNome());
        model.addAttribute("residencias", usuario.getResidencias());
        
        //Importante para garantir que o morador não consulte outras residencias através do URL
        boolean podeVerEssaResidencia = false;
        if(residenciaId != null){
            for (Residencia r : usuario.getResidencias()){
                if(r.getId() == residenciaId){
                    podeVerEssaResidencia = true;
                    model.addAttribute("residenciaNumero", r.getNumero());
                }         
            }      
        }
        int residenciaIdBusca;
        if(podeVerEssaResidencia)
            residenciaIdBusca = residenciaId;
        else{
            Residencia r = usuario.getResidencias().iterator().next();
            residenciaIdBusca = r.getId();
            model.addAttribute("residenciaNumero", r.getNumero());
        }   
            
        
        ResponseEntity<Medicao[]> responseMedicoes = restTemplate.getForEntity(API_URL + "/medicao/residencia/" + residenciaIdBusca, Medicao[].class);
        List<Medicao>medicoes = Arrays.asList(responseMedicoes .getBody());
        model.addAttribute("medicoes", medicoes);
        
        ResponseEntity<Media[]> responseMedias = restTemplate.getForEntity(API_URL + "/medicao/media", Media[].class);
        List<Media>medias = Arrays.asList(responseMedias .getBody());
        model.addAttribute("medias", medias);
        
        return "dashboardMorador"; // Retorna a página "dashboard.html" (por exemplo)
    }
    
    @GetMapping("/dashboard")
    public String mostrarDashboard(HttpSession session, Model model, RedirectAttributes redirectAttributes) {       
        Ator usuario = (Ator) session.getAttribute("usuarioLogado");
        if (usuario == null) {
            // Não está logado, redireciona para a home (login)
            redirectAttributes.addFlashAttribute("mensagem", "Faça Login para acessar essa página!");
            return "redirect:/";
        }
        if(usuario.getPapel() == null){
            redirectAttributes.addFlashAttribute("mensagem", "O administrador do sistema ainda não autorizou seu acesso a plataforma, entre em contato com ele para normalizar a situação");
            return "redirect:/";
        }
        if (usuario.getPapel().equals("MORAR")){
            return "redirect:/morador?residencia=0";
        }
        
        // 3. Se chegou aqui, ele está logado!
        return "dashboardAdmin"; // Retorna a página "dashboard.html" (por exemplo)
    }
    
    @GetMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
        session.invalidate(); // Limpa/invalida a sessão
        redirectAttributes.addFlashAttribute("mensagem", "Você saiu do sistema.");
        return "redirect:/";
    }
    
}