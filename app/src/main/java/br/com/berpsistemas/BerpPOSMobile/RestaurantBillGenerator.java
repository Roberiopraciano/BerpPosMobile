package br.com.berpsistemas.BerpPOSMobile;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.icu.text.DecimalFormat;
import android.icu.text.NumberFormat;
import android.icu.text.SimpleDateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import br.com.berpsistemas.BerpPOSMobile.model.ContaFields;
import br.com.berpsistemas.BerpPOSMobile.model.PagamentoModel;
import br.com.berpsistemas.BerpPOSMobile.model.Produto;
import br.com.berpsistemas.BerpPOSMobile.model.Variaveis;
import br.com.berpsistemas.BerpPOSMobile.R;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Classe responsável por gerar uma imagem de conta de restaurante
 */
public class RestaurantBillGenerator {
    private Context context;
    private int orderNumber;
    private List<BillItem> billItems;
    private double subtotal = 0.0;
    private double serviceFee = 0.0;
    private double total = 0.0;
    private ContaFields conta; // armazenar a conta original

    /**
     * Construtor
     * @param context Contexto da aplicação
     */
    public RestaurantBillGenerator(Context context) {
        this.context = context;
        this.orderNumber = (int) (Math.random() * 10000);
        this.billItems = new ArrayList<>();


    }



    private void calculateTotals() {
        subtotal = 0.0;
        for (BillItem item : billItems) {
            subtotal += item.getTotal();
        }

        serviceFee = subtotal * 0.10; // 10% de taxa de serviço
        total = subtotal + serviceFee;
    }


    private void setupBillHeader(View billView) {
        // TextViews do layout
        TextView tvRestaurantName = billView.findViewById(R.id.tvRestaurantName);
        TextView tvTipoVenda = billView.findViewById(R.id.tvTipoVenda);
        TextView tvOrderNumber = billView.findViewById(R.id.tvOrderNumber);
        TextView tvVendaId = billView.findViewById(R.id.tvVendaId);
        TextView tvTerminal = billView.findViewById(R.id.tvTerminal);
        TextView tvGarcom = billView.findViewById(R.id.tvGarcom);
        TextView tvDateAbertura = billView.findViewById(R.id.tvDateAbertura);
        TextView tvDateAtual = billView.findViewById(R.id.tvDateAtual);
        TextView tvTempoPermanencia = billView.findViewById(R.id.tvTempoPermanencia);
        TextView tvDesconto = billView.findViewById(R.id.tvDesconto);
        LinearLayout layoutDesconto = billView.findViewById(R.id.layoutDesconto);

        if (conta == null) return;

        // Nome do restaurante
        String nomeFantasia = conta.getFantazia();
        tvRestaurantName.setText(nomeFantasia != null && !nomeFantasia.isEmpty()
                ? nomeFantasia
                : "..");

        // Tipo de venda
        tvTipoVenda.setText(conta.getVen_nmtpvend() +' ' +conta.getCdMesa());

        // Número do pedido
        tvOrderNumber.setText("Venda Nº: " + (conta.getCdVenda()));

        // ID da venda
        tvVendaId.setText("Id      : " + conta.getId());

        // Terminal
        tvTerminal.setText("Terminal: " + Variaveis.getTerminal_id());

        // Garçom
        String garcom = (conta.getNomeGarcom() != null ? conta.getNomeGarcom() : "") +
                " (" + (conta.getCdGarcom() != null ? conta.getCdGarcom() : "") + ")";
        tvGarcom.setText("Atendente:" + garcom.trim());

        // Formata datas
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        Date abertura = new Date();
        try {
            if (conta.getDataHoraMovimento() != null) {
                abertura = sdf.parse(conta.getDataHoraMovimento());
            }
        } catch (Exception e) {
            Log.e("BILL_HEADER", "Erro ao parsear data de abertura", e);
        }

        Date agora = new Date();

        // Data de abertura
        tvDateAbertura.setText("Abertura: " + sdf.format(abertura));

        // Data atual
        tvDateAtual.setText("Data    : " + sdf.format(agora));

        // Tempo de permanência
        long diffMillis = agora.getTime() - abertura.getTime();
        long diffMinutes = diffMillis / (60 * 1000);
        long horas = diffMinutes / 60;
        long minutos = diffMinutes % 60;

        String tempo = String.format(Locale.getDefault(), "%02d:%02d", horas, minutos);
        tvTempoPermanencia.setText("Permanência: " + tempo);

        // Desconto, se houver
        String vlrDesconto = conta.getVlrDesconto();
        if (vlrDesconto != null && !vlrDesconto.trim().isEmpty() && !vlrDesconto.equals("0") && !vlrDesconto.equals("0,00")) {
            layoutDesconto.setVisibility(View.VISIBLE);
            tvDesconto.setText(vlrDesconto);
        } else {
            layoutDesconto.setVisibility(View.GONE);
        }
    }


