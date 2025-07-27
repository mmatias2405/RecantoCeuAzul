package com.recantoceuazul.api.controller;

import com.recantoceuazul.api.model.Abastecimento;
import com.recantoceuazul.api.repository.AbastecimentoRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/abastecimentos")
@CrossOrigin(origins = "*")
public class AbastecimentoController {

    @Autowired
    private AbastecimentoRepository repo;

    @GetMapping
    public List<Abastecimento> listar() {
        return repo.findAll();
    }

    @GetMapping("/{id}")
    public Abastecimento buscarPorId(@PathVariable Integer id) {
        return repo.findById(id).orElse(null);
    }

    @PostMapping
    public Abastecimento criar(@RequestBody Abastecimento abastecimento) {
        return repo.save(abastecimento);
    }

    @PutMapping("/{id}")
    public Abastecimento atualizar(@PathVariable Integer id, @RequestBody Abastecimento atualizado) {
        return repo.findById(id).map(existente -> {
            existente.setVolumeAgua(atualizado.getVolumeAgua());
            existente.setHoraInicio(atualizado.getHoraInicio());
            existente.setHoraFim(atualizado.getHoraFim());
            existente.setSetor(atualizado.getSetor());
            existente.setAdministrador(atualizado.getAdministrador());
            return repo.save(existente);
        }).orElse(null);
    }

    @DeleteMapping("/{id}")
    public void deletar(@PathVariable Integer id) {
        repo.deleteById(id);
    }
}