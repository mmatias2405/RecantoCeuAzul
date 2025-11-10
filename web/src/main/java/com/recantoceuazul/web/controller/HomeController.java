package com.recantoceuazul.web.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
@Controller
public class HomeController {

    @Value("${api.url}")
    private String API_URL;

    @GetMapping("/")
    public String index(Model model) {
        return "index";
    }

}