    /**
     * Gera apenas o bitmap da conta de restaurante, sem salvar no arquivo
     * @return Bitmap da conta
     */
    public Bitmap generateBillBitmap() throws Exception {
        // Infla o layout da conta
        LayoutInflater inflater = LayoutInflater.from(context);
        View billView = inflater.inflate(R.layout.activity_restaurant_bill, null);

        // Configura o tamanho do layout de acordo com o tamanho do papel da impressora térmica
        // Padrão para impressoras térmicas é geralmente 58mm (384px) ou 80mm (576px)
        int width = 384;

        // Configura data, número do pedido, etc.
        setupBillHeader(billView);

        // Adiciona os itens da conta
        addItemsToBill(billView);

        // Configura os totais
        setupTotals(billView);

        setupPayments(billView);

        // Mede o layout para determinar a altura necessária
        billView.measure(
                View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        );

        // Layout da view com as dimensões medidas
        billView.layout(0, 0, width, billView.getMeasuredHeight());

        // Cria o bitmap e desenha a view nele
        Bitmap bitmap = Bitmap.createBitmap(width,
                billView.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE); // Fundo branco
        billView.draw(canvas);

        return bitmap;
    }

    /**
     * Gera a imagem da conta de restaurante
     * @return Arquivo da imagem gerada
     */
    public File generateBillImage() throws Exception {
        // Infla o layout da conta
        LayoutInflater inflater = LayoutInflater.from(context);
        View billView = inflater.inflate(R.layout.activity_restaurant_bill, null);

        // Configura o tamanho do layout de acordo com o tamanho do papel da impressora térmica
        // Largura padrão para impressoras térmicas é geralmente de 58mm ou 80mm
        // Usamos 58mm que equivale a aproximadamente 384 pixels (considerando densidade de ~170dpi)
        int width = 384;

        // Configura data, número do pedido, etc.
        setupBillHeader(billView);

        // Adiciona os itens da conta
        addItemsToBill(billView);

        // Configura os totais
        setupTotals(billView);

        // Mede o layout para determinar a altura
        billView.measure(
                View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        );

        // Layout da view com as dimensões medidas
        billView.layout(0, 0, width, billView.getMeasuredHeight());

        // Logs para debug
        Log.d("BILL_DEBUG", "View medida: " + width + "x" + billView.getMeasuredHeight());

        // Cria o bitmap e desenha a view nele
        Bitmap bitmap = Bitmap.createBitmap(width,
                billView.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE); // Fundo branco
        billView.draw(canvas);

        // Agora vamos salvar no cache interno
        try {
            // Usamos o diretório de cache interno que não requer permissões especiais
            File cacheDir = context.getCacheDir();
            File picturesDir = new File(cacheDir, "RestaurantBills");

            if (!picturesDir.exists()) {
                boolean created = picturesDir.mkdirs();
                Log.d("BILL_DEBUG", "Diretório criado: " + created + " - " + picturesDir.getAbsolutePath());
            }

            String fileName = "Conta_" + orderNumber + ".png";
            File file = new File(picturesDir, fileName);

            Log.d("BILL_DEBUG", "Salvando imagem em: " + file.getAbsolutePath());

            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();

            Log.d("BILL_DEBUG", "Imagem salva com sucesso. Tamanho: " + file.length() + " bytes");

            return file;
        } catch (IOException e) {
            Log.e("BILL_ERROR", "Erro ao salvar arquivo: " + e.getMessage(), e);

            // Se falhar, tenta novamente com um caminho diferente
            try {
                // Tenta usar o diretório files interno
                File filesDir = context.getFilesDir();
                File file = new File(filesDir, "conta.png");

                Log.d("BILL_DEBUG", "Tentando caminho alternativo: " + file.getAbsolutePath());

                FileOutputStream fos = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.flush();
                fos.close();

                Log.d("BILL_DEBUG", "Imagem salva com sucesso no caminho alternativo");

                return file;
            } catch (IOException e2) {
                Log.e("BILL_ERROR", "Erro no caminho alternativo: " + e2.getMessage(), e2);
                throw new IOException("Não foi possível salvar a imagem: " + e2.getMessage());
            }
        }
    }


