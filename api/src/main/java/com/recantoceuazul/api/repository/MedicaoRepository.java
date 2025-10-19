package com.recantoceuazul.api.repository;

import com.recantoceuazul.api.model.Medicao;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface MedicaoRepository extends JpaRepository<Medicao, Integer> {
    /**
     * Encontra a última medição (Top 1) para uma dada residência,
     * ordenando pela data da medição em ordem decrescente.
     * O Spring Data JPA vai gerar a query SQL automaticamente a partir do nome do método.
     *
     * @param residenciaId O ID da residência a ser pesquisada.
     * @return um Optional contendo a última Medicao, ou um Optional vazio se não houver nenhuma.
     */
    Optional<Medicao> findTopByResidenciaIdOrderByDataMedicaoDesc(Integer residenciaId);
}