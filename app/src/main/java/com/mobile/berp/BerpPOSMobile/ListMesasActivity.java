package com.mobile.berp.BerpPOSMobile;

import android.content.Context;
import android.os.AsyncTask;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.mobile.berp.BerpPOSMobile.model.Configuracao;
import com.mobile.berp.BerpPOSMobile.model.Mesa;
import com.mobile.berp.BerpPOSMobile.Controller.Proxy;
import com.mobile.berp.BerpPOSMobile.model.BerpModel;
import com.mobile.berp.BerpPOSMobile.model.RecyclerAdapterMesas;
import com.mobile.berp.BerpPOSMobile.model.Variaveis;
import com.mobile.berp.BerpPosMobile.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


public class ListMesasActivity extends AppCompatActivity implements View.OnClickListener{
    private Button btMostrartodos,btnVoltar;
    private RecyclerView recyclerView;
    private RecyclerAdapterMesas adapter;
    private List<Mesa> mesas;
    private ProgressBar pblistamesa;
    private EditText editTextPesquisa;
    private ImageView iconClearFilter;
    private List<Mesa> originalMesas;
    private boolean mesaAtiva, cartaoAtivo, balcaoAtivo, deliveryAtivo,mesaCheck,cartaoCheck,balcaoCheck,deliveryCheck;
    private TextView txtMesasCount, txtCartoesCount, txtBalcoesCount;

    private androidx.appcompat.widget.SwitchCompat switchMesa, switchCartao, switchBalcao,switchDelivery;

    public List<Mesa> getMesas() {
        return mesas;
    }

    public void setMesas(List<Mesa> mesas) {
        this.mesas = mesas;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_mesas);

        pblistamesa = findViewById(R.id.pblistamesa);

        btMostrartodos = findViewById(R.id.btnMostrartodos);
        btnVoltar = findViewById(R.id.btnVoltar);

        btMostrartodos.setOnClickListener(this);
        btnVoltar.setOnClickListener(this);

        editTextPesquisa = findViewById(R.id.editTextPesquisa);
        iconClearFilter = findViewById(R.id.iconClearFilter);

        switchMesa = findViewById(R.id.switchMesa);
        switchCartao = findViewById(R.id.switchCartao);
        switchBalcao = findViewById(R.id.switchBalcao);
        switchDelivery = findViewById(R.id.switchDelivery);
        txtMesasCount = findViewById(R.id.txtMesasCount);
        txtCartoesCount = findViewById(R.id.txtCartoesCount);
        txtBalcoesCount = findViewById(R.id.txtBalcoesCount);


