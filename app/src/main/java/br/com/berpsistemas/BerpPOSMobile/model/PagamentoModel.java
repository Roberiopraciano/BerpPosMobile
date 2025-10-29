package br.com.berpsistemas.BerpPOSMobile.model;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import java.util.List;

public class PagamentoModel {

    @SerializedName("ID")
    private int id;

    @SerializedName("PGP_NRCAIX")
    private int pgpNrcaix;

    @SerializedName("PGP_SEQCAIXA")
    private int pgpSeqcaixa;

    @SerializedName("PGP_CDUSUA")
    private int pgpCdusua;

    @SerializedName("PGP_EVTIPO")
    private int pgpEvtipo;

    @SerializedName("PGP_DHEVEN")
    private double pgpDheven;

    @SerializedName("PGP_EFETUA")
    private String pgpEfetua;

    @SerializedName("PGP_PAGSEQ")
    private int pgpPagseq;

    @SerializedName("PGP_CDFPAG")
    private int pgpCdfpag;

    @SerializedName("PGP_VLRPAG")
    private double pgpVlrpag;

    @SerializedName("PGP_CDFUNC")
    private int pgpCdfunc;

    @SerializedName("PGP_NRMOVD")
    private int pgpNrmovd;

    @SerializedName("PGP_NRPEDI")
    private int pgpNrpedi;

    @SerializedName("PGP_NMCLIE")
    private String pgpNmclie;

    @SerializedName("PGP_PGSTAT")
    private int pgpPgstat;

    @SerializedName("ID_MOVVENDA")
    private int idMovvenda;

    @SerializedName("NSU_TEF")
    private String nsuTef;

    @SerializedName("NSU_HOST")
    private String nsuHost;

    @SerializedName("AUTORIZACAO")
    private String autorizacao;

    @SerializedName("BANDEIRA")
    private String bandeira;

    @SerializedName("CARTAO")
    private String cartao;

    @SerializedName("TERMINAL")
    private String terminal;

    @SerializedName("PGP_NRLOJA")
    private int pgpNrloja;

    @SerializedName("PGP_NRMOVI")
    private int pgpNrmovi;

    @SerializedName("PGP_CDVEND")
    private int pgpCdvend;

    @SerializedName("PGP_POS_TEXT")
    private String pgpPosText;

    @SerializedName("PGP_OBSERVACAO")
    private String pgpObservacao;

    @SerializedName("PGP_ORIGEMPAGTO")
    private String pgpOrigempagto;

    @SerializedName("ID_MOVCAIXA")
    private int idMovcaixa;

    @SerializedName("ADQUIRENTE")
    private String adquirente;

    @SerializedName("ORIGEMOPERACAO")
    private String origemoperacao;

    @SerializedName("NSU_CANCEL")
    private String nsuCancel;

    @SerializedName("DATA_HORA_CANCEL")
    private double dataHoraCancel;

    @SerializedName("TRANSACTION_ID")
    private String transactionId;

    @SerializedName("VIA_CLIENTE")
    private String viaCliente;

    @SerializedName("VIA_ESTABELECIMENTO")
    private String viaEstabelecimento;

    @SerializedName("GUID")
    private String guid;

    @SerializedName("GUID_VENDA")
    private String guidVenda;

    @SerializedName("GUID_MOVCAIXA")
    private String guidMovcaixa;

    @SerializedName("ID_PLATAFORMA")
    private String idPlataforma;

    @SerializedName("ID_PLATAFORMA_REDUZIDO")
    private String idPlataformaReduzido;

    @SerializedName("ID_EMPRESA")
    private int idEmpresa;

    @SerializedName("TIPO_CARTAO_DEB_CRE")
    private String tipoCartaoDebCre;

    @SerializedName("ORIGEMPAGAMENTO")
    private String origempagamento;

    @SerializedName("NSU")
    private String nsu;

    @SerializedName("REDE")
    private String rede;

    @SerializedName("CVNUMBER")
    private String cvNumber;

    @SerializedName("USUARIO")
    private String usuario;

    @SerializedName("ID_ATENDIMENTO")
    private String idAtendimento;

    @SerializedName("ID_ORDER")
    private String idOrder;

    @SerializedName("ID_PAGAMENTO_MOVPAGPA")
    private String idPagamentoMovpagpa;

    @SerializedName("ControleSequencia")
    private String controleSequencia;

    @SerializedName("BIN_CARTAO")
    private String binCartao;

    @SerializedName("CODIGOCARTAOCONVERSAO")
    private String codigoCartaoConversao;



    private String externalId;
    private String status;
    private String acquirer;
    private long cancelledAt;


