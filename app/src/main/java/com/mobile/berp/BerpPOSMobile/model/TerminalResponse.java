package com.mobile.berp.BerpPOSMobile.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class TerminalResponse {
    @SerializedName("result")
    private List<Terminal> result;

    public List<Terminal> getResult() {
        return result;
    }

    public void setResult(List<Terminal> result) {
        this.result = result;
    }
}