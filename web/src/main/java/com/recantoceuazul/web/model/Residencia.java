package com.recantoceuazul.web.model;

import java.util.Set;

import java.util.HashSet;


/**
 * Representa uma residência cadastrada no sistema.
 * 
 * Cada residência possui um número identificador (como o número da casa ou apartamento)
 * e pode estar associada a um ou mais atores (moradores, administradores, técnicos etc.)
 * através de uma relação muitos-para-muitos.
 */
public class Residencia {
    private int id; // Identificador único da residência (gerado automaticamente)
    private int numero; // Número da residência (por exemplo, número da casa ou unidade habitacional)
    private Set<Ator> atores = new HashSet<>();
    
    // Construtor padrão exigido pelo JPA
    public Residencia() {
    }

    // Construtor completo para inicialização manual
    public Residencia(int numero,Set<Ator> atores ) {
        this.numero = numero;
        this.atores = atores;
    }

    // Getters e Setters (acessores e modificadores dos atributos)
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public int getNumero() {
        return numero;
    }
    public void setNumero(int numero) {
        this.numero = numero;
    }
    public Set<Ator> getAtores() {
        return atores;
    }
    public void setAtores(Set<Ator> atores) {
        this.atores = atores;
    }
    
}
