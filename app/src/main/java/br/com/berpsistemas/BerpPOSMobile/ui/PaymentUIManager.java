package br.com.berpsistemas.BerpPOSMobile.ui;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import br.com.berpsistemas.BerpPOSMobile.R;


public class PaymentUIManager {

    private static final String TAG = "PaymentUIManager";

    private final Activity activity;
    private Dialog currentDialog;
    private View currentDialogView; // Cache da view atual
    private PaymentUICallback callback;

    // Handler para debounce das mensagens SDK
    private final Handler sdkMessageHandler = new Handler(Looper.getMainLooper());
    private Runnable pendingSdkMessage;
    private static final int SDK_MESSAGE_DEBOUNCE_DELAY = 150; // ms

    // Cache dos últimos parâmetros para evitar recriações desnecessárias
    private String lastMessage = "";
    private String lastMessageType = "";
    private boolean lastShowCancel = false;

    private Dialog pinDialog;
    private TextView tvPinDisplay;
    private StringBuilder pinInput = new StringBuilder();
    private static final int MAX_PIN_LENGTH = 8;
    private static final String PIN_MASK_CHAR = "●";

    // Interface para callbacks das telas
    public interface PaymentUICallback {
        void onOptionSelected(int optionIndex, String optionValue);
        void onInputProvided(String input);
        void onCancelPressed();
        void onConfirmPressed();
        void onPrintReceiptSelected(boolean shouldPrint);
        void onReprintSelected(String receiptType);
    }

    public PaymentUIManager(Activity activity) {
        this.activity = activity;
    }

    public void setCallback(PaymentUICallback callback) {
        this.callback = callback;
    }

    // =================== TELA 1: MENSAGENS DO SDK (OTIMIZADA) ===================

    /**
     * Exibe mensagens informativas do SDK para o cliente/operador
     * Versão otimizada que evita o efeito de "piscar"
     */
    public void showSDKMessage(String message, String messageType, boolean showCancel) {
        Log.d(TAG, "Exibindo mensagem SDK: " + message + " | Tipo: " + messageType);

        // Cancelar mensagem pendente anterior
        if (pendingSdkMessage != null) {
            sdkMessageHandler.removeCallbacks(pendingSdkMessage);
        }

        pendingSdkMessage = () -> {
            activity.runOnUiThread(() -> {
                showSDKMessageInternal(message, messageType, showCancel);
            });
        };

        // Usar debounce para evitar atualizações muito rápidas
        sdkMessageHandler.postDelayed(pendingSdkMessage, SDK_MESSAGE_DEBOUNCE_DELAY);
    }

    /**
     * Método interno que realiza a exibição da mensagem SDK
     */
    private void showSDKMessageInternal(String message, String messageType, boolean showCancel) {
        // Verificar se já existe um dialog SDK e se os parâmetros são os mesmos
        if (currentDialog != null && currentDialog.isShowing() &&
                currentDialogView != null && isSDKMessageDialog(currentDialogView)) {

            // Verificar se o conteúdo é idêntico - se for, não fazer nada
            if (message.equals(lastMessage) &&
                    messageType.equals(lastMessageType) &&
                    showCancel == lastShowCancel) {
                Log.d(TAG, "Mensagem SDK idêntica à anterior - mantendo dialog atual");
                return;
            }

            // Atualizar conteúdo do dialog existente
            Log.d(TAG, "Atualizando conteúdo do dialog SDK existente");
            updateSDKMessageContent(currentDialogView, message, messageType, showCancel);

            // Salvar últimos parâmetros
            lastMessage = message;
            lastMessageType = messageType;
            lastShowCancel = showCancel;
            return;
        }

        // Criar novo dialog apenas se necessário
        createSDKMessageDialog(message, messageType, showCancel);
    }

    /**
     * Verifica se a view é de um dialog SDK Message
     */
    private boolean isSDKMessageDialog(View view) {
        return view.findViewById(R.id.tvMessageTitle) != null &&
                view.findViewById(R.id.tvMessage) != null &&
                view.findViewById(R.id.tvMessageType) != null;
    }

