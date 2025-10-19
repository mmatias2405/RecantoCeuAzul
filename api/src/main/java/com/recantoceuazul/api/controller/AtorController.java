package com.recantoceuazul.api.controller;

import com.recantoceuazul.api.model.Ator;
import com.recantoceuazul.api.repository.AtorRepository;
import com.recantoceuazul.api.repository.ResidenciaRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;



@RestController
@RequestMapping("/api/ator")
@CrossOrigin(origins = "*") // importante para permitir chamadas do frontend
public class AtorController {
    private final AtorRepository repo;
    private final ResidenciaRepository residencias;

    public AtorController(AtorRepository repo, ResidenciaRepository residencias) {
        this.repo = repo;
        this.residencias = residencias;
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
    
    @PutMapping("/{id}/tofiscal")
    public Ator promoveFiscal(@PathVariable Integer id) {
        return repo.findById(id)
        .map(atorExistente -> {
            if(!atorExistente.getResidencias().isEmpty()){
                return new Ator();
            }
            atorExistente.setPapel("FISCA");
            return repo.save(atorExistente);
        }).orElse(new Ator());
    }
    @PutMapping("/{id}/toadmin")
    public Ator promoveAdmin(@PathVariable Integer id) {
        return repo.findById(id)
        .map(atorExistente -> {
            if(!atorExistente.getResidencias().isEmpty()){
                return new Ator();
            }
            atorExistente.setPapel("ADMIN");
            return repo.save(atorExistente);
        }).orElse(new Ator());
    }
    @PutMapping("/residencia/{id}")
    public Ator adicionaResidencia(@PathVariable Integer id, @RequestBody Ator entity) {
        Ator atorArmazenado = this.getByIDAtor(entity.getId());
        if(atorArmazenado.getPapel() == null){
            atorArmazenado.setPapel("MORAR");
        }
        else{
            if(atorArmazenado.getPapel().equals("ADMIN") || atorArmazenado.getPapel().equals("FISCA"))
            return new Ator();
        }
        
        if (residencias.findById(id).isPresent()){
            atorArmazenado.getResidencias().add(residencias.findById(id).get());
        }
        else
            return new Ator();
        return repo.save(atorArmazenado);
    }

    @DeleteMapping("/{id}")
    public void deletar(@PathVariable Integer id) {
        repo.deleteById(id);
    }
}
