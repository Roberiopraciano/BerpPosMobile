package com.mobile.berp.BerpPOSMobile.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ItensFields {
    @SerializedName("FOwnsObjects")
    private boolean ownsObjects;

    @SerializedName("FListHelper")
    private List<Item> listHelper;

    public boolean isOwnsObjects() {
        return ownsObjects;
    }

    public void setOwnsObjects(boolean ownsObjects) {
        this.ownsObjects = ownsObjects;
    }

    public List<Item> getListHelper() {
        return listHelper;
    }

    public void setListHelper(List<Item> listHelper) {
        this.listHelper = listHelper;
    }
}