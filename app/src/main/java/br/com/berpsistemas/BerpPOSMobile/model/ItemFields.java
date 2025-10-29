package br.com.berpsistemas.BerpPOSMobile.model;

import com.google.gson.annotations.SerializedName;

public class ItemFields {

    @SerializedName("vIT_VLRTOT")
    private String valorTotal;

    @SerializedName("vALORUNIT")
    private String valorUnitario;

    @SerializedName("vIT_QTDPRO")
    private String quantidade;

    @SerializedName("vIT_GARCON")
    private String garcom;

    @SerializedName("cODIGO")
    private String codigo;

    @SerializedName("vIT_DHLANC")
    private String dataHoraLancamento;

    @SerializedName("vIT_OBSER")
    private String observacao;

    @SerializedName("nOME")
    private String nome;

    @SerializedName("vIT_DHIMP")
    private String dataHoraImpressao;

    @SerializedName("vIT_SEQPRO")
    private String sequencia;

    // Getters e Setters

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
}
