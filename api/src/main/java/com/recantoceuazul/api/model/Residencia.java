package com.recantoceuazul.api.model;

import jakarta.persistence.*;
import java.util.Set;
import java.util.HashSet;

@Entity
public class Residencia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private int numero;

    @ManyToMany(mappedBy = "residencias") // 'mappedBy' aponta para o nome do campo na classe Ator
    private Set<Ator> atores = new HashSet<>();
    
    public Residencia() {
    }

    public Residencia(int numero,Set<Ator> atores ) {
        this.numero = numero;
        this.atores = atores;
    }

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
