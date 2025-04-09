package com.mobile.berp.BerpPOSMobile;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.DialogFragment;

import com.mobile.berp.BerpPOSMobile.model.BerpModel;
import com.mobile.berp.BerpPosMobile.R;

import java.util.Objects;

public class OpcoesDialogFragment extends DialogFragment {

    public interface OnOptionSelectedListener {
        void onOptionSelected(String option);
    }

    private OnOptionSelectedListener listener;
    private boolean mesaAtiva;
    private boolean cartaoAtivo;
    private boolean balcaoAtivo;

    public static OpcoesDialogFragment newInstance(boolean mesaAtiva, boolean cartaoAtivo, boolean balcaoAtivo) {
        OpcoesDialogFragment fragment = new OpcoesDialogFragment();
        Bundle args = new Bundle();
        args.putBoolean("mesaAtiva", mesaAtiva);
        args.putBoolean("cartaoAtivo", cartaoAtivo);
        args.putBoolean("balcaoAtivo", balcaoAtivo);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnOptionSelectedListener) {
            listener = (OnOptionSelectedListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnOptionSelectedListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_opcoes, container, false);

         CardView btnMesa = view.findViewById(R.id.cardMesa);
         CardView btnCartao = view.findViewById(R.id.cardCartao);
         CardView btnBalcao = view.findViewById(R.id.cardBalcao);

        // Obter as opções ativas
        if (getArguments() != null) {
            mesaAtiva = getArguments().getBoolean("mesaAtiva");
            cartaoAtivo = getArguments().getBoolean("cartaoAtivo");
            balcaoAtivo = getArguments().getBoolean("balcaoAtivo");
        }

        // Configurar a visibilidade dos botões
        btnMesa.setVisibility(mesaAtiva ? View.VISIBLE : View.GONE);
        btnCartao.setVisibility(cartaoAtivo ? View.VISIBLE : View.GONE);
        btnBalcao.setVisibility((balcaoAtivo && Objects.equals(BerpModel.getSelectedCMD(), "1"))? View.VISIBLE : View.GONE);

        // Configurar os listeners dos botões
        btnMesa.setOnClickListener(v -> {
            if (listener != null) {
                listener.onOptionSelected("Mesa");
            }
            dismiss();
        });

        btnCartao.setOnClickListener(v -> {
            if (listener != null) {
                listener.onOptionSelected("Cartão");
            }
            dismiss();
        });

        btnBalcao.setOnClickListener(v -> {
            if (listener != null) {
                listener.onOptionSelected("Balcão");
            }
            dismiss();
        });

        return view;
    }
}