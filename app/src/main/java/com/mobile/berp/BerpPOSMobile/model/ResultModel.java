package com.mobile.berp.BerpPOSMobile.model;
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ResultModel {
    @SerializedName("result")
    private List<Integer> result;

    // Getter
    public List<Integer> getResult() {
        return result;
    }

    // Setter
    public void setResult(List<Integer> result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return "ResultModel{" +
                "result=" + result +
                '}';
    }
}
