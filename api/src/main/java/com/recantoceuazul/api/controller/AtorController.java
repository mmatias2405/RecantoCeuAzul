package com.recantoceuazul.api.controller;

import com.recantoceuazul.api.model.Ator;
import com.recantoceuazul.api.repository.AtorRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/ator")
@CrossOrigin(origins = "*") // importante para permitir chamadas do frontend
public class AtorController {
    private final AtorRepository repo;

    public AtorController(AtorRepository repo) {
        this.repo = repo;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Ator request) {
        Optional<Ator> adm = repo.findByEmailAndSenha(request.getEmail(), request.getSenha());
        if (adm.isPresent()) {
            return ResponseEntity.ok(adm.get().getId()); // retorna apenas o ID
        } else {
            return ResponseEntity.status(401).body("Credenciais inválidas");
        }
    }

    @GetMapping
    public List<Ator> listar() {
        return repo.findAll();
    }

    @GetMapping("/{id}")
    public Ator getByIDAtor(@PathVariable Integer id) {
        if (id == null || id == 0)
            return null;

        return repo.findById(id).orElse(null);
    }

    @PostMapping
    public Ator salvar(@RequestBody Ator ator) {
        return repo.save(ator);
    }

    @DeleteMapping("/{id}")
    public void deletar(@PathVariable Integer id) {
        repo.deleteById(id);
    }
}
