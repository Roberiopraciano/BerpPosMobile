package br.com.berpsistemas.BerpPOSMobile.model;

import java.util.List;

public class StatusMesaResult {

    /** Aqui fica o array de strings escapadas */
    private List<String> result;

    public List<String> getResult() {
        return result;
    }

    public void setResult(List<String> result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return "StatusMesaResult{" +
                "result=" + result +
                '}';
    }
}
