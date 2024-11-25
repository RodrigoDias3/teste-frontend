package com.cd.demo.form;

import jakarta.validation.constraints.*;

public class SignUpForm {
    @NotEmpty(message = "O nome é obrigatório.")
    @Size(max = 20, message = "O nome não pode ter mais de 20 caracteres.")
    public String nome;

    @NotEmpty(message = "O email é obrigatório.")
    @Size(max = 100, message = "O email não pode ter mais de 100 caracteres.")
    public String email;

    @NotEmpty(message = "A senha é obrigatória.")
    @Size(min = 6, max = 100, message = "A senha deve ter entre 6 e 100 caracteres.")
    public String password;

    @NotEmpty(message = "A confirmação da senha é obrigatória.")
    public String confirmPassword;

    // Getters e Setters

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

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
