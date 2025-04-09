package com.mobile.berp.BerpPOSMobile.pagamento;

import android.app.Activity;
import android.content.Context;


import com.mobile.berp.BerpPosMobile.BuildConfig;
public class PagamentoFactory {
    public static IPagamento criarPagamento(Context context) {
        String posModel = BuildConfig.POS_MODEL; // Ex.: "getnet", "pagseguro", "stone", etc.
        switch (posModel) {
            case "getnet":
                return new PagamentoPos((Activity) context);
            case "pagseguro":
              //  return instantiatePagamento("com.mobile.berp.BerpPOSMobile.pagamento.PagamentoPagSeguro", context);
                return new PagamentoPos((Activity) context);
            case "stone":
                return new PagamentoPos((Activity) context);
            case "cielo":
                return new PagamentoPos((Activity) context);
            case "quickpay":
                return new PagamentoPos((Activity) context);
            case "rede":
                return new PagamentoPos((Activity) context);

            default:
                throw new IllegalStateException("Nenhuma implementação de pagamento para: " + posModel);
        }
    }

    // Método auxiliar que tenta instanciar a classe usando um construtor que aceite Activity ou, se não encontrar, o padrão
    private static IPagamento instantiatePagamento(String className, Context context) {
        try {
            Class<?> clazz = Class.forName(className);
            try {
                // Tenta usar um construtor que receba Activity (pois, por exemplo, PagamentoPagSeguro precisa disso)
                return (IPagamento) clazz.getDeclaredConstructor(Activity.class).newInstance((Activity) context);
            } catch (NoSuchMethodException e) {
                // Se não existir esse construtor, tenta o construtor padrão
                return (IPagamento) clazz.getDeclaredConstructor().newInstance();
            }
        } catch (Exception e) {
            throw new IllegalStateException("Implementação de pagamento não disponível: " + className, e);
        }
    }
}