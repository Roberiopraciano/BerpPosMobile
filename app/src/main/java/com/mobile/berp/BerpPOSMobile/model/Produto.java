package com.mobile.berp.BerpPOSMobile.model;

import java.text.NumberFormat;
import java.util.Locale;
import java.text.NumberFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Calendar;
import java.util.Date;



public class Produto {

    private int qtdAparente, cod, combinado, fracionado = 0;
    private String desc, qtd, obs;
    private double valorUnitario, valorTotal;
    private String garcon, dhLancamento;
    private int seq,seqpai;
    private Date timeInicio;
    private Date timeFim;
    private Date dataInicio;
    private Date dataValidade;
    private double peso;
    private int controle_data;

    private int segunda, terca, quarta, quinta, sexta, sabado, domingo;
    private int alicodaliq;
    private String servico;
    private int proCdgrup;
    private String abrev;
    private String atrazoPorKms;
    private String apareceControlador;
    private String alicotaecf;

    // Construtores
    public Produto() {
        combinado = 0;
        qtd ="0";

    }

    public Produto(Produto p) {
        this.cod = p.cod;
        this.desc = p.desc;
        this.qtd = p.qtd;
        this.obs = p.obs;
        this.valorUnitario = p.valorUnitario;
        this.valorTotal = p.valorTotal;
        this.combinado = p.combinado;
        this.fracionado = p.fracionado;
        this.qtdAparente = p.qtdAparente;
        this.seq = p.seq;
        this.seqpai = p.seqpai;
        this.timeInicio = p.timeInicio;
        this.timeFim = p.timeFim;
        this.dataInicio = p.dataInicio;
        this.dataValidade = p.dataValidade;
        this.segunda = p.segunda;
        this.terca = p.terca;
        this.quarta = p.quarta;
        this.quinta = p.quinta;
        this.sexta = p.sexta;
        this.sabado = p.sabado;
        this.domingo = p.domingo;
        this.controle_data = p.controle_data;
    }


    public Produto(int cod, String desc, String qtd, String obs, double valorUnitario, int seq, int seqpai) {
        this.cod = cod;
        this.desc = desc;
        this.qtd = qtd;
        this.obs = obs;
        this.valorUnitario = valorUnitario;
        this.valorTotal = calculateValorTotal();
        this.combinado = 0;
        this.seq= seq;
        this.seqpai = seqpai;
    }

    public Produto(String cod, String desc, String qtd, String obs, double valorUnitario,int seq, int seqpai) {
        this.cod = Integer.parseInt(cod);
        this.desc = desc;
        this.qtd = qtd;
        this.obs = obs;
        this.valorUnitario = valorUnitario;
        this.valorTotal = calculateValorTotal();
        this.combinado = 0;
        this.seq= seq;
        this.seqpai = seqpai;
    }

    public Produto(String cod, String desc, String qtd, String obs, double valorUnitario, int combinado) {
        this.cod = Integer.parseInt(cod);
        this.desc = desc;
        this.qtd = qtd;
        this.obs = obs;
        this.valorUnitario = valorUnitario;
        this.valorTotal = calculateValorTotal();
        this.combinado = combinado;
        this.seq= seq;
        this.seqpai = seqpai;
    }

    public void setTimeInicio(double timestamp) {
        this.timeInicio = converterTimestamp(timestamp);
    }

    public void setTimeFim(double timestamp) {
        this.timeFim = converterTimestamp(timestamp);}

    public void setDataInicio(double timestamp) {
        this.dataInicio = converterTimestamp(timestamp);
    }

    public void setDataValidade(double timestamp) {
        this.dataValidade = converterTimestamp(timestamp);
    }

    // Método auxiliar para converter timestamp de dias em uma data correta
    private Date converterTimestamp(double timestamp) {
        // Definir a data base como 30/12/1899
        Calendar baseDate = Calendar.getInstance();
        baseDate.set(1899, Calendar.DECEMBER, 30, 0, 0, 0);
        baseDate.set(Calendar.MILLISECOND, 0);

        // Separar a parte inteira (dias) e a parte decimal (horário)
        int dias = (int) timestamp;
        double fracaoDia = timestamp - dias;

        // Adicionar os dias à base (30/12/1899)
        baseDate.add(Calendar.DATE, dias);

        // Converter a parte decimal do dia em horas, minutos e segundos
        int segundosDoDia = (int) (fracaoDia * 24 * 60 * 60);
        int horas = segundosDoDia / 3600;
        int minutos = (segundosDoDia % 3600) / 60;
        int segundos = segundosDoDia % 60;

        // Definir a hora na data
        baseDate.set(Calendar.HOUR_OF_DAY, horas);
        baseDate.set(Calendar.MINUTE, minutos);
        baseDate.set(Calendar.SECOND, segundos);

        // Retornar a data corretamente ajustada
        return baseDate.getTime();
    }

