package br.com.berpsistemas.BerpPOSMobile.application;


import android.app.Application;
import br.com.berpsistemas.BerpPOSMobile.database.TransactionDatabaseHelperV2;
import br.com.berpsistemas.BerpPOSMobile.managers.IPosAuthManager;
import br.com.berpsistemas.BerpPOSMobile.managers.PosAuthManager; // A implementação específica do flavor
import br.com.berpsistemas.BerpPOSMobile.model.BerpModel;

public class MyBerpApplication extends Application {
    private IPosAuthManager posAuthManager;
    private static TransactionDatabaseHelperV2 dbHelper;

    @Override
    public void onCreate() {
        super.onCreate();

        // Inicializa o banco de dados como um singleton
        dbHelper = new TransactionDatabaseHelperV2(getApplicationContext());

        // Inicializa o manager de autenticação do POS
        posAuthManager = new PosAuthManager();

        // Inicializa o modelo de dados Berp
        BerpModel.inicializar();
    }

    public IPosAuthManager getPosAuthManager() {
        return posAuthManager;
    }

    public static synchronized TransactionDatabaseHelperV2 getDbHelper() {
        return dbHelper;
    }
}
