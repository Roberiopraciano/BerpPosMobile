package com.mobile.berp.BerpPOSMobile.model;

public class Impressora {

    private int cod;
    private String nome;

    public Impressora(int Cod, String impressoraIntegrada) {
        this.cod=Cod;
        this.nome=impressoraIntegrada;
    }

    public int getCod() {
        return cod;
    }

    public void setCod(int cod) {
        this.cod = cod;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }
}