    /**
     * Adiciona os itens da conta ao layout
     */
    private void addItemsToBill(View billView) {
        // Obtém o container onde serão adicionados os itens
        LinearLayout itemsContainer = billView.findViewById(R.id.layoutItems);

        // Formatador para valores monetários
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

        // Formatadores para quantidade
        DecimalFormat decimalFormatInteiro = new DecimalFormat("0");
        DecimalFormat decimalFormatDecimal = new DecimalFormat("0.000");

        for (BillItem item : billItems) {
            View itemView = LayoutInflater.from(context).inflate(R.layout.item_bill_row, null);

            TextView tvQuantity = itemView.findViewById(R.id.tvQuantity);
            TextView tvItemName = itemView.findViewById(R.id.tvItemName);
            TextView tvUnitPrice = itemView.findViewById(R.id.tvUnitPrice);
            TextView tvItemTotal = itemView.findViewById(R.id.tvItemTotal);
            TextView tvCodigo = itemView.findViewById(R.id.tvCodigo);

            // Verifica se a quantidade tem parte decimal
            double qtd = item.getQuantity();
            String formattedQtd;
            if (qtd == Math.floor(qtd)) {
                // Número inteiro
                formattedQtd = decimalFormatInteiro.format(qtd);
            } else {
                // Número com casas decimais
                formattedQtd = decimalFormatDecimal.format(qtd);
            }

            tvQuantity.setText(formattedQtd);
            tvItemName.setText(item.getName());
            tvUnitPrice.setText(currencyFormat.format(item.getUnitPrice()));
            tvItemTotal.setText(currencyFormat.format(item.getTotal()));
            tvCodigo.setText(String.valueOf(item.codigo));

            itemsContainer.addView(itemView);
        }
    }

    /**
     * Configura os totais da conta
     */
    private void setupTotals(View billView) {
        // Obtém as TextViews dos totais
        TextView tvSubtotal = billView.findViewById(R.id.tvSubtotal);
        TextView tvServiceFee = billView.findViewById(R.id.tvServiceFee);
        TextView tvTotal = billView.findViewById(R.id.tvTotal);

        // Formatador para valores monetários
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

        // Define os valores formatados
        tvSubtotal.setText(currencyFormat.format(subtotal));
        tvServiceFee.setText(currencyFormat.format(serviceFee));
        tvTotal.setText(currencyFormat.format(total));
    }

    /**
     * Classe interna para representar um item da conta
     */
    // Classe para representar um item da conta
    // Classe para representar um item da conta
    private static class BillItem {
        private String name;
        private double quantity;
        private double unitPrice;
        private double total;

        public String getCodigo() {
            return codigo;
        }

        private String codigo;

        public BillItem(String Codigo,String name, double quantity, double unitPrice, double total) {
            this.name = name;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
            this.total = total;
            this.codigo = Codigo;
        }

        public String getName() {
            return name;
        }

        public double getQuantity() {
            return quantity;
        }

        public double getUnitPrice() {
            return unitPrice;
        }

        public double getTotal() {
            return total;
        }
    }


