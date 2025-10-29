package br.com.berpsistemas.BerpPOSMobile;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.TextView;



import java.util.Calendar;

import br.com.berpsistemas.BerpPOSMobile.R;


public class InformationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_information);

        TextView tvanosistema = findViewById(R.id.tvanosistema);

        Calendar calOne = Calendar.getInstance();

        tvanosistema.setText(String.format("2005-%d Berp Sistemas", calOne.get(Calendar.YEAR)));

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if ((keyCode == KeyEvent.KEYCODE_BACK))
        {
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }
}
