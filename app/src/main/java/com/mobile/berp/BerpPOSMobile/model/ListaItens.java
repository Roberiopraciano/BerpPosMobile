package com.mobile.berp.BerpPOSMobile.model;

import com.google.gson.annotations.SerializedName;

public class ListaItens {
    @SerializedName("type")
    private String type;

    @SerializedName("id")
    private int id;

    @SerializedName("fields")
    private ItensFields fields;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public ItensFields getFields() {
        return fields;
    }

    public void setFields(ItensFields fields) {
        this.fields = fields;
    }
}