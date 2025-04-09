package com.mobile.berp.BerpPOSMobile.model;

import com.google.gson.annotations.SerializedName;

public class LojaFields {
    @SerializedName("FNRLOJA")
    private String fnrLoja;

    @SerializedName("FNOMELOJA")
    private String fNomeLoja;

    public String getFnrLoja() {
        return fnrLoja;
    }

    public void setFnrLoja(String fnrLoja) {
        this.fnrLoja = fnrLoja;
    }

    public String getfNomeLoja() {
        return fNomeLoja;
    }

    public void setfNomeLoja(String fNomeLoja) {
        this.fNomeLoja = fNomeLoja;
    }

    @Override
    public String toString() {
        return "LojaFields{" +
                "fnrLoja='" + fnrLoja + '\'' +
                ", fNomeLoja='" + fNomeLoja + '\'' +
                '}';
    }
}