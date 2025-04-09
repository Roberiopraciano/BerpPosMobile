package com.mobile.berp.BerpPOSMobile.Printer;

public interface IPrinter {
    void init() throws Exception;
    void setGray(int level);
    void setFontFormat(PrinterFontFormat fontFormat);
    void addText(PrinterAlignMode alignMode, String text);
    void print(PrinterCallback callback);
}
