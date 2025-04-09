package com.mobile.berp.BerpPOSMobile.Printer;

import android.content.Context;

public class PrinterFactory {

    public static IPrinter getPrinterInstance(Context context) {
        // Aqui você pode decidir qual implementação retornar de acordo com a configuração
        // ou flavor. Exemplo:
        // if (BuildConfig.PRINTER_IMPL.equals("PosDigital")) {
        //     return new PosDigitalPrinter(context);
        // }
        // Retorne null se não houver implementação disponível.
        return null;
    }
}
