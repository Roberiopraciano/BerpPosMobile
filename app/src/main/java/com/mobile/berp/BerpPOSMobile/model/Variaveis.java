package com.mobile.berp.BerpPOSMobile.model;


import java.util.ArrayList;
import java.util.Vector;
import java.util.Date;
import java.util.Vector;
import java.util.Calendar;


public class Variaveis {

    private static Vector<Produto> produtos;
    private static Vector<Produto>		produtosCargaPesquisa;
    private static Vector<Produto>		produtosCombinados;
    private static Vector<Funcionario>	funcionarios;
    private static Vector<Observacao>	observacoes;
    private static Vector<Impressora>	impressoras;
    private static Vector<Configuracao>	configuracoes;
    private static String				numTerminal;
    private static String				ipServidor;
    private static String				portaServidor;
    private static String				dataCarga;
    private static boolean              imprimirConta;
    private static Terminal				terminal;
    private static Loja                 loja;
    private static String               cpfcliente;
    private static String               nomecliente;
    private static String               numerologicoPOS;
    private static String               device_id;
    private static String               Terminal_id;

    private static String token;
    private static int userId;
    private static String userName;

    public static void setToken(String t) {
        token = t;
    }

    public static String getToken() {
        return token;
    }

    public static void setFuncionario(int id, String name) {
        userId = id;
        userName = name;
    }

    public static int getUserId() {
        return userId;
    }

    public static String getUserName() {
        return userName;
    }

    public static String getUser_name() {
        return User_name;
    }

    public static void setUser_name(String user_name) {
        User_name = user_name;
    }

    private static String               User_name;

    public static String getTerminal_name() {
        return Terminal_name;
    }

    public static void setTerminal_name(String terminal_name) {
        Terminal_name = terminal_name;
    }

    public static String getTerminal_id() {
        return Terminal_id;
    }

    public static void setTerminal_id(String terminal_id) {
        Terminal_id = terminal_id;
    }

    private static String               Terminal_name;


    private static String numeroDispositivo;

    public static String getNumeroDispositivo() {
        return numeroDispositivo;
    }

    public static void setNumeroDispositivo(String numeroDispositivo) {
        Variaveis.numeroDispositivo = numeroDispositivo;
    }

    public static String getcpfcliente(){
        return cpfcliente;
    }

    public static String getNomecliente(){
        return nomecliente;
    }

    public static void setCpfcliente(String cpfcliente){
        Variaveis.cpfcliente = cpfcliente;
    }

    public static void setNomecliente(String nomecliente){
        Variaveis.nomecliente = nomecliente;
    }

    public static String getNumTerminal() {

        return numTerminal;
    }

    public static void setNumTerminal(String numTerminal) {

        Variaveis.numTerminal = numTerminal;
    }

    public static boolean getImprimirConta(){
        return imprimirConta;
    }

    public static void setImprimirConta(boolean imprimirConta){
        Variaveis.imprimirConta = imprimirConta;
    }

    public static void zerarVariaveis() {

        produtos = new Vector<>(1, 1);
        produtosCombinados = new Vector<>(1, 1);
        funcionarios = new Vector<>(1, 1);
        observacoes = new Vector<>(1, 1);
        impressoras = new Vector<>(1, 1);
        configuracoes = new Vector<>(1, 1);
    }


    public static Configuracao getConfiguracao(String nome) {
        if (configuracoes == null) {
            configuracoes = new Vector<>();
        }
        for (int i = 0; i < configuracoes.size(); i++) {
            Configuracao configuracao = configuracoes.elementAt(i);
            if (configuracao.getNome().equalsIgnoreCase(nome)) {
                return configuracao;
            }
        }
        // Retorna uma configuração padrão se não encontrar
        return new Configuracao(nome, "N");
    }

//    public static Configuracao getConfiguracao(String nome) {
//        for (int i = 0; i < configuracoes.size(); i++) {
//            Configuracao configuracao = configuracoes.elementAt(i);
//            if (configuracao.getNome().equalsIgnoreCase(nome)) {
//                return configuracao;
//            }
//        }
//        // Retorna uma configuração padrão se não encontrar
//        return new Configuracao(nome, "N"); // ou qualquer valor padrão que faça sentido
//    }

    public static void addConfiguracao(Configuracao configuracao) {

        configuracoes.addElement(configuracao);
    }

    public static Vector<Impressora> getImpressoras() {

        return impressoras;
    }

    public static Impressora getImpressora(String nome) {

        Impressora impressora;
        for (int i = 0; i < impressoras.size(); i++) {
            impressora = impressoras.elementAt(i);

            if (impressora.getNome().equals(nome)) {
                return impressora;
            }
        }
        return null;
    }

    public static void addImpressora(Impressora impressora) {

        impressoras.addElement(impressora);
    }

    public static Vector<Observacao> getObservacoes() {

        return observacoes;
    }

    public static void setObservacoes(Vector<Observacao> observacoes) {

        Variaveis.observacoes = observacoes;
    }

    public static Observacao getObservacao(int cod) {

        Observacao observacao;
        for (int i = 0; i < observacoes.size(); i++) {
            observacao = observacoes.elementAt(i);

            if (observacao.getCod() == cod) {
                return observacao;
            }
        }
        return null;
    }

