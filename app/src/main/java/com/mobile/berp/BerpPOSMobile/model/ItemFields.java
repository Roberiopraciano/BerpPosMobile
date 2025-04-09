package com.mobile.berp.BerpPOSMobile.model;

import com.google.gson.annotations.SerializedName;

public class ItemFields {
    @SerializedName("FVIT_VLRTOT")
    private String valorTotal;

    @SerializedName("FVALORUNIT")
    private String valorUnitario;

    @SerializedName("FVIT_QTDPRO")
    private String quantidade;

    public String getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(String valorTotal) {
        this.valorTotal = valorTotal;
    }

    public String getValorUnitario() {
        return valorUnitario;
    }

    public void setValorUnitario(String valorUnitario) {
        this.valorUnitario = valorUnitario;
    }

    public String getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(String quantidade) {
        this.quantidade = quantidade;
    }

    public String getGarcom() {
        return garcom;
    }

    public void setGarcom(String garcom) {
        this.garcom = garcom;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getDataHoraLancamento() {
        return dataHoraLancamento;
    }

    public void setDataHoraLancamento(String dataHoraLancamento) {
        this.dataHoraLancamento = dataHoraLancamento;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDataHoraImpressao() {
        return dataHoraImpressao;
    }

    public void setDataHoraImpressao(String dataHoraImpressao) {
        this.dataHoraImpressao = dataHoraImpressao;
    }

    public String getSequencia() {
        return sequencia;
    }

    public void setSequencia(String sequencia) {
        this.sequencia = sequencia;
    }

    @SerializedName("FVIT_GARCON")
    private String garcom;

    @SerializedName("FCODIGO")
    private String codigo;

    @SerializedName("FVIT_DHLANC")
    private String dataHoraLancamento;

    @SerializedName("FVIT_OBSER")
    private String observacao;

    @SerializedName("FNOME")
    private String nome;

    @SerializedName("FVIT_DHIMP")
    private String dataHoraImpressao;

    @SerializedName("FVIT_SEQPRO")
    private String sequencia;

    // Getters e Setters omitidos por brevidade
}