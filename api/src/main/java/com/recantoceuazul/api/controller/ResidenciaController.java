package com.recantoceuazul.api.controller;

import com.recantoceuazul.api.model.Residencia;
import com.recantoceuazul.api.repository.ReidenciaRepository;

import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/residencia")
@CrossOrigin(origins = "*") 
public class ResidenciaController {

    private final ReidenciaRepository repo;
    
    public ResidenciaController(ReidenciaRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<Residencia> listar() {
        return repo.findAll();
    }

    @GetMapping("/{id}")
    public Residencia getByIDResidencia(@PathVariable Integer id) {
        if (id == null || id == 0)
            return null;

        return repo.findById(id).orElse(null);
    }   

    @PutMapping("/{id}")
    public Residencia atualizar(@PathVariable Integer id, @RequestBody Residencia residenciaAtualizado) {
        return repo.findById(id)
            .map(residenciaExistente -> {
                residenciaExistente.setNumero(residenciaAtualizado.getNumero());
                return repo.save(residenciaExistente);
            })
            .orElseThrow(() -> new RuntimeException("Residencia não encontrado com id " + id));
    }

    @PostMapping
    public Residencia salvar(@RequestBody Residencia residencia) {
        return repo.save(residencia);
    }

    @DeleteMapping("/{id}")
    public void deletar(@PathVariable Integer id) {
        repo.deleteById(id);
    }

    
}
