package com.recantoceuazul.api.controller;

import com.recantoceuazul.api.model.Medicao;
import com.recantoceuazul.api.repository.MedicaoRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/medicao")
@CrossOrigin(origins = "*")
public class MedicaoController {

    @Autowired
    private MedicaoRepository repo;

    @GetMapping
    public List<Medicao> listar() {
        return repo.findAll();
    }

    @GetMapping("/{id}")
    public Medicao buscarPorId(@PathVariable Integer id) {
        return repo.findById(id).orElse(null);
    }

    @PostMapping
    public Medicao criar(@RequestBody Medicao medicao) {
        return repo.save(medicao);
    }

    @PutMapping("/{id}")
    public Medicao atualizar(@PathVariable Integer id, @RequestBody Medicao atualizado) {
        return repo.findById(id).map(existente -> {
            existente.setVolume_Agua(atualizado.getVolume_Agua());
            existente.setData_medicao(atualizado.getData_medicao());
            existente.setResidencia(atualizado.getResidencia());
            existente.setDelta(atualizado.getDelta());
            existente.setAtor(atualizado.getAtor());
            return repo.save(existente);
        }).orElse(null);
    }

    @DeleteMapping("/{id}")
    public void deletar(@PathVariable Integer id) {
        repo.deleteById(id);
    }
}