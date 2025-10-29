package br.com.berpsistemas.BerpPOSMobile.Printer;

public interface PrinterStatusCallback {
    void onError(Exception e);
    void onConnected();
    void onSucess(String msg);
    void onDisconnected();
}



