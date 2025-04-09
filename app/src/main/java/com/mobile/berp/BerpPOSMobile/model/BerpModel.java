package com.mobile.berp.BerpPOSMobile.model;

import com.mobile.berp.BerpPOSMobile.Controller.Funcoes;
import com.mobile.berp.BerpPOSMobile.Controller.Proxy;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.CompletableFuture;

public class BerpModel {

    private static Vector[]	mesa = null;
    private static Boolean filtromesa = false;

    public static Boolean getFiltromesa() {
        return filtromesa;
    }

    public static void setFiltromesa(Boolean filtromesa) {
        BerpModel.filtromesa = filtromesa;
    }

    public static Boolean getFiltrocartao() {
        return filtrocartao;
    }

    public static void setFiltrocartao(Boolean filtrocartao) {
        BerpModel.filtrocartao = filtrocartao;
    }

    public static Boolean getFiltrobalcao() {
        return filtrobalcao;
    }

    public static void setFiltrobalcao(Boolean filtrobalcao) {
        BerpModel.filtrobalcao = filtrobalcao;
    }

    private static Boolean filtrocartao = false;
    private static Boolean filtrobalcao = false;

    public static String getId() {
        return id;
    }

    public static void setId(String id) {
        BerpModel.id = id;
    }

    private static String id;
    private static String	funcionario	= "0", numMesa = "0", selectedCMD = "0" ,tpvend="0" ,nomeCliente="0" ,localEntrega="0";


    public static void setSelectedCMD(String selectedCMD) {

        BerpModel.selectedCMD = selectedCMD;
    }

    public static String getSelectedCMD() {

        return selectedCMD;
    }

    public static int getCombinadoAtu(String nMesa) {

        int codigo = Integer.parseInt(nMesa);
        if (mesa[codigo] == null)
            return -1;
        else
            return verificaComb(codigo);
    }

    private static int verificaComb(int nMesa) {

        int max = 0;
        for (int i = 0; i < mesa[nMesa].size(); i++) {
            Produto p = (Produto) mesa[nMesa].elementAt(i);
            if (p.getCombinado() > max) {
                max = p.getCombinado();
            }
        }
        return max;
    }

    public static void setFuncionario(String funcionario) {

        BerpModel.funcionario = funcionario;
    }

    public static String getFuncionario() {

        return funcionario;
    }

    public static String getNumMesa() {

        return numMesa;
    }

    public static void setNumMesa(String numMesa) {

        BerpModel.numMesa = numMesa;
    }

    public static void setNomeCliente(String nomeCliente) {
        BerpModel.nomeCliente= nomeCliente;
    }

    public static String getNomeCliente() {
        return nomeCliente;
    }

    public static String getLocalEntrega() {
        return localEntrega;
    }

    public static String getTpvend() {
        return tpvend;
    }

    public static void setLocalEntrega(String localEntrega) {
        BerpModel.localEntrega = localEntrega;
    }

    public static void setTpvend(String tpvend) {
        BerpModel.tpvend = tpvend;
    }



