package br.com.berpsistemas.BerpPOSMobile.model;

public interface LoginCallback {
    void onSuccess();
    void onFailure(String error);
}
