package br.com.berpsistemas.BerpPOSMobile.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class VersaoResponseModel {
    private List<VersaoItem> result;

    public List<VersaoItem> getResult() {
        return result;
    }
    public void setResult(List<VersaoItem> result) {
        this.result = result;
    }

    public static class VersaoItem {
        @SerializedName("Versao")
        private String versao;

        public String getVersao() {
            return versao;
        }
        public void setVersao(String versao) {
            this.versao = versao;
        }

        @Override
        public String toString() {
            return "VersaoItem{versao='" + versao + "'}";
        }
    }

    @Override
    public String toString() {
        return "VersaoResponseModel{result=" + result + "}";
    }
}