    public String getCodigoCartaoConversao() {
        return codigoCartaoConversao;
    }

    public void setCodigoCartaoConversao(String codigoCartaoConversao) {
        this.codigoCartaoConversao = codigoCartaoConversao;
    }


    // --- Construtores ---
    public PagamentoModel() {}

    public PagamentoModel(int pgpCdfpag, double pgpVlrpag, int pgpEvtipo) {
        this.pgpCdfpag = pgpCdfpag;
        this.pgpVlrpag = pgpVlrpag;
        this.pgpEvtipo = pgpEvtipo;
    }

    // --- Getters e Setters ---
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getPgpNrcaix() { return pgpNrcaix; }
    public void setPgpNrcaix(int pgpNrcaix) { this.pgpNrcaix = pgpNrcaix; }

    public int getPgpSeqcaixa() { return pgpSeqcaixa; }
    public void setPgpSeqcaixa(int pgpSeqcaixa) { this.pgpSeqcaixa = pgpSeqcaixa; }

    public int getPgpCdusua() { return pgpCdusua; }
    public void setPgpCdusua(int pgpCdusua) { this.pgpCdusua = pgpCdusua; }

    public int getPgpEvtipo() { return pgpEvtipo; }
    public void setPgpEvtipo(int pgpEvtipo) { this.pgpEvtipo = pgpEvtipo; }

    public double getPgpDheven() { return pgpDheven; }
    public void setPgpDheven(double pgpDheven) { this.pgpDheven = pgpDheven; }

    public String getPgpEfetua() { return pgpEfetua; }
    public void setPgpEfetua(String pgpEfetua) { this.pgpEfetua = pgpEfetua; }

    public int getPgpPagseq() { return pgpPagseq; }
    public void setPgpPagseq(int pgpPagseq) { this.pgpPagseq = pgpPagseq; }

    public int getPgpCdfpag() { return pgpCdfpag; }
    public void setPgpCdfpag(int pgpCdfpag) { this.pgpCdfpag = pgpCdfpag; }

    public double getPgpVlrpag() { return pgpVlrpag; }
    public void setPgpVlrpag(double pgpVlrpag) { this.pgpVlrpag = pgpVlrpag; }

    public int getPgpCdfunc() { return pgpCdfunc; }
    public void setPgpCdfunc(int pgpCdfunc) { this.pgpCdfunc = pgpCdfunc; }

    public int getPgpNrmovd() { return pgpNrmovd; }
    public void setPgpNrmovd(int pgpNrmovd) { this.pgpNrmovd = pgpNrmovd; }

    public int getPgpNrpedi() { return pgpNrpedi; }
    public void setPgpNrpedi(int pgpNrpedi) { this.pgpNrpedi = pgpNrpedi; }

    public String getPgpNmclie() { return pgpNmclie; }
    public void setPgpNmclie(String pgpNmclie) { this.pgpNmclie = pgpNmclie; }

    public int getPgpPgstat() { return pgpPgstat; }
    public void setPgpPgstat(int pgpPgstat) { this.pgpPgstat = pgpPgstat; }

    public int getIdMovvenda() { return idMovvenda; }
    public void setIdMovvenda(int idMovvenda) { this.idMovvenda = idMovvenda; }

    public String getNsuTef() { return nsuTef; }
    public void setNsuTef(String nsuTef) { this.nsuTef = nsuTef; }

    public String getNsuHost() { return nsuHost; }
    public void setNsuHost(String nsuHost) { this.nsuHost = nsuHost; }

    public String getAutorizacao() { return autorizacao; }
    public void setAutorizacao(String autorizacao) { this.autorizacao = autorizacao; }

    public String getBandeira() { return bandeira; }
    public void setBandeira(String bandeira) { this.bandeira = bandeira; }

    public String getCartao() { return cartao; }
    public void setCartao(String cartao) { this.cartao = cartao; }

    public String getTerminal() { return terminal; }
    public void setTerminal(String terminal) { this.terminal = terminal; }

    public int getPgpNrloja() { return pgpNrloja; }
    public void setPgpNrloja(int pgpNrloja) { this.pgpNrloja = pgpNrloja; }

    public int getPgpNrmovi() { return pgpNrmovi; }
    public void setPgpNrmovi(int pgpNrmovi) { this.pgpNrmovi = pgpNrmovi; }

    public int getPgpCdvend() { return pgpCdvend; }
    public void setPgpCdvend(int pgpCdvend) { this.pgpCdvend = pgpCdvend; }

    public String getPgpPosText() { return pgpPosText; }
    public void setPgpPosText(String pgpPosText) { this.pgpPosText = pgpPosText; }

