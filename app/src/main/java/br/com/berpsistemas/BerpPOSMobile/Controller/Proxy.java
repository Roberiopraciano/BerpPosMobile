package br.com.berpsistemas.BerpPOSMobile.Controller;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import br.com.berpsistemas.BerpPOSMobile.model.BerpModel;
import br.com.berpsistemas.BerpPOSMobile.model.ContaFields;
import br.com.berpsistemas.BerpPOSMobile.model.Mesa;
import br.com.berpsistemas.BerpPOSMobile.model.PagamentoModel;
import br.com.berpsistemas.BerpPOSMobile.model.ResultModel;
import br.com.berpsistemas.BerpPOSMobile.model.StatusMesaResult;
import br.com.berpsistemas.BerpPOSMobile.model.Terminal;
import br.com.berpsistemas.BerpPOSMobile.model.TerminalResponse;
import br.com.berpsistemas.BerpPOSMobile.model.Variaveis;
import br.com.berpsistemas.BerpPOSMobile.model.VersaoResponseModel;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CompletableFuture;


public class Proxy {

    private static final String	CONFIGS		= "'MOBILE_PERMITE_CANCELAMENTO','MOBILE_PERMITE_REIMPRESSAO','MAX_MESAS','CELULAR_REABRIR_CONTA','PROD_COMBINADO','CONTROLAR_QTD_MAXIMA_POR_ITEN_VENDA','CELULAR_CARGA_ONLINE','DATA_ULTIMA_CARGA','MAX_PAGAMENTOS_POS','COMPROVANTE_NAOFISCAL_POS','MOBILE_VENDA_MESA','MOBILE_VENDA_CARTAO','MOBILE_VENDA_BALCAO'";
    private static final String	BASE_URL	= "/datasnap/rest/TServerMethods1/";
   // private static final String	BASE_URL	= "/";


    public static CompletableFuture<Integer> statusMesa(String id) {
        // Criar um CompletableFuture para encapsular a lógica assíncrona
        CompletableFuture<Integer> future = new CompletableFuture<>();

        String url = new Funcoes().getUrl(Proxy.BASE_URL + "RetornaStatusMesa2/" + id);
        System.out.println("URL: " + url);

        RestConnection.get(url, ResultModel.class, new RestConnection.ResponseCallback<ResultModel>() {
            @Override
            public void onSuccess(ResultModel response) {
                try {
                    System.out.println("Resposta recebida: " + response.toString());

                    // Verificar e converter a resposta
                    if (response != null && !response.toString().isEmpty()) {
                        int status = response.getResult().get(0);
                        future.complete(status); // Completa o CompletableFuture com o valor
                    } else {
                        future.completeExceptionally(new IllegalArgumentException("Resposta vazia ou inválida"));
                    }
                } catch (NumberFormatException e) {
                    future.completeExceptionally(new IllegalArgumentException("Erro ao converter resposta para número: " + e.getMessage()));
                }
            }

            @Override
            public void onError(Exception e) {
                System.err.println("Erro na conexão: " + e.getMessage());
                future.completeExceptionally(e); // Completa o CompletableFuture com a exceção
            }
        });

        return future;
    }