    public static boolean inicializar() {

        try {
            Proxy.cargas();
            int nMesas = Integer.parseInt(Variaveis.getConfiguracao("max_mesas").getValor());
            mesa = new Vector[4000];
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean verificaPagamentos(String id) throws Exception {

        boolean retorno;
        id = String.valueOf(Integer.parseInt(id));
        switch (Proxy.statusPagamento(id)) {
            case -1:
                //throw new IllegalArgumentException("Erro ao tentar verificar a mesa!");
                retorno = true;
                break;
            case 1:
               // throw new IllegalArgumentException("Mesa ja contem pagamentos!");
                retorno = true;
                break;
            case 0:
                retorno = true;
                break;
            default:
                throw new IOException("Erro desconhecido! Reinicie o aplicativo do servidor e tente novamente!");
        }
        return retorno;
    }






    public static boolean efetuarLoginSync(String usuario, String senha) throws JSONException {
        Funcionario f = Variaveis.getFuncionario(Integer.parseInt(usuario));
        if (f != null && f.getSenha().equals(senha)) {
            funcionario = String.valueOf(f.getCodigo());
            Variaveis.setUser_name(f.getNome());

            String json = new JSONObject()
                    .put("usuario", usuario)
                    .put("senha", senha)
                    .put("terminal_id", Variaveis.getTerminal_id())
                    .put("device_id", Variaveis.getDevice_id())
                    .toString();

            return Proxy.efetuarLogin(json).join(); // Chamada síncrona, mas fora da UI thread
        }
        return false;
    }

    public static boolean efetuarLogin(String usuario, String senha) throws JSONException {
        boolean retorno = false;

        Funcionario f = Variaveis.getFuncionario(Integer.parseInt(usuario));
        if (f != null && f.getSenha().equals(senha)) {
            funcionario = String.valueOf(f.getCodigo());
            Variaveis.setUser_name(f.getNome());

            String json = new JSONObject()
                    .put("usuario", usuario)
                    .put("senha", senha)
                    .put("terminal_id",Variaveis.getTerminal_id())
                    .put("device_id", Variaveis.getDevice_id())
                    .toString();

            try {
                // Aguarda o término da operação assíncrona
                boolean success= Proxy.efetuarLogin(json).join();
                if (success && Variaveis.getToken() != null && !Variaveis.getToken().isEmpty()) {
                    System.out.println("Login realizado com sucesso!");
                    System.out.println("Usuário: " + Variaveis.getUserName());
                    System.out.println("Token: " + Variaveis.getToken());
                    retorno = true;
                } else {
                    System.out.println("Login falhou: token inválido ou inexistente.");
                }
            } catch (Exception e) {
                System.err.println("Erro ao efetuar login: " + e.getMessage());
            }
        }
        return retorno;
    }

    public static String[][] Mesas() throws Exception {

        Mesa m;
        Vector<Mesa> mesas = Proxy.listarMesas().get();
        String[][] retorno = new String[][] { { "", "", "", "" } };

        if ((mesas != null)) {
            retorno = new String[mesas.size()][4];
            for (int i = 0; i < mesas.size(); i++) {
                m = mesas.elementAt(i);
                retorno[i][0] = m.getNumMesa();
                retorno[i][1] = m.getCdFunci();
                retorno[i][2] = m.getVlrVen();
                retorno[i][3] = m.getStatus();
            }
        }
        return retorno;
    }

    public static String[] Mesas2() throws Exception {

        Mesa m;
        Vector<Mesa> mesas = Proxy.listarMesas().get();
        String[] retorno = new String[] { "", "", "", "" };

        if ((mesas != null)) {
            retorno = new String[mesas.size()];
            for (int i = 0; i < mesas.size(); i++) {
                m = mesas.elementAt(i);
                retorno[i] = m.getNumMesa();
                retorno[i] += " | " + m.getCdFunci();
                retorno[i] += " | " + m.getVlrVen();
                retorno[i] += " | " + m.getStatus();
            }
        }
        return retorno;
    }

    public static String prodDesc(String cod) {

        Produto p = Variaveis.getProduto(Integer.parseInt(cod));
        String retorno = "Produto Inválido";
        if (p != null) {
            retorno = p.getDesc();
        }
        return retorno;
    }

    public static String prodValor(String cod) {

       Produto p = Variaveis.getProduto(Integer.parseInt(cod));
        String retorno = "0";

        if (p != null) {
            retorno = p.getValorUnitarioFormatado().replace("R$","");
            retorno = retorno.replace(",",".");
        }
        return retorno.replaceAll("\\s+", "");
    }

    public static String getProdCodigo(String descricao) {

        Produto p = Variaveis.getProdutoDescricao(descricao);
        String retorno = "";
        if (p != null) {
            retorno = String.valueOf(p.getCod());
        }
        return retorno;
    }

    public static boolean verificaProduto(String cod){
        Produto p = Variaveis.getProduto(Integer.parseInt(cod));
        return p != null;
    }

    public static String prodDescComb(String cod) {

        Produto p = Variaveis.getProdutoComb(Integer.parseInt(cod));
        String retorno = "Produto Inválido";
        if (p != null) {
            retorno = p.getDesc();
        }
        return retorno;
    }

    public static String obsDesc(String cod) {

        Observacao o = Variaveis.getObservacao(Integer.parseInt(cod));
        String retorno = "Observação Inválida";
        if (o != null) {
            retorno = o.getDescricao();
        }
        return retorno;
    }

    @SuppressWarnings("unchecked")
    public static boolean addProd(String nMesa, String codProdu, String obs, String qtd) {

        boolean retorno;
        if (mesa[Integer.parseInt(nMesa)] == null) {
            mesa[Integer.parseInt(nMesa)] = new Vector<Produto>(1, 1);

        }
        Produto p = Variaveis.getProduto(Integer.parseInt(codProdu));
        if (p != null){
            p.setObs(obs);
            p.setQtd(qtd);
            p.setCombinado(0);
            mesa[Integer.parseInt(nMesa)].addElement(p);
            retorno = true;
        }else {
            retorno = false;
        }

        return retorno;
    }


    @SuppressWarnings("unchecked")
    public static boolean addProd(String nMesa, String codProdu, String obs, String qtd, int combinado) throws Exception {

        boolean retorno;
        if (mesa[Integer.parseInt(nMesa)] == null) {
            mesa[Integer.parseInt(nMesa)] = new Vector<Produto>(1, 1);

        }
        Produto p = Variaveis.getProduto(Integer.parseInt(codProdu));
        p.setObs(obs);
        p.setQtd(qtd);
        p.setCombinado(combinado);
        mesa[Integer.parseInt(nMesa)].addElement(p);
        retorno = true;
        return retorno;

    }

    public static void addPag(String nMesa, int cadfpaga, double valor, int cai_evtipo) {

        if (mesa[Integer.parseInt(nMesa)] == null) {
            mesa[Integer.parseInt(nMesa)] = new Vector<Pagamento>(1, 1);
        }

        Pagamento pag = new Pagamento();
        pag.setCdfpaga(cadfpaga);
        pag.setValor(valor);
        pag.setEvtipo(cai_evtipo);

        mesa[Integer.parseInt(nMesa)].addElement(pag);

    }

    public static void addPag(String nMesa, int cadfpaga, double valor, int cai_evtipo,String nsu,String autorizacao,String bandeira, String cvNumber) {

        if (mesa[Integer.parseInt(nMesa)] == null) {
            mesa[Integer.parseInt(nMesa)] = new Vector<Pagamento>(1, 1);
        }

        Pagamento pag = new Pagamento();
        pag.setCdfpaga(cadfpaga);
        pag.setValor(valor);
        pag.setEvtipo(cai_evtipo);
        pag.setNsu(nsu);
        pag.setAutorizacao(autorizacao);
        pag.setBandeira(bandeira);
        pag.setCvNumber(cvNumber);
        mesa[Integer.parseInt(nMesa)].addElement(pag);

    }

    public static String[][] listaProdutos(String nMesa) {

        return new Funcoes().listaProdutos(mesa[Integer.parseInt(nMesa)]);
    }

    public static List<Produto> listaProdutosMesa(String nMesa) {

        return new Funcoes().listaProdutosMesa(mesa[Integer.parseInt(nMesa)]);
    }

    public static List<Pagamento> listaPagamentosMesa(String nMesa) {

        return new Funcoes().listaPagamentosMesa(mesa[Integer.parseInt(nMesa)]);
    }


    public static void limpaMesa(String nMesa) {

        mesa[Integer.parseInt(nMesa)].removeAllElements();
    }

    public static void limpaMesaPagamentos(String nMesa, List<Pagamento> pags) {

        mesa[Integer.parseInt(nMesa)].removeAll(pags);
    }

    @SuppressWarnings("unchecked")
    public static boolean enviarPedido(String nMesa, String garcom, String controleDuplicidade,String tipoVenda,String localEntrega,String nomeCliente) throws Exception {
        String Tpvend;
        boolean retorno;
        int numMesa = Integer.parseInt(nMesa);
        if (mesa[numMesa] == null) {
            throw new NullPointerException("Esta mesa não existe!");
        }
        if (mesa[numMesa].isEmpty()) {
            throw new IllegalArgumentException("Pedido Sem Itens!");
        }

        switch (tipoVenda)  {
            case "MESA":
                Tpvend="0";
                break;
            case "CARTAO":
                Tpvend="4";
                break;

            case "BALCAO":
                Tpvend="1";
                break;
            default:
               Tpvend="0";
               break;
        }


        Mesa m = new Mesa(nMesa, garcom, mesa[numMesa], controleDuplicidade,Tpvend,localEntrega,nomeCliente);
        switch (Proxy.abrirMesa(m.toJSON()).get()) {
            case 1:
                retorno = true;
                break;
            case 3:
                throw new IllegalArgumentException("Esta mesa encontra-se fechada e não pode receber novos itens!");
            case -1:
                throw new IOException("Falha ao enviar pedido!");
            case -2:
                throw new IllegalArgumentException(
                        "Não será possível efetuar lançamentos até que o movimento do dia esteja aberto!");
            case -3:
                throw new IllegalArgumentException(
                        "Erro ao Enviar Pedido!");
            default:
                throw new IOException("Erro desconhecido! Reinicie o aplicativo do servidor e tente novamente!");

        }
        return retorno;
    }

    public static CompletableFuture<Boolean> statusMesa(String nmesa, String TP_VEND) {
        // Garantir que nMesa seja um número válido
      //  nMesa = String.valueOf(Integer.parseInt(nMesa));

        // Retorna um CompletableFuture que gerencia a lógica
        return Proxy.statusMesa_posicao(nmesa,TP_VEND).thenApply(status -> {
            switch (status) {
                case -1:
                    throw new IllegalArgumentException("Erro ao tentar verificar a mesa!");
                case -2:
                    throw new IllegalArgumentException(
                            "Não será possível efetuar lançamentos até que o movimento do dia esteja aberto!");
                case 3:
                    throw new IllegalArgumentException("A Posicao " + id + " já encontra-se aguardando pagamento!!");
                case 1:
                case 0:
                    // Inicializa a mesa com um novo vetor de produtos
                    mesa[Integer.parseInt(id)] = new Vector<>(1, 1);
                    return true; // Retorna verdadeiro para indicar sucesso
                default:
                    throw new RuntimeException("Erro desconhecido! Reinicie o aplicativo do servidor e tente novamente!");
            }
        }).exceptionally(e -> {
            System.err.println("Erro ao verificar status da mesa: " + e.getMessage());
            return false; // Retorna falso em caso de erro
        });
    }

    public static CompletableFuture<Boolean> statusAtendimento(String nmesa, String TP_VEND)  {
        // Garantir que nMesa seja um número válido
        numMesa="1000";
        if (!nmesa.equalsIgnoreCase("")){ numMesa = String.valueOf(Integer.parseInt(nmesa));}
        // Retorna um CompletableFuture que gerencia a lógica
        return Proxy.statusMesa_posicao(nmesa,TP_VEND).thenApply(status -> {
            switch (status) {
                case -1:
                    throw new IllegalArgumentException("Erro ao tentar verificar a mesa!");
                case -2:
                    throw new IllegalArgumentException(
                            "Não será possível efetuar lançamentos até que o movimento do dia esteja aberto!");
                case 3:
                    throw new IllegalArgumentException("A Posicao " + nmesa + " já encontra-se aguardando pagamento!!");
                case 1:
                case 0:
                    // Inicializa a mesa com um novo vetor de produtos
                    mesa[Integer.parseInt(numMesa)] = new Vector<>(1, 1);
                    return true; // Retorna verdadeiro para indicar sucesso
                default:
                    throw new RuntimeException("Erro desconhecido! Reinicie o aplicativo do servidor e tente novamente!");
            }
        }).exceptionally(e -> {
            System.err.println("Erro ao verificar status da mesa: " + e.getMessage());
            return false; // Retorna falso em caso de erro
        });
    }



    public static CompletableFuture<Integer> idAtendimento(String nmesa, String TP_VEND) {
        return Proxy.retornaIdatendimento(nmesa,TP_VEND).thenApply(id -> id).exceptionally(e -> {
            System.err.println("Erro ao verificar status da mesa: " + e.getMessage());
            return -1; // Retorna -1 em caso de erro
        });
    }



    public static CompletableFuture<Integer> statusMesas(String nmesa,String TP_VEND) {
        // Garantir que nMesa seja um número válido
        //id = String.valueOf(Integer.parseInt(id));

        // Retorna um CompletableFuture que gerencia a lógica
        return Proxy.statusMesa_posicao(nmesa,TP_VEND).thenApply(status -> {
            switch (status) {
                case -1:
                    throw new IllegalArgumentException("Erro ao tentar verificar a mesa!");
                case -2:
                    throw new IllegalArgumentException(
                            "Não será possível efetuar lançamentos até que o movimento do dia esteja aberto!");
                case 3:
                    return 3; // Retorna status 3
                case 1:
                    return 1; // Retorna status 1
                case 0:
                    throw new IllegalArgumentException("Esta mesa não encontrada ou está sem itens!");
                default:
                    throw new RuntimeException("Erro desconhecido! Reinicie o aplicativo do servidor e tente novamente!");
            }
        }).exceptionally(e -> {
            System.err.println("Erro ao verificar status da mesa: " + e.getMessage());
            return -1; // Retorna -1 em caso de erro
        });
    }
    public static boolean isMesaEmpty(String nMesa) {

        if (mesa[Integer.parseInt(nMesa)] == null) {
            return true;
        } else if (mesa[Integer.parseInt(nMesa)].size() == 0) {
            return true;
        } else {
            return false;
        }
    }

    public static Vector<String> mesaSimples(String id) throws Exception {

        Vector<String> retorno = null;
        ContaFields m = Proxy.visualizaConta(id, 1).get();
        if (m != null) {
            retorno = new Funcoes().preparaImpressaoList(m);
        }
        return retorno;
    }

    public static Vector<String> mesaDetalhada(String id) throws Exception {

        ContaFields m = Proxy.visualizaConta(id, 0).get();
        return new Funcoes().preparaImpressaoDetalhada(m);
    }

    public static String mesaDetalhadaPraImpressao(String id) throws Exception {

        ContaFields m = Proxy.visualizaConta(id, 0).get();
        return new Funcoes().preparaImpressaoConta(m);
    }

    public static String mesaDetalhadaPraImpressao(ContaFields m) {

       // Mesa m = Proxy.java.visualizaConta(nMesa, 0);
        return new Funcoes().preparaImpressaoConta(m);

    }

    public static String mesaPagaPraImpressao(ContaFields m, List<Pagamento> pagamentos) {

     //   Mesa m = Proxy.java.visualizaConta(nMesa, 0);
        return new Funcoes().preparaImpressaoCompPagamento(m, pagamentos);
    }

    public static boolean permissao(String nmPermi) {

        boolean retorno = false;
        if (Variaveis.getConfiguracao(nmPermi) != null) {
            if (Variaveis.getConfiguracao(nmPermi).getValor().equalsIgnoreCase("s")
                    || Variaveis.getConfiguracao(nmPermi).getValor().equalsIgnoreCase("1")) {
                retorno = true;
            }
        }

        return retorno;
    }

    public static String fecharMesa(String nMesa, String garcon, String imp, String numeroClientes,String id) throws Exception {

        // Fechar Mesa com impressora
    //    Impressora impressora = Variaveis.getImpressora(imp);
        nMesa = String.valueOf(Integer.parseInt(nMesa));
        String retorno = "Não foi possivel conectar ao servidor!";
        switch (Proxy.fecharMesa(id, garcon, Variaveis.getNumTerminal(), imp, numeroClientes).get()) {
            // Erro
            case -1:
                retorno = "Falha ao enviar pedido!";
                break;
            // Mesa nao existe
            case 0:
                retorno = "Atendimento " + nMesa + " não existe!";
                break;
            // Mesa fechada
            case 1:
                retorno = "Atendimento " + nMesa + " fechada com sucesso!";
                break;
            // Movimento fechado
            case -2:
                retorno = "Não será possivel fazer lançamentos até que o movimento do dia seja aberto!";
                break;
        }

        return retorno;
    }

    @SuppressWarnings("unchecked")
    public static String efetuarPagamento(String nMesa, String garcon) throws Exception {

        int numMesa = Integer.parseInt(nMesa);
        if (mesa[numMesa] == null) {
            throw new NullPointerException("Esta mesa não existe!");
        }

        String retorno = "Não foi possivel conectar ao servidor!";
        Mesa m = new Mesa(nMesa, garcon, mesa[numMesa]);
        switch (Proxy.efetuaPagamento(m.pagamentotoJSON())) {
            // Erro
            case -1:
                retorno = "Falha ao enviar pagamento!";
                break;
            // Mesa nao existe
            case 0:
                retorno = "A mesa " + nMesa + " não existe!";
                break;
            // Pagamento efetuado
            case 1:
                retorno = "Pagamento enviado com sucesso!";
                break;
             // ja tem pagamento
            case 2:
                retorno = "Venda ja contem pagamentos!";
                break;
            // Movimento fechado
            case -2:
                retorno = "Não será possivel fazer pagamentos até que o movimento do dia seja aberto!";
                break;
        }

        return retorno;
    }

    public static String[] listaImpressoras() {

        Vector<Impressora> impressoras = Variaveis.getImpressoras();
        Impressora imp;
        String[] retorno = new String[impressoras.size()];
        for (int i = 0; i < impressoras.size(); i++) {
            imp = impressoras.elementAt(i);
            retorno[i] = imp.getNome();
        }
        return retorno;
    }

    public static boolean reAbrirMesa(String nMesa) throws Exception {

        boolean retorno;
        retorno = Proxy.reAbrirMesa(nMesa).get();
        return retorno;
    }

    public static boolean maxMesa(String nMesa) {

        boolean retorno = false;
        int max = Integer.parseInt(Variaveis.getConfiguracao("max_mesas").getValor()), min = 0, num = Integer.parseInt(nMesa);
        if (num > min && num <= max) {
            retorno = true;
        }
        return retorno;
    }

    public static void removeProd(String nMmesa, int index) throws Exception{

        if (index >= mesa[Integer.parseInt(nMmesa)].size() || index < 0) {
            throw new Exception("Este Item não existe");
        }

        mesa[Integer.parseInt(nMmesa)].removeElementAt(index);
    }

    public static void removePag(String nMmesa, int index) throws Exception{

        if (index >= mesa[Integer.parseInt(nMmesa)].size() || index < 0) {
            throw new Exception("Este Item não existe");
        }

        mesa[Integer.parseInt(nMmesa)].removeElementAt(index);
    }

    public static String ControleDuplicidade() {

        String obsRandom = "";
        Random random = new Random();
        for (int i = 0; i < 5; i++) {

            obsRandom += String.valueOf(random.nextInt(10));


        }
        return obsRandom;

    }


}
