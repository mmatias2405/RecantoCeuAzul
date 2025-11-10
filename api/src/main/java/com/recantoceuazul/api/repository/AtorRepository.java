package com.recantoceuazul.api.repository;

import com.recantoceuazul.api.model.Ator;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

// A interface abaixo é responsável por acessar e manipular os dados da entidade "Ator" no banco de dados.
// Ela estende a interface JpaRepository, que fornece métodos prontos para operações básicas de CRUD
// (criar, ler, atualizar e deletar), além de consultas personalizadas.

public interface AtorRepository extends JpaRepository<Ator, Integer> {

    // Método personalizado que busca um ator no banco de dados a partir do e-mail e da senha informados.
    // É utilizado, por exemplo, no processo de login, para verificar se as credenciais estão corretas.
    Optional<Ator> findByEmailAndSenha(String email, String senha);
}

