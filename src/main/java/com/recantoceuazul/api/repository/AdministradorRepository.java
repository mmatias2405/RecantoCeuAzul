package com.recantoceuazul.api.repository;

import com.recantoceuazul.api.model.Administrador;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AdministradorRepository extends JpaRepository<Administrador, Integer> {
    Optional<Administrador> findByEmailAndSenha(String email, String senha);
}