    /**
     * Atualiza o conteúdo de um dialog SDK existente
     */
    private void updateSDKMessageContent(View view, String message, String messageType, boolean showCancel) {
        TextView tvTitle = view.findViewById(R.id.tvMessageTitle);
        TextView tvMessage = view.findViewById(R.id.tvMessage);
        TextView tvMessageType = view.findViewById(R.id.tvMessageType);
        ImageView ivIcon = view.findViewById(R.id.ivMessageIcon);
        ProgressBar progressBar = view.findViewById(R.id.progressBar);
        Button btnCancel = view.findViewById(R.id.btnCancel);

        if (tvMessage != null) tvMessage.setText(message);
        if (tvMessageType != null) tvMessageType.setText(messageType);

        // Configurar ícone e título baseado no tipo de mensagem
        configureMessageDisplay(messageType, tvTitle, ivIcon, progressBar);

        // Atualizar botão cancelar
        if (btnCancel != null) {
            btnCancel.setVisibility(showCancel ? View.VISIBLE : View.GONE);
        }
    }

    /**
     * Cria um novo dialog SDK Message
     */
    private void createSDKMessageDialog(String message, String messageType, boolean showCancel) {
        Log.d(TAG, "Criando novo dialog SDK");

        dismissCurrentDialog();

        currentDialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_sdk_message, null);

        TextView tvTitle = currentDialogView.findViewById(R.id.tvMessageTitle);
        TextView tvMessage = currentDialogView.findViewById(R.id.tvMessage);
        TextView tvMessageType = currentDialogView.findViewById(R.id.tvMessageType);
        ImageView ivIcon = currentDialogView.findViewById(R.id.ivMessageIcon);
        ProgressBar progressBar = currentDialogView.findViewById(R.id.progressBar);
        Button btnCancel = currentDialogView.findViewById(R.id.btnCancel);
        Button btnOk = currentDialogView.findViewById(R.id.btnOk);

        // Configurar conteúdo
        tvMessage.setText(message);
        tvMessageType.setText(messageType);

        // Configurar ícone e título baseado no tipo de mensagem
        configureMessageDisplay(messageType, tvTitle, ivIcon, progressBar);

        // Botão cancelar
        btnCancel.setVisibility(showCancel ? View.VISIBLE : View.GONE);
        btnCancel.setOnClickListener(v -> {
            if (callback != null) callback.onCancelPressed();
            dismissCurrentDialog();
        });

        // Botão OK - CORRIGIDO: mostrar quando showCancel for false
        if (btnOk != null) {
            btnOk.setVisibility(showCancel ? View.GONE : View.VISIBLE);
            btnOk.setOnClickListener(v -> {
                if (callback != null) callback.onConfirmPressed();
                dismissCurrentDialog();
            });
        }

        // Criar e mostrar dialog
        currentDialog = new AlertDialog.Builder(activity)
                .setView(currentDialogView)
                .setCancelable(false)
                .create();

