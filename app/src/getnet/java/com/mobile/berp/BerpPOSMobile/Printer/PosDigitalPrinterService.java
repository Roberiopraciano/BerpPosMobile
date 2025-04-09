package com.mobile.berp.BerpPOSMobile.Printer;

import android.content.Context;

import com.getnet.posdigital.PosDigital;
import com.getnet.posdigital.printer.AlignMode;
import com.getnet.posdigital.printer.FontFormat;
import com.getnet.posdigital.printer.IPrinterCallback;
import com.getnet.posdigital.printer.PrinterStatus;

public class PosDigitalPrinterService   implements IPrinterService {
    @Override
    public void register(Context context, PrinterStatusCallback callback) {
        PosDigital.register(context, new PosDigital.BindCallback() {
            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }

            @Override
            public void onConnected() {
                callback.onConnected();
            }

            @Override
            public void onDisconnected() {
                callback.onDisconnected();
            }
        }
        );
    }


}
