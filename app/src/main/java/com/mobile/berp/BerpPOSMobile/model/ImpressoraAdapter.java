package com.mobile.berp.BerpPOSMobile.model;

import android.app.Activity;
import android.content.Context;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;


import com.mobile.berp.BerpPosMobile.R;

import java.util.Vector;

public class ImpressoraAdapter extends ArrayAdapter<Impressora> {

    private Vector<Impressora> listImp;
    private Context context;
    private int resourceId;
    private LayoutInflater inflater;

    public ImpressoraAdapter( Context context, int resourceId, Vector<Impressora> list) {
        super(context, resourceId, list);

        this.listImp = list;
        this.context = context;
        this.resourceId = resourceId;

        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return listImp.size();
    }

    public Impressora getItem(int index) {
        return listImp.get(index);
    }

    @NonNull
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(resourceId, null);
        }

        TextView txtImpress = convertView.findViewById(R.id.txtImpress);

        String nomeimp = listImp.get(position).getNome();

        txtImpress.setText(nomeimp);

        return convertView;
    }
}
