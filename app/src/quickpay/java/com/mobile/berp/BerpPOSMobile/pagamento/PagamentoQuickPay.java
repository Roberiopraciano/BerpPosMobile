package com.mobile.berp.BerpPOSMobile.pagamento;
import android.util.Log;

public class PagamentoStone implements IPagamento {

    @Override
    public boolean iniciarPagamento(int valor) {
        Log.d("PagamentoStone", "Iniciando pagamento no Stone: R$" + (valor / 100.0));
        // Aqui você chama o SDK da Stone para iniciar a transação
        return true; // Simulação de sucesso
    }

    @Override
    public boolean cancelarPagamento(String transacaoId) {
        Log.d("PagamentoStone", "Cancelando pagamento no Stone, ID: " + transacaoId);
        // Aqui você chama o SDK da Stone para cancelar a transação
        return true; // Simulação de sucesso
    }

    @Override
    public String obterStatusPagamento() {
        // Aqui você chama o SDK da Stone para obter o status da transação
        return "Aprovado"; // Simulação
    }
}
