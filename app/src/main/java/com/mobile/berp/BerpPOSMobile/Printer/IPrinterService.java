package com.mobile.berp.BerpPOSMobile.Printer;


import android.content.Context;

import com.mobile.berp.BerpPOSMobile.model.ContaFields;

public interface IPrinterService {
    void register(Context context, PrinterStatusCallback callback);
    void print(ContaFields conta);
}
