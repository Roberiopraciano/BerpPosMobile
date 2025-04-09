package com.mobile.berp.BerpPOSMobile.Printer;

import android.content.Context;

public class PrinterServiceFactory {

    public static IPrinterService getPrinterService(Context context) {
        // Aqui você pode escolher qual implementação usar com base em configurações ou flavor
        // Exemplo: se estiver usando PosDigital, retorne a implementação PosDigitalPrinterService.
        return new PosDigitalPrinterService();
    }
}
