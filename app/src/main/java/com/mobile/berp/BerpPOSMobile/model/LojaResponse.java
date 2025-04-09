package com.mobile.berp.BerpPOSMobile.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class LojaResponse {
    @SerializedName("result")
    private List<List<Loja>> result;

    public List<List<Loja>> getResult() {
        return result;
    }

    public void setResult(List<List<Loja>> result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return "LojaResponse{" +
                "result=" + result +
                '}';
    }
}