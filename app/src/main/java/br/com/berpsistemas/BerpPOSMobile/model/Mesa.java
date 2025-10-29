package br.com.berpsistemas.BerpPOSMobile.model;

import android.location.Location;
import android.util.Log;

import br.com.berpsistemas.BerpPOSMobile.Controller.Funcoes;
import br.com.berpsistemas.BerpPOSMobile.TrackingService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Vector;

public class Mesa {

    private String numMesa, cdFunci, vlrVen, vlrSer, numVenda, vlrLiq, status, sequencia, dhAbertura, vlrDesconto, vlrTotal;
    private String tipoVenda;
    private String localEntrega;
    private String deviceId;
    private double lat;
    private double lng;
    private Vector<Produto> produtos;
    private Vector<PagamentoModel> pagamentos;
    private int combAtu;
    private String nomeCliente;
    private String Id;
    private double latitude;
    private double longitude;

    // Constructor with separated typed vectors
    public Mesa(String numMesa, String cdFunci, Vector<Produto> produtos, String sequencia) {
        this.numMesa = numMesa;
        this.cdFunci = cdFunci;
        this.produtos = produtos != null ? produtos : new Vector<Produto>();
        this.sequencia = sequencia;
        combAtu = 0;
        this.tipoVenda = "0";
        this.localEntrega = "";
        this.deviceId = "";
        this.lat = 0.0;
        this.lng = 0.0;
        this.nomeCliente = "";
        this.pagamentos = new Vector<PagamentoModel>();
    }

    public Mesa() {
        combAtu = 0;
        this.deviceId = "";
        this.lat = 0.0;
        this.lng = 0.0;
        this.produtos = new Vector<Produto>();
        this.pagamentos = new Vector<PagamentoModel>();
    }

    // Constructor with separated typed vectors and additional parameters
    public Mesa(String numMesa, String cdFunci, Vector<Produto> produtos, String sequencia, String tipoVenda, String localEntrega, String nomeCliente) {
        this.numMesa = numMesa;
        this.cdFunci = cdFunci;
        this.produtos = produtos != null ? produtos : new Vector<Produto>();
        this.sequencia = sequencia;
        combAtu = 0;
        this.tipoVenda = tipoVenda;
        this.localEntrega = localEntrega;
        this.deviceId = Variaveis.getDevice_id();
        this.lat = 0.0;
        this.lng = 0.0;
        this.nomeCliente = nomeCliente;
        this.pagamentos = new Vector<PagamentoModel>();
    }

    // Constructor for mixed data (backward compatibility) - using different parameter order to avoid conflict
    public Mesa(String numMesa, String cdFunci, String sequencia, String tipoVenda, String localEntrega, String nomeCliente, Vector<Object> mixedData) {
        this.numMesa = numMesa;
        this.cdFunci = cdFunci;
        this.sequencia = sequencia;
        this.tipoVenda = tipoVenda;
        this.localEntrega = localEntrega;
        this.deviceId = Variaveis.getDevice_id();
        this.lat = 0.0;
        this.lng = 0.0;
        this.nomeCliente = nomeCliente;
        combAtu = 0;

        // Separate mixed data into products and payments
        this.produtos = new Vector<Produto>();
        this.pagamentos = new Vector<PagamentoModel>();

        if (mixedData != null) {
            for (Object obj : mixedData) {
                if (obj instanceof Produto) {
                    this.produtos.add((Produto) obj);
                } else if (obj instanceof PagamentoModel) {
                    this.pagamentos.add((PagamentoModel) obj);
                }
            }
        }
    }



    // Constructor for mixed data (backward compatibility) - shorter version
    public Mesa(String numMesa, String cdFunci, Vector<Object> mixedData) {
        this.numMesa = numMesa;
        this.cdFunci = cdFunci;
        combAtu = 0;

        // Separate mixed data into products and payments
        this.produtos = new Vector<Produto>();
        this.pagamentos = new Vector<PagamentoModel>();

        if (mixedData != null) {
            for (Object obj : mixedData) {
                if (obj instanceof Produto) {
                    this.produtos.add((Produto) obj);
                } else if (obj instanceof PagamentoModel) {
                    this.pagamentos.add((PagamentoModel) obj);
                }
            }
        }
    }