    public Date getTimeInicio() { return timeInicio; }
    public Date getTimeFim() { return timeFim; }
    public Date getDataInicio() { return dataInicio; }
    public Date getDataValidade() { return dataValidade; }

    public void setSegunda(int segunda) { this.segunda = segunda; }
    public void setTerca(int terca) { this.terca = terca; }
    public void setQuarta(int quarta) { this.quarta = quarta; }
    public void setQuinta(int quinta) { this.quinta = quinta; }
    public void setSexta(int sexta) { this.sexta = sexta; }
    public void setSabado(int sabado) { this.sabado = sabado; }
    public void setDomingo(int domingo) { this.domingo = domingo; }

    public void setControle_data(int controle_data) {this.controle_data = controle_data;}
    public int getControle_data(){ return controle_data; };

    public int getSegunda() { return segunda; }
    public int getTerca() { return terca; }
    public int getQuarta() { return quarta; }
    public int getQuinta() { return quinta; }
    public int getSexta() { return sexta; }
    public int getSabado() { return sabado; }
    public int getDomingo() { return domingo; }


    // Métodos de acesso
    public int getFracionado() {
        return fracionado;
    }

    public void setFracionado(int fracionado) {
        this.fracionado = fracionado;
    }

    public double getValorUnitario() {
        return valorUnitario;
    }

    public void setValorUnitario(double valorUnitario) {
        this.valorUnitario = valorUnitario;
        this.valorTotal = calculateValorTotal();
    }

    public void setValorTotal(double valorTotal) {
        this.valorTotal = valorTotal;
    }

    public int getSeq() {  return this.seq;  }

    public int getSeqpai() { return this.seqpai; }

    public double getValorTotal() {
        return valorTotal;
    }

    public int getCombinado() {
        return combinado;
    }

    public void setCombinado(int combinado) {
        this.combinado = combinado;
    }

    public int getCod() {
        return cod;
    }

    public void setCod(int cod) {
        this.cod = cod;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getObs() {
        return obs;
    }

    public void setObs(String obs) {
        this.obs = obs;
    }

    public String getQtd() {
        return qtd;
    }

    public void setQtd(String qtd) {
        this.qtd = qtd;
        this.valorTotal = calculateValorTotal();
    }

    public int getQtdAparente() {
        return qtdAparente;
    }

    public void setQtdAparente(int qtdAparente) {
        this.qtdAparente = qtdAparente;
    }

    public String getGarcon() {
        return garcon;
    }

    public void setGarcon(String garcon) {
        this.garcon = garcon;
    }

    public String getDhLancamento() {
        return dhLancamento;
    }

    public void setDhLancamento(String dhLancamento) {
        this.dhLancamento = dhLancamento;
    }

    public double getQtdAsDouble() {
        if (qtd == null) return 0.0;

        try {
            String limpa = qtd.replace("X", "")
                    .replace(",", ".")
                    .trim();
            return Double.parseDouble(limpa);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private double calculateValorTotal() {
        return getQtdAsDouble() * valorUnitario;
    }

    // Métodos para formatar os valores como strings em reais
    public String getValorUnitarioFormatado() {
        return NumberFormat.getCurrencyInstance(new Locale("pt", "BR")).format(valorUnitario);
    }

    public String getValorTotalFormatado() {
        return NumberFormat.getCurrencyInstance(new Locale("pt", "BR")).format(valorTotal);
    }

    public void setPeso(double fproPrpeso) { this.peso =fproPrpeso;}

    public void setAliq(int faliCdaliq) { this.alicodaliq =faliCdaliq;
    }

    public void setIcotaecf(String falicotaecf) { 
        this.alicotaecf=falicotaecf;
    }

    public void setServico(String fservico) { 
        this.servico = fservico;
    }

    public void setCodGrupo(int fproCdgrup) {
        this.proCdgrup =fproCdgrup;
    }

    public void setAbreviado(String fabrev) {
        this.abrev= fabrev;
    }

    public void setAtrasoPorKms(String fatrazoPorKms) {
        this.atrazoPorKms=fatrazoPorKms;
    }

    public void setApareceControlador(String fapareceControlador) {
        this.apareceControlador=fapareceControlador;
    }

}