        // CONFIGURAR PARA A TELA SUBIR COM O TECLADO
        if (currentDialog.getWindow() != null) {
            currentDialog.getWindow().setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE |
                            WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN
            );
        }

        currentDialog.show();

        // Salvar últimos parâmetros
        lastMessage = message;
        lastMessageType = messageType;
        lastShowCancel = showCancel;
    }

    // =================== TELA 2: LISTA DE OPÇÕES ===================

    /**
     * Exibe lista de opções para o usuário selecionar
     */
    public void showOptionsList(String title, String[] options, boolean showCancel, int defaultOption) {
        Log.d(TAG, "Exibindo lista de opções: " + title);

        activity.runOnUiThread(() -> {
            dismissCurrentDialog();

            currentDialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_options_list, null);

            TextView tvTitle = currentDialogView.findViewById(R.id.tvOptionsTitle);
            LinearLayout llOptions = currentDialogView.findViewById(R.id.llOptions);
            Button btnCancel = currentDialogView.findViewById(R.id.btnCancel);

            tvTitle.setText(title);

            // Criar botões para cada opção
            for (int i = 0; i < options.length; i++) {
                final int index = i;
                final String option = options[i];

                Button btnOption = new Button(activity);
                btnOption.setText(option);
                btnOption.setTextSize(16);

                // Estilo do botão
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                params.setMargins(0, 8, 0, 8);
                btnOption.setLayoutParams(params);

                // Destacar opção padrão se especificada
                if (index == defaultOption) {
                    btnOption.setBackgroundColor(Color.LTGRAY);
                }

                btnOption.setOnClickListener(v -> {
                    Log.d(TAG, "Opção selecionada: " + index + " - " + option);
                    if (callback != null) callback.onOptionSelected(index, option);
                    dismissCurrentDialog();
                });

                llOptions.addView(btnOption);
            }

            // Botão cancelar
            btnCancel.setVisibility(showCancel ? View.VISIBLE : View.GONE);
            btnCancel.setOnClickListener(v -> {
                if (callback != null) callback.onCancelPressed();
                dismissCurrentDialog();
            });

            currentDialog = new AlertDialog.Builder(activity)
                    .setView(currentDialogView)
                    .setCancelable(false)
                    .create();

            currentDialog.show();

            // Limpar cache do SDK message
            clearSDKMessageCache();
        });
    }

    // =================== TELA 3: INPUT DO USUÁRIO ===================

    /**
     * Solicita entrada de dados do usuário
     */
    // Atualizar o método showInputDialog para usar a configuração de teclado
    public void showInputDialog(String title, String hint, String inputType, boolean showCancel) {
        Log.d(TAG, "Solicitando input do usuário: " + title);

        activity.runOnUiThread(() -> {
            dismissCurrentDialog();

            currentDialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_user_input, null);

            TextView tvTitle = currentDialogView.findViewById(R.id.tvInputTitle);
            EditText etInput = currentDialogView.findViewById(R.id.etInput);
            Button btnConfirm = currentDialogView.findViewById(R.id.btnConfirm);
            Button btnCancel = currentDialogView.findViewById(R.id.btnCancel);

            tvTitle.setText(title);
            etInput.setHint(hint);

            // Configurar tipo de input
            configureInputType(etInput, inputType);

            // Botão confirmar
            btnConfirm.setOnClickListener(v -> {
                String input = etInput.getText().toString().trim();
                if (!input.isEmpty()) {
                    Log.d(TAG, "Input fornecido: " + input);
                    if (callback != null) callback.onInputProvided(input);
                    dismissCurrentDialog();
                } else {
                    etInput.setError("Campo obrigatório");
                }
            });

            // Botão cancelar
            btnCancel.setVisibility(showCancel ? View.VISIBLE : View.GONE);
            btnCancel.setOnClickListener(v -> {
                if (callback != null) callback.onCancelPressed();
                dismissCurrentDialog();
            });

            // Usar o método auxiliar que configura o teclado automaticamente
            currentDialog = createConfiguredDialog(currentDialogView);
            currentDialog.show();

            // Focar no campo de input DEPOIS de mostrar o dialog
            etInput.requestFocus();

            // Forçar a abertura do teclado após um pequeno delay
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (etInput != null) {
                    etInput.requestFocus();
                    android.view.inputmethod.InputMethodManager imm =
                            (android.view.inputmethod.InputMethodManager) activity.getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.showSoftInput(etInput, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
                    }
                }
            }, 100);

            // Limpar cache do SDK message
            clearSDKMessageCache();
        });
    }

    // =================== TELA 4: RESUMO DA OPERAÇÃO ===================

    /**
     * Exibe resumo da operação para confirmação
     */
    public void showOperationSummary(PaymentSummary summary, boolean showCancel) {
        Log.d(TAG, "Exibindo resumo da operação: " + summary.getPaymentType() + " - R$ " + summary.getAmount());

        activity.runOnUiThread(() -> {
            dismissCurrentDialog();

            currentDialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_operation_summary, null);

            TextView tvTitle = currentDialogView.findViewById(R.id.tvSummaryTitle);
            TextView tvPaymentType = currentDialogView.findViewById(R.id.tvPaymentType);
            TextView tvAmount = currentDialogView.findViewById(R.id.tvAmount);
            TextView tvInstallments = currentDialogView.findViewById(R.id.tvInstallments);
            TextView tvCardBrand = currentDialogView.findViewById(R.id.tvCardBrand);
            TextView tvDateTime = currentDialogView.findViewById(R.id.tvDateTime);
            Button btnConfirm = currentDialogView.findViewById(R.id.btnConfirm);
            Button btnCancel = currentDialogView.findViewById(R.id.btnCancel);

            // Preencher informações
            tvTitle.setText("Confirmar Operação");
            tvPaymentType.setText("Tipo: " + summary.getPaymentType());
            tvAmount.setText("Valor: R$ " + summary.getAmount());

            if (summary.getInstallments() > 1) {
                tvInstallments.setVisibility(View.VISIBLE);
                tvInstallments.setText("Parcelas: " + summary.getInstallments() + "x");
            } else {
                tvInstallments.setVisibility(View.GONE);
            }

            if (summary.getCardBrand() != null && !summary.getCardBrand().isEmpty()) {
                tvCardBrand.setVisibility(View.VISIBLE);
                tvCardBrand.setText("Bandeira: " + summary.getCardBrand());
            } else {
                tvCardBrand.setVisibility(View.GONE);
            }

            tvDateTime.setText("Data/Hora: " + summary.getDateTime());

            // Botão confirmar
            btnConfirm.setOnClickListener(v -> {
                Log.d(TAG, "Operação confirmada pelo usuário");
                if (callback != null) callback.onConfirmPressed();
                dismissCurrentDialog();
            });

            // Botão cancelar
            btnCancel.setVisibility(showCancel ? View.VISIBLE : View.GONE);
            btnCancel.setOnClickListener(v -> {
                if (callback != null) callback.onCancelPressed();
                dismissCurrentDialog();
            });

            currentDialog = new AlertDialog.Builder(activity)
                    .setView(currentDialogView)
                    .setCancelable(false)
                    .create();

            currentDialog.show();

            // Limpar cache do SDK message
            clearSDKMessageCache();
        });
    }

    // =================== TELA ESPECIAL: PIX QR CODE ===================
    /**
     * Exibe QR Code PIX para pagamento - versão melhorada
     */
    public void showPixQRCode(String qrCodeData, String amount, boolean showCancel) {
        Log.d(TAG, "Exibindo QR Code PIX: R$ " + amount);

        activity.runOnUiThread(() -> {
            dismissCurrentDialog();

            currentDialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_pix_qrcode, null);

            TextView tvTitle = currentDialogView.findViewById(R.id.tvPixTitle);
            TextView tvAmount = currentDialogView.findViewById(R.id.tvPixAmount);
            ImageView ivQRCode = currentDialogView.findViewById(R.id.ivQRCode);
            TextView tvInstructions = currentDialogView.findViewById(R.id.tvInstructions);

            TextView tvQRCodeText = currentDialogView.findViewById(R.id.tvQRCodeText);
            ProgressBar progressBar = currentDialogView.findViewById(R.id.progressBar);
            Button btnCancel = currentDialogView.findViewById(R.id.btnCancel);
            Button btnCopyCode = currentDialogView.findViewById(R.id.btnCopyCode);

            tvTitle.setText("Pagamento PIX");
            tvAmount.setText("Valor: R$ " + amount);
            tvInstructions.setText("Escaneie o QR Code com seu banco ou carteira digital");

            // Mostrar código PIX como texto sempre
            if (tvQRCodeText != null) {
                tvQRCodeText.setText(qrCodeData);
                tvQRCodeText.setVisibility(View.VISIBLE);
            }

            // Gerar QR Code
            Bitmap qrBitmap = generateQRCode(qrCodeData);
            if (qrBitmap != null) {
                ivQRCode.setImageBitmap(qrBitmap);
                progressBar.setVisibility(View.GONE);
                Log.d(TAG, "QR Code exibido com sucesso");
            } else {
                Log.w(TAG, "Não foi possível gerar QR Code, mostrando apenas texto");
                ivQRCode.setVisibility(View.GONE);
                progressBar.setVisibility(View.GONE);

                // Se não conseguir gerar QR Code, dar mais destaque ao texto
                if (tvQRCodeText != null) {
                    tvQRCodeText.setTextSize(12);
                    tvQRCodeText.setVisibility(View.VISIBLE);
                }

                tvInstructions.setText("Copie o código PIX abaixo e cole no seu banco ou carteira digital");
            }

            // Botão para copiar código PIX
            if (btnCopyCode != null) {
                btnCopyCode.setOnClickListener(v -> {
                    copyToClipboard(qrCodeData);
                    showToast("Código PIX copiado!");
                });
            }

            // Adicionar funcionalidade de copiar ao clicar no QR Code também
            ivQRCode.setOnClickListener(v -> {
                copyToClipboard(qrCodeData);
                showToast("Código PIX copiado!");
            });

            // Botão cancelar
            btnCancel.setVisibility(showCancel ? View.VISIBLE : View.GONE);
            btnCancel.setOnClickListener(v -> {
                if (callback != null) callback.onCancelPressed();
                dismissCurrentDialog();

            });

            currentDialog = new AlertDialog.Builder(activity)
                    .setView(currentDialogView)
                    .setCancelable(false)
                    .create();

            currentDialog.show();

            // Limpar cache do SDK message
            clearSDKMessageCache();
        });
    }

    /**
     * Limpa o cache das mensagens SDK
     */
    private void clearSDKMessageCache() {
        lastMessage = "";
        lastMessageType = "";
        lastShowCancel = false;
    }

    /**
     * Copia texto para a área de transferência
     */
    private void copyToClipboard(String text) {
        try {
            android.content.ClipboardManager clipboard =
                    (android.content.ClipboardManager) activity.getSystemService(android.content.Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText("PIX Code", text);
            clipboard.setPrimaryClip(clip);
        } catch (Exception e) {
            Log.e(TAG, "Erro ao copiar para clipboard", e);
        }
    }

    /**
     * Mostra toast
     */
    private void showToast(String message) {
        activity.runOnUiThread(() ->
                android.widget.Toast.makeText(activity, message, android.widget.Toast.LENGTH_SHORT).show()
        );
    }

    // =================== MÉTODOS AUXILIARES ===================

    private void configureMessageDisplay(String messageType, TextView tvTitle, ImageView ivIcon, ProgressBar progressBar) {
        switch (messageType.toUpperCase()) {
            case "PROCESSING":
            case "AGUARDANDO":
                tvTitle.setText("Processando...");
                progressBar.setVisibility(View.VISIBLE);
                ivIcon.setVisibility(View.GONE);
                break;

            case "INSERT_CARD":
            case "INSIRA_CARTAO":
                tvTitle.setText("Insira o Cartão");
                progressBar.setVisibility(View.GONE);
                ivIcon.setVisibility(View.VISIBLE);
                // ivIcon.setImageResource(R.drawable.ic_insert_card);
                break;

            case "ENTER_PIN":
            case "DIGITE_SENHA":
                tvTitle.setText("Digite a Senha");
                progressBar.setVisibility(View.GONE);
                ivIcon.setVisibility(View.VISIBLE);
                // ivIcon.setImageResource(R.drawable.ic_pin);
                break;

            case "REMOVE_CARD":
            case "RETIRE_CARTAO":
                tvTitle.setText("Retire o Cartão");
                progressBar.setVisibility(View.GONE);
                ivIcon.setVisibility(View.VISIBLE);
                // ivIcon.setImageResource(R.drawable.ic_remove_card);
                break;

            default:
                tvTitle.setText("Informação");
                progressBar.setVisibility(View.GONE);
                ivIcon.setVisibility(View.VISIBLE);
                // ivIcon.setImageResource(R.drawable.ic_info);
                break;
        }
    }

    private void configureInputType(EditText editText, String inputType) {
        switch (inputType.toLowerCase()) {
            case "number":
            case "numeric":
                editText.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
                break;

            case "password":
            case "pin":
                editText.setInputType(android.text.InputType.TYPE_CLASS_NUMBER |
                        android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD);
                break;

            case "email":
                editText.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                break;

            default:
                editText.setInputType(android.text.InputType.TYPE_CLASS_TEXT);
                break;
        }
    }

    /**
     * Gera QR Code usando ZXing
     */
    private Bitmap generateQRCode(String data) {
        try {
            Log.d(TAG, "Gerando QR Code para: " + data);

            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix bitMatrix = writer.encode(data, BarcodeFormat.QR_CODE, 400, 400);

            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }

            Log.d(TAG, "QR Code gerado com sucesso!");
            return bitmap;

        } catch (WriterException e) {
            Log.e(TAG, "Erro ao gerar QR Code", e);
            return null;
        } catch (Exception e) {
            Log.e(TAG, "Erro geral ao gerar QR Code", e);
            return null;
        }
    }

    public void dismissCurrentDialog() {
        // Cancelar mensagens SDK pendentes
        if (pendingSdkMessage != null) {

            sdkMessageHandler.removeCallbacks(pendingSdkMessage);
            pendingSdkMessage = null;
        }

        if (currentDialog != null && currentDialog.isShowing()) {
            currentDialog.dismiss();
            currentDialog = null;
        }
        currentDialogView = null;
        clearSDKMessageCache();
    }

    public void dismissAll() {
        activity.runOnUiThread(() -> dismissCurrentDialog());
        dismissPinDialog();
    }

    // =================== CLASSE DE DADOS ===================

    public static class PaymentSummary {
        private String paymentType;
        private String amount;
        private int installments;
        private String cardBrand;
        private String dateTime;

        public PaymentSummary(String paymentType, String amount) {
            this.paymentType = paymentType;
            this.amount = amount;
            this.installments = 1;
            this.dateTime = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss",
                    java.util.Locale.getDefault()).format(new java.util.Date());
        }

        // Getters e Setters
        public String getPaymentType() { return paymentType; }
        public void setPaymentType(String paymentType) { this.paymentType = paymentType; }

        public String getAmount() { return amount; }
        public void setAmount(String amount) { this.amount = amount; }

        public int getInstallments() { return installments; }
        public void setInstallments(int installments) { this.installments = installments; }

        public String getCardBrand() { return cardBrand; }
        public void setCardBrand(String cardBrand) { this.cardBrand = cardBrand; }

        public String getDateTime() { return dateTime; }
        public void setDateTime(String dateTime) { this.dateTime = dateTime; }
    }

    // =================== MÉTODOS DE PIN (mantidos iguais) ===================

    public void createViewToDisplayPasswordInput() {
        Log.d(TAG, "Criando tela de entrada de PIN");

        activity.runOnUiThread(() -> {
            dismissCurrentDialog();

            View view = LayoutInflater.from(activity).inflate(R.layout.dialog_pin_input, null);

            TextView tvTitle = view.findViewById(R.id.tvPinTitle);
            tvPinDisplay = view.findViewById(R.id.tvPinDisplay);
            TextView tvInstructions = view.findViewById(R.id.tvPinInstructions);
            Button btnCancel = view.findViewById(R.id.btnCancel);

            tvTitle.setText("Digite sua Senha");
            tvInstructions.setText("Digite sua senha no terminal");
            tvPinDisplay.setText("");

            // Limpar entrada anterior
            pinInput.setLength(0);

            // Botão cancelar
            btnCancel.setOnClickListener(v -> {
                if (callback != null) callback.onCancelPressed();
                dismissPinDialog();
            });

            pinDialog = new AlertDialog.Builder(activity)
                    .setView(view)
                    .setCancelable(false)
                    .create();

            // CONFIGURAR PARA A TELA SUBIR COM O TECLADO
            if (pinDialog.getWindow() != null) {
                pinDialog.getWindow().setSoftInputMode(
                        WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE |
                                WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN
                );
            }

            pinDialog.show();

            // Limpar cache do SDK message
            clearSDKMessageCache();
        });
    }

    /**
     * Finaliza a entrada de PIN
     */
    public void finishPasswordInput() {
        Log.d(TAG, "Finalizando entrada de PIN");

        activity.runOnUiThread(() -> {
            if (tvPinDisplay != null) {
                tvPinDisplay.setText("Senha confirmada ✓");
            }

            // Aguardar um pouco antes de fechar para dar feedback visual
            new Handler().postDelayed(() -> {
                dismissPinDialog();
                showSDKMessage("Processando pagamento...", "PROCESSING", true);
            }, 1500);
        });
    }

    /**
     * Trata a inserção de um caractere no PIN
     */
    public void handlePasswordCharacterInput() {
        Log.d(TAG, "Caractere de PIN inserido");

        activity.runOnUiThread(() -> {
            if (pinInput.length() < MAX_PIN_LENGTH && tvPinDisplay != null) {
                pinInput.append("*");
                updatePinDisplay();
            }
        });
    }

    /**
     * Trata a remoção de um caractere do PIN
     */
    public void handlePasswordCharacterRemoved() {
        Log.d(TAG, "Caractere de PIN removido");

        activity.runOnUiThread(() -> {
            if (pinInput.length() > 0 && tvPinDisplay != null) {
                pinInput.deleteCharAt(pinInput.length() - 1);
                updatePinDisplay();
            }
        });
    }

    /**
     * Trata a limpeza completa do PIN
     */
    public void handlePasswordCharacterCleared() {
        Log.d(TAG, "PIN limpo");

        activity.runOnUiThread(() -> {
            pinInput.setLength(0);
            if (tvPinDisplay != null) {
                updatePinDisplay();
            }
        });
    }

    /**
     * Atualiza a exibição visual do PIN
     */
    private void updatePinDisplay() {
        if (tvPinDisplay != null) {
            StringBuilder display = new StringBuilder();
            for (int i = 0; i < pinInput.length(); i++) {
                display.append(PIN_MASK_CHAR).append(" ");
            }
            tvPinDisplay.setText(display.toString().trim());

            // Adicionar cursor piscante se não atingiu o limite
            if (pinInput.length() < MAX_PIN_LENGTH) {
                tvPinDisplay.append(" |");
            }
        }
    }

    /**
     * Exibe dialog perguntando se quer imprimir a via do cliente
     */
    public void showPrintReceiptDialog(String transactionInfo, boolean showCancel) {
        Log.d(TAG, "Exibindo dialog de impressão da via do cliente");

        activity.runOnUiThread(() -> {
            dismissCurrentDialog();

            currentDialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_print_receipt, null);

            TextView tvTitle = currentDialogView.findViewById(R.id.tvPrintTitle);
            TextView tvTransactionInfo = currentDialogView.findViewById(R.id.tvTransactionInfo);
            ImageView ivPrintIcon = currentDialogView.findViewById(R.id.ivPrintIcon);
            Button btnPrintYes = currentDialogView.findViewById(R.id.btnPrintYes);
            Button btnPrintNo = currentDialogView.findViewById(R.id.btnPrintNo);
            Button btnCancel = currentDialogView.findViewById(R.id.btnCancel);

            tvTitle.setText("Imprimir Via do Cliente?");
            tvTransactionInfo.setText(transactionInfo);

            // Configurar ícone de impressora
            if (ivPrintIcon != null) {
                ivPrintIcon.setImageResource(android.R.drawable.ic_menu_agenda);
            }

            // Botão SIM - Imprimir
            btnPrintYes.setOnClickListener(v -> {
                Log.d(TAG, "Usuário escolheu imprimir via do cliente");
                if (callback != null) {
                    callback.onPrintReceiptSelected(true);
                }
                dismissCurrentDialog();
            });

            // Botão NÃO - Não imprimir
            btnPrintNo.setOnClickListener(v -> {
                Log.d(TAG, "Usuário escolheu não imprimir via do cliente");
                if (callback != null) {
                    callback.onPrintReceiptSelected(false);
                }
                dismissCurrentDialog();
            });

            // Botão Cancelar (opcional)
            btnCancel.setVisibility(showCancel ? View.VISIBLE : View.GONE);
            btnCancel.setOnClickListener(v -> {
                Log.d(TAG, "Usuário cancelou dialog de impressão");
                if (callback != null) {
                    callback.onCancelPressed();
                }
                dismissCurrentDialog();
            });

            currentDialog = new AlertDialog.Builder(activity)
                    .setView(currentDialogView)
                    .setCancelable(false)
                    .create();

            currentDialog.show();

            // Limpar cache do SDK message
            clearSDKMessageCache();
        });
    }

    /**
     * Versão simplificada usando AlertDialog nativo do Android
     */
    public void showPrintReceiptDialogSimple(String transactionInfo) {
        Log.d(TAG, "Exibindo dialog simples de impressão");

        activity.runOnUiThread(() -> {
            dismissCurrentDialog();

            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle("🖨️ Imprimir Via do Cliente?");
            builder.setMessage(transactionInfo + "\n\nDeseja imprimir a via do cliente?");
            builder.setIcon(android.R.drawable.ic_menu_agenda);

            // Botão SIM
            builder.setPositiveButton("✅ SIM", (dialog, which) -> {
                Log.d(TAG, "Usuário escolheu imprimir via do cliente");
                if (callback != null) {
                    callback.onPrintReceiptSelected(true);
                }
                dialog.dismiss();
            });

            // Botão NÃO
            builder.setNegativeButton("❌ NÃO", (dialog, which) -> {
                Log.d(TAG, "Usuário escolheu não imprimir via do cliente");
                if (callback != null) {
                    callback.onPrintReceiptSelected(false);
                }
                dialog.dismiss();
            });

            // Botão CANCELAR (neutro)
            builder.setNeutralButton("Cancelar", (dialog, which) -> {
                Log.d(TAG, "Usuário cancelou dialog de impressão");
                if (callback != null) {
                    callback.onCancelPressed();
                }
                dialog.dismiss();
            });

            builder.setCancelable(false);
            currentDialog = builder.create();
            currentDialog.show();

            // Limpar cache do SDK message
            clearSDKMessageCache();
        });
    }

    /**
     * Fecha o dialog de PIN
     */
    private void dismissPinDialog() {
        if (pinDialog != null && pinDialog.isShowing()) {
            pinDialog.dismiss();
            pinDialog = null;
        }
        tvPinDisplay = null;
        pinInput.setLength(0);
    }

    /**
     * Exibe dialog para escolher qual via reimprimir
     */
    public void showReprintDialog(String transactionInfo) {
        Log.d(TAG, "Exibindo dialog de reimpressão");

        activity.runOnUiThread(() -> {
            dismissCurrentDialog();

            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle("🖨️ Reimprimir Comprovante");
            builder.setMessage(transactionInfo + "\n\nQual via deseja reimprimir?");
            builder.setIcon(android.R.drawable.ic_menu_agenda);

            // Via do Cliente
            builder.setPositiveButton("👤 Via do Cliente", (dialog, which) -> {
                Log.d(TAG, "Usuário escolheu reimprimir via do cliente");
                if (callback != null) {
                    callback.onReprintSelected("CUSTOMER");
                }
                dialog.dismiss();
            });

            // Via do Estabelecimento
            builder.setNegativeButton("🏪 Via do Estabelecimento", (dialog, which) -> {
                Log.d(TAG, "Usuário escolheu reimprimir via do estabelecimento");
                if (callback != null) {
                    callback.onReprintSelected("ESTABLISHMENT");
                }
                dialog.dismiss();
            });

            // Cancelar
            builder.setNeutralButton("❌ Cancelar", (dialog, which) -> {
                Log.d(TAG, "Usuário cancelou reimpressão");
                if (callback != null) {
                    callback.onCancelPressed();
                }
                dialog.dismiss();
            });

            builder.setCancelable(false);
            currentDialog = builder.create();
            currentDialog.show();

            // Limpar cache do SDK message
            clearSDKMessageCache();
        });
    }


    /**
     * Configura o dialog para ajustar quando o teclado aparecer
     */
    private void configureDialogForKeyboard(Dialog dialog) {
        if (dialog != null && dialog.getWindow() != null) {
            dialog.getWindow().setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE |
                            WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN
            );
        }
    }

    /**
     * Método auxiliar para criar dialogs com configuração automática de teclado
     */
    private AlertDialog createConfiguredDialog(View view) {
        AlertDialog dialog = new AlertDialog.Builder(activity)
                .setView(view)
                .setCancelable(false)
                .create();

        configureDialogForKeyboard(dialog);
        return dialog;
    }

    /**
     * Dialog personalizado para reimpressão com layout customizado
     */
    public void showReprintDialogCustom(String transactionInfo) {
        Log.d(TAG, "Exibindo dialog customizado de reimpressão");

        activity.runOnUiThread(() -> {
            dismissCurrentDialog();

            currentDialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_reprint_options, null);

            TextView tvTitle = currentDialogView.findViewById(R.id.tvReprintTitle);
            TextView tvTransactionInfo = currentDialogView.findViewById(R.id.tvTransactionInfo);
            Button btnCustomer = currentDialogView.findViewById(R.id.btnReprintCustomer);
            Button btnEstablishment = currentDialogView.findViewById(R.id.btnReprintEstablishment);
            Button btnCancel = currentDialogView.findViewById(R.id.btnCancel);

            tvTitle.setText("Reimprimir Comprovante");
            tvTransactionInfo.setText(transactionInfo);

            // Via do Cliente
            btnCustomer.setOnClickListener(v -> {
                Log.d(TAG, "Selecionada reimpressão da via do cliente");
                if (callback != null) {
                    callback.onReprintSelected("CUSTOMER");
                }
                dismissCurrentDialog();
            });

            // Via do Estabelecimento
            btnEstablishment.setOnClickListener(v -> {
                Log.d(TAG, "Selecionada reimpressão da via do estabelecimento");
                if (callback != null) {
                    callback.onReprintSelected("ESTABLISHMENT");
                }
                dismissCurrentDialog();
            });

            // Cancelar
            btnCancel.setOnClickListener(v -> {
                if (callback != null) {
                    callback.onCancelPressed();
                }
                dismissCurrentDialog();
            });

            currentDialog = new AlertDialog.Builder(activity)
                    .setView(currentDialogView)
                    .setCancelable(false)
                    .create();

            currentDialog.show();

            // Limpar cache do SDK message
            clearSDKMessageCache();
        });
    }
}