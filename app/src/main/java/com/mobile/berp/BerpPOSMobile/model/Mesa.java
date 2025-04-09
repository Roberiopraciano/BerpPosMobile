package com.mobile.berp.BerpPOSMobile.model;

import android.location.Location;
import android.util.Log;

import com.mobile.berp.BerpPOSMobile.Controller.Funcoes;
import com.mobile.berp.BerpPOSMobile.TrackingService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Vector;

public class Mesa {

    private String	numMesa, cdFunci, vlrVen, vlrSer, numVenda, vlrLiq, status, sequencia, dhAbertura, vlrDesconto, vlrTotal;


    private String tipoVenda;
    private String localEntrega;
    private String deviceId;
    private double lat;
    private double lng;
    private Vector<Produto> produtos;
    private Vector<Pagamento> pagamentos;
    private int				combAtu;
    private String nomeCliente;
    private String Id;



    public Mesa(String numMesa, String cdFunci, Vector<Produto> produtos, String sequencia) {

        this.numMesa = numMesa;
        this.cdFunci = cdFunci;
        this.produtos = produtos;
        this.sequencia = sequencia;
        combAtu = 0;
        this.tipoVenda = "0";
        this.localEntrega ="";
        this.deviceId = "";
        this.lat = 0.0;
        this.lng = 0.0;
        this.nomeCliente ="";

    }

    public Mesa() {

        combAtu = 0;
        this.deviceId = "";
        this.lat = 0.0;
        this.lng = 0.0;

    }

    public Mesa(String numMesa, String cdFunci, Vector<Produto> produtos, String sequencia, String tipoVenda, String localEntrega,String nomeCliente) {


        this.numMesa = numMesa;
        this.cdFunci = cdFunci;
        this.produtos = produtos;
        this.vlrVen = vlrVen;
        this.sequencia = sequencia;
        combAtu = 0;
        this.tipoVenda = tipoVenda;
        this.localEntrega = localEntrega;
        this.deviceId = Variaveis.getDevice_id();
        this.lat = 0.0;
        this.lng = 0.0;
        this.nomeCliente =nomeCliente;

    }

    public Mesa(String numMesa, String cdFunci, Vector<Pagamento> pagamentos) {
        this.numMesa = numMesa;
        this.cdFunci = cdFunci;
        this.pagamentos = pagamentos;
    }

    public String getVlrTotal() {
        return vlrTotal;
    }

    private double latitude;
    private double longitude;

    // Métodos para obter e definir a localização
    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getNomeCliente() {
        return nomeCliente;
    }

