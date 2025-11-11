package com.recantoceuazul.web.model;

import java.util.Set;
import java.util.HashSet;


/**
 * Representa um ator dentro do sistema (por exemplo, um morador, funcionário, administrador, etc.).
 * 
 * A entidade Ator é mapeada para o banco de dados e contém informações básicas de identificação e 
 * autenticação, além de seu relacionamento com as residências associadas.
 */
public class Ator {
   
    private int id; // Identificador único do ator (gerado automaticamente pelo banco de dados)
   

    private String nome;     // Nome completo do ator
    private String email;    // Endereço de e-mail do ator (também usado para login)
    private String telefone; // Telefone de contato do ator
    private String senha;    // Senha usada para autenticação
    private String papel;    // Papel ou função do ator no sistema (ex: "ADMIN", "MORADOR", "FUNCIONARIO")
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
    public void setId(int id) {
            this.id = id;
        }
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
