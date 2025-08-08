package com.recantoceuazul.api.controller;

import com.recantoceuazul.api.model.Administrador;
import com.recantoceuazul.api.repository.AdministradorRepository;
import com.recantoceuazul.api.dto.LoginRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/administrador")
@CrossOrigin(origins = "*") // importante para permitir chamadas do frontend
public class AdministradorController {
    private final AdministradorRepository repo;

    public AdministradorController(AdministradorRepository repo) {
        this.repo = repo;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Administrador request) {
        Optional<Administrador> adm = repo.findByEmailAndSenha(request.getEmail(), request.getSenha());
        if (adm.isPresent()) {
            return ResponseEntity.ok(adm.get().getId()); // retorna apenas o ID
        } else {
            return ResponseEntity.status(401).body("Credenciais inválidas");
        }
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
