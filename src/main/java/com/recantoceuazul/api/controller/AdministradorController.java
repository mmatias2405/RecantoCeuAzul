package com.recantoceuazul.api.controller;

import com.recantoceuazul.api.model.Administrador;
import com.recantoceuazul.api.repository.AdministradorRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/administrador")
@CrossOrigin(origins = "*") // importante para permitir chamadas do frontend
public class AdministradorController {
    private final AdministradorRepository repo;

    public AdministradorController(AdministradorRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<Administrador> listar() {
        return repo.findAll();
    }

    @PostMapping
    public Administrador salvar(@RequestBody Administrador administrador) {
        return repo.save(administrador);
    }

    @DeleteMapping("/{id}")
    public void deletar(@PathVariable Integer id) {
        repo.deleteById(id);
    }
}
