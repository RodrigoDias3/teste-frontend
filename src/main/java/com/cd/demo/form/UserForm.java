package com.cd.demo.form;

import jakarta.validation.constraints.NotEmpty;

public class UserForm {
    @NotEmpty(message = "Erro: O nome tem que estar preenchido")
    public String nome;
    @NotEmpty(message = "Erro: O email tem que estar preenchido")
    public String email;
    @NotEmpty(message = "Erro: A password tem que estar preenchida")
    public String password;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
