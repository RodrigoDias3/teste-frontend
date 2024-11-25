package com.cd.demo.form;

import jakarta.validation.constraints.*;

public class ConsultaForm {
    @NotEmpty(message = "A clinica é obrigatória.")
    public String clinica;

    @NotNull(message = "O medico é obrigatório.")
    @Min(value = 1, message = "O valor de ID médico deve ser pelo menos 1.")
    @Max(value = 99, message = "O valor de ID médico deve ser no máximo 99.")
    public int medico;

    @NotEmpty(message = "A data é obrigatória.")
    public String data;

    // Getters e Setters
    public String getClinica() {
        return clinica;
    }

    public int getMedico() {
        return medico;
    }

    public String getData() {
        return data;
    }

    public void setMedico(int medico) {
        this.medico = medico;
    }

    public void setClinica(String clinica) {
        this.clinica = clinica;
    }

    public void setData(String data) {
        this.data = data;
    }
}
