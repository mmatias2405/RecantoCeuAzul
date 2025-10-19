package com.recantoceuazul.api.controller;

import com.recantoceuazul.api.dto.MedicaoRequest;
import com.recantoceuazul.api.model.Medicao;
import com.recantoceuazul.api.repository.MedicaoRepository;
import com.recantoceuazul.api.service.MedicaoService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/medicao")
@CrossOrigin(origins = "*")
public class MedicaoController {

    @Autowired
    private MedicaoService medicaoService;

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

    @PutMapping("/{id}")
    public Medicao atualizar(@PathVariable Integer id, @RequestBody Medicao atualizado) {
        return repo.findById(id).map(existente -> {
            existente.setVolumeAgua(atualizado.getVolumeAgua());
            existente.setDataMedicao(atualizado.getDataMedicao());
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


    @PostMapping
    public ResponseEntity<Medicao> criarMedicao(@RequestBody MedicaoRequest medicaoDTO) {
        try {
            Medicao novaMedicao = medicaoService.registrarNovaMedicao(medicaoDTO);
            return new ResponseEntity<>(novaMedicao, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            // Tratamento de erro simples se ator ou residência não forem encontrados
            return new ResponseEntity<>(new Medicao(), HttpStatus.BAD_REQUEST);
        }
    }
}