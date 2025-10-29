package br.com.berpsistemas.BerpPOSMobile.Printer;

import android.content.Context;

import br.com.berpsistemas.BerpPOSMobile.model.ContaFields;

public class PosDigitalPrinterService   implements IPrinterService {
    @Override
    public void register(Context context, PrinterStatusCallback callback) {
//        PosDigital.register(context, new PosDigital.BindCallback() {
//            @Override
//            public void onError(Exception e) {
//                callback.onError(e);
//            }
//
//            @Override
//            public void onConnected() {
//                callback.onConnected();
//            }
//
//            @Override
//            public void onDisconnected() {
//                callback.onDisconnected();
//            }
//        }
       // );
    }

    @Override
    public void print(ContaFields conta) {

    }


}
