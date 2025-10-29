package br.com.berpsistemas.BerpPOSMobile.model;

import android.util.Log;

import br.com.berpsistemas.BerpPOSMobile.Controller.Funcoes;
import br.com.berpsistemas.BerpPOSMobile.Controller.Proxy;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class BerpModel {

    public static int cdAtendimento;
    private static Vector[] mesa = null; // Keep for backward compatibility
    private static Vector<Produto>[] mesaProdutos = null;
    private static Vector<PagamentoModel>[] mesaPagamentos = null;
    private static Boolean filtromesa = false;
    private static String id;

    private static Boolean filtrocartao = false;
    private static Boolean filtrobalcao = false;

    private static String funcionario = "0", numMesa = "0", selectedCMD = "0", tpvend = "0", nomeCliente = "0", localEntrega = "0";

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

    public static String getId() {
        return id;
    }

    public static void setId(String id) {
        BerpModel.id = id;
    }

    public static String sincronizarPagamentos(PagamentoModel pag) {
        try {
            String controleDuplicidade = BerpModel.ControleDuplicidade();
            Proxy.enviarPagamento(pag, controleDuplicidade).thenAccept(resultado -> {
                if (resultado) {
                    Log.d("PAGAMENTO", "Enviado com sucesso");
                }
            });
            return "Sincronização concluída com sucesso";
        } catch (Exception e) {
            return "Erro: " + e.getMessage();
        }
    }

    public static void setSelectedCMD(String selectedCMD) {
        BerpModel.selectedCMD = selectedCMD;
    }

    public static String getSelectedCMD() {
        return selectedCMD;
    }

    public static int getCombinadoAtu(String nMesa) {
        int codigo = Integer.parseInt(nMesa);
        if (mesaProdutos[codigo] == null)
            return -1;
        else
            return verificaComb(codigo);
    }

    private static int verificaComb(int nMesa) {
        int max = 0;
        if (mesaProdutos[nMesa] != null) {
            for (int i = 0; i < mesaProdutos[nMesa].size(); i++) {
                Produto p = mesaProdutos[nMesa].elementAt(i);
                if (p.getCombinado() > max) {
                    max = p.getCombinado();
                }
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
        BerpModel.nomeCliente = nomeCliente;
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

    public static int getCdAtendimento() {
        return cdAtendimento;
    }

    public static void setCdAtendimento(int cdAtendimento) {
        BerpModel.cdAtendimento = cdAtendimento;
    }

    public static String getNmTpvend() {
        if (BerpModel.getTpvend() == null) {
            return "Desconhecido";
        }
        switch (BerpModel.getTpvend()) {
            case "0":
                return "Mesa";
            case "1":
                return "Balcão";
            case "2":
                return "Delivery";
            case "3":
                return "Drive‑in";
            case "4":
                return "Cartão";
            case "5":
                return "Encomenda";
            default:
                return "Desconhecido";
        }
    }

    @SuppressWarnings("unchecked")
    public static boolean inicializar() {
        try {
            Proxy.cargas();
            int nMesas = Integer.parseInt(Variaveis.getConfiguracao("max_mesas", "2500").getValor());
            mesa = new Vector[4000]; // Keep for backward compatibility
            mesaProdutos = new Vector[4000]; // Initialize separate arrays
            mesaPagamentos = new Vector[4000];
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
                retorno = true;
                break;
            case 1:
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

            return Proxy.efetuarLogin(json).join();
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
                    .put("terminal_id", Variaveis.getTerminal_id())
                    .put("device_id", Variaveis.getDevice_id())
                    .toString();

            try {
                boolean success = Proxy.efetuarLogin(json).join();
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
        String[][] retorno = new String[][]{{""}, {""}, {""}, {""}};

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
        String[] retorno = new String[]{"", "", "", ""};

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
            retorno = p.getValorUnitarioFormatado().replace("R$", "");
            retorno = retorno.replace(",", ".");
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

    public static boolean verificaProduto(String cod) {
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
        int mesaIndex = Integer.parseInt(nMesa);

        // Initialize if null
        if (mesaProdutos[mesaIndex] == null) {
            mesaProdutos[mesaIndex] = new Vector<Produto>(1, 1);
        }

        Produto p = Variaveis.getProduto(Integer.parseInt(codProdu));
        if (p != null) {
            p.setObs(obs);
            p.setQtd(qtd);
            p.setCombinado(0);
            mesaProdutos[mesaIndex].addElement(p);
            retorno = true;
        } else {
            retorno = false;
        }
        return retorno;
    }

    @SuppressWarnings("unchecked")
    public static boolean addProd(String nMesa, String codProdu, String obs, String qtd, int combinado) throws Exception {
        boolean retorno;
        int mesaIndex = Integer.parseInt(nMesa);

        if (mesaProdutos[mesaIndex] == null) {
            mesaProdutos[mesaIndex] = new Vector<Produto>(1, 1);
        }

        Produto p = Variaveis.getProduto(Integer.parseInt(codProdu));
        p.setObs(obs);
        p.setQtd(qtd);
        p.setCombinado(combinado);
        mesaProdutos[mesaIndex].addElement(p);
        retorno = true;
        return retorno;
    }

    public static void addPag(String nMesa, int cadfpaga, double valor, int cai_evtipo) {
        int mesaIndex = Integer.parseInt(nMesa);

        if (mesaPagamentos[mesaIndex] == null) {
            mesaPagamentos[mesaIndex] = new Vector<PagamentoModel>(1, 1);
        }

        PagamentoModel pag = new PagamentoModel();
        pag.setPgpCdfpag(cadfpaga);
        pag.setPgpVlrpag(valor);
        pag.setPgpEvtipo(cai_evtipo);

        mesaPagamentos[mesaIndex].addElement(pag);
    }

    public static void addPag(String nMesa, int cadfpaga, double valor, int cai_evtipo, String nsu, String autorizacao, String bandeira, String cvNumber) {
        int mesaIndex = Integer.parseInt(nMesa);

        if (mesaPagamentos[mesaIndex] == null) {
            mesaPagamentos[mesaIndex] = new Vector<PagamentoModel>(1, 1);
        }

        PagamentoModel pag = new PagamentoModel();
        pag.setPgpCdfpag(cadfpaga);
        pag.setPgpVlrpag(valor);
        pag.setPgpEvtipo(cai_evtipo);
        pag.setNsu(nsu);
        pag.setAutorizacao(autorizacao);
        pag.setBandeira(bandeira);
        pag.setCvNumber(cvNumber);
        mesaPagamentos[mesaIndex].addElement(pag);
    }

    public static String[][] listaProdutos(String nMesa) {
        return new Funcoes().listaProdutos(mesaProdutos[Integer.parseInt(nMesa)]);
    }

    public static List<Produto> listaProdutosMesa(String nMesa) {
        int mesaIndex = Integer.parseInt(nMesa);
        List<Produto> retorno = new ArrayList<>();

        if (mesaProdutos[mesaIndex] != null) {
            for (int i = 0; i < mesaProdutos[mesaIndex].size(); i++) {
                retorno.add(mesaProdutos[mesaIndex].elementAt(i));
            }
        }
        return retorno;
    }

    public static List<PagamentoModel> listaPagamentosMesa(String nMesa) {
        int mesaIndex = Integer.parseInt(nMesa);
        List<PagamentoModel> retorno = new ArrayList<>();

        if (mesaPagamentos[mesaIndex] != null) {
            for (int i = 0; i < mesaPagamentos[mesaIndex].size(); i++) {
                retorno.add(mesaPagamentos[mesaIndex].elementAt(i));
            }
        }
        return retorno;
    }

    public static void limpaMesa(String nMesa) {
        int mesaIndex = Integer.parseInt(nMesa);
        if (mesaProdutos[mesaIndex] != null) {
            mesaProdutos[mesaIndex].removeAllElements();
        }
        if (mesaPagamentos[mesaIndex] != null) {
            mesaPagamentos[mesaIndex].removeAllElements();
        }
        // Keep for backward compatibility
        if (mesa[mesaIndex] != null) {
            mesa[mesaIndex].removeAllElements();
        }
    }

    public static void limpaMesaPagamentos(String nMesa, List<PagamentoModel> pags) {
        int mesaIndex = Integer.parseInt(nMesa);
        if (mesaPagamentos[mesaIndex] != null) {
            mesaPagamentos[mesaIndex].removeAll(pags);
        }
    }

    // For methods that need to combine both (like Mesa constructor), create helper method
    private static Vector<Object> getCombinedMesaData(String nMesa) {
        int mesaIndex = Integer.parseInt(nMesa);
        Vector<Object> combined = new Vector<>();

        if (mesaProdutos[mesaIndex] != null) {
            combined.addAll(mesaProdutos[mesaIndex]);
        }
        if (mesaPagamentos[mesaIndex] != null) {
            combined.addAll(mesaPagamentos[mesaIndex]);
        }

        return combined;
    }

    @SuppressWarnings("unchecked")
    public static boolean enviarPedido(String nMesa, String garcom, String controleDuplicidade, String tipoVenda, String localEntrega, String nomeCliente) throws Exception {
        String Tpvend;
        boolean retorno;
        int numMesa = Integer.parseInt(nMesa);

        if (mesaProdutos[numMesa] == null || mesaProdutos[numMesa].isEmpty()) {
            throw new IllegalArgumentException("Pedido Sem Itens!");
        }

        switch (tipoVenda) {
            case "MESA":
                Tpvend = "0";
                break;
            case "CARTAO":
                Tpvend = "4";
                break;
            case "BALCAO":
                Tpvend = "1";
                break;
            default:
                Tpvend = "0";
                break;
        }

        // Use combined data for Mesa constructor
        Vector<Object> combinedData = getCombinedMesaData(nMesa);
        Mesa m = new Mesa(nMesa, garcom, controleDuplicidade, Tpvend, localEntrega, nomeCliente, combinedData);

        switch (Proxy.abrirMesa(m.toJSON()).get()) {
            case 1:
                retorno = true;
                break;
            case 3:
                throw new IllegalArgumentException("Esta mesa encontra-se fechada e não pode receber novos itens!");
            case -1:
                throw new IOException("Falha ao enviar pedido!");
            case -2:
                throw new IllegalArgumentException("Não será possível efetuar lançamentos até que o movimento do dia esteja aberto!");
            case -3:
                throw new IllegalArgumentException("Erro ao Enviar Pedido!");
            default:
                throw new IOException("Erro desconhecido! Reinicie o aplicativo do servidor e tente novamente!");
        }
        return retorno;
    }

    public static CompletableFuture<Boolean> statusMesa(String nmesa, String TP_VEND) {
        return Proxy.statusMesa_posicao(nmesa, TP_VEND).thenApply(status -> {
            switch (status) {
                case -1:
                    throw new IllegalArgumentException("Erro ao tentar verificar a mesa!");
                case -2:
                    throw new IllegalArgumentException("Não será possível efetuar lançamentos até que o movimento do dia esteja aberto!");
                case 3:
                    throw new IllegalArgumentException("A Posicao " + id + " já encontra-se aguardando pagamento!!");
                case 1:
                case 0:
                    // Initialize separate arrays
                    int mesaId = Integer.parseInt(id);
                    if (mesaProdutos[mesaId] == null) {
                        mesaProdutos[mesaId] = new Vector<>(1, 1);
                    }
                    if (mesaPagamentos[mesaId] == null) {
                        mesaPagamentos[mesaId] = new Vector<>(1, 1);
                    }
                    mesa[mesaId] = new Vector<>(1, 1); // Keep for compatibility
                    return true;
                default:
                    throw new RuntimeException("Erro desconhecido! Reinicie o aplicativo do servidor e tente novamente!");
            }
        }).exceptionally(e -> {
            System.err.println("Erro ao verificar status da mesa: " + e.getMessage());
            return false;
        });
    }

    public static CompletableFuture<Integer> statusAtendimento(String nmesa, String TP_VEND) {
        String numMesa;
        if (!nmesa.isEmpty()) {
            numMesa = String.valueOf(Integer.parseInt(nmesa));
        } else {
            numMesa = "1000";
        }

        return Proxy.statusMesa_posicao(nmesa, TP_VEND).thenApply(status -> {
            switch (status) {
                case -1:
                    throw new IllegalArgumentException("Erro ao tentar verificar a mesa!");
                case -2:
                    throw new IllegalArgumentException("Movimento do dia não está aberto!");
                case 3:
                    return status;
                case 1:
                case 0:
                    int mesaId = Integer.parseInt(numMesa);
                    if (mesaProdutos[mesaId] == null) {
                        mesaProdutos[mesaId] = new Vector<>(1, 1);
                    }
                    if (mesaPagamentos[mesaId] == null) {
                        mesaPagamentos[mesaId] = new Vector<>(1, 1);
                    }
                    mesa[mesaId] = new Vector<>(1, 1);
                    return status;
                default:
                    throw new RuntimeException("Erro desconhecido! Reinicie o servidor e tente novamente!");
            }
        }).exceptionally(e -> {
            System.err.println("Erro ao verificar status da mesa: " + e.getMessage());
            return -999;
        });
    }

    public static CompletableFuture<Integer> idAtendimento(String nmesa, String TP_VEND) {
        try {
            return Proxy.retornaIdatendimento(nmesa, TP_VEND).thenApply(id -> id).exceptionally(e -> {
                Log.d("idAtendimento", "Erro ao verificar status da mesa: " + e.getMessage());
                return -1;
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static CompletableFuture<Integer> statusMesas(String nmesa, String TP_VEND) {
        return Proxy.statusMesa_posicao(nmesa, TP_VEND).thenApply(status -> {
            switch (status) {
                case -1:
                    throw new IllegalArgumentException("Erro ao tentar verificar a mesa!");
                case -2:
                    throw new IllegalArgumentException("Não será possível efetuar lançamentos até que o movimento do dia esteja aberto!");
                case 3:
                case 70:
                    return 3;
                case 1:
                    return 1;
                case 0:
                    throw new IllegalArgumentException("Esta mesa não encontrada ou está sem itens!");
                default:
                    throw new RuntimeException("Erro desconhecido! Reinicie o aplicativo do servidor e tente novamente!");
            }
        }).exceptionally(e -> {
            System.err.println("Erro ao verificar status da mesa: " + e.getMessage());
            return -1;
        });
    }

    public static boolean isMesaEmpty(String nMesa) {
        int mesaIndex = Integer.parseInt(nMesa);

        boolean produtosEmpty = (mesaProdutos[mesaIndex] == null || mesaProdutos[mesaIndex].size() == 0);
        boolean pagamentosEmpty = (mesaPagamentos[mesaIndex] == null || mesaPagamentos[mesaIndex].size() == 0);

        return produtosEmpty && pagamentosEmpty;
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
        return new Funcoes().preparaImpressaoConta(m);
    }

    public static String mesaPagaPraImpressao(ContaFields m, List<PagamentoModel> pagamentos) {
        return new Funcoes().preparaImpressaoCompPagamento(m, pagamentos);
    }

    public static boolean permissao(String nmPermi) {
        boolean retorno = false;
        if (Variaveis.getConfiguracao(nmPermi, "N") != null) {
            if (Variaveis.getConfiguracao(nmPermi, "N").getValor().equalsIgnoreCase("s")
                    || Variaveis.getConfiguracao(nmPermi, "0").getValor().equalsIgnoreCase("1")) {
                retorno = true;
            }
        }
        return retorno;
    }

    public static String fecharMesa(String nMesa, String garcon, String imp, String numeroClientes, String id) throws Exception {
        nMesa = String.valueOf(Integer.parseInt(nMesa));
        String retorno = "Não foi possivel conectar ao servidor!";
        switch (Proxy.fecharMesa(id, garcon, Variaveis.getNumTerminal(), imp, numeroClientes).get()) {
            case -1:
                retorno = "Falha ao enviar pedido!";
                break;
            case 0:
                retorno = "Atendimento " + nMesa + " não existe!";
                break;
            case 1:
                retorno = "Atendimento " + nMesa + " fechada com sucesso!";
                break;
            case -2:
                retorno = "Não será possivel fazer lançamentos até que o movimento do dia seja aberto!";
                break;
        }
        return retorno;
    }

    @SuppressWarnings("unchecked")
    public static String efetuarPagamento(String nMesa, String garcon) throws Exception {
        int numMesa = Integer.parseInt(nMesa);
        if (mesaPagamentos[numMesa] == null) {
            throw new NullPointerException("Esta mesa não existe!");
        }

        String retorno = "Não foi possivel conectar ao servidor!";
        Vector<Object> combinedData = getCombinedMesaData(nMesa);
        Mesa m = new Mesa(nMesa, garcon, combinedData);

        switch (Proxy.efetuaPagamento(m.pagamentotoJSON())) {
            case -1:
                retorno = "Falha ao enviar pagamento!";
                break;
            case 0:
                retorno = "A mesa " + nMesa + " não existe!";
                break;
            case 1:
                retorno = "Pagamento enviado com sucesso!";
                break;
            case 2:
                retorno = "Venda ja contem pagamentos!";
                break;
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

    public static boolean reAbrirMesa(String id) throws Exception {
        boolean retorno;
        retorno = Proxy.reAbrirMesa(id).join();
        return retorno;
    }

    public static void reAbrirMesaAsync(String id, Consumer<Boolean> onSuccess, Consumer<Throwable> onError) {
        Proxy.reAbrirMesa(id)
                .thenAccept(onSuccess)
                .exceptionally(err -> {
                    onError.accept(err);
                    return null;
                });
    }

    public static boolean maxMesa(String nMesa) {
        boolean retorno = false;
        int max = Integer.parseInt(Variaveis.getConfiguracao("max_mesas", "2500").getValor()), min = 0, num = Integer.parseInt(nMesa);
        if (num > min && num <= max) {
            retorno = true;
        }
        return retorno;
    }

    public static void removeProd(String nMmesa, int index) throws Exception {
        int mesaIndex = Integer.parseInt(nMmesa);

        if (mesaProdutos[mesaIndex] == null || index >= mesaProdutos[mesaIndex].size() || index < 0) {
            throw new Exception("Este Item não existe");
        }

        mesaProdutos[mesaIndex].removeElementAt(index);
    }

    public static void removePag(String nMmesa, int index) throws Exception {
        int mesaIndex = Integer.parseInt(nMmesa);

        if (mesaPagamentos[mesaIndex] == null || index >= mesaPagamentos[mesaIndex].size() || index < 0) {
            throw new Exception("Este Item não existe");
        }

        mesaPagamentos[mesaIndex].removeElementAt(index);
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