    public static void addObservacao(Observacao obersavacao) {

        observacoes.addElement(obersavacao);
    }

    public static Funcionario getFuncionario(int cod) {

        Funcionario funcionario;
        for (int i = 0; i < funcionarios.size(); i++) {
            funcionario = funcionarios.elementAt(i);

            if (funcionario.getCodigo() == cod) {
                return funcionario;
            }
        }
        return null;
    }

    public static void addFuncionario(Funcionario f) {

        funcionarios.addElement(f);
    }

    public static Vector<Produto> getProdutos() {

        return produtos;
    }


    public static Produto getProduto(int cod) {
        // Obtendo a data e hora atual
        Calendar calendar = Calendar.getInstance();

        // Obtém o dia da semana (1 = Domingo, 2 = Segunda, ..., 7 = Sábado)
        int diaSemana = calendar.get(Calendar.DAY_OF_WEEK);

        // Ajusta o valor do dia para corresponder aos atributos da classe Produto
        int diaAtual = 0;
        switch (diaSemana) {
            case Calendar.SUNDAY: diaAtual = 1; break;
            case Calendar.MONDAY: diaAtual = 2; break;
            case Calendar.TUESDAY: diaAtual = 3; break;
            case Calendar.WEDNESDAY: diaAtual = 4; break;
            case Calendar.THURSDAY: diaAtual = 5; break;
            case Calendar.FRIDAY: diaAtual = 6; break;
            case Calendar.SATURDAY: diaAtual = 7; break;
        }

        // Obtém a hora atual (somente a hora, sem minutos)
        int horaAtual = calendar.get(Calendar.HOUR_OF_DAY);

        // Obtém a data atual sem hora/minuto/segundo
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date dataAtual = calendar.getTime();

        // Percorre a lista de produtos
        for (Produto produto : produtos) {
            if (produto.getCod() == cod) {
                // Verifica se o produto tem controle de data ativado
                if (produto.getControle_data() == 1) {
                    // Verifica se o produto está disponível no dia da semana atual
                    if ((diaAtual == 2 && produto.getSegunda() == 1) ||
                            (diaAtual == 3 && produto.getTerca() == 1) ||
                            (diaAtual == 4 && produto.getQuarta() == 1) ||
                            (diaAtual == 5 && produto.getQuinta() == 1) ||
                            (diaAtual == 6 && produto.getSexta() == 1) ||
                            (diaAtual == 7 && produto.getSabado() == 1) ||
                            (diaAtual == 1 && produto.getDomingo() == 1)) {

                        // Obtém os valores de tempo e data do produto
                        Date timeInicio = produto.getTimeInicio();
                        Date timeFim = produto.getTimeFim();
                        Date dataInicio = produto.getDataInicio();
                        Date dataValidade = produto.getDataValidade();

                        // Verifica se os valores não são nulos antes de comparar
                        if (timeInicio != null && timeFim != null && dataInicio != null && dataValidade != null) {
                            // Obtém a data e hora atual
                            Calendar calAtual = Calendar.getInstance();

                            // Obtém a data e hora de início do produto
                            Calendar calInicio = Calendar.getInstance();
                            calInicio.setTime(timeInicio);

                            // Obtém a data e hora de fim do produto
                            Calendar calFim = Calendar.getInstance();
                            calFim.setTime(timeFim);

                            // Converte tudo para milissegundos para comparação precisa
                            long millisAtual = calAtual.getTimeInMillis();
                            long millisInicio = calInicio.getTimeInMillis();
                            long millisFim = calFim.getTimeInMillis();

                            // Verifica se a hora atual está dentro do intervalo permitido
                            if (millisAtual >= millisInicio && millisAtual <= millisFim) {
                                // Verifica se a data atual está dentro do período válido do produto
                                if (!dataAtual.before(dataInicio) && !dataAtual.after(dataValidade)) {
                                    return new Produto(produto);
                                }
                            }
                        }
                    }
                } else {
                    return new Produto(produto);
                }
            }
        }
        return null;
    }

    public static void addProduto(Produto p){

        produtos.addElement(p);
    }

    public static Vector<Produto> getProdutosComb() {

        return produtosCombinados;
    }

    public static Produto getProdutoComb(int cod) {

        if (produtosCombinados != null) {

            Produto produto;
            for (int i = 0; i < produtosCombinados.size(); i++) {
                produto =  produtosCombinados.elementAt(i);

                if (produto.getCod() == cod) {
                    return new Produto(produto);
                }
            }
        }
        return null;
    }

    public static void addProdutoComb(Produto p) {

        produtosCombinados.addElement(p);
    }

    public static Produto getProdutoDescricao(String descricao) {
        // TODO Auto-generated method stub
        Produto produto;
        for (int i = 0; i < produtos.size(); i++) {
            produto =  produtos.elementAt(i);

            if (produto.getDesc().equalsIgnoreCase(descricao)) {
                return new Produto(produto);
            }
        }
        return null;
    }

    public static String getIpServidor() {
        return ipServidor;
    }

