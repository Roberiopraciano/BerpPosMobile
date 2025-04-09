package com.mobile.berp.BerpPOSMobile;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.mobile.berp.BerpPosMobile.BuildConfig;
import com.mobile.berp.BerpPosMobile.R;

public class SplashActivity extends AppCompatActivity {
    private TextView txtVersion, txtFlavor;
    private boolean updateCheckCompleted = false;
    private boolean animationCompleted = false;
    private boolean isUpdating = false; // Track if the update is in progress

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        txtVersion = findViewById(R.id.txtVersion);
        txtFlavor = findViewById(R.id.txtFlavor);

        // Display version and flavor information
        txtVersion.setText("Version: " + BuildConfig.VERSION_NAME);
        txtFlavor.setText("Flavor: " + BuildConfig.FLAVOR);

        // Verificar se estamos no flavor "celular" antes de chamar a UpdateClass
        if (BuildConfig.FLAVOR.equals("celular")) {
            UpdateClass.checkForUpdate(this, this::onUpdateCheckCompleted);
        } else {
            updateCheckCompleted = true; // Se não for "celular", seguir direto
        }

        // Play fade-in animation
        LinearLayout splashLayout = findViewById(R.id.splashLayout);
        Animation fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        splashLayout.startAnimation(fadeInAnimation);

        // Listener to track when the animation ends
        fadeInAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                animationCompleted = true;
                proceedToNextScreen(); // Check if we can proceed
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
    }

    // Callback when update check is completed
    private void onUpdateCheckCompleted(boolean updateRequired) {
        updateCheckCompleted = true;
        if (updateRequired) {
            showUpdateDialog();
        } else {
            proceedToNextScreen();
        }
    }

    // Show dialog asking if the user wants to update
    private void showUpdateDialog() {
        runOnUiThread(() -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(SplashActivity.this);
            builder.setTitle("Nova versão disponível");
            builder.setMessage("Há uma nova versão do aplicativo disponível. Deseja atualizar agora?");
            builder.setPositiveButton("Atualizar", (dialog, which) -> {
                isUpdating = true;
                if (BuildConfig.FLAVOR.equals("celular")) {
                    UpdateClass.startUpdate(this, success -> {
                        if (!success) {
                            restartApp();
                        }
                    });
                }
            });
            builder.setNegativeButton("Agora não", (dialog, which) -> {
                isUpdating = false;
                proceedToNextScreen();
            });
            builder.setCancelable(false);
            builder.show();
        });
    }

    // Proceed to the main screen
    private void proceedToNextScreen() {
        if (updateCheckCompleted && animationCompleted && !isUpdating) {
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    // Restart the app if update fails
    private void restartApp() {
        Intent intent = getBaseContext().getPackageManager()
                .getLaunchIntentForPackage(getBaseContext().getPackageName());
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
        finish();
    }
}