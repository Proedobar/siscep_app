package com.proedobar.siscep;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.proedobar.siscep.api.ApiClient;
import com.proedobar.siscep.models.VerifyRequest;
import com.proedobar.siscep.models.VerifyResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VerifyOtpActivity extends AppCompatActivity {
    private EditText[] otpFields = new EditText[6];
    private MaterialButton verifyButton;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_otp);

        // Obtener el user_id del intent
        userId = getIntent().getIntExtra("user_id", -1);
        if (userId == -1) {
            Toast.makeText(this, "Error: No se pudo obtener el ID de usuario", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Inicializar campos OTP
        otpFields[0] = findViewById(R.id.otp1);
        otpFields[1] = findViewById(R.id.otp2);
        otpFields[2] = findViewById(R.id.otp3);
        otpFields[3] = findViewById(R.id.otp4);
        otpFields[4] = findViewById(R.id.otp5);
        otpFields[5] = findViewById(R.id.otp6);

        verifyButton = findViewById(R.id.verifyButton);

        // Configurar la navegación automática entre campos
        for (int i = 0; i < otpFields.length; i++) {
            final int currentIndex = i;
            otpFields[i].addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {
                    if (s.length() == 1 && currentIndex < otpFields.length - 1) {
                        otpFields[currentIndex + 1].requestFocus();
                    }
                }
            });
        }

        // Configurar el botón de verificación
        verifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String otp = getOtpCode();
                if (otp.length() == 6) {
                    verifyOtp(otp);
                } else {
                    Toast.makeText(VerifyOtpActivity.this, 
                        "Por favor, ingrese el código completo", 
                        Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private String getOtpCode() {
        StringBuilder otp = new StringBuilder();
        for (EditText field : otpFields) {
            otp.append(field.getText().toString());
        }
        return otp.toString();
    }

    private void verifyOtp(String code) {
        verifyButton.setEnabled(false);

        VerifyRequest request = new VerifyRequest(code, userId);
        ApiClient.getClient().verifyUser(request).enqueue(new Callback<VerifyResponse>() {
            @Override
            public void onResponse(Call<VerifyResponse> call, Response<VerifyResponse> response) {
                verifyButton.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    VerifyResponse verifyResponse = response.body();
                    if ("success".equals(verifyResponse.getStatus())) {
                        Toast.makeText(VerifyOtpActivity.this, 
                            verifyResponse.getMessage(), 
                            Toast.LENGTH_LONG).show();
                        
                        // Volver a la pantalla de inicio de sesión
                        Intent intent = new Intent(VerifyOtpActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(VerifyOtpActivity.this, 
                            verifyResponse.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(VerifyOtpActivity.this, 
                        "Error al verificar el código", 
                        Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<VerifyResponse> call, Throwable t) {
                verifyButton.setEnabled(true);
                Toast.makeText(VerifyOtpActivity.this, 
                    "Error de conexión: " + t.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            }
        });
    }
} 