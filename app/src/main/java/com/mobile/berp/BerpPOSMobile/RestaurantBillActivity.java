package com.mobile.berp.BerpPOSMobile;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.mobile.berp.BerpPosMobile.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class RestaurantBillActivity extends AppCompatActivity {

    private LinearLayout layoutItems;
    private TextView tvSubtotal, tvServiceFee, tvTotal, tvDate, tvOrderNumber;
    private Button btnAddItem, btnGenerateBill;
    private List<BillItem> billItems;
    private double subtotal = 0.0;
    private double serviceFee = 0.0;
    private double total = 0.0;
    private int orderNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_bill);



        // Configurar listeners
        btnAddItem.setVisibility(View.GONE); // Esconder botão de adicionar item, já que usamos dados mock
        btnGenerateBill.setOnClickListener(v -> generateAndShareBill());
    }

    // Método removido pois estamos usando dados mockados

    private void addItemToUI(BillItem item) {
        View itemView = LayoutInflater.from(this).inflate(R.layout.item_bill_row, null);

        TextView tvQuantity = itemView.findViewById(R.id.tvQuantity);
        TextView tvItemName = itemView.findViewById(R.id.tvItemName);
        TextView tvUnitPrice = itemView.findViewById(R.id.tvUnitPrice);
        TextView tvItemTotal = itemView.findViewById(R.id.tvItemTotal);
        TextView tvCodigo = itemView.findViewById(R.id.tvCodigo);


        // Formatadores para valores monetários
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

        tvQuantity.setText(String.valueOf(item.getQuantity()));
        tvItemName.setText(item.getName());
        tvUnitPrice.setText(currencyFormat.format(item.getUnitPrice()));
        tvItemTotal.setText(currencyFormat.format(item.getTotal()));
        tvCodigo.setText(item.getCodigo());

        layoutItems.addView(itemView);
    }

    private void updateTotals() {
        // Recalcular subtotal
        subtotal = 0.0;
        for (BillItem item : billItems) {
            subtotal += item.getTotal();
        }

        // Calcular taxa de serviço e total
        serviceFee = subtotal * 0.10; // 10% de taxa de serviço
        total = subtotal + serviceFee;

        // Formatador para valores monetários
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

        // Atualizar UI
        tvSubtotal.setText(currencyFormat.format(subtotal));
        tvServiceFee.setText(currencyFormat.format(serviceFee));
        tvTotal.setText(currencyFormat.format(total));
    }

//    /**
//     * Carrega dados mockados para a conta
//     */
//    private void loadMockData() {
//        // Criar itens de exemplo
//        BillItem item1 = new BillItem("X-Burger", 2, 28.90, 57.80);
//        BillItem item2 = new BillItem("Batata Frita (G)", 1, 19.50, 19.50);
//        BillItem item3 = new BillItem("Refrigerante 350ml", 3, 6.50, 19.50);
//        BillItem item4 = new BillItem("Pizza Calabresa (M)", 1, 45.00, 45.00);
//        BillItem item5 = new BillItem("Sobremesa Especial", 2, 12.90, 25.80);
//
//        // Adicionar itens à lista
//        billItems.add(item1);
//        billItems.add(item2);
//        billItems.add(item3);
//        billItems.add(item4);
//        billItems.add(item5);
//
//        // Adicionar itens à UI
//        for (BillItem item : billItems) {
//            addItemToUI(item);
//        }
//
//        // Atualizar totais
//        updateTotals();
//    }

    private void generateAndShareBill() {
        // Como estamos usando dados mockados, não precisamos verificar se a lista está vazia

        // Obter todo o layout da conta
        View rootView = findViewById(android.R.id.content).getRootView();

        // Configurar o layout para desenho (tornando-o branco para impressão)
        int originalBgColor = rootView.getDrawingCacheBackgroundColor();
        boolean originalCacheState = rootView.isDrawingCacheEnabled();
        rootView.setDrawingCacheBackgroundColor(Color.WHITE);
        rootView.setDrawingCacheEnabled(true);

        // Criar um bitmap a partir do layout
        Bitmap bitmap = Bitmap.createBitmap(rootView.getWidth(), rootView.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        rootView.draw(canvas);

        // Restaurar estado original
        rootView.setDrawingCacheBackgroundColor(originalBgColor);
        rootView.setDrawingCacheEnabled(originalCacheState);

        // Salvar o bitmap como BMP
        try {
            File directory = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "RestaurantBills");
            if (!directory.exists()) {
                directory.mkdirs();
            }

            String fileName = "Conta_" + orderNumber + ".bmp";
            File file = new File(directory, fileName);

            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();

            // Compartilhar o arquivo
            shareFile(file);

            Toast.makeText(this, "Conta gerada com sucesso!", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Erro ao gerar a conta: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void shareFile(File file) {
        // Criar URI para o arquivo usando FileProvider
        Uri fileUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", file);

        // Criar intent para compartilhar
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("image/bmp");
        shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        // Iniciar a atividade de compartilhamento
        startActivity(Intent.createChooser(shareIntent, "Compartilhar Conta"));

        // Opcionalmente, você pode conectar a uma impressora Bluetooth aqui
        // Exemplo: Se você já tiver o endereço MAC da impressora
        // String printerMacAddress = "00:11:22:33:44:55"; // Substitua pelo endereço MAC da sua impressora
        // PrintUtils printUtils = new PrintUtils(this, printerMacAddress);
        // if (printUtils.isBluetoothPrinterAvailable()) {
        //     printUtils.printBitmap(bitmap); // Você precisaria manter a referência do bitmap
        // }
    }

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

        public BillItem(String Codigo,String name, int quantity, double unitPrice, double total) {
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
}