    public static CompletableFuture<Integer> statusMesa_posicao(String nmesa, String tpvend) {
        CompletableFuture<Integer> future = new CompletableFuture<>();

        // se for balcão, devolve 0 direto
        if ("1".equals(tpvend)) {
            future.complete(0);
        } else {
            String url = new Funcoes().getUrl(Proxy.BASE_URL + "RetornaStatusMesa3/" + nmesa + "/" + tpvend);
            System.out.println("URL: " + url);

            RestConnection.get(url, StatusMesaResult.class, new RestConnection.ResponseCallback<StatusMesaResult>() {
                @Override
                public void onSuccess(StatusMesaResult response) {
                    try {
                        System.out.println("Resposta recebida raw: " + response);

                        // 1) Pega o array "result"
                        List<String> raw = response.getResult();
                        if (raw == null || raw.isEmpty()) {
                            throw new IllegalStateException("Result vazio do servidor");
                        }

                        // 2) A string dentro de result[0] está escapada: "{\"status\":3,\"idVenda\":213834}"
                        String innerJson = raw.get(0);

                        // 3) Faz o parse de verdade
                        JSONObject jo = new JSONObject(innerJson);
                        int status  = jo.getInt("status");
                        int idVenda = jo.optInt("idVenda", 0);

                        // 4) Armazena idVenda se veio
                        if (idVenda > 0 && status > 0) {
                            BerpModel.setId(String.valueOf(idVenda));
                            System.out.println("ID da venda gravado: " + idVenda);
                        }

                        // 5) Completa o futuro com o status
                        future.complete(status);

                    } catch (Exception e) {
                        future.completeExceptionally(
                                new IllegalArgumentException("Erro ao processar JSON: " + e.getMessage(), e)
                        );
                    }
                }

                @Override
                public void onError(Exception e) {
                    System.err.println("Erro na conexão: " + e.getMessage());
                    future.completeExceptionally(e);
                }
            });
        }

        return future;
    }



    public static CompletableFuture<Integer> statusAtendimento(String nMesa,String TP_vend) {
        // Criar um CompletableFuture para encapsular a lógica assíncrona
        CompletableFuture<Integer> future = new CompletableFuture<>();

        String url = new Funcoes().getUrl(Proxy.BASE_URL + "RetornaStatusMesa2/" +TP_vend+"/" + nMesa);
        System.out.println("URL: " + url);

        RestConnection.get(url, ResultModel.class, new RestConnection.ResponseCallback<ResultModel>() {
            @Override
            public void onSuccess(ResultModel response) {
                try {
                    System.out.println("Resposta recebida: " + response.toString());

                    // Verificar e converter a resposta
                    if (response != null && !response.toString().isEmpty()) {
                        int status = response.getResult().get(0);
                        future.complete(status); // Completa o CompletableFuture com o valor
                    } else {
                        future.completeExceptionally(new IllegalArgumentException("Resposta vazia ou inválida"));
                    }
                } catch (NumberFormatException e) {
                    future.completeExceptionally(new IllegalArgumentException("Erro ao converter resposta para número: " + e.getMessage()));
                }
            }

            @Override
            public void onError(Exception e) {
                System.err.println("Erro na conexão: " + e.getMessage());
                future.completeExceptionally(e); // Completa o CompletableFuture com a exceção
            }
        });

        return future;
    }


    public static int statusPagamento(String id) throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        final int[] result = new int[1];

        RestConnection.get("RetornaSeTemPagamentos/" + id, String.class, new RestConnection.ResponseCallback<String>() {
            @Override
            public void onSuccess(String response) {
                try {
                    // Processa a resposta convertendo-a para inteiro
                    result[0] = Integer.parseInt(response);
                } catch (NumberFormatException e) {
                    result[0] = -1; // Valor de erro
                } finally {
                    latch.countDown(); // Libera o latch
                }
            }

            @Override
            public void onError(Exception e) {
                result[0] = -1; // Valor de erro
                latch.countDown(); // Libera o latch mesmo em caso de erro
            }
        });
        latch.await(); // Espera até que o CountDownLatch seja liberado

