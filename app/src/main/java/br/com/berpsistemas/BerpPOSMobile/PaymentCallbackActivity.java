package br.com.berpsistemas.BerpPOSMobile;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import br.com.berpsistemas.BerpPOSMobile.pagamento.PaymentCallbackHandlerFactory;

public class PaymentCallbackActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();

        if (intent != null && Intent.ACTION_VIEW.equals(intent.getAction())) {
            PaymentCallbackHandlerFactory
                    .getHandler()
                    .handleCallback(this, intent);
        }

        finish();
    }
}