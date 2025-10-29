package br.com.berpsistemas.BerpPOSMobile.Printer;


import android.content.Context;

import br.com.berpsistemas.BerpPOSMobile.model.ContaFields;

public interface IPrinterService {
    void print(ContaFields conta);

    void register(Context context, PrinterStatusCallback printerStatusCallback);
}