        recyclerView = findViewById(R.id.listmesas);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));



        Configuracao mesaConfig = Variaveis.getConfiguracao("MOBILE_VENDA_MESA");
        Configuracao cartaoConfig = Variaveis.getConfiguracao("MOBILE_VENDA_CARTAO");
        Configuracao balcaoConfig = Variaveis.getConfiguracao("MOBILE_VENDA_BALCAO");
        Configuracao deliveryConfig = Variaveis.getConfiguracao("MOBILE_VENDA_DELIVERY");

        mesaAtiva = mesaConfig.getValor().equalsIgnoreCase("S");
        cartaoAtivo = cartaoConfig.getValor().equalsIgnoreCase("S");
        balcaoAtivo = balcaoConfig.getValor().equalsIgnoreCase("S");
        deliveryAtivo = deliveryConfig.getValor().equalsIgnoreCase("S");



        if (!mesaAtiva) switchMesa.setVisibility(View.GONE);
        if (!cartaoAtivo) switchCartao.setVisibility(View.GONE);
        if (!balcaoAtivo) switchBalcao.setVisibility(View.GONE);
        if (!deliveryAtivo) switchDelivery.setVisibility(View.GONE);

        if (mesaCheck == Boolean.parseBoolean(null)){
            mesaCheck=true;
        }
        if (cartaoCheck == Boolean.parseBoolean(null)){
            cartaoCheck=true;
        }
        if (balcaoCheck == Boolean.parseBoolean(null)) {
            balcaoCheck=true;
        }





        switchMesa.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mesaCheck = isChecked;
            BerpModel.setFiltromesa(isChecked);
            filtrarPorSwitchAtivo();
        });

        switchCartao.setOnCheckedChangeListener((buttonView, isChecked) -> {
            cartaoCheck = isChecked;
            BerpModel.setFiltrocartao(isChecked);
            filtrarPorSwitchAtivo();
        });

        switchBalcao.setOnCheckedChangeListener((buttonView, isChecked) -> {
            balcaoCheck = isChecked;
            BerpModel.setFiltrobalcao(isChecked);
            filtrarPorSwitchAtivo();
        });
        switchDelivery.setOnCheckedChangeListener((buttonView, isChecked) -> {
            deliveryCheck = isChecked;
            filtrarPorSwitchAtivo();
        });

        switchMesa.setChecked(true);
        switchCartao.setChecked(true);
        switchBalcao.setChecked(true);
        filtrarPorSwitchAtivo();

        Processo processo = new Processo(this);
        processo.execute(BerpModel.getFuncionario(), "", "");


        editTextPesquisa.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterMesas(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Clear filter logic
        iconClearFilter.setOnClickListener(v -> {
            editTextPesquisa.setText("");
            adapter = new RecyclerAdapterMesas(originalMesas, ListMesasActivity.this);
            recyclerView.setAdapter(adapter);
        });
    }


    private void atualizarContadores() {
        if (originalMesas == null) return;

        int mesasCount = (int) originalMesas.stream().filter(m -> "0".equals(m.getTipoVenda())).count();
        int cartoesCount = (int) originalMesas.stream().filter(m -> "4".equals(m.getTipoVenda())).count();
        int balcoesCount = (int) originalMesas.stream().filter(m -> "1".equals(m.getTipoVenda())).count();

        txtMesasCount.setText("Mesas: " + mesasCount);
        txtCartoesCount.setText("Cartões: " + cartoesCount);
        txtBalcoesCount.setText("Balcões: " + balcoesCount);
    }

    @Override
    public void onClick(View view) {
        if (view == btMostrartodos){
            try{
               if (btMostrartodos.getText().equals("Mostrar Todos")) {
                   Processo processo = new Processo(this);
                   processo.execute("", "", "");
                   btMostrartodos.setText("Apenas Individual");


               }
               else{
                   Processo processo = new Processo(this);
                   processo.execute(BerpModel.getFuncionario(), "", "");
                   btMostrartodos.setText("Mostrar Todos");


               }
            }catch (Exception e){

            }

        }else if (view == btnVoltar){
            finish();
        }
    }


    private void filterMesas(String query) {
        if (originalMesas == null || originalMesas.isEmpty()) return;

        List<Mesa> filteredMesas = originalMesas.stream()
                .filter(mesa -> mesa.getNumMesa().contains(query))
                .collect(Collectors.toList());

        adapter = new RecyclerAdapterMesas(filteredMesas, ListMesasActivity.this);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    private void filtrarPorSwitchAtivo() {
        if (originalMesas == null || originalMesas.isEmpty()) return;

        // Lista filtrada
        List<Mesa> mesasFiltradas = new ArrayList<>();


        // Verificar switches ativos e adicionar as mesas correspondentes
        if (mesaAtiva && mesaCheck) {

            mesasFiltradas.addAll(
                    originalMesas.stream()
                            .filter(mesa -> Objects.equals(mesa.getTipoVenda(), "0")) // Filtrar por TP_VEND=0
                            .collect(Collectors.toList())
            );
        }

        if (cartaoAtivo && cartaoCheck) {

            mesasFiltradas.addAll(
                    originalMesas.stream()
                            .filter(mesa -> Objects.equals(mesa.getTipoVenda(), "4")) // Filtrar por TP_VEND=4
                            .collect(Collectors.toList())
            );
        }
        if (balcaoAtivo && balcaoCheck) {


            mesasFiltradas.addAll(
                    originalMesas.stream()
                            .filter(mesa -> Objects.equals(mesa.getTipoVenda(), "1")) // Filtrar por TP_VEND=1
                            .collect(Collectors.toList())
            );
        }

        if (deliveryAtivo) {
            mesasFiltradas.addAll(
                     originalMesas.stream()
                    .filter(mesa -> Objects.equals(mesa.getTipoVenda(), "2")) //
                    .collect(Collectors.toList()
                    ));
        }

        atualizarContadores();

        // Atualizar o adapter com a lista filtrada
        adapter = new RecyclerAdapterMesas(mesasFiltradas, ListMesasActivity.this);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        // Exibir mensagem se nenhum item for encontrado
        if (mesasFiltradas.isEmpty()) {
            Toast.makeText(this, "Nenhum item encontrado com os filtros ativos!", Toast.LENGTH_SHORT).show();
        }
    }

    public class Processo extends AsyncTask<String, String, Integer> {
        private Context context;

        public Processo(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pblistamesa.setVisibility(View.VISIBLE);

        }

        @Override
        protected void onPostExecute(Integer integer) {

            try {
                pblistamesa.setVisibility(View.GONE);
                if (mesas.size() > 0) {

                    originalMesas = new ArrayList<>(mesas != null ? mesas : new ArrayList<>());

                    filtrarPorSwitchAtivo();
                    adapter.notifyDataSetChanged();
                } else {
                    mesas.clear();
                    recyclerView.setAdapter(null);
                    new AlertDialog.Builder(context).setTitle("AVISO!").setMessage("Sem Mesas para Listar!").setCancelable(false)
                            .setPositiveButton("Ok", (dialog, which) -> {

                            }).show();
                }
                atualizarContadores();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                Toast.makeText(context, "Erro! n/" + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }


            try{
                if (mesas.size() > 0) {
                    adapter = new RecyclerAdapterMesas(getMesas(), ListMesasActivity.this);
                    recyclerView.setAdapter(adapter);
                }
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                Toast.makeText(context, "Erro! n/" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected Integer doInBackground(String... strings) {
            try
            {
                setMesas(Proxy.getListaMesas(strings[0]).get());
            } catch (Exception e) {
                // exception = e.getMessage();
                return 99;
            }
            return 99;
        }
    }
}
