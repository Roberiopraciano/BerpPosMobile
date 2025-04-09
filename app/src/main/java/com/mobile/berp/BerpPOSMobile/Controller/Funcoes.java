package com.mobile.berp.BerpPOSMobile.Controller;
import com.mobile.berp.BerpPOSMobile.model.ContaFields;
import com.mobile.berp.BerpPOSMobile.model.Mesa;
import com.mobile.berp.BerpPOSMobile.model.Pagamento;
import com.mobile.berp.BerpPOSMobile.model.Produto;
import com.mobile.berp.BerpPOSMobile.model.Variaveis;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Vector;
import java.util.regex.Pattern;


public class Funcoes {

    public  String getUrl(String finalUrl)  {

        String ipServidor = Variaveis.getIpServidor();
        String portaServidor = Variaveis.getPortaServidor();

        if (ipServidor == null || portaServidor == null) {
            throw new IllegalArgumentException("Server IP or port cannot be null");
        }

        // Constrói manualmente a base da URL com IP e porta
        String baseUrl = "http://" + ipServidor + ":" + portaServidor;

        // Retorna a URL completa concatenada com o caminho final
        return baseUrl + finalUrl;
    }


    public double convertStringToDouble(String str) {
        double result = 0.0;
        String regex = "[-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?";
        if (Pattern.matches(regex, str)) {
            try {
                result = Double.parseDouble(str);
            } catch (NumberFormatException e) {
                // Log.e("Conversion Error", "Invalid input string for double: " + str);
                // Tratar exceção, como definir um valor padrão ou informar o usuário
                result = 0.0; // ou qualquer outro valor padrão que você desejar
            }
        } else {
            // Log.e("Invalid Format", "String is not a valid double: " + str);
            // Tratar erro de formato inválido
            result = 0.0; // ou qualquer outro valor padrão que você desejar
        }
        return result;
    }

   public String[][] listaProdutos(Vector<?> mesa) {
        String[][] retorno;
        if (mesa == null) {
            retorno = new String[][] { { "", "", "" , "", ""} };
        } else if (mesa.isEmpty()) {
            retorno = new String[][] { { "", "", "" , "" , ""} };
        } else {
            retorno = new String[mesa.size()][];
            for (int i = 0; i < mesa.size(); i++) {
                Produto p = (Produto) mesa.elementAt(i);
                retorno[i] = new String[] { String.valueOf(i),
                        String.valueOf(p.getCod()), p.getDesc(), p.getQtd() };
            }

        }
        return retorno;
    }

    public List<Produto> listaProdutosMesa(Vector<?> mesa) {
        List<Produto> retorno = new ArrayList<>();
        if (mesa != null) {
            for (int i = 0; i < mesa.size(); i++) {
                Produto p = (Produto) mesa.elementAt(i);
                retorno.add(p);
            }
        }
        return retorno;
    }

    public List<Pagamento> listaPagamentosMesa(Vector<?> mesa) {
        List<Pagamento> retorno = new ArrayList<>();
        if (mesa != null) {
            for (int i = 0; i < mesa.size(); i++) {
                Pagamento pg = (Pagamento) mesa.elementAt(i);
                retorno.add(pg);
            }
        }
        return retorno;
    }


    public String preparaImpressao(Mesa mesa) {

        String retorno = "";

        retorno += "No. Mesa : " + mesa.getNumMesa() + "\n";

        retorno += "No. Venda: " + mesa.getNumVenda() + "\n";

        retorno += "==========================\n" + "Sub. Total\t:\t\t"
                + mesa.getVlrLiq() + "\n" + "Taxa Servico*(+)\t:\t\t"
                + mesa.getVlrSer() + "\n" + "Total R$\t:\t\t"
                + mesa.getVlrVen();

        return retorno;
    }

