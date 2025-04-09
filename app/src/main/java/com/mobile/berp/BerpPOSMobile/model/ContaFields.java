package com.mobile.berp.BerpPOSMobile.model;
import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class ContaFields {
    @SerializedName("FVEN_CDGARC")
    private String cdGarcom;

    @SerializedName("FVEN_CDMESA")
    private String cdMesa;

    @SerializedName("FVEN_CDVEND")
    private String cdVenda;

    @SerializedName("FVEN_VLRBRU")
    private String vlrBruto;

    @SerializedName("FVEN_VLRSER")
    private String vlrServico;

    @SerializedName("FVEN_VLRLIQ")
    private String vlrLiquido;

    public String getFantazia() {
        return fantazia;
    }

    public void setFantazia(String fantazia) {
        this.fantazia = fantazia;
    }

    @SerializedName("FFANTASIA")
    private String fantazia;





    @SerializedName("FNM_GARCON")
    private String nomeGarcom;

    @SerializedName("FVEN_TPVEND")
    private String ven_tpvend;


    @SerializedName("FVEN_NMCLIE")
    private String ven_nmcli;



    @SerializedName("FLOCAL_ENTREGA")
    private String local_entrega;



    @SerializedName("FVEN_VLRVEN")
    private String vlrVenda;

    @SerializedName("FVEN_STATUS")
    private String status;

    @SerializedName("FVEN_VLRNOT")
    private String vlrNota;

    @SerializedName("FVIT_LIST")
    private ListaItens vitList;

    @SerializedName("FVEN_VLRDES")
    private String vlrDesconto;

    @SerializedName("FVEN_DHMOVI")
    private String dataHoraMovimento;

    @SerializedName("FID")
    private int id;

    @SerializedName("FVEN_NRMOVI")
    private String nrMovimento;

    @SerializedName("Funiqueid")
    private String uniqueId;

    @SerializedName("FVEN_NRLOJA")
    private String nrLoja;

    // Lista de produtos (adicionado para suportar a função)
    private List<Produto> produtos = new ArrayList<>();

    // Getters e Setters (alguns omitidos para brevidade)


    public Produto[] getProdutosAsArray() {
        return produtos.toArray(new Produto[0]);
    }

    public Vector<Produto> getProdutosAsVector() {
        return new Vector<>(produtos);
    }

    private List<Pagamento> pagamentos = new ArrayList<>(); // Lista de pagamentos

    public void addPagamento(Pagamento pagamento) {
        if (pagamento != null) {
            pagamentos.add(pagamento);
        }
    }





    public void delPagamento(Pagamento pagamento) {
        if (pagamento != null) {
             pagamentos.remove(pagamento);
        }

    }


    public String getVen_nmcli() {
        return ven_nmcli;
    }

    public void setVen_nmcli(String ven_nmcli) {
        this.ven_nmcli = ven_nmcli;
    }

    public String getLocal_entrega() {
        return local_entrega;
    }

    public void setLocal_entrega(String local_entrega) {
        this.local_entrega = local_entrega;
    }



    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNrMovimento() {
        return nrMovimento;
    }

    public void setNrMovimento(String nrMovimento) {
        this.nrMovimento = nrMovimento;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getNrLoja() {
        return nrLoja;
    }

    public void setNrLoja(String nrLoja) {
        this.nrLoja = nrLoja;
    }

    public String getNomeGarcom() {
        return nomeGarcom;
    }

    public void setNomeGarcom(String nomeGarcom) {
        this.nomeGarcom = nomeGarcom;
    }

    public String getVen_tpvend() {
        return ven_tpvend;
    }

    public void setVen_tpvend(String ven_tpvend) {
        this.ven_tpvend = ven_tpvend;
    }

    public String getVlrVenda() {
        return vlrVenda;
    }

    public void setVlrVenda(String vlrVenda) {
        this.vlrVenda = vlrVenda;
    }

    public String getVlrNota() {
        return vlrNota;
    }

    public void setVlrNota(String vlrNota) {
        this.vlrNota = vlrNota;
    }



    public String getCdGarcom() {
        return cdGarcom;
    }

    public void setCdGarcom(String cdGarcom) {
        this.cdGarcom = cdGarcom;
    }

    public String getCdMesa() {
        return cdMesa;
    }

    public void setCdMesa(String cdMesa) {
        this.cdMesa = cdMesa;
    }

    public String getCdVenda() {
        return cdVenda;
    }

    public void setCdVenda(String cdVenda) {
        this.cdVenda = cdVenda;
    }

    public String getVlrBruto() {
        return vlrBruto;
    }

    public void setVlrBruto(String vlrBruto) {
        this.vlrBruto = vlrBruto;
    }

    public String getVlrServico() {
        return vlrServico;
    }

    public void setVlrServico(String vlrServico) {
        this.vlrServico = vlrServico;
    }

    public String getVlrLiquido() {
        return vlrLiquido;
    }

    public void setVlrLiquido(String vlrLiquido) {
        this.vlrLiquido = vlrLiquido;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public ListaItens getVitList() {
        return vitList;
    }

    public void setVitList(ListaItens vitList) {
        this.vitList = vitList;
    }

    public String getVlrDesconto() {
        return vlrDesconto;
    }

    public void setVlrDesconto(String vlrDesconto) {
        this.vlrDesconto = vlrDesconto;
    }

    public String getDataHoraMovimento() {
        return dataHoraMovimento;
    }

    public void setDataHoraMovimento(String dataHoraMovimento) {
        this.dataHoraMovimento = dataHoraMovimento;
    }

    public List<Produto> getProdutos() {
        return produtos;
    }

    public void addProduto(Produto produto) {
        this.produtos.add(produto);
    }


    public String getVen_nmtpvend() {
        return switch (this.ven_tpvend) {
            case "0" -> "MESA";
            case "1" -> "BALCAO";
            case "4" -> "CARTAO";
            default -> "MESA";
        };
    }
}
