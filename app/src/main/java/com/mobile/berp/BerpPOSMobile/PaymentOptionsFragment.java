package com.mobile.berp.BerpPOSMobile;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.mobile.berp.BerpPosMobile.R;

import java.text.NumberFormat;
import java.util.Locale;

public class PaymentOptionsFragment extends Fragment {

    private static final String ARG_PAYMENT_ID = "payment_id";
    private static final String ARG_PAYMENT_VALUE = "payment_value";

    private String paymentId;
    private double paymentValue;

    // Interface para comunicação com a Activity
    public interface PaymentOptionsListener {
        void onPaymentMethodSelected(String method);
        void onPaymentOptionsCancelled();
    }

    private PaymentOptionsListener listener;

    public static PaymentOptionsFragment newInstance(String paymentId, double paymentValue) {
        PaymentOptionsFragment fragment = new PaymentOptionsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PAYMENT_ID, paymentId);
        args.putDouble(ARG_PAYMENT_VALUE, paymentValue);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof PaymentOptionsListener) {
            listener = (PaymentOptionsListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement PaymentOptionsListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Recupera os parâmetros passados
        if (getArguments() != null) {
            paymentId = getArguments().getString(ARG_PAYMENT_ID);
            paymentValue = getArguments().getDouble(ARG_PAYMENT_VALUE);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_payment_options, container, false);

        // Exemplo: atualizando um TextView que exibe os detalhes do pagamento
        TextView tvPaymentDetails = view.findViewById(R.id.tv_payment_details);
        tvPaymentDetails.setText(new StringBuilder().append("Atendimento ").append(paymentId).append(" - Valor: ").append(NumberFormat.getCurrencyInstance(new Locale("pt", "BR")).format(paymentValue)).toString());

        // Configuração dos botões permanece a mesma
        Button btnDinheiro = view.findViewById(R.id.btn_dinheiro);
        Button btnCredito = view.findViewById(R.id.btn_credito);
        Button btnDebito = view.findViewById(R.id.btn_debito);
        Button btnPix = view.findViewById(R.id.btn_pix);
        Button btnCancel = view.findViewById(R.id.btn_cancel);

        btnDinheiro.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPaymentMethodSelected("dinheiro");
            }
        });

        btnCredito.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPaymentMethodSelected("credito");
            }
        });

        btnDebito.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPaymentMethodSelected("debito");
            }
        });

        btnPix.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPaymentMethodSelected("pix");
            }
        });

        btnCancel.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPaymentOptionsCancelled();
            }
        });

        return view;
    }
}