package com.recantoceuazul.api.repository;

import com.recantoceuazul.api.model.Residencia;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

// A interface abaixo é responsável por acessar e manipular os dados da entidade "Residencia" no banco de dados.
// Ela estende JpaRepository, o que fornece métodos prontos para realizar operações básicas de CRUD
// (criação, leitura, atualização e exclusão) sem necessidade de implementação manual.

public interface ResidenciaRepository extends JpaRepository<Residencia, Integer> {
    //Método personalizado para encontra os atores que estão sem papel definido;
    @Query(
        value = "SELECT *  FROM residencia r WHERE NOT EXISTS (\n" + //
                        "\tSELECT 1 FROM medicao m\tWHERE m.fk_Residencia_id = r.id \n" + //
                        "\t\tAND MONTH(m.data_medicao) = MONTH(CURDATE())\n" + //
                        "\t\tAND YEAR(m.data_medicao) = YEAR(CURDATE()));", 
        nativeQuery = true
    )
    List<Residencia> findBySemMedicao();
}
