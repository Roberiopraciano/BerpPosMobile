package com.mobile.berp.BerpPOSMobile;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import com.google.android.material.textfield.TextInputLayout;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.SwitchCompat;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.mobile.berp.BerpPOSMobile.model.BerpModel;
import com.mobile.berp.BerpPOSMobile.model.LoginCallback;
import com.mobile.berp.BerpPOSMobile.model.Variaveis;
import com.mobile.berp.BerpPosMobile.R;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener  {

    private TextInputLayout txtlayoutusuario,txtlayoutsenha;
    private AppCompatEditText edtxtUsuario,edtTxtSenha;
    private AppCompatButton btnEntrar;

    private SwitchCompat switchManterLogado;

    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "LoginPrefs";
    private static final String KEY_USUARIO = "usuario";
    private static final String KEY_SENHA = "senha";
    private static final String KEY_MANTER_LOGADO = "manter_logado";
    private TextView terminal_id, terminal_name;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        txtlayoutusuario =  findViewById(R.id.input_layout_usuario);
        txtlayoutsenha =  findViewById(R.id.input_layout_senha);
        edtTxtSenha =  findViewById(R.id.edtTxtSenha);
        edtxtUsuario =  findViewById(R.id.edtxtUsuario);
        btnEntrar =  findViewById(R.id.btnEntrar);

        terminal_id = findViewById(R.id.labelTerminalId);
        terminal_name = findViewById(R.id.labelTerminalName);

        terminal_id.setText(Variaveis.getTerminal_id());
        terminal_name.setText(Variaveis.getTerminal_name());

        switchManterLogado = findViewById(R.id.switchManterLogado);

        // Configura SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // Carrega os dados salvos (se houver)
        carregarDadosSalvos();

        btnEntrar.setOnClickListener(this);


    }


    public static void efetuarLoginBackground(String usuario, String senha, LoginCallback callback) {
        new Thread(() -> {
            try {
                boolean success = BerpModel.efetuarLoginSync(usuario, senha); // Método síncrono para login
                if (success) {
                    callback.onSuccess();
                } else {
                    callback.onFailure("Credenciais inválidas ou token ausente.");
                }
            } catch (Exception e) {
                callback.onFailure("Erro ao efetuar login: " + e.getMessage());
            }
        }).start();
    }

    @Override
    public void onClick(View view) {

        String usuarioInformado = edtxtUsuario.getText().toString();
        String senhaInformada = edtTxtSenha.getText().toString();

        if (usuarioInformado.isEmpty()) {
            txtlayoutusuario.setError("Preencha o campo");
            return;
        }

        if (senhaInformada.isEmpty()) {
            txtlayoutsenha.setError("Preencha o campo");
            return;
        }

        efetuarLoginBackground(usuarioInformado, senhaInformada, new LoginCallback() {
            @Override
            public void onSuccess() {
                // Atualiza a UI na thread principal
                runOnUiThread(() -> {
                    Toast.makeText(getApplicationContext(), "Login realizado com sucesso!", Toast.LENGTH_SHORT).show();
                    if (switchManterLogado.isChecked()) {
                        salvarDados(usuarioInformado, senhaInformada);
                    } else {
                        limparDados();
                    }

                    // Navega para a próxima atividade
                    Intent it = new Intent(LoginActivity.this, DashboardActivity.class);
                    startActivity(it);
                    finish();
                });
            }

            @Override
            public void onFailure(String error) {
                // Atualiza a UI na thread principal
                runOnUiThread(() -> {
                    Toast.makeText(getApplicationContext(), "Erro: " + error, Toast.LENGTH_SHORT).show();
                    Toast.makeText(LoginActivity.this, "Usuário ou senha inválidos", Toast.LENGTH_SHORT).show();
                    edtTxtSenha.setText("");
                });
            }
        });


    }

    private void carregarDadosSalvos() {
        // Verifica se "manter logado" está ativado e preenche os campos
        boolean manterLogado = sharedPreferences.getBoolean(KEY_MANTER_LOGADO, false);
        switchManterLogado.setChecked(manterLogado);

        if (manterLogado) {
            String usuario = sharedPreferences.getString(KEY_USUARIO, "");
            String senha = sharedPreferences.getString(KEY_SENHA, "");

            edtxtUsuario.setText(usuario);
            edtTxtSenha.setText(senha);
        }
    }

    private void salvarDados(String usuario, String senha) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_USUARIO, usuario);
        editor.putString(KEY_SENHA, senha);
        editor.putBoolean(KEY_MANTER_LOGADO, true);
        editor.apply();
    }

    private void limparDados() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }
}
