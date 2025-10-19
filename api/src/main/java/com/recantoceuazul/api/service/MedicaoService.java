package com.recantoceuazul.api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Optional;

import com.recantoceuazul.api.repository.MedicaoRepository;
import com.recantoceuazul.api.repository.ResidenciaRepository;
import com.recantoceuazul.api.repository.AtorRepository;
import com.recantoceuazul.api.model.*;
import com.recantoceuazul.api.dto.MedicaoRequest;

@Service
public class MedicaoService {

    @Autowired
    private MedicaoRepository medicaoRepository;

    @Autowired
    private ResidenciaRepository residenciaRepository; // Você precisará deste repositório

    @Autowired
    private AtorRepository atorRepository; // E deste também

    public Medicao registrarNovaMedicao(MedicaoRequest medicaoDTO) {
        // 1. Buscar a última medição para a residência informada
        Optional<Medicao> ultimaMedicaoOpt = medicaoRepository
                .findTopByResidenciaIdOrderByDataMedicaoDesc(medicaoDTO.getResidenciaId());

        // 2. Calcular o delta
        float delta;
        if (ultimaMedicaoOpt.isPresent()) {
            Medicao ultimaMedicao = ultimaMedicaoOpt.get();
            delta = medicaoDTO.getVolumeAgua() - ultimaMedicao.getVolumeAgua();
        } else {
            // Se não houver medição anterior, o delta é 0
            delta = 0;
        }

        // 3. Buscar as entidades Ator e Residencia (lançará exceção se não encontrar)
        Residencia residencia = residenciaRepository.findById(medicaoDTO.getResidenciaId())
                .orElseThrow(() -> new RuntimeException("Residência não encontrada com id: " + medicaoDTO.getResidenciaId()));

        Ator ator = atorRepository.findById(medicaoDTO.getAtorId())
                .orElseThrow(() -> new RuntimeException("Ator não encontrado com id: " + medicaoDTO.getAtorId()));

        // 4. Criar a nova entidade Medicao com todos os dados
        Medicao novaMedicao = new Medicao();
        novaMedicao.setVolumeAgua(medicaoDTO.getVolumeAgua());
        novaMedicao.setDelta(delta);
        novaMedicao.setDataMedicao(LocalDateTime.now()); // Data e hora atuais
        novaMedicao.setResidencia(residencia);
        novaMedicao.setAtor(ator);

        // 5. Salvar no banco de dados
        return medicaoRepository.save(novaMedicao);
    }
}