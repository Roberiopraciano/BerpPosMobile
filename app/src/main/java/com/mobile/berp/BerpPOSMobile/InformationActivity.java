package com.mobile.berp.BerpPOSMobile;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.TextView;

import com.mobile.berp.BerpPosMobile.R;

import java.util.Calendar;


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
