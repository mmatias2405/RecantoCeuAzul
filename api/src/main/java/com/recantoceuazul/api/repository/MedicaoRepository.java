package com.recantoceuazul.api.repository;

import com.recantoceuazul.api.model.Medicao;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MedicaoRepository extends JpaRepository<Medicao, Integer> {
}