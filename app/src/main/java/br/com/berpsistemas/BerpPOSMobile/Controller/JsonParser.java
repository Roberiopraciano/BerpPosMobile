package br.com.berpsistemas.BerpPOSMobile.Controller;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import br.com.berpsistemas.BerpPOSMobile.model.Configuracao;
import br.com.berpsistemas.BerpPOSMobile.model.ContaFields;
import br.com.berpsistemas.BerpPOSMobile.model.Funcionario;
import br.com.berpsistemas.BerpPOSMobile.model.Impressora;
import br.com.berpsistemas.BerpPOSMobile.model.ItemFields;
import br.com.berpsistemas.BerpPOSMobile.model.Loja;
import br.com.berpsistemas.BerpPOSMobile.model.LojaResponse;
import br.com.berpsistemas.BerpPOSMobile.model.Mesa;
import br.com.berpsistemas.BerpPOSMobile.model.Observacao;
import br.com.berpsistemas.BerpPOSMobile.model.Produto;
import br.com.berpsistemas.BerpPOSMobile.model.Variaveis;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.lang.reflect.Type;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Vector;

public class JsonParser {

    public int resultInt(String json)  {
        try {
            JSONObject result = new JSONObject(json);
            JSONArray arr = result.getJSONArray("result");

            return arr.getInt(0);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean resultBoolean(String json)  {

        try {
            JSONObject result = new JSONObject(json);
            JSONArray arr = result.getJSONArray("result");

            return arr.getBoolean(0);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public Vector<Mesa> mesas(String json) throws Exception {

        JSONObject mesasInfo;
        Mesa m;
        Vector<Mesa> mesas = new Vector<Mesa>(1, 1);

        mesasInfo = new JSONObject(json).getJSONArray("result")
                .getJSONObject(0);

        if (!mesasInfo.has("VEN_VLRLIQ")) {
            throw new IllegalArgumentException("Não existem mesas abertas!");
        }

        for (int i = 0; i < mesasInfo.getJSONArray("VEN_VLRLIQ").length(); i++) {
            m = new Mesa();

            m.setVlrLiq(mesasInfo.getJSONArray("VEN_VLRLIQ").getString(i));
            m.setCdFunci(mesasInfo.getJSONArray("VEN_CDGARC").getString(i));
            m.setNumVenda(mesasInfo.getJSONArray("VEN_CDMESA").getString(i));
            m.setVlrVen(mesasInfo.getJSONArray("VEN_VLRBRU").getString(i));
            m.setVlrSer(mesasInfo.getJSONArray("VEN_VLRSER").getString(i));
            switch (Integer.parseInt(mesasInfo.getJSONArray("VEN_STATUS")
                    .getString(i))) {
                case 1:
                    m.setStatus("Aberta");
                    break;
                case 3:
                    m.setStatus("Fechada");
                    break;
                default:
                    m.setStatus("Desconhecido");
            }
            String x = mesasInfo.getJSONArray("VEN_CDMESA").getString(i);
            if (x.length() < 5) {
                String y = "";
                for (int j = 0; j < 5 - x.length(); j++) {
                    y = y + " ";
                }
                x = y + x;
            }
            m.setNumMesa(x);
            m.setId(mesasInfo.getJSONArray("FID").getString(i));
            mesas.addElement(m);
        }

        return mesas;
    }

    public class StringToDoubleConverter {
        public static double safeParseDouble(String value, double defaultValue) {
            return Optional.ofNullable(value)
                    .map(v -> {
                        try {
                            return Double.parseDouble(v);
                        } catch (NumberFormatException e) {
                            return defaultValue;
                        }
                    })
                    .orElse(defaultValue);
        }

        public static void main(String[] args) {
            System.out.println(safeParseDouble("123.45", 0.0)); // 123.45
            System.out.println(safeParseDouble("xyz", 0.0)); // 0.0
        }
    }

    public void cargaProdutos(String json) throws Exception {
        Produto prod;
        JSONArray tabela, array;
        JSONObject result, produtos, produto;

        try {
            result = new JSONObject(json);
            tabela = result.getJSONArray("result");
            array = tabela.getJSONArray(0);
            for (int i = 0; i < array.length(); i++) {
                produtos = array.getJSONObject(i);
                produto = produtos.getJSONObject("fields");

                Log.d("cargaProdutos", produto.toString());


                prod = new Produto();


                prod.setCod(produto.optInt("FPRO_CDPROD", 0));
                prod.setCodGrupo(produto.optInt("FPRO_CDGRUP", 0));
                prod.setDesc(produto.optString("FPRO_NMPROD", "Sem descrição"));
                prod.setValorUnitario(parseDoubleSafe(produto.optString("FPRO_VLRVEN", "0")));
                prod.setCombinado(produto.optInt("FCOMBINADO", 0));
                prod.setAbreviado(produto.optString("FABREV", ""));
                prod.setFracionado(produto.optInt("FFRACIONADO", 0));
                prod.setQtdAparente(produto.optInt("FQTDAPARENTE", 0));
                prod.setAliq(produto.optInt("Fali_cdaliq", 0));
                prod.setIcotaecf(produto.optString("Falicotaecf", ""));
                prod.setServico(produto.optString("Fservico", ""));
                prod.setAtrasoPorKms(produto.optString("Fatrazo_por_kms", ""));
                prod.setApareceControlador(produto.optString("Faparece_controlador", ""));
                prod.setPeso(parseDoubleSafe(produto.optString("Fpro_prpeso", "0")));
                prod.setSegunda(produto.optInt("FSEGUNDA", -1));
                prod.setTerca(produto.optInt("FTERCA", -1));
                prod.setQuarta(produto.optInt("FQUARTA", -1));
                prod.setQuinta(produto.optInt("FQUINTA", -1));
                prod.setSexta(produto.optInt("FSEXTA", -1));
                prod.setSabado(produto.optInt("FSABADO", -1));
                prod.setDomingo(produto.optInt("FDOMINGO", -1));
                prod.setControle_data(produto.optInt("FCONTROLE_HORA", 0));
                prod.setTimeInicio(produto.optDouble("FTIME_INI", 0));
                prod.setTimeFim(produto.optDouble("FTIME_FIM", 0));
                prod.setDataInicio(produto.optInt("FINICIO", 0));
                prod.setDataValidade(produto.optInt("FVALIDADE", 0));

                Variaveis.addProduto(prod);
            }
        } catch (Exception e) {
            throw new Exception("Não foi possível conectar ao servidor!( "
                    + e.getMessage() + ")");
        }
    }

    private static double parseDoubleSafe(String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    public void cargaProdutosComb(String json) throws Exception {
        Produto prod;
        JSONArray tabela, array;
        JSONObject result, produtos, produto;

        try {
            result = new JSONObject(json);
            tabela = result.getJSONArray("result");
            array = tabela.getJSONArray(0);

            for (int i = 0; i < array.length(); i++) {
                produtos = array.getJSONObject(i);
                produto = produtos.getJSONObject("fields");

                prod = new Produto();
                prod.setCod(produto.getInt("FPRO_CDPROD"));
                prod.setDesc(produto.getString("FPRO_NMPROD"));
                prod.setValorUnitario(1);
                Variaveis.addProdutoComb(prod);
            }
        } catch (Exception e) {
            throw new Exception("Não foi possível conectar ao servidor!( "
                    + e.getMessage() + ")");
        }
    }

    public void cargaConfiguracoes(String json) throws Exception {
        Configuracao config;
        JSONArray tabela, array;
        JSONObject result, configuracoes, configuracao;

        try {
            result = new JSONObject(json);
            tabela = result.getJSONArray("result");
            array = tabela.getJSONArray(0);

            for (int i = 0; i < array.length(); i++) {
                configuracoes = array.getJSONObject(i);
                configuracao = configuracoes.getJSONObject("fields");

                config = new Configuracao(configuracao.getString("FCON_CDCODI"),configuracao.getString("FCON_DESCRI"));

                Variaveis.addConfiguracao(config);
            }
        } catch (Exception e) {
            throw new Exception("Não foi possível conectar ao servidor!( "
                    + e.getMessage() + ")");
        }
    }

    public void cargaFuncionarios(String json) throws Exception {
        Funcionario funci;
        JSONArray tabela, array;
        JSONObject result, funcionarios, funcionario;

        try {
            result = new JSONObject(json);
            tabela = result.getJSONArray("result");
            array = tabela.getJSONArray(0);

            for (int i = 0; i < array.length(); i++) {
                funcionarios = array.getJSONObject(i);
                funcionario = funcionarios.getJSONObject("fields");

                funci = new Funcionario();
                funci.setCodigo(funcionario.getInt("FFUN_CDCODI"));
                funci.setSenha(funcionario.getString("FFUN_NMSENH"));
                funci.setNome(funcionario.getString("FFUN_NMFUNC"));
                Variaveis.addFuncionario(funci);
            }
        } catch (Exception e) {
            throw new Exception("Não foi possível conectar ao servidor!( "
                    + e.getMessage() + ")");
        }
    }

    public void cargaImpressoras(String json) throws Exception {
        Impressora imp;
        JSONArray tabela, array;
        JSONObject result, impressoras, impressora;


//        if (Variaveis.getImprimirConta()){
//            imp = new Impressora(-1,"Pos");
//        Variaveis.addImpressora(imp);}

        imp = new Impressora(0,"Caixa");
        Variaveis.addImpressora(imp);

        try {

            result = new JSONObject(json);
            tabela = result.getJSONArray("result");
            array = tabela.getJSONArray(0);

            for (int i = 0; i < array.length(); i++) {
                impressoras = array.getJSONObject(i);
                impressora = impressoras.getJSONObject("fields");

                imp = new Impressora(Integer.parseInt(impressora.getString("FIMP_CDCODI")),impressora.getString("FIMP_DESCRI"));

                Variaveis.addImpressora(imp);
            }
        } catch (Exception e) {
            throw new Exception("Não foi possível conectar ao servidor!( "
                    + e.getMessage() + ")");
        }
    }

    public void cargaLoja(String json) throws Exception {


        try{
            Gson gson = new GsonBuilder().create();
            LojaResponse lojaResponse = gson.fromJson(json, LojaResponse.class);

            // Acessando os dados
            if (lojaResponse.getResult() != null) {
                for (List<Loja> lojaList : lojaResponse.getResult()) {
                    for (Loja loja : lojaList) {
                        System.out.println("Loja ID: " + loja.getId());
                        System.out.println("Loja Nome: " + loja.getFields().getfNomeLoja());
                        System.out.println("Loja Número: " + loja.getFields().getFnrLoja());
                        Variaveis.setLoja(loja);
                    }
                }
            }



        }catch (Exception e) {
            throw new Exception("Não foi possível conectar ao servidor!( "
                    + e.getMessage() + ")");
        }
    }

    public void cargaObservacoes(String json) throws Exception {
        Observacao obs;
        JSONArray tabela, array;
        JSONObject result, observacoes, observacao;

        try {
            result = new JSONObject(json);
            tabela = result.getJSONArray("result");
            array = tabela.getJSONArray(0);

            for (int i = 0; i < array.length(); i++) {
                observacoes = array.getJSONObject(i);
                observacao = observacoes.getJSONObject("fields");

                obs = new Observacao();
                obs.setCod(Integer.parseInt(observacao.getString("FOBS_CDCODI")));
                obs.setDescricao(observacao.getString("FOBS_DESCRI"));
                Variaveis.addObservacao(obs);
            }
        } catch (Exception e) {
            throw new Exception("Não foi possível conectar ao servidor!( "
                    + e.getMessage() + ")");
        }
    }

    public static <c> Object parserJsonObject(Type typeVariables, String json) {

        Gson gson = new Gson();
        // Type collectionType = new TypeToken<Collection<c>>(){}.getType();
        JSONArray tabela;
        JSONObject array;
        JSONObject result, terminais;
        Object ints2 = null;

        try {
            result = new JSONObject(json);
            tabela = result.getJSONArray("result");
            array = tabela.getJSONObject(0);

            terminais = array.getJSONObject("fields");
            // terminal = terminais.getJSONObject("fields");
            ints2 = gson.fromJson(terminais.toString(), typeVariables);

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return ints2;

    }
    // Método atualizado para JSON que já tem os campos no primeiro objeto de "result"
    public static ContaFields cargaMesaDetalhada(String json) throws Exception {
        // Parsear o JSON bruto
        JsonElement el = com.google.gson.JsonParser
                .parseString(json);

        JsonArray arr;
        if (el.isJsonObject()) {
            // {"result":[{…}]}
            JsonObject root = el.getAsJsonObject();
            arr = root.getAsJsonArray("result");
        } else if (el.isJsonArray()) {
            // [{…}]
            arr = el.getAsJsonArray();
        } else {
            throw new IllegalArgumentException("Formato inesperado de JSON");
        }

        if (arr == null || arr.size() == 0) {
            throw new IllegalArgumentException("Resposta sem dados de conta!");
        }

        // Pega o primeiro objeto que já é a conta
        JsonObject contaJson = arr.get(0).getAsJsonObject();

        // Desserializa TUDO de uma vez
        Gson gson = new GsonBuilder().create();
        ContaFields fields = gson.fromJson(contaJson, ContaFields.class);

        // Validações
        if (fields.getCdMesa() == null) {
            throw new IllegalArgumentException("Esta mesa não está aberta ou está sem itens!");
        }

        // Traduz status
        int st = Integer.parseInt(fields.getStatus());
        fields.setStatus(st == 1 ? "Aberta"
                : (st == 3 || st == 70) ? "Fechada"
                : "Desconhecido");

        // Converte itens em produtos (se você ainda precisar)
        if (fields.getVitList() == null ||
                fields.getVitList().getListHelper() == null ||
                fields.getVitList().getListHelper().isEmpty()) {
            throw new IllegalArgumentException("Mesa sem itens!");
        }
        for (ItemFields f : fields.getVitList().getListHelper()) {
            Produto p = new Produto();
            String tot  = f.getValorTotal()
                    .replace("R$", "")
                    .replace(".", "")
                    .replace(",", ".").trim();
            String unit = f.getValorUnitario()
                    .replace("R$", "")
                    .replace(".", "")
                    .replace(",", ".").trim();
            p.setCod(Integer.parseInt(f.getCodigo()));
            p.setDesc(f.getNome());
            p.setValorUnitario(Double.parseDouble(unit));
            p.setQtd(f.getQuantidade());
            p.setGarcon(f.getGarcom());
            p.setDhLancamento(f.getDataHoraLancamento());
            p.setValorTotal(Double.parseDouble(tot));
            fields.addProduto(p);
        }

        // Os pagamentos já estão em fields.getPagamentos()
        return fields;
    }



    private static JsonObject parse(String json) {
        return com.google.gson.JsonParser
                .parseString(json)
                .getAsJsonObject();
    }


    public static Mesa cargaMesaDetalhada_old(String json) throws Exception {
        JSONObject retorno, mesaInfo, prodInf;
        String erro;
        JSONArray arr, arrAux, arrProd;
        Mesa m = new Mesa();
        Produto p;

        retorno = new JSONObject(json);

        if (retorno.has("error")) {
            throw new IllegalArgumentException("Erro no Servidor! \n"
                    + retorno.get("error").toString());

        }

        arr = retorno.getJSONArray("result");

        arrAux = arr.getJSONArray(0);

        if (arrAux.length() <= 0) {
            throw new IllegalArgumentException(
                    "Esta mesa não se encontra aberta ou está sem itens!");
        }
        mesaInfo = arrAux.getJSONObject(0);
        mesaInfo = mesaInfo.getJSONObject("fields");

        if (!mesaInfo.has("FVEN_CDMESA")) {
            throw new IllegalArgumentException(
                    "Esta mesa não se encontra aberta ou está sem itens!");
        }

        // arrAux = mesaInfo.get("FVEN_CDMESA");
        m.setNumMesa(mesaInfo.get("FVEN_CDMESA").toString());

        m.setCdFunci(mesaInfo.getString("FVEN_CDGARC"));

        m.setVlrVen(mesaInfo.getString("FVEN_VLRBRU"));

        m.setNumVenda(mesaInfo.getString("FVEN_CDVEND"));

        m.setVlrLiq(mesaInfo.getString("FVEN_VLRLIQ"));

        m.setVlrSer(mesaInfo.getString("FVEN_VLRSER"));

        m.setVlrDesconto(mesaInfo.getString("FVEN_VLRDES"));

        m.setDhAbertura(mesaInfo.getString("FVEN_DHMOVI"));

        m.setId(mesaInfo.getString("FID"));

        switch (Integer.parseInt(mesaInfo.getString("FVEN_STATUS"))) {
            case 1:
                m.setStatus("Aberta");
                break;
            case 3:
                m.setStatus("Fechada");
                break;

            case 70:
                m.setStatus("Fechada");
                break;
            default:
                m.setStatus("Desconhecido");
        }

        prodInf = mesaInfo.getJSONObject("FVIT_LIST");

        prodInf = prodInf.getJSONObject("fields");

        if (!prodInf.has("FItems")) {
            throw new JSONException("Mesa sem itens!");

        }

        arrProd = prodInf.getJSONArray("FItems");

        for (int i = 0; i < arrProd.length(); i++) {

            if (!arrProd.isNull(i)) {
                prodInf = arrProd.getJSONObject(i);
                prodInf = prodInf.getJSONObject("fields");
                p = new Produto();

                p.setCod(prodInf.getInt("FCODIGO"));

                p.setDesc(prodInf.getString("FNOME"));

                p.setValorUnitario(1);

               // p.setValorUnitario(prodInf.getString("FVIT_VLRTOT"));

                p.setQtd(prodInf.getString("FVIT_QTDPRO"));

                p.setGarcon(prodInf.getString("FVIT_GARCON"));

                p.setDhLancamento(prodInf.getString("FVIT_DHLANC"));

                m.addProduto(p);
            }

        }
        return m;
    }

    public List<Mesa> getListaMesa(String json) {
        // TODO Auto-generated method stub

        JSONArray mesasInfo = null;
        JSONObject mesa;
        Mesa m;
        List<Mesa> mesas = new ArrayList<Mesa>();

        try {
            mesasInfo = new JSONObject(json).getJSONArray("result");

            // .getJSONObject(0);

            for (int i = 0; i < mesasInfo.getJSONArray(0).length(); i++) {
                mesa = mesasInfo.getJSONArray(0).getJSONObject(i).getJSONObject("fields");

                m = new Mesa();
                m.setDhAbertura(mesa.getString("FVEN_DHABER"));
                m.setNumMesa(mesa.getString("FVEN_CDMESA"));
                m.setNumVenda(mesa.getString("FVEN_CDVEND"));
                m.setStatus(mesa.getString("FVEN_STATUS"));
                m.setVlrTotal(mesa.getString("FVEN_VLRBRU"));
                m.setVlrVen(mesa.getString("FVEN_VLRNOT"));
                m.setCdFunci(mesa.getString("FFUN_NMFUNC"));
                m.setTipoVenda(mesa.getString("FVEN_TPVEND"));
                m.setNomeCliente(mesa.getString("FVEN_NMCLIE"));
                m.setLocalEntrega(mesa.getString("Flocal_entrega"));
                m.setId(mesa.getString("FID"));

                mesas.add(m);
            }
            // m.setDhAbertura(mesasInfo.getJSONArray("FVEN_DHABER").getString(0));
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return mesas;
    }
}