    public static void setIpServidor(String ipServidor) {
        Variaveis.ipServidor = ipServidor;
    }

    public static String getPortaServidor() {
        return portaServidor;
    }

    public static void setPortaServidor(String portaServidor) {
        Variaveis.portaServidor = portaServidor;
    }

    public static String getDevice_id() {
		return device_id;
	}

    public static void setDevice_id(String device_id) {
		Variaveis.device_id = device_id;
	}


    public static String getDataCarga() {
        return dataCarga;
    }

    public static void setDataCarga(String dataCarga) {
        Variaveis.dataCarga = dataCarga;
    }



    public static ArrayList<String> preencherAutoComplete(String codigo, int tipo) {
        ArrayList<String> autoComplete = new ArrayList<>();

        // Obtendo a data e hora atual
        Calendar calendar = Calendar.getInstance();

        // Obtém o dia da semana (1 = Domingo, 2 = Segunda, ..., 7 = Sábado)
        int diaSemana = calendar.get(Calendar.DAY_OF_WEEK);

        // Ajusta o valor do dia para corresponder aos atributos da classe Produto
        int diaAtual = 0;
        switch (diaSemana) {
            case Calendar.SUNDAY: diaAtual = 1; break;
            case Calendar.MONDAY: diaAtual = 2; break;
            case Calendar.TUESDAY: diaAtual = 3; break;
            case Calendar.WEDNESDAY: diaAtual = 4; break;
            case Calendar.THURSDAY: diaAtual = 5; break;
            case Calendar.FRIDAY: diaAtual = 6; break;
            case Calendar.SATURDAY: diaAtual = 7; break;
        }

        // Obtém a hora atual (somente a hora, sem minutos)
        int horaAtual = calendar.get(Calendar.HOUR_OF_DAY);

        // Obtém a data atual sem hora/minuto/segundo para evitar comparações imprecisas
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date dataAtual = calendar.getTime();

        // Converte o código de busca para maiúsculas para evitar problemas de case sensitivity
        String codigoUpper = codigo.toUpperCase();

        // Percorre a lista de produtos
        for (Produto produto : produtos) {
            if (produto.getDesc().toUpperCase().contains(codigoUpper)) {
                if (produto.getControle_data() == 1) {
                    // Verifica se o produto pode ser exibido no dia atual
                    if ((diaAtual == 2 && produto.getSegunda() == 1) ||
                            (diaAtual == 3 && produto.getTerca() == 1) ||
                            (diaAtual == 4 && produto.getQuarta() == 1) ||
                            (diaAtual == 5 && produto.getQuinta() == 1) ||
                            (diaAtual == 6 && produto.getSexta() == 1) ||
                            (diaAtual == 7 && produto.getSabado() == 1) ||
                            (diaAtual == 1 && produto.getDomingo() == 1)) {

                        // Obtém os valores de tempo e data do produto
                        Date timeInicio = produto.getTimeInicio();
                        Date timeFim = produto.getTimeFim();
                        Date dataInicio = produto.getDataInicio();
                        Date dataValidade = produto.getDataValidade();

                        // Verifica se os valores não são nulos antes de comparar
                        if (timeInicio != null && timeFim != null && dataInicio != null && dataValidade != null) {
                            // Obtém a data e hora atual
                            Calendar calAtual = Calendar.getInstance();

                            // Obtém a data e hora de início do produto
                            Calendar calInicio = Calendar.getInstance();
                            calInicio.setTime(timeInicio);

                            // Obtém a data e hora de fim do produto
                            Calendar calFim = Calendar.getInstance();
                            calFim.setTime(timeFim);

                            // Converte tudo para milissegundos para comparação precisa
                            long millisAtual = calAtual.getTimeInMillis();
                            long millisInicio = calInicio.getTimeInMillis();
                            long millisFim = calFim.getTimeInMillis();

                            // Verifica se a hora atual está dentro do intervalo permitido
                            if (millisAtual >= millisInicio && millisAtual <= millisFim) {
                                // Verifica se a data atual está dentro do período válido do produto
                                if (!dataAtual.before(dataInicio) && !dataAtual.after(dataValidade)) {
                                    if (tipo == 1 || (tipo == 0 && getProdutoComb(produto.getCod()) != null)) {
                                        autoComplete.add(produto.getDesc());
                                    }
                                }
                            }
                        }
                    }
                } else {
                    if (tipo == 1 || (tipo == 0 && getProdutoComb(produto.getCod()) != null)) {
                        autoComplete.add(produto.getDesc());
                    }
                }
            }
        }

        return autoComplete;
    }

    public static Terminal getTerminal() {
        return terminal;
    }

    public static void setTerminal(Terminal terminal) {
        Variaveis.terminal = terminal;
    }

    public static Loja getLoja(){
        return loja;
    }

    public static void setLoja(Loja loja) {
        Variaveis.loja = loja;
    }

    public static String getNumerologicoPOS() {
        return numerologicoPOS;
    }

    public static void setNumerologicoPOS(String numerologicoPOS) {
        Variaveis.numerologicoPOS = numerologicoPOS;
    }
}
