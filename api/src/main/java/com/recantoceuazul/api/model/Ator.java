package com.recantoceuazul.api.model;

import jakarta.persistence.*;

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




    // Construtores
    public Ator() {}

    public Ator(String nome, String email, String telefone, String senha, String papel) {
        this.nome = nome;
        this.email = email;
        this.telefone = telefone;
        this.senha = senha;
        this.papel = papel;
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


}
