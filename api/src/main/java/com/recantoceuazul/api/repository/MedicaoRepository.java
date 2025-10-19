package com.recantoceuazul.api.repository;

import com.recantoceuazul.api.model.Medicao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
public interface MedicaoRepository extends JpaRepository<Medicao, Integer> {
    Optional<Medicao> findTopByResidenciaIdOrderByDataMedicaoDesc(Integer residenciaId);
    List<Medicao> findByResidenciaIdOrderByDataMedicaoDesc(Integer residenciaId);
}