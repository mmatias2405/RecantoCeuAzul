// Classes do tipo DTO (Data Transfer Object) são responsáveis por transportar dados entre diferentes camadas da aplicação,
// geralmente entre o frontend e o backend. Elas servem como objetos intermediários que encapsulam apenas as informações
// necessárias para determinada operação, sem expor diretamente as entidades do modelo (Model).


package com.recantoceuazul.api.dto;

// A classe LoginRequest representa o objeto de transferência de dados utilizado para autenticação de um usuário no sistema. Ela carrega as credenciais (email e senha)
// enviadas pelo cliente durante o processo de login.

public class LoginRequest {

    // Endereço de e-mail do usuário, utilizado como identificador de login.
    private String email;

    // Senha correspondente ao e-mail informado, utilizada para autenticação.
    private String senha;
    
    // Retorna o e-mail do usuário.
    public String getEmail() {
        return email;
    }

    // Define o e-mail do usuário.
    public void setEmail(String email) {
        this.email = email;
    }
    
    // Retorna a senha do usuário.
    public String getSenha() {
        return senha;
    }

    // Define a senha do usuário.
    public void setSenha(String senha) {
        this.senha = senha;
    }

}