    public void setNomeCliente(String nomeCliente) {
        this.nomeCliente = nomeCliente;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    // Método para atualizar a localização a partir do serviço
    public void updateLocation() {
        Location location = TrackingService.getLastLocation();
        if (location != null) {
            this.latitude = location.getLatitude();
            this.longitude = location.getLongitude();
        } else {
            Log.w("Mesa", "Localização não disponível. Certifique-se de que o serviço está ativo.");
        }
    }


    public void setVlrTotal(String vlrTotal) {
        this.vlrTotal = vlrTotal;
    }


    private String getSequencia() { //roberio
        return sequencia;
    }

    public void setSequencia(String numVenda) { //roberio
        this.sequencia = numVenda;
    }

    public int getCombAtu() {

        return combAtu;
    }

    public void setCombAtu(int combAtu) {

        this.combAtu = combAtu;
    }

    public int getCombAtuAdd() {

        combAtu++;
        return combAtu;
    }

    public String getNumVenda() {

        return numVenda;
    }

    public void setNumVenda(String numVenda) {

        this.numVenda = numVenda;
    }

    public String getStatus() {

        return status;
    }

    public void setStatus(String status) {

        this.status = status;
    }

    public String getVlrSer() {

        return vlrSer;
    }

    public void setVlrSer(String vlrSer) {

        this.vlrSer = vlrSer;
    }

    public String getVlrLiq() {

        return vlrLiq;
    }

    public void setVlrLiq(String vlrLiq) {

        this.vlrLiq = vlrLiq;
    }

    public String getCdFunci() {

        return cdFunci;
    }

    public void setCdFunci(String cdFunci) {

        this.cdFunci = cdFunci;
    }

    public String getNumMesa() {

        return numMesa;
    }

    public void setNumMesa(String numMesa) {

        this.numMesa = numMesa;
    }

    public String getVlrVen() {

        return vlrVen;
    }

    public void setVlrVen(String vlrVen) {

        this.vlrVen = vlrVen;
    }

    public String getDhAbertura() {
        return dhAbertura;
    }

    public void setDhAbertura(String dhAbertura) {
        this.dhAbertura = dhAbertura;
    }

    public String getVlrDesconto() {
        return vlrDesconto;
    }

    public void setVlrDesconto(String vlrDesconto) {
        this.vlrDesconto = vlrDesconto;
    }

    public Vector<Produto> getProdutos() {

        return produtos;
    }

    public void setProdutos(Vector<Produto> produtos) {

        this.produtos = produtos;
    }

    public int getLenghtProdutos() {

        return produtos.size();
    }

    public Produto getElement(int index) {

        if (index < produtos.size()) {
            return produtos.elementAt(index);
        } else {
            return null;
        }

    }

    public Produto getProduto(int cod) {

        Produto produto;
        for (int i = 0; i < produtos.size(); i++) {
            produto = produtos.elementAt(i);

            if (produto.getCod() == cod) {
                return produto;
            }
        }
        return null;
    }

    public void addProduto(Produto p) {

        if (produtos == null) {
            produtos = new Vector<>(1, 1);
        }
        produtos.addElement(p);
    }

    public void addPagamento(Pagamento pg) {

        if (pagamentos == null) {
            pagamentos = new Vector<>(1, 1);
        }
        pagamentos.addElement(pg);
    }

    public void delPagamento(Pagamento pg) {

        pagamentos.removeElement(pg);
    }

    public void delProduto(Produto p) {

        produtos.removeElement(p);
    }

    public String getTipoVenda() {
        return tipoVenda;
    }

    public void setTipoVenda(String tipoVenda) {
        this.tipoVenda = tipoVenda;
    }

    public String getLocalEntrega() {
        return localEntrega;
    }

    public void setLocalEntrega(String localEntrega) {
        this.localEntrega = localEntrega;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }


    public String toJSON() throws Exception {

        JSONObject retorno = new JSONObject();
        JSONArray arrProdutos = new JSONArray();
        try {

            for (Produto p : produtos) {
                arrProdutos.put(new JSONObject()
                        .put("FCODIGO", String.valueOf(p.getCod()))
                        .put("FQUANTIDADE", p.getQtd())
                        .put("FOBSERVACAO", p.getObs())
                        .put("FVALORUNIT", p.getValorUnitario())
                        .put("FSUBSEQ", p.getCombinado())
                        .put("FSEQPAI", p.getSeqpai()));
            }

            retorno.put("FPRODUTOS", arrProdutos);
            retorno.put("FGARCON", getCdFunci());
            retorno.put("FNUM_MESA", getNumMesa());
            retorno.put("FTERMINAL", Variaveis.getNumTerminal());
            retorno.put("FSEQUENCIA", getSequencia());
            retorno.put("FTIPOVENDA", getTipoVenda());
            retorno.put("Flocal_entrega", getLocalEntrega());
            retorno.put("device_id", Variaveis.getDevice_id());
            retorno.put("NOME_CLIENTE",getNomeCliente());

            updateLocation();

            retorno.put("lat", getLatitude());
            retorno.put("lng", getLongitude());


        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }

        return retorno.toString();
    }

    public String pagamentotoJSON() throws Exception {

        JSONObject retorno = new JSONObject();
        JSONArray arrPagamentos = new JSONArray();
        try {

            for (int i = 0; i < pagamentos.size(); i++) {
                Pagamento pg = pagamentos.elementAt(i);
                arrPagamentos.put(new JSONObject().put("FCDFPAGA", String.valueOf(pg.getCdfpaga()))
                        .put("FVALOR", String.valueOf(pg.getValor()))
                        .put("FEVTIPO", pg.getEvtipo()).put("FNSU", pg.getNsu())
                        .put("FAUTORIZACAO",pg.getAutorizacao()).put("FBANDEIRA",pg.getBandeira()));
            }

            retorno.put("FPAGAMENTOS", arrPagamentos);
            retorno.put("FGARCOM", getCdFunci());
            retorno.put("FNUM_MESA", getNumMesa());
            retorno.put("FNUM_TERMINAL", Variaveis.getNumTerminal());
            retorno.put("FCPFCNPJ_CLIENTE",Variaveis.getcpfcliente());
            retorno.put("FNOME_CLIENTE",Variaveis.getNomecliente());

        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }

        return retorno.toString();
    }

    public Mesa fromJson(String json) throws Exception {

        JSONObject retorno, mesaInfo;
        JSONArray arr, arrAux, arrProd;
        Mesa m = new Mesa();
        Produto p;

        retorno = new JSONObject(json);
        arr = retorno.getJSONArray("result");
        mesaInfo = arr.getJSONObject(0);

        if (!mesaInfo.has("VEN_CDMESA")) {
            throw new IllegalArgumentException("Esta mesa não se encontra aberta ou está sem itens!");
        }

        arrAux = mesaInfo.getJSONArray("VEN_CDMESA");
        m.setNumMesa(arrAux.getString(0));

        arrAux = mesaInfo.getJSONArray("VEN_CDGARC");
        m.setCdFunci(arrAux.getString(0));

        arrAux = mesaInfo.getJSONArray("VEN_VLRBRU");
        m.setVlrVen(arrAux.getString(0));

        arrAux = mesaInfo.getJSONArray("VEN_CDVEND");
        m.setNumVenda(arrAux.getString(0));

        arrAux = mesaInfo.getJSONArray("VEN_VLRLIQ");
        m.setVlrLiq(arrAux.getString(0));

        arrAux = mesaInfo.getJSONArray("VEN_VLRSER");
        m.setVlrSer(arrAux.getString(0));

        arrAux = mesaInfo.getJSONArray("VEN_STATUS");
        switch (Integer.parseInt(arrAux.getString(0))) {
            case 1:
                m.setStatus("Aberta");
                break;
            case 3:
                m.setStatus("Fechada");
                break;
            default:
                m.setStatus("Desconhecido");
        }

        if (!mesaInfo.has("NOME")) {
            throw new JSONException("Mesa sem itens!");

        }

        arrProd = mesaInfo.getJSONArray("NOME");
        for (int i = 0; i < arrProd.length(); i++) {
            p = new Produto();
            p.setDesc(arrProd.getString(i));
            arrAux = mesaInfo.getJSONArray("VALORUNIT");
            p.setValorUnitario(new Funcoes().convertStringToDouble(arrAux.getString(i)));
            arrAux = mesaInfo.getJSONArray("VIT_VLRTOT");
           // p.set(new Funcoes().convertStringToDouble(arrAux.getString(i)));
            arrAux = mesaInfo.getJSONArray("VIT_QTDPRO");
            p.setQtd(arrAux.getString(i));

            m.addProduto(p);

        }

        return m;

    }

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }
}