    private String toCamelCase(String input) {
        if (input == null) return "";

        String[] words = input.toLowerCase().split(" ");
        StringBuilder sb = new StringBuilder();

        for (String word : words) {
            if (word.length() == 0) continue;
            sb.append(Character.toUpperCase(word.charAt(0)));
            if (word.length() > 1) {
                sb.append(word.substring(1));
            }
            sb.append(" ");
        }

        return sb.toString().trim();
    }
    public void setConta(ContaFields conta) {
        if (conta == null) return;

        this.conta = conta;

        // Limpa itens anteriores
        billItems.clear();

        // Define número do pedido
        try {
            this.orderNumber = Integer.parseInt(conta.getCdVenda());
        } catch (Exception e) {
            this.orderNumber = (int) (Math.random() * 10000);
        }

        // Converte produtos da conta em BillItem
        for (Produto produto : conta.getProdutos()) {
          //  String nome = produto.getDesc();
            String nome = toCamelCase(produto.getDesc());
            String Codigo = String.valueOf((produto.getCod()));
            Double total = produto.getValorTotal();
            double qtd = 1;

            try {
                qtd = produto.getQtdAsDouble();
            } catch (Exception e) {
                // usa 1 por padrão
            }

            double precoUnit = 0.0;
            try {

                precoUnit = produto.getValorUnitario();
            } catch (Exception e) {
                // ignora erro
            }

            billItems.add(new BillItem(Codigo,nome, qtd, precoUnit,total));
        }

        // Calcula subtotal
        subtotal = 0.0;
        for (BillItem item : billItems) {
            subtotal += item.getTotal();
        }

        // Tenta puxar taxa de serviço diretamente, se estiver preenchida
        try {
            if (conta.getVlrServico() != null && !conta.getVlrServico().isEmpty()) {
                serviceFee = Double.parseDouble(conta.getVlrServico().replace("R$", "").replace(",", ".").trim());
            } else {
                serviceFee = subtotal * 0.10;
            }
        } catch (Exception e) {
            serviceFee = subtotal * 0.10;
        }

        total = subtotal + serviceFee;
    }

    /**
     * Adicione este método abaixo do método setupTotals na classe RestaurantBillGenerator.java
     */

    /**
     * Configura a seção de pagamentos efetuados
     */
    private void setupPayments(View billView) {
        // Verifica se há pagamentos
        if (conta == null || conta.getPagamentos() == null || conta.getPagamentos().isEmpty()) {
            // Se não houver pagamentos, oculta a seção
            View paymentHeader = billView.findViewById(R.id.layoutPagamentosHeader);
            View totalPayments = billView.findViewById(R.id.layoutTotalPagamentos);

            if (paymentHeader != null) paymentHeader.setVisibility(View.GONE);
            if (totalPayments != null) totalPayments.setVisibility(View.GONE);
            return;
        }

        // Obtém o container para os pagamentos
        LinearLayout paymentsContainer = billView.findViewById(R.id.layoutPagamentos);
        if (paymentsContainer == null) return;

        // Formatador para valores monetários
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

        // Esvazia o container (caso esteja sendo reutilizado)
        paymentsContainer.removeAllViews();

        // Adiciona cada pagamento ao container
        for (PagamentoModel pagamento : conta.getPagamentos()) {
            // Infla o layout do item de pagamento
            View paymentView = LayoutInflater.from(context).inflate(R.layout.item_payment_row, null);

            // Configura os campos
            TextView tvFormaPagamento = paymentView.findViewById(R.id.tvFormaPagamento);
            TextView tvValorPagamento = paymentView.findViewById(R.id.tvValorPagamento);

            // Define os textos
            tvFormaPagamento.setText(pagamento.getBandeira());
            tvValorPagamento.setText(currencyFormat.format(pagamento.getPgpVlrpag()));

            // Adiciona ao container
            paymentsContainer.addView(paymentView);
        }

        // Configura o total pago
        TextView tvTotalPagamentos = billView.findViewById(R.id.tvTotalPagamentos);
        if (tvTotalPagamentos != null) {
            tvTotalPagamentos.setText("R$ " + conta.getTotalPagoStr());
        }
    }

/**
 * Modifique o método generateBillBitmap e generateBillImage adicionando a
 * chamada para o método setupPayments logo após setupTotals, assim:
 *
 * // Configura os totais
 * setupTotals(billView);
 *
 * // Configura os pagamentos
 * setupPayments(billView);
 */
}