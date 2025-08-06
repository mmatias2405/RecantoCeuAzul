package com.recantoceuazul.api.controller;

import com.recantoceuazul.api.model.Captacao;
import com.recantoceuazul.api.repository.CaptacaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/captacoes")
@CrossOrigin(origins = "*")
public class CaptacaoController {

    @Autowired
    private CaptacaoRepository repo;

    @GetMapping
    public List<Captacao> listar() {
        return repo.findAll();
    }

    @GetMapping("/{id}")
    public Captacao buscarPorId(@PathVariable Integer id) {
        return repo.findById(id).orElse(null);
    }

    @PostMapping
    public Captacao criar(@RequestBody Captacao captacao) {
        return repo.save(captacao);
    }

    @PutMapping("/{id}")
    public Captacao atualizar(@PathVariable Integer id, @RequestBody Captacao atualizado) {
        return repo.findById(id).map(existente -> {
            existente.setVolumeAgua(atualizado.getVolumeAgua());
            existente.setHora(atualizado.getHora());
            existente.setAdministrador(atualizado.getAdministrador());
            return repo.save(existente);
        }).orElse(null);
    }

    @DeleteMapping("/{id}")
    public void deletar(@PathVariable Integer id) {
        repo.deleteById(id);
    }
}
