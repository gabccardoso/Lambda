package br.com.humburguer.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DadosUsuario {
    private String cpf;
    private String nome;
    private String email;

    public DadosUsuario() {
    }

    public DadosUsuario(String cpf, String nome, String email) {
        this.cpf = cpf;
        this.nome = nome;
        this.email = email;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

}
