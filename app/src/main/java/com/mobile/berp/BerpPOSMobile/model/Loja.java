package com.mobile.berp.BerpPOSMobile.model;

import com.google.gson.annotations.SerializedName;

public class Loja {
    @SerializedName("type")
    private String type;

    @SerializedName("id")
    private int id;

    @SerializedName("fields")
    private LojaFields fields;

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

    public LojaFields getFields() {
        return fields;
    }

    public void setFields(LojaFields fields) {
        this.fields = fields;
    }

    @Override
    public String toString() {
        return "Loja{" +
                "type='" + type + '\'' +
                ", id=" + id +
                ", fields=" + fields +
                '}';
    }
}