    public String preparaImpressaoConta(ContaFields mesa) {

        String retorno = "";

        Date dataHoraAtual = new Date();

        Locale locale = new Locale("pt", "BR");

        String data = new SimpleDateFormat("dd/MM/yyyy",locale).format(dataHoraAtual);
        String hora = new SimpleDateFormat("HH:mm",locale).format(dataHoraAtual);

        Date HoraAbertura = new Date();

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm",locale);

        try {
            HoraAbertura = sdf.parse(mesa.getDataHoraMovimento());
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // Calculate difference in milliseconds
        long mills = dataHoraAtual.getTime() - HoraAbertura.getTime();

        int hours = (int) (mills/(1000 * 60 * 60));
        int mins = (int) ((mills/(1000*60)) % 60);

        String diff = "Perm. "+hours + " horas " + mins + " minutos";

        String nomeloja = Variaveis.getLoja().getFields().getfNomeLoja();

        if(nomeloja.length() > 30){
            nomeloja = nomeloja.substring(0,30);
        }


        retorno += nomeloja+"\n";
        retorno += "-------------------------------"+ "\n";
        retorno += data+"               "+hora+"\n";
        retorno += "-------------------------------"+ "\n";
        retorno += "MESA : " + mesa.getCdMesa() + "\n";
        retorno += "Venda: " + mesa.getCdVenda()+ "\n";
        retorno += "Garcom     : " + mesa.getCdGarcom() + "\n";
        retorno += "==============================="+ "\n";
        retorno += "Prod      Qt.  Vl.unit  Total"+ "\n";
        retorno += "==============================="+ "\n";

        for (int i = 0; i < mesa.getProdutos().size(); i++) {
            Produto p = mesa.getProdutos().get(i);

            String desc = separador(p.getDesc(), 11),
                    vlrUnit = separador(p.getValorUnitarioFormatado().replace("R$",""), 7),
                    vlrTot = separadorEsq(p.getValorTotalFormatado(), 7);

            String qtd = p.getQtd();

            if(qtd.length() > 6 ){
                qtd = separadorEsq(qtd, 5);
                if(Integer.parseInt(qtd.substring(2,4)) ==0){
                    qtd = separadorEsq(qtd.substring(0,1),4);
                }
            }else{
                qtd = separadorEsq(qtd.substring(0,1),4);
            }




            retorno += p.getDesc().toLowerCase()+ "\n";
            retorno += qtd + " X " + vlrUnit + "      " + vlrTot+ "\n";

        }

        retorno += "=============================="+ "\n";
        retorno += "Sub. Total  :  " + mesa.getVlrLiquido().replace("R$","")+ "\n";
        retorno += "Servico     :  " + mesa.getVlrServico().replace("R$","")+ "\n";
        retorno += "=============================="+ "\n";
        retorno += "Total R$    :  " + mesa.getVlrBruto().replace("R$","")+ "\n";
        retorno += "-------------------------------"+ "\n";
        retorno += "\n";
        retorno += diff+"\n";
        retorno += "\n";
        retorno += "\n";
        retorno += "\n";
        retorno += "\n";
        retorno += "\n";
        retorno += "\n";
        retorno += "\n";

        return retorno;
    }

    public String preparaImpressaoCompPagamento(ContaFields mesa, List<Pagamento> pagamentos) {

        String retorno = "";

        Date dataHoraAtual = new Date();

        Locale locale = new Locale("pt", "BR");

        String data = new SimpleDateFormat("dd/MM/yyyy",locale).format(dataHoraAtual);
        String hora = new SimpleDateFormat("HH:mm",locale).format(dataHoraAtual);

        String nomeloja = Variaveis.getLoja().getFields().getfNomeLoja();

        if(nomeloja.length() > 30){
            nomeloja = nomeloja.substring(0,30);
        }


        retorno += nomeloja+"\n";
        retorno += ">>COMPROVANTE NAO FISCAL<<"+"\n";
        retorno += "-------------------------------"+ "\n";
        retorno += data+"               "+hora+"\n";
        retorno += "-------------------------------"+ "\n";
        retorno += "MESA : " + mesa.getCdMesa() + "\n";
        retorno += "Venda: " + mesa.getCdVenda()+ "\n";
        retorno += "Garcom     : " + mesa.getCdGarcom() + "\n";
        retorno += "==============================="+ "\n";
        retorno += "Prod      Qt.  Vl.unit  Total"+ "\n";
        retorno += "==============================="+ "\n";

        for (int i = 0; i < mesa.getProdutos().size(); i++) {
            Produto p = mesa.getProdutos().get(i);

            String desc = separador(p.getDesc(), 11),
                    vlrUnit = separador(p.getValorUnitarioFormatado().replace("R$",""), 7),
                    vlrTot = separadorEsq(p.getValorTotalFormatado(), 7);

            String qtd = p.getQtd();

            if(qtd.length() > 6 ){
                qtd = separadorEsq(qtd, 5);
                if(Integer.parseInt(qtd.substring(2,4)) ==0){
                    qtd = separadorEsq(qtd.substring(0,1),4);
                }
            }else{
                qtd = separadorEsq(qtd.substring(0,1),4);
            }




            retorno += p.getDesc().toLowerCase()+ "\n";
            retorno += qtd + " X " + vlrUnit + "      " + vlrTot+ "\n";

        }

        retorno += "=============================="+ "\n";
        retorno += "Sub. Total  :  " + mesa.getVlrLiquido().replace("R$","")+ "\n";
        retorno += "Servico     :  " + mesa.getVlrServico().replace("R$","")+ "\n";
        retorno += "              --------------  "+ "\n";
        retorno += "Total R$    :  " + mesa.getVlrBruto().replace("R$","")+ "\n";
        retorno += "-------------------------------"+ "\n";

        for(int j = 0;j < pagamentos.size();j++) {
            String descpag = "";
            switch (pagamentos.get(j).getCdfpaga()){
                case 1:
                    if (pagamentos.get(j).getEvtipo() == 2){
                        descpag = "DINHEIRO";
                    }else {
                        descpag = "TROCO   ";
                    }
                    break;
                case 86:
                    descpag = "DEBITO  ";
                    break;
                case 87:
                    descpag = "CREDITO ";
                    break;
            }

            retorno += descpag+"    :  "+ String.format(locale,"%.02f", pagamentos.get(j).getValor())+"\n";

        }

        retorno += "-------------------------------"+ "\n";
        retorno += "\n";
        retorno += "\n";
        retorno += "\n";
        retorno += "\n";
        retorno += "\n";
        retorno += "\n";
        retorno += "\n";

        return retorno;
    }

    public Vector<String> preparaImpressaoList(ContaFields mesa) {

        Vector<String> retorno = new Vector<>(1, 1);
        retorno.addElement("No. Mesa : " + mesa.getCdMesa());
        retorno.addElement("No. Venda: " + mesa.getCdVenda());
        retorno.addElement("Sub. Total\t\t\t:\t " + mesa.getVlrLiquido());
        retorno.addElement("Taxa Servico*(+)\t :\t " + mesa.getVlrServico());
        retorno.addElement("Total R$\t\t\t  :\t " + mesa.getVlrBruto());

        return retorno;
    }

    public Vector<String> preparaImpressaoDetalhada(ContaFields mesa) {

        Vector<String> retorno = new Vector<>(1, 1);

        retorno.addElement("No. Mesa : " + mesa.getCdMesa());
        retorno.addElement("No. Venda: " + mesa.getCdVenda());
        retorno.addElement("Garcom     : (" + mesa.getCdGarcom() + ")");
        retorno.addElement("==========================");
        retorno.addElement("Qt.    Produto          P.U.    Valor");
        retorno.addElement("==========================");

        for (int i = 0; i < mesa.getProdutos().size(); i++) {
            Produto p = mesa.getProdutos().get(i);

            String qtd = separador(p.getQtd(), 3), desc = separador(
                    p.getDesc(), 11), vlrUnit = separador(p.getValorUnitarioFormatado(), 5), vlrTot = separador(
                    p.getValorTotalFormatado(), 5);
            retorno.addElement(qtd + " " + desc + " " + vlrUnit + " " + vlrTot);

        }

        retorno.addElement("==========================");
        retorno.addElement("Sub. Total       :                    "
                + mesa.getVlrLiquido());
        retorno.addElement("Taxa Servico*(+) :                    "
                + mesa.getVlrServico());
        retorno.addElement("==========================");
        retorno.addElement("Total R$         :                    "
                + mesa.getVlrBruto());

        return retorno;
    }


    private String separador(String texto, int maxTam) {
        String retorno;
        if (texto.length() <= maxTam) {
            String y = "";
            for (int j = 0; j <= maxTam - texto.length(); j++) {
                y = y + " ";
            }
            retorno = texto + y;
        } else {
            retorno = texto.substring(0, maxTam);
        }
        return retorno;
    }

    private String separadorEsq(String texto, int maxTam) {
        String retorno;
        if (texto.length() <= maxTam) {
            String y = "";
            for (int j = 0; j <= maxTam - texto.length(); j++) {
                y = " " + y ;
            }
            retorno = y + texto;
        } else {
            retorno = texto.substring(0, maxTam);
        }
        return retorno;
    }
}
