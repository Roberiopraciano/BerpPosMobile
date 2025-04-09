package com.mobile.berp.BerpPOSMobile.model;
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class MesaResult {
    @SerializedName("result")
    private List<List<Conta>> result;

    public List<List<Conta>> getResult() {
        return result;
    }

    public void setResult(List<List<Conta>> result) {
        this.result = result;
    }

}