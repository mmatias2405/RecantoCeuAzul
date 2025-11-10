package com.recantoceuazul.api.repository;

import com.recantoceuazul.api.model.Medicao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

// A interface abaixo é responsável por acessar e manipular os dados da entidade "Medicao" no banco de dados.
// Ela estende JpaRepository, o que permite utilizar métodos prontos para operações básicas de CRUD,
// além de possibilitar a criação de consultas personalizadas.
public interface MedicaoRepository extends JpaRepository<Medicao, Integer> {

    // Busca a medição mais recente (última registrada) de uma residência específica,
    // ordenando as medições pela data em ordem decrescente.
    Optional<Medicao> findTopByResidenciaIdOrderByDataMedicaoDesc(Integer residenciaId);

    // Retorna todas as medições associadas a uma residência específica,
    // ordenadas da mais recente para a mais antiga.
    List<Medicao> findByResidenciaIdOrderByDataMedicaoDesc(Integer residenciaId);
}