    public String getPgpObservacao() { return pgpObservacao; }
    public void setPgpObservacao(String pgpObservacao) { this.pgpObservacao = pgpObservacao; }

    public String getPgpOrigempagto() { return pgpOrigempagto; }
    public void setPgpOrigempagto(String pgpOrigempagto) { this.pgpOrigempagto = pgpOrigempagto; }

    public int getIdMovcaixa() { return idMovcaixa; }
    public void setIdMovcaixa(int idMovcaixa) { this.idMovcaixa = idMovcaixa; }

    public String getAdquirente() { return adquirente; }
    public void setAdquirente(String adquirente) { this.adquirente = adquirente; }

    public String getOrigemoperacao() { return origemoperacao; }
    public void setOrigemoperacao(String origemoperacao) { this.origemoperacao = origemoperacao; }

    public String getNsuCancel() { return nsuCancel; }
    public void setNsuCancel(String nsuCancel) { this.nsuCancel = nsuCancel; }

    public double getDataHoraCancel() { return dataHoraCancel; }
    public void setDataHoraCancel(double dataHoraCancel) { this.dataHoraCancel = dataHoraCancel; }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public String getViaCliente() { return viaCliente; }
    public void setViaCliente(String viaCliente) { this.viaCliente = viaCliente; }

    public String getViaEstabelecimento() { return viaEstabelecimento; }
    public void setViaEstabelecimento(String viaEstabelecimento) { this.viaEstabelecimento = viaEstabelecimento; }

    public String getGuid() { return guid; }
    public void setGuid(String guid) { this.guid = guid; }

    public String getGuidVenda() { return guidVenda; }
    public void setGuidVenda(String guidVenda) { this.guidVenda = guidVenda; }

    public String getGuidMovcaixa() { return guidMovcaixa; }
    public void setGuidMovcaixa(String guidMovcaixa) { this.guidMovcaixa = guidMovcaixa; }

    public String getIdPlataforma() { return idPlataforma; }
    public void setIdPlataforma(String idPlataforma) { this.idPlataforma = idPlataforma; }

    public String getIdPlataformaReduzido() { return idPlataformaReduzido; }
    public void setIdPlataformaReduzido(String idPlataformaReduzido) { this.idPlataformaReduzido = idPlataformaReduzido; }

    public int getIdEmpresa() { return idEmpresa; }
    public void setIdEmpresa(int idEmpresa) { this.idEmpresa = idEmpresa; }

    public String getTipoCartaoDebCre() { return tipoCartaoDebCre; }
    public void setTipoCartaoDebCre(String tipoCartaoDebCre) { this.tipoCartaoDebCre = tipoCartaoDebCre; }

    public String getOrigempagamento() { return origempagamento; }
    public void setOrigempagamento(String origempagamento) { this.origempagamento = origempagamento; }

    public String getNsu() { return nsu; }
    public void setNsu(String nsu) { this.nsu = nsu; }

    public String getRede() { return rede; }
    public void setRede(String rede) { this.rede = rede; }

    public String getCvNumber() { return cvNumber; }
    public void setCvNumber(String cvNumber) { this.cvNumber = cvNumber; }

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }

    public String getIdAtendimento() { return idAtendimento; }
    public void setIdAtendimento(String idAtendimento) { this.idAtendimento = idAtendimento; }

    public String getIdOrder() { return idOrder; }
    public void setIdOrder(String idOrder) { this.idOrder = idOrder; }

    public String getIdPagamentoMovpagpa() { return idPagamentoMovpagpa; }
    public void setIdPagamentoMovpagpa(String idPagamentoMovpagpa) { this.idPagamentoMovpagpa = idPagamentoMovpagpa; }

    public String getControleSequencia() { return controleSequencia; }
    public void setControleSequencia(String controleSequencia) { this.controleSequencia = controleSequencia; }

    public String getBinCartao() { return binCartao; }
    public void setBinCartao(String binCartao) { this.binCartao = binCartao; }

    // --- Serialização / Desserialização ---
    public String toJson() {
        return new Gson().toJson(this);
    }

    public static PagamentoModel fromJson(String json) {
        return new Gson().fromJson(json, PagamentoModel.class);
    }

    public static List<PagamentoModel> listFromJsonArray(String jsonArrayString) {
        return new Gson().fromJson(jsonArrayString,
                new TypeToken<List<PagamentoModel>>(){}.getType());
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAcquirer() {
        return acquirer;
    }

    public void setAcquirer(String acquirer) {
        this.acquirer = acquirer;
    }

    public long getCancelledAt() {
        return cancelledAt;
    }

    public void setCancelledAt(long cancelledAt) {
        this.cancelledAt = cancelledAt;
    }
}
