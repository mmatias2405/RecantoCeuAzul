package com.recantoceuazul.api.model;

import jakarta.persistence.*;
import java.util.Set;
import java.util.HashSet;


/**
 * Representa um ator dentro do sistema (por exemplo, um morador, funcionário, administrador, etc.).
 * 
 * A entidade Ator é mapeada para o banco de dados e contém informações básicas de identificação e 
 * autenticação, além de seu relacionamento com as residências associadas.
 */
@Entity
public class Ator {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // Identificador único do ator (gerado automaticamente pelo banco de dados)
    private int id;

    private String nome;     // Nome completo do ator
    private String email;    // Endereço de e-mail do ator (também usado para login)
    private String telefone; // Telefone de contato do ator
    private String senha;    // Senha usada para autenticação
    private String papel;    // Papel ou função do ator no sistema (ex: "ADMIN", "MORADOR", "FUNCIONARIO")

    /**
     * Relação muitos-para-muitos entre Ator e Residencia.
     * 
     * Um ator pode estar associado a várias residências e uma residência pode estar associada a vários atores.
     * Essa relação é implementada através da tabela de junção "possuir".
     */
    @ManyToMany(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinTable(
        name = "possuir", // Nome da tabela de junção
        joinColumns = @JoinColumn(name = "fk_Ator_id"), // Coluna que referencia esta entidade (Ator)
        inverseJoinColumns = @JoinColumn(name = "fk_Residencia_id") // Coluna que referencia a outra entidade (Residencia)
    )
    private Set<Residencia> residencias = new HashSet<>();
    // Construtores
    public Ator() {}

    // Construtor completo para inicialização manual
    public Ator(String nome, String email, String telefone, String senha, String papel, Set<Residencia> residencias) {
        this.nome = nome;
        this.email = email;
        this.telefone = telefone;
        this.senha = senha;
        this.papel = papel;
        this.residencias = residencias;
    }


    // Getters e Setters (acessores e modificadores de atributos)
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
