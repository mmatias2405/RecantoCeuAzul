package com.recantoceuazul.web.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
@Controller
public class HomeController {

    @Value("${api.url}")
    private String API_URL;

    @GetMapping("/")
    public String index(Model model) {
        return "index";
    }

    @PostMapping("/auth")
    public String auth(Model model, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("mensagem", "Teste de backend");
        return "redirect:/";
    }

}