package com.recantoceuazul.web.controller;

import com.recantoceuazul.web.dto.*;
import com.recantoceuazul.web.model.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
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
import org.springframework.http.HttpMethod;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;






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
        // 1. Converta o Set para um ArrayList
        List<Residencia> listaDeResidencias = new ArrayList<>(usuario.getResidencias());
        listaDeResidencias.sort(Comparator.comparing(Residencia::getNumero));
        
        model.addAttribute("residencias", listaDeResidencias);
        
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
        model.addAttribute("nomeUsuario", usuario.getNome());
        // 3. Se chegou aqui, ele está logado!
        ResponseEntity<Residencia[]> responseResidencia = restTemplate.getForEntity(API_URL + "/residencia/semmedicao", Residencia[].class);
        List<Residencia>residenciasSemMedicao = Arrays.asList(responseResidencia .getBody());
        model.addAttribute("residenciasSemMedicao", residenciasSemMedicao);
        
        ResponseEntity<Residencia[]> responseAllResidencia = restTemplate.getForEntity(API_URL + "/residencia", Residencia[].class);
        List<Residencia>allResidencias= Arrays.asList(responseAllResidencia .getBody());
        model.addAttribute("allResidencias", allResidencias);
        
        
        ResponseEntity<Ator[]> responseAtor = restTemplate.getForEntity(API_URL + "/ator/sempapel", Ator[].class);
        List<Ator>atoresSemPapel = Arrays.asList(responseAtor .getBody());
        model.addAttribute("atoresSemPapel", atoresSemPapel);
        
        ResponseEntity<DadosGerais[]> responseDadosGerais = restTemplate.getForEntity(API_URL + "/medicao/dadosgerais", DadosGerais[].class);
        List<DadosGerais>dadosGerais = Arrays.asList(responseDadosGerais.getBody());
        model.addAttribute("dadosGerais", dadosGerais);
        
        return "dashboardAdmin"; 
    }
    
    @GetMapping("/consumo")
    public String cosumo(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        
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
        model.addAttribute("nomeUsuario", usuario.getNome());
        ResponseEntity<Medicao[]> responseMedicoes = restTemplate.getForEntity(API_URL + "/medicao", Medicao[].class);
        List<Medicao>medicoes = Arrays.asList(responseMedicoes .getBody());
        medicoes.sort(Comparator.comparing(Medicao::getDataMedicao).reversed());
        model.addAttribute("medicoes", medicoes);
        return "consumo";
    }
    @PostMapping("/alterarMedicao")
    public String postAlterarMedicao(@ModelAttribute Medicao medicao, RedirectAttributes redirectAttributes, HttpSession session) {
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
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Medicao> request = new HttpEntity<>(medicao, headers);
        ResponseEntity<Medicao> response = restTemplate.exchange(
        API_URL + "/medicao/"+medicao.getId(),
        HttpMethod.PUT,
        request,
        Medicao.class // O que você espera receber de volta (pode ser String.class ou Void.class)
        );
        if (response.getStatusCode().is2xxSuccessful()) {
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Medição alterada com sucesso");
            return "redirect:/consumo";
        }
        else{
            redirectAttributes.addFlashAttribute("mensagem", "Houve um erro na alteração do registro, tente novamente!");
            return "redirect:/consumo";
        }
    }
    
    @PostMapping("/registrarConsumo")
    public String postRegistroConsumo(@ModelAttribute MedicaoRequest medicao, RedirectAttributes redirectAttributes, HttpSession session) {
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
        
        medicao.setAtorId(usuario.getId());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<MedicaoRequest> request = new HttpEntity<>(medicao, headers);
        
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(API_URL + "/medicao", request, String.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                redirectAttributes.addFlashAttribute("mensagemSucesso", "Registro efetuado com sucesso");
                return "redirect:/dashboard";
            }
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().value() == 401) {
                redirectAttributes.addFlashAttribute("mensagem", "Houve um erro no registro, tente novamente!");
                return "redirect:/dashboard";
            }
        }
        
        redirectAttributes.addFlashAttribute("mensagem", "Houve um erro inesperado no registro, tente novamente!");
        return "redirect:/dashboard";
    }
    @PostMapping("/aprovarAtor")
    public String postAprovarAtor(@RequestBody AprovacaoRequest aprovacao, RedirectAttributes redirectAttributes, HttpSession session) {
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
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Ator> request = new HttpEntity<>(new Ator(), headers);
        
        if(aprovacao.getPapel().equals("ADMIN")){
            ResponseEntity<Ator> response = restTemplate.exchange(
            API_URL + "/ator/"+aprovacao.getAtorId()+"/toadmin",
            HttpMethod.PUT,
            request,
            Ator.class // O que você espera receber de volta (pode ser String.class ou Void.class)
            );
            if (response.getStatusCode().is2xxSuccessful()) {
                redirectAttributes.addFlashAttribute("mensagemSucesso", "Usuário aprovado com sucesso");
                return "redirect:/dashboard";
            }
            else{
                redirectAttributes.addFlashAttribute("mensagem", "Houve um erro no registro, tente novamente!");
                return "redirect:/dashboard";
            }
        }
        if(aprovacao.getPapel().equals("FISCA")){
            ResponseEntity<Ator> response = restTemplate.exchange(
            API_URL + "/ator/"+aprovacao.getAtorId()+"/tofiscal",
            HttpMethod.PUT,
            request,
            Ator.class // O que você espera receber de volta (pode ser String.class ou Void.class)
            );
            if (response.getStatusCode().is2xxSuccessful()) {
                redirectAttributes.addFlashAttribute("mensagemSucesso", "Usuário aprovado com sucesso");
                return "redirect:/dashboard";
            }
            else{
                redirectAttributes.addFlashAttribute("mensagem", "Houve um erro no registro, tente novamente!");
                return "redirect:/dashboard";
            }
        }
        if(aprovacao.getPapel().equals("MORAR")){
            Ator a = new Ator();
            a.setId(aprovacao.getAtorId());
            request = new HttpEntity<>(a, headers);
            for(int residencia : aprovacao.getResidencias()){
                ResponseEntity<Ator> response = restTemplate.exchange(
                API_URL + "/ator/residencia/" + residencia,
                HttpMethod.PUT,
                request,
                Ator.class // O que você espera receber de volta (pode ser String.class ou Void.class)
                );
                if(!response.getStatusCode().is2xxSuccessful()){
                    redirectAttributes.addFlashAttribute("mensagem", "Houve um erro no registro, tente novamente!");
                    return "redirect:/dashboard"; 
                }
            }
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Usuário aprovado com sucesso");
            return "redirect:/dashboard";      
        }        
        redirectAttributes.addFlashAttribute("mensagem", "Houve um erro inesperado no registro, tente novamente!");
        return "redirect:/dashboard";
    }    
    @GetMapping("/usuario")
    public String getUsuario(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        
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
        model.addAttribute("nomeUsuario", usuario.getNome());
        return "usuario";
    }
    @GetMapping("/residencia")
    public String getResidencia(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        
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
        model.addAttribute("nomeUsuario", usuario.getNome());
        return "registrarResidencia";
    }
    @GetMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
        session.invalidate(); // Limpa/invalida a sessão
        redirectAttributes.addFlashAttribute("mensagem", "Você saiu do sistema.");
        return "redirect:/";
    }
    
}