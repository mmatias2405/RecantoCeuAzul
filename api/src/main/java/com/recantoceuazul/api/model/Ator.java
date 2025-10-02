package com.recantoceuazul.api.model;

import jakarta.persistence.*;
import java.util.Set;
import java.util.HashSet;

@Entity
public class Ator {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String nome;
    private String email;
    private String telefone;
    private String senha;
    private String papel;

    @ManyToMany(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinTable(
        name = "possuir", // Nome da tabela de junção
        joinColumns = @JoinColumn(name = "fk_Ator_id"), // Coluna que referencia esta entidade (Ator)
        inverseJoinColumns = @JoinColumn(name = "fk_Residencia_id") // Coluna que referencia a outra entidade (Residencia)
    )
    private Set<Residencia> residencias = new HashSet<>();
    // Construtores
    public Ator() {}

    public Ator(String nome, String email, String telefone, String senha, String papel, Set<Residencia> residencias) {
        this.nome = nome;
        this.email = email;
        this.telefone = telefone;
        this.senha = senha;
        this.papel = papel;
        this.residencias = residencias;
    }


    // Getters e Setters
    public int getId() { return id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }

    public String getPapel() {
        return papel;
    }

    public void setPapel(String papel) {
        this.papel = papel;
    }
    // Getters e Setters para o campo 'residencias'
    public Set<Residencia> getResidencias() {
        return residencias;
    }

    public void setResidencias(Set<Residencia> residencias) {
        this.residencias = residencias;
    }


}
