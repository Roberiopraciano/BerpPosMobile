package com.mobile.berp.BerpPOSMobile.model;
import com.google.gson.annotations.SerializedName;

public class Terminal {
    @SerializedName("vERSAO")
    private String versao;

    @SerializedName("FVERSAO")
    private String Fversao;


    @SerializedName("nUM_TERMINAL")
    private String numTerminal;

    @SerializedName("FNUM_TERMINAL")
    private String FnumTerminal;


    @SerializedName("mAC")
    private String mac;

    @SerializedName("FMAC")
    private String fmac;

    @SerializedName("iP")
    private String ip;

    @SerializedName("FIP")
    private String fip;

    @SerializedName("sTATUS")
    private String status;

    @SerializedName("nOME")
    private String nome;

    @SerializedName("FDEVICE_ID")
    private String FDEVICE_ID;

    @SerializedName("fabricante")
    private String fabricante;

    @SerializedName("modelo")
    private String modelo;

    public String getFabricante() {
        return fabricante;
    }

    public void setFabricante(String fabricante) {
        this.fabricante = fabricante;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public String getVersaoSO() {
        return versaoSO;
    }

    public void setVersaoSO(String versaoSO) {
        this.versaoSO = versaoSO;
    }

    public String getNomeDispositivo() {
        return NomeDispositivo;
    }

    public void setNomeDispositivo(String nomeDispositivo) {
        NomeDispositivo = nomeDispositivo;
    }

    @SerializedName("versaoSO")
    private String versaoSO;

    @SerializedName("nome_dispositivo")
    private String NomeDispositivo;


    public String getDevice_id() {
        return FDEVICE_ID;
    }

    public void setDevice_id(String device_id) {
        this.FDEVICE_ID = device_id;
    }

    public String getID() {
        return FID;
    }

    public void setID(String ID) {
        this.FID = ID;
    }

    @SerializedName("FID")
    private String FID;


    // Getters e Setters
    public String getVersao() {
        return versao;
    }

    public void setVersao(String versao) {
        this.versao = versao;
        this.Fversao =versao;
    }

    public String getNumTerminal() {
        return numTerminal;
    }

    public void setNumTerminal(String numTerminal) {
        this.numTerminal = numTerminal;
        this.FnumTerminal = numTerminal;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
        this.fmac = mac;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
        this.fip = ip;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    @Override
    public String toString() {
        return "Terminal{" +
                "versao='" + versao + '\'' +
                ", numTerminal='" + numTerminal + '\'' +
                ", mac='" + mac + '\'' +
                ", ip='" + ip + '\'' +
                ", status='" + status + '\'' +
                ", nome='" + nome + '\'' +
                '}';
    }
}