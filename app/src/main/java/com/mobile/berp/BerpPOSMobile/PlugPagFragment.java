package com.mobile.berp.BerpPOSMobile;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mobile.berp.BerpPosMobile.R;

public class PlugPagFragment extends Fragment {

    private TextView txtPlugPagLog;

    // Receiver para escutar os eventos do PagSeguro
    private final BroadcastReceiver plugPagReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("plugpag_message");
            if (message != null && txtPlugPagLog != null) {
                txtPlugPagLog.setText(message + "\n");
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_plugpag, container, false);
        txtPlugPagLog = view.findViewById(R.id.txtPlugPagLog);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Registra o receiver para a action definida
        IntentFilter filter = new IntentFilter("com.myapp.PAGSEGURO_EVENT");
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(plugPagReceiver, filter);
    }

    @Override
    public void onPause() {
        super.onPause();
        // Desregistra o receiver
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(plugPagReceiver);
    }
}