package com.recantoceuazul.api.repository;

import com.recantoceuazul.api.model.Ator;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AtorRepository extends JpaRepository<Ator, Integer> {
    Optional<Ator> findByEmailAndSenha(String email, String senha);
}

