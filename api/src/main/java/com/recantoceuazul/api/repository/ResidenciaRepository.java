package com.recantoceuazul.api.repository;

import com.recantoceuazul.api.model.Residencia;

import org.springframework.data.jpa.repository.JpaRepository;

// A interface abaixo é responsável por acessar e manipular os dados da entidade "Residencia" no banco de dados.
// Ela estende JpaRepository, o que fornece métodos prontos para realizar operações básicas de CRUD
// (criação, leitura, atualização e exclusão) sem necessidade de implementação manual.

public interface ResidenciaRepository extends JpaRepository<Residencia, Integer> {
}
