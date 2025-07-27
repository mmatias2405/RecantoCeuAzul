package com.recantoceuazul.api.controller;

import com.recantoceuazul.api.model.Setor;
import com.recantoceuazul.api.repository.SetorRepository;

import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/setor")
@CrossOrigin(origins = "*") 
public class SetorController {

    private final SetorRepository repo;
    
    public SetorController(SetorRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<Setor> listar() {
        return repo.findAll();
    }

    @GetMapping("/{id}")
    public Setor getByIDSetor(@PathVariable Integer id) {
        if (id == null || id == 0)
            return null;

        return repo.findById(id).orElse(null);
    }   

    @PutMapping("/{id}")
    public Setor atualizar(@PathVariable Integer id, @RequestBody Setor setorAtualizado) {
        return repo.findById(id)
            .map(setorExistente -> {
                setorExistente.setNome(setorAtualizado.getNome());
                return repo.save(setorExistente);
            })
            .orElseThrow(() -> new RuntimeException("Setor não encontrado com id " + id));
    }

    @PostMapping
    public Setor salvar(@RequestBody Setor setor) {
        return repo.save(setor);
    }

    @DeleteMapping("/{id}")
    public void deletar(@PathVariable Integer id) {
        repo.deleteById(id);
    }

    
}
