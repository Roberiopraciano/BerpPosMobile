package br.com.berpsistemas.BerpPOSMobile;



import android.content.Context;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPag;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagAppIdentification;

public class PlugPagProvider {
    private static PlugPag instance;
    public String tag;

    public static synchronized PlugPag getInstance(Context context) {
        if (instance == null) {
            instance = new PlugPag(context.getApplicationContext());

        }
        return instance;
    }

    public boolean hasTag(String o) {
        if (this.tag.equals(o))
            return true;
        return false;
    }
}