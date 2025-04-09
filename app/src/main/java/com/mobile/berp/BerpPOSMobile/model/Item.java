package com.mobile.berp.BerpPOSMobile.model;

import com.google.gson.annotations.SerializedName;

public class Item {
    @SerializedName("type")
    private String type;

    @SerializedName("id")
    private int id;

    @SerializedName("fields")
    private ItemFields fields;

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

    public ItemFields getFields() {
        return fields;
    }

    public void setFields(ItemFields fields) {
        this.fields = fields;
    }
}