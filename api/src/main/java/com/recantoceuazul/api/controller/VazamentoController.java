package com.recantoceuazul.api.controller;

import com.recantoceuazul.api.dto.AnaliseVazamentoResponseDTO;
import com.recantoceuazul.api.repository.ResidenciaRepository;
import com.recantoceuazul.api.service.VazamentoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class VazamentoController {

    private final VazamentoService vazamentoService;
    private final ResidenciaRepository residenciaRepository;

    public VazamentoController(VazamentoService vazamentoService, ResidenciaRepository residenciaRepository) {
        this.vazamentoService = vazamentoService;
        this.residenciaRepository = residenciaRepository;
    }

    @GetMapping({"/residencia/{id}/vazamento", "/residencias/{id}/vazamento"})
    public ResponseEntity<AnaliseVazamentoResponseDTO> diagnosticarVazamento(@PathVariable Integer id) {
        if (id == null || id <= 0) {
            AnaliseVazamentoResponseDTO invalido = AnaliseVazamentoResponseDTO.servicoIndisponivel(
                    id,
                    "Identificador de residência inválido."
            );
            invalido.setStatus("Residência inválida");
            return ResponseEntity.badRequest().body(invalido);
        }

        if (!residenciaRepository.existsById(id)) {
            AnaliseVazamentoResponseDTO naoEncontrada = AnaliseVazamentoResponseDTO.servicoIndisponivel(
                    id,
                    "Residência não encontrada no cadastro do condomínio."
            );
            naoEncontrada.setStatus("Residência não encontrada");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(naoEncontrada);
        }

        AnaliseVazamentoResponseDTO diagnostico = vazamentoService.analisarVazamentoResidencia(id);
        return ResponseEntity.ok(diagnostico);
    }
}
