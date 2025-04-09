package com.mobile.berp.BerpPOSMobile.model;

import android.app.Activity;

public class Pagamento {

    private int evtipo;
    private double valor;
    private int cdfpaga;
    private String nsu;
    private String autorizacao;
    private String bandeira;
    private String cvNumber;

    public Pagamento(){

    }

    public Pagamento(int cdfpaga,double valor, int evtipo){
        this.cdfpaga = cdfpaga;
        this.valor = valor;
        this.evtipo = evtipo;
    }

    public Pagamento(int cdfpaga,double valor, int evtipo,String nsu, String autorizacao, String bandeira, String cvNumber){
        this.cdfpaga = cdfpaga;
        this.valor = valor;
        this.evtipo = evtipo;
        this.nsu = nsu;
        this.autorizacao = autorizacao;
        this.bandeira = bandeira;
        this.cvNumber = cvNumber;
    }



    public int getEvtipo() {
        return evtipo;
    }

    public void setEvtipo(int evtipo) {
        this.evtipo = evtipo;
    }

    public double getValor() {
        return valor;
    }

    public void setValor(double valor) {
        this.valor = valor;
    }

    public int getCdfpaga() {
        return cdfpaga;
    }

    public void setCdfpaga(int cdfpaga) {
        this.cdfpaga = cdfpaga;
    }

    public String getNsu() {
        return nsu;
    }

    public void setNsu(String nsu) {
        this.nsu = nsu;
    }

    public String getAutorizacao() {
        return autorizacao;
    }

    public void setAutorizacao(String autorizacao) {
        this.autorizacao = autorizacao;
    }

    public String getBandeira() {
        return bandeira;
    }

    public void setBandeira(String bandeira) {
        this.bandeira = bandeira;
    }

    public String getCvNumber(){
        return cvNumber;
    }

    public void setCvNumber(String cvNumber){
        this.cvNumber = cvNumber;
    }


}