    public String getVlrTotal() {
        return vlrTotal;
    }

    public void setVlrTotal(String vlrTotal) {
        this.vlrTotal = vlrTotal;
    }

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

    private String getSequencia() {
        return sequencia;
    }

    public void setSequencia(String numVenda) {
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

    public String getNMStatus() {
        if (status == null) {
            return "Desconhecido";
        }

        switch (status) {
            case "1":
                return "Aberta";
            case "3":
            case "70":
                return "Fechada";
            default:
                return "Desconhecido";
        }
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

    public Vector<PagamentoModel> getPagamentos() {
        return pagamentos;
    }

    public void setPagamentos(Vector<PagamentoModel> pagamentos) {
        this.pagamentos = pagamentos;
    }

    public int getLenghtProdutos() {
        return produtos != null ? produtos.size() : 0;
    }

    public Produto getElement(int index) {
        if (produtos != null && index < produtos.size()) {
            return produtos.elementAt(index);
        } else {
            return null;
        }
    }

    public Produto getProduto(int cod) {
        if (produtos == null) return null;

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

    public void addPagamento(PagamentoModel pg) {
        if (pagamentos == null) {
            pagamentos = new Vector<>(1, 1);
        }
        pagamentos.addElement(pg);
    }

    public void delPagamento(PagamentoModel pg) {
        if (pagamentos != null) {
            pagamentos.removeElement(pg);
        }
    }

    public void delProduto(Produto p) {
        if (produtos != null) {
            produtos.removeElement(p);
        }
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
            if (produtos != null) {
                for (Produto p : produtos) {
                    arrProdutos.put(new JSONObject()
                            .put("FCODIGO", String.valueOf(p.getCod()))
                            .put("FQUANTIDADE", p.getQtd())
                            .put("FOBSERVACAO", p.getObs())
                            .put("FVALORUNIT", p.getValorUnitario())
                            .put("FSUBSEQ", p.getCombinado())
                            .put("FSEQPAI", p.getSeqpai()));
                }
            }

            retorno.put("FPRODUTOS", arrProdutos);
            retorno.put("FGARCON", getCdFunci());
            retorno.put("FNUM_MESA", getNumMesa());
            retorno.put("FTERMINAL", Variaveis.getNumTerminal());
            retorno.put("FSEQUENCIA", getSequencia());
            retorno.put("FTIPOVENDA", getTipoVenda());
            retorno.put("Flocal_entrega", getLocalEntrega());
            retorno.put("device_id", Variaveis.getDevice_id());
            retorno.put("NOME_CLIENTE", getNomeCliente());

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
            if (pagamentos != null) {
                for (int i = 0; i < pagamentos.size(); i++) {
                    PagamentoModel pg = pagamentos.elementAt(i);
                    arrPagamentos.put(new JSONObject()
                            .put("FCDFPAGA", String.valueOf(pg.getPgpCdfpag()))
                            .put("FVALOR", String.valueOf(pg.getPgpVlrpag()))
                            .put("FEVTIPO", pg.getPgpEvtipo())
                            .put("FNSU", pg.getNsu())
                            .put("FAUTORIZACAO", pg.getAutorizacao())
                            .put("FBANDEIRA", pg.getBandeira()));
                }
            }

            retorno.put("FPAGAMENTOS", arrPagamentos);
            retorno.put("FGARCOM", getCdFunci());
            retorno.put("FNUM_MESA", getNumMesa());
            retorno.put("FNUM_TERMINAL", Variaveis.getNumTerminal());
            retorno.put("FCPFCNPJ_CLIENTE", Variaveis.getcpfcliente());
            retorno.put("FNOME_CLIENTE", Variaveis.getNomecliente());

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