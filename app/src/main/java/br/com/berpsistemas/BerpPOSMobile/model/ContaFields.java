package br.com.berpsistemas.BerpPOSMobile.model;

import android.icu.math.BigDecimal;
import android.icu.text.DecimalFormat;
import android.icu.text.DecimalFormatSymbols;
import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

public class ContaFields {
    @SerializedName("vEN_CDGARC")
    private String cdGarcom;

    @SerializedName("vEN_CDMESA")
    private String cdMesa;

    @SerializedName("vEN_CDVEND")
    private String cdVenda;

    @SerializedName("vEN_VLRBRU")
    private String vlrBruto;

    @SerializedName("vEN_VLRSER")
    private String vlrServico;

    @SerializedName("vEN_VLRLIQ")
    private String vlrLiquido;

    @SerializedName("fANTAZIA")
    private String fantazia;

    @SerializedName("nM_GARCON")
    private String nomeGarcom;

    @SerializedName("vEN_TPVEND")
    private String ven_tpvend;

    @SerializedName("vEN_NMCLIE")
    private String ven_nmcli;

    @SerializedName("lOCAL_ENTREGA")
    private String local_entrega;

    @SerializedName("vEN_VLRVEN")
    private String vlrVenda;

    @SerializedName("vEN_STATUS")
    private String status;

    @SerializedName("vEN_VLRNOT")
    private String vlrNota;

    @SerializedName("vIT_LIST")
    private ListaItens vitList;

    @SerializedName("vEN_VLRDES")
    private String vlrDesconto;

    @SerializedName("vEN_DHMOVI")
    private String dataHoraMovimento;

    @SerializedName("iD")
    private int id;

    @SerializedName("vEN_NRMOVI")
    private String nrMovimento;

    @SerializedName("uniqueid")
    private String uniqueId;

    @SerializedName("vEN_NRLOJA")
    private String nrLoja;

    @SerializedName("vEN_VLRENT")
    private String vlrEntrega;

    @SerializedName("pagamentos")
    private List<PagamentoModel> pagamentos = new ArrayList<>();

    // ----- Getters e Setters -----

    public String getCdGarcom() { return cdGarcom; }
    public void setCdGarcom(String cdGarcom) { this.cdGarcom = cdGarcom; }

    public String getCdMesa() { return cdMesa; }
    public void setCdMesa(String cdMesa) { this.cdMesa = cdMesa; }

    public String getCdVenda() { return cdVenda; }
    public void setCdVenda(String cdVenda) { this.cdVenda = cdVenda; }

    public String getVlrBruto() { return vlrBruto; }
    public void setVlrBruto(String vlrBruto) { this.vlrBruto = vlrBruto; }

    public String getVlrServico() { return vlrServico; }
    public void setVlrServico(String vlrServico) { this.vlrServico = vlrServico; }

    public String getVlrLiquido() { return vlrLiquido; }
    public void setVlrLiquido(String vlrLiquido) { this.vlrLiquido = vlrLiquido; }

    public String getFantazia() { return fantazia; }
    public void setFantazia(String fantazia) { this.fantazia = fantazia; }

    public String getNomeGarcom() { return nomeGarcom; }
    public void setNomeGarcom(String nomeGarcom) { this.nomeGarcom = nomeGarcom; }

    public String getVen_tpvend() { return ven_tpvend; }
    public void setVen_tpvend(String ven_tpvend) { this.ven_tpvend = ven_tpvend; }

    public String getVen_nmcli() { return ven_nmcli; }
    public void setVen_nmcli(String ven_nmcli) { this.ven_nmcli = ven_nmcli; }

    public String getLocal_entrega() { return local_entrega; }
    public void setLocal_entrega(String local_entrega) { this.local_entrega = local_entrega; }

    public String getVlrVenda() { return vlrVenda; }
    public void setVlrVenda(String vlrVenda) { this.vlrVenda = vlrVenda; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getVlrNota() { return vlrNota; }
    public void setVlrNota(String vlrNota) { this.vlrNota = vlrNota; }

    public ListaItens getVitList() { return vitList; }
    public void setVitList(ListaItens vitList) { this.vitList = vitList; }

    public String getVlrDesconto() { return vlrDesconto; }
    public void setVlrDesconto(String vlrDesconto) { this.vlrDesconto = vlrDesconto; }

    public String getDataHoraMovimento() { return dataHoraMovimento; }
    public void setDataHoraMovimento(String dataHoraMovimento) { this.dataHoraMovimento = dataHoraMovimento; }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNrMovimento() { return nrMovimento; }
    public void setNrMovimento(String nrMovimento) { this.nrMovimento = nrMovimento; }

    public String getUniqueId() { return uniqueId; }
    public void setUniqueId(String uniqueId) { this.uniqueId = uniqueId; }

    public String getNrLoja() { return nrLoja; }
    public void setNrLoja(String nrLoja) { this.nrLoja = nrLoja; }

    public String getVlrEntrega() { return vlrEntrega; }
    public void setVlrEntrega(String vlrEntrega) { this.vlrEntrega = vlrEntrega; }

    // ----- Produtos -----
    private List<Produto> produtos = new ArrayList<>();
    public List<Produto> getProdutos() { return produtos; }
    public Produto[] getProdutosAsArray() { return produtos.toArray(new Produto[0]); }
    public Vector<Produto> getProdutosAsVector() { return new Vector<>(produtos); }
    public void addProduto(Produto produto) { produtos.add(produto); }

    // ----- Pagamentos -----
    public List<PagamentoModel> getPagamentos() { return pagamentos; }
    public void setPagamentos(List<PagamentoModel> pagamentos) { this.pagamentos = pagamentos; }

    /**
     * Adiciona um pagamento à lista.
     */
    public void addPagamento(PagamentoModel pagamento) {
        if (pagamento != null) {
            pagamentos.add(pagamento);
        }
    }

    /**
     * Limpa todos os pagamentos.
     */
    public void clearPagamentos() {
        pagamentos.clear();
    }

    /**
     * Remove um pagamento da lista.
     */
    public void delPagamento(PagamentoModel pagamento) {
        if (pagamento != null) {
            pagamentos.remove(pagamento);
        }
    }

    /**
     * Soma e formata o total dos pagamentos (pt-BR).
     */
    public String getTotalPagoStr() {
        BigDecimal total = BigDecimal.ZERO;
        for (PagamentoModel pg : pagamentos) {
            total = total.add(BigDecimal.valueOf(pg.getPgpVlrpag()));
        }
        DecimalFormatSymbols s = new DecimalFormatSymbols(new Locale("pt","BR"));
        s.setDecimalSeparator(',');
        s.setGroupingSeparator('.');
        return new DecimalFormat("#,##0.00", s).format(total);
    }

    /**
     * Retorna a descrição do tipo de venda.
     */
    public String getVen_nmtpvend() {
        return switch (ven_tpvend) {
            case "0" -> "MESA";
            case "1" -> "BALCAO";
            case "4" -> "CARTAO";
            default  -> "MESA";
        };
    }
}