        return result[0]; // Retorna o resultado obtido na resposta
    }

    public static CompletableFuture<Boolean> reAbrirMesa(String id)  {
        String url = new Funcoes().getUrl(Proxy.BASE_URL + "ReabrirMesa2/" + id);
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        RestConnection.get(url, String.class, new RestConnection.ResponseCallback<String>() {
            @Override
            public void onSuccess(String response) {

                    try {
                        boolean result = new JsonParser().resultBoolean(response);
                        future.complete(result);  // Completa o Future com sucesso
                    } catch (Exception e) {
                        future.completeExceptionally(e);  // Completa o Future com erro
                    }



            }

            @Override
            public void onError(Exception e) {
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    public static CompletableFuture<Boolean> efetuarLogin(String json) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        String url = new Funcoes().getUrl(Proxy.BASE_URL + "login"); // Endpoint de login no servidor

        RestConnection.post(url, json, String.class, new RestConnection.ResponseCallback<String>() {
            @Override
            public void onSuccess(String response) {
                try {
                    // Parse do JSON recebido
                    JSONObject mainResponse = new JSONObject(response);
                    JSONArray resultArray = mainResponse.getJSONArray("result");

                    if (resultArray.length() > 0) {
                        // Extrai o primeiro elemento do array e converte para JSON
                        String innerJson = resultArray.getString(0);
                        JSONObject jsonResponse = new JSONObject(innerJson);

                        boolean success = jsonResponse.getBoolean("success");
                        if (success) {
                            // Recupera e armazena o token e outras informações do usuário
                            String token = jsonResponse.getString("token");
                            int userId = jsonResponse.getInt("user_id");
                            String userName = jsonResponse.getString("user_name");

                            // Salva os dados localmente
                            Variaveis.setToken(token);
                            Variaveis.setFuncionario(userId, userName);

                            future.complete(true); // Login bem-sucedido
                        } else {
                            future.complete(false); // Credenciais inválidas
                        }
                    } else {
                        // Array de resultados vazio
                        future.complete(false);
                    }
                } catch (Exception e) {
                    Log.d("efetuarLogin", "Erro ao processar a resposta JSON: " + e.getMessage());
                    future.completeExceptionally(e);
                }
            }

            @Override
            public void onError(Exception e) {
                future.completeExceptionally(e); // Completa o Future com erro
            }
        });

        return future;
    }


    public static CompletableFuture<Boolean> finishOrder(int id) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        // Monta a URL com o ID na rota
        String url = new Funcoes().getUrl(Proxy.BASE_URL + "finishorder/" + id);

        // Faz o POST (não enviamos corpo, só o path param)
        RestConnection.post(url, "", String.class, new RestConnection.ResponseCallback<String>() {
            @Override
            public void onSuccess(String response) {
                // Se chegar aqui, devolvemos true
                future.complete(true);
            }

            @Override
            public void onError(Exception e) {
                // Em erro de rede/servidor, completamos com exceção
                future.completeExceptionally(e);
            }
        });

        return future;
    }


    public static  CompletableFuture<Integer> abrirMesa(String json) {
        CompletableFuture<Integer> future = new CompletableFuture<>();
        String url = new Funcoes().getUrl(Proxy.BASE_URL + "Pedidov2/");
        RestConnection.post(url, json,String.class, new RestConnection.ResponseCallback<String>() {
            @Override
            public void onSuccess(String response) {
               try{
                   Integer result = new JsonParser().resultInt(response);
                   future.complete(result);  //


            } catch (Exception e) {
                future.completeExceptionally(e);  // Completa o Future com erro
            }
            }

            @Override
            public void onError(Exception e) {
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    public static CompletableFuture<Integer> fecharMesa(String nMesa, String cdGarcon, String nTerminal) throws Exception {
        CompletableFuture<Integer> future = new CompletableFuture<>();
        String url = new Funcoes().getUrl(Proxy.BASE_URL + "Fecharmesa/" + nMesa + "/" + cdGarcon
                + "/" + nTerminal);
        RestConnection.get(url, String.class, new RestConnection.ResponseCallback<String>() {
            @Override
            public void onSuccess(String response) {

                try {
                     Integer result = new JsonParser().resultInt(response);
                    future.complete(result);  // Completa o Future com sucesso
                } catch (Exception e) {
                    future.completeExceptionally(e);  // Completa o Future com erro
                }



            }

            @Override
            public void onError(Exception e) {
                future.completeExceptionally(e);
            }
        });
        return future;

    }

    public static CompletableFuture<Integer> fecharMesa(String id, String cdGarcon, String nTerminal, String impressora, String numeroClientes) throws Exception {
        CompletableFuture<Integer> future = new CompletableFuture<>();
        String url = new Funcoes().getUrl(Proxy.BASE_URL + "Fecharmesa2/" + id + "/" + cdGarcon
                + "/" + nTerminal + "/" + impressora + "/" + numeroClientes);
        RestConnection.get(url, String.class, new RestConnection.ResponseCallback<String>() {
            @Override
            public void onSuccess(String response) {

                try {
                    Integer result = new JsonParser().resultInt(response);
                    future.complete(result);  // Completa o Future com sucesso
                } catch (Exception e) {
                    future.completeExceptionally(e);  // Completa o Future com erro
                }



            }

            @Override
            public void onError(Exception e) {
                future.completeExceptionally(e);
            }
        });
        return future;


    }

    public static CompletableFuture<Vector<Mesa>> listarMesas() throws Exception {
        CompletableFuture<Vector<Mesa>> future = new CompletableFuture<>();
        String url = new Funcoes().getUrl(new Funcoes().getUrl(BASE_URL + "Visualizar_Mesas"));
        RestConnection.get(url, String.class, new RestConnection.ResponseCallback<String>() {
            @Override
            public void onSuccess(String response) {

                try {
                    Vector<Mesa> result = new JsonParser().mesas(response);
                    future.complete(result);  // Completa o Future com sucesso
                } catch (Exception e) {
                    future.completeExceptionally(e);  // Completa o Future com erro
                }



            }

            @Override
            public void onError(Exception e) {
                future.completeExceptionally(e);
            }
        });
        return future;



    }

    public static CompletableFuture<ContaFields> visualizaConta(String id, int tipo) throws Exception {
        CompletableFuture<ContaFields> future = new CompletableFuture<>();
        String url;

        if(tipo == 0){
            url = new Funcoes().getUrl(Proxy.BASE_URL + "Visualizar_Contadet4/" + id);
        }else{
            url= new Funcoes().getUrl(Proxy.BASE_URL + "Visualizar_Conta4/" + id);
        }



        RestConnection.get(url, String.class, new RestConnection.ResponseCallback<String>() {
            @Override
            public void onSuccess(String response) {

                try {
                    ContaFields result = new JsonParser().cargaMesaDetalhada(response);
                    future.complete(result);  // Completa o Future com sucesso
                } catch (Exception e) {
                    future.completeExceptionally(e);  // Completa o Future com erro
                }



            }

            @Override
            public void onError(Exception e) {
                future.completeExceptionally(e);
            }
        });
        return future;



    }

    public static void cargas() throws Exception {

        Variaveis.zerarVariaveis();
        cargaLoja().join();
        //Variaveis.setNumTerminal(new LeitorXml().getTerminal());
        cargaConfiguracoes().join();
        cargaFuncionarios().join();
        cargaImpressoras().join();
        cargaObservacoes().join();
        cargaProdutos().join();
        cargaProdutosComb().join();

    }


    // Método genérico para chamadas GET com CompletableFuture
    private static CompletableFuture<String> getRequest(String url) {
        CompletableFuture<String> future = new CompletableFuture<>();
        RestConnection.get(url, String.class, new RestConnection.ResponseCallback<String>() {
            @Override
            public void onSuccess(String response) {
                future.complete(response);  // Completa o Future com sucesso
            }

            @Override
            public void onError(Exception e) {
                future.completeExceptionally(e);  // Completa o Future com erro
            }
        });
        return future;
    }
    public static CompletableFuture<String> VersaoServer() {
        CompletableFuture<String> future = new CompletableFuture<>();

        String url = new Funcoes().getUrl(Proxy.BASE_URL + "VersaoServer");
        System.out.println("URL: " + url);

        RestConnection.get(url, VersaoResponseModel.class, new RestConnection.ResponseCallback<VersaoResponseModel>() {
            @Override
            public void onSuccess(VersaoResponseModel response) {
                try {
                    System.out.println("Resposta recebida: " + response);

                    if (response != null
                            && response.getResult() != null
                            && !response.getResult().isEmpty()
                            && response.getResult().get(0).getVersao() != null) {

                        String versao = response.getResult().get(0).getVersao();
                        future.complete(versao);

                    } else {
                        future.completeExceptionally(
                                new IllegalArgumentException("Resposta vazia, sem campo 'Versao' ou formato inesperado"));
                    }

                } catch (Exception e) {
                    future.completeExceptionally(
                            new IllegalArgumentException("Erro processando resposta: " + e.getMessage(), e));
                }
            }

            @Override
            public void onError(Exception e) {
                System.err.println("Erro na conexão: " + e.getMessage());
                future.completeExceptionally(e);
            }
        });

        return future;
    }


    // Carga Loja
    public static CompletableFuture<Void> cargaLoja() {
        String url = new Funcoes().getUrl(Proxy.BASE_URL + "Loja/");
        return getRequest(url).thenAccept(json -> {
            try {
                Log.d("DEBUG", "Carga Loja JSON Recebido: " + json);
                new JsonParser().cargaLoja(json);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    // Carga Configurações
    public static CompletableFuture<Void> cargaConfiguracoes() {
        String url = new Funcoes().getUrl(Proxy.BASE_URL + "Configuracoes/" + Proxy.CONFIGS);
        return getRequest(url).thenAccept(json -> {
            try {
                new JsonParser().cargaConfiguracoes(json);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    // Data Carga
    public static CompletableFuture<Integer> dataCarga(String numTerminal, String dataCarga) {
        String url = new Funcoes().getUrl(Proxy.BASE_URL + "datacarga/" + numTerminal + "/" + dataCarga);
        return getRequest(url).thenApply(json -> new JsonParser().resultInt(json));
    }

    public static CompletableFuture<Integer> retornaIdatendimento(String atendimento, String tpvend) {
        String url = new Funcoes().getUrl(Proxy.BASE_URL + "retornaIdAtendimento/" + atendimento + "/" + tpvend);
        return getRequest(url)
                .thenApply(json -> {
                    Log.d( "retornaIdatendimento: ","JSON recebido: " + json); // Depuração
                    return new JsonParser().resultInt(json); // Analisa e retorna o valor correto
                });
    }

    // Carga Produtos
    public static CompletableFuture<Void> cargaProdutos() {
        String url = new Funcoes().getUrl(Proxy.BASE_URL + "Produto/");
        return getRequest(url).thenAccept(json -> {
            try {
                new JsonParser().cargaProdutos(json);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    // Carga Produtos Combinados
    public static CompletableFuture<Void> cargaProdutosComb() {
        String url = new Funcoes().getUrl(Proxy.BASE_URL + "ProdutoCombinado/");
        return getRequest(url).thenAccept(json -> {
            try {
                new JsonParser().cargaProdutosComb(json);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    // Carga Funcionários
    public static CompletableFuture<Void> cargaFuncionarios() {
        String url = new Funcoes().getUrl(Proxy.BASE_URL + "Funcionarios/");
        return getRequest(url).thenAccept(json -> {
            try {
                new JsonParser().cargaFuncionarios(json);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    // Carga Observações
    public static CompletableFuture<Void> cargaObservacoes() {
        String url = new Funcoes().getUrl(Proxy.BASE_URL + "Observacoes/");
        return getRequest(url).thenAccept(json -> {
            try {
                new JsonParser().cargaObservacoes(json);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    // Carga Impressoras
    public static CompletableFuture<Void> cargaImpressoras() {
        String url = new Funcoes().getUrl(Proxy.BASE_URL + "Impressoras/");
        return getRequest(url).thenAccept(json -> {
            try {
                new JsonParser().cargaImpressoras(json);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }




    public static CompletableFuture<Boolean> enviarPagamento(PagamentoModel pagamento, String controle) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        String url = new Funcoes().getUrl(Proxy.BASE_URL + "pagamento");

        // Envelopando com controle para evitar duplicidade
        JSONObject jsonWrapper = new JSONObject();
        try {
            jsonWrapper.put("controle", controle);
            jsonWrapper.put("pagamento", new JSONObject(pagamento.toJson()));

        } catch (Exception e) {
            future.completeExceptionally(e);
            return future;
        }

        RestConnection.post(url, jsonWrapper.toString(), String.class, new RestConnection.ResponseCallback<String>() {
            @Override
            public void onSuccess(String response) {
                try {
                    boolean result = new JsonParser().resultBoolean(response);
                    future.complete(result);
                } catch (Exception e) {
                    future.completeExceptionally(e);
                }
            }

            @Override
            public void onError(Exception e) {
                future.completeExceptionally(e);
            }
        });

        return future;
    }

    public static CompletableFuture<Boolean> deletePagamento(String idPagamento, String controle) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        String url = new Funcoes().getUrl(Proxy.BASE_URL + "pagamento");

        JSONObject json = new JSONObject();
        try {
            json.put("id_pagamento", idPagamento);
            json.put("controle", controle);
        } catch (Exception e) {
            future.completeExceptionally(e);
            return future;
        }

        RestConnection.post(url, json.toString(), String.class, new RestConnection.ResponseCallback<String>() {
            @Override
            public void onSuccess(String response) {
                try {
                    boolean result = new JsonParser().resultBoolean(response);
                    future.complete(result);
                } catch (Exception e) {
                    future.completeExceptionally(e);
                }
            }

            @Override
            public void onError(Exception e) {
                future.completeExceptionally(e);
            }
        });

        return future;
    }

    public static CompletableFuture<List<PagamentoModel>> listarPagamentos(String idAtendimento) {
        String url = new Funcoes().getUrl(BASE_URL + "listarPagamentos/" + idAtendimento);
        CompletableFuture<List<PagamentoModel>> future = new CompletableFuture<>();

        RestConnection.get(url, String.class, new RestConnection.ResponseCallback<String>() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONObject root      = new JSONObject(response);
                    JSONArray resultArr  = root.optJSONArray("result");
                    List<PagamentoModel> pagamentos = new ArrayList<>();

                    // se veio algo em result, pega o primeiro objeto e dele o array "pagamentos"
                    if (resultArr != null && resultArr.length() > 0) {
                        JSONObject wrapper    = resultArr.getJSONObject(0);
                        JSONArray pagArr      = wrapper.optJSONArray("pagamentos");
                        if (pagArr != null) {
                            for (int i = 0; i < pagArr.length(); i++) {
                                JSONObject obj = pagArr.getJSONObject(i);
                                PagamentoModel pag = new Gson()
                                        .fromJson(obj.toString(), PagamentoModel.class);
                                pagamentos.add(pag);
                            }
                        }
                    }

                    future.complete(pagamentos);

                } catch (Exception e) {
                    future.completeExceptionally(e);
                }
            }

            @Override
            public void onError(Exception e) {
                future.completeExceptionally(e);
            }
        });

        return future;
    }



    // Checar Terminal
    public static CompletableFuture<Terminal> checaTerminal(Terminal terminal) {
        CompletableFuture<Terminal> future = new CompletableFuture<>();
        String url = new Funcoes().getUrl(Proxy.BASE_URL + "ConsultaTerminalV2/");

        // Criação de JSON sem escapamento
        Gson gson = new GsonBuilder()
                .disableHtmlEscaping()
                .create();

        String json = gson.toJson(terminal);

        // Verificação no console (opcional)
        System.out.println("JSON Enviado: " + json);
        Log.d("DEBUG" ,"JSON Enviado: " + json);


        // Enviando JSON puro no corpo da requisição
        RestConnection.post(url, json, String.class, new RestConnection.ResponseCallback<String>() {
            @Override
            public void onSuccess(String response) {
                try {
                    // Verificação do conteúdo da resposta
                    System.out.println("Resposta Recebida: " + response);
                    Log.d("DEBUG", "Conteúdo da resposta: " + response);
                    // Parse direto usando Gson
                    TerminalResponse terminalResponse = gson.fromJson(response, TerminalResponse.class);

                    if (terminalResponse != null && terminalResponse.getResult() != null && !terminalResponse.getResult().isEmpty()) {
                        Terminal result = terminalResponse.getResult().get(0); // Pega o primeiro terminal
                        future.complete(result); // Completa o Future com sucesso
                    } else {
                        throw new Exception("Lista de Terminais vazia ou nula.");
                    }

                    // Verifique se o resultado é nulo
                   // if (result == null) {
                    //    throw new Exception("O objeto Terminal é nulo após o parse.");
                    //}

                   // future.complete(result);  // Completa o Future com sucesso
                } catch (Exception e) {
                    future.completeExceptionally(e);  // Completa o Future com erro
                }
            }

            @Override
            public void onError(Exception e) {
                future.completeExceptionally(e);  // Completa o Future com erro
            }
        });

        return future;
    }// Obter Lista de Mesas
    public static CompletableFuture<List<Mesa>> getListaMesas(String garcon) {
        String url = new Funcoes().getUrl(Proxy.BASE_URL + "Visualizar_Mesas2/" + garcon);
        return getRequest(url).thenApply(json -> new JsonParser().getListaMesa(json));
    }









    public static int efetuaPagamento(String s) {
        return 0;
    }

    public static CompletableFuture<Boolean> invalidarSessao(String sessionToken) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        String url = new Funcoes().getUrl(Proxy.BASE_URL + "logout"); // Endpoint do servidor para logout

        // Criação do JSON de requisição
        JSONObject jsonRequest = new JSONObject();
        try {
            jsonRequest.put("session_token", sessionToken);
        } catch (Exception e) {
            future.completeExceptionally(e);
            return future;
        }

        // Envia a requisição ao servidor
        RestConnection.post(url, jsonRequest.toString(), String.class, new RestConnection.ResponseCallback<String>() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONObject mainResponse = new JSONObject(response);
                    JSONArray resultArray = mainResponse.getJSONArray("result");

                    if (resultArray.length() > 0) {
                        // Extrai o primeiro elemento do array e converte para JSON
                        String innerJson = resultArray.getString(0);
                        JSONObject jsonResponse = new JSONObject(innerJson);

                        boolean success = jsonResponse.getBoolean("success");
                        if (success) {
                            // Recupera e armazena o token e outras informações do usuário

                            // Salva os dados localmente
                            Variaveis.setToken("");

                            future.complete(true); // Login bem-sucedido
                        } else {
                            future.complete(false); // Credenciais inválidas
                        }
                    } else {
                        // Array de resultados vazio
                        future.complete(false);
                    }
                } catch (Exception e) {
                    future.completeExceptionally(e); // Completa com erro se ocorrer uma exceção
                }
            }

            @Override
            public void onError(Exception e) {
                future.completeExceptionally(e); // Completa com erro se a requisição falhar
            }
        });

        return future;
    }

    public static CompletableFuture<Boolean> validarSessao(String sessionToken) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        String url = new Funcoes().getUrl(Proxy.BASE_URL + "validateSession"); // Endpoint para validar a sessão

        // Criação do JSON de requisição
        JSONObject jsonRequest = new JSONObject();
        try {
            jsonRequest.put("session_token", sessionToken);
        } catch (Exception e) {
            future.completeExceptionally(e);
            return future;
        }

        // Envia a requisição ao servidor
        RestConnection.post(url, jsonRequest.toString(), String.class, new RestConnection.ResponseCallback<String>() {
            @Override
            public void onSuccess(String response) {
                try {
                    // Analisa a resposta JSON do servidor
                    JSONObject mainResponse = new JSONObject(response);
                    JSONArray resultArray = mainResponse.getJSONArray("result");

                    if (resultArray.length() > 0) {
                        // Extrai o primeiro elemento do array e converte para JSON
                        String innerJson = resultArray.getString(0);
                        JSONObject jsonResponse = new JSONObject(innerJson);

                        boolean success = jsonResponse.getBoolean("success");
                        if (success) {
                            // Sessão válida
                            future.complete(true);
                        } else {
                            // Sessão inválida
                            future.complete(false);
                        }
                    } else {
                        // Array de resultados vazio
                        future.complete(false);
                    }
                } catch (Exception e) {
                    future.completeExceptionally(e); // Completa com erro se ocorrer uma exceção
                }
            }

            @Override
            public void onError(Exception e) {
                future.complete(true);
              //  future.completeExceptionally(e); // Completa com erro se a requisição falhar
            }
        });

        return future;
    }
}



