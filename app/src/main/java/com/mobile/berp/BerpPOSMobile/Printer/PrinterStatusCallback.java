package com.mobile.berp.BerpPOSMobile.Printer;

public interface PrinterStatusCallback {
    void onError(Exception e);
    void onConnected();
    void onDisconnected();
}
