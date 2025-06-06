package com.proedobar.siscep;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.proedobar.siscep.api.ApiClient;
import com.proedobar.siscep.models.RegisterRequest;
import com.proedobar.siscep.models.RegisterResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {
    private ViewPager2 viewPager;
    private MaterialButton btnNext;
    private ImageButton btnBack;
    private LinearProgressIndicator progressIndicator;
    private RegisterPagerAdapter pagerAdapter;

    // Referencias a los campos de entrada
    private TextInputEditText documentInput;
    private TextInputEditText emailInput;
    private TextInputEditText passwordInput;
    private TextInputEditText confirmPasswordInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Inicializar vistas
        viewPager = findViewById(R.id.viewPager);
        btnNext = findViewById(R.id.btnNext);
        btnBack = findViewById(R.id.btnBack);
        progressIndicator = findViewById(R.id.progressIndicator);

        // Configurar el adaptador
        pagerAdapter = new RegisterPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        // Configurar el botón de retroceso
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (viewPager.getCurrentItem() > 0) {
                    viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
                } else {
                    finish(); // Volver a la actividad anterior
                }
            }
        });

        // Configurar el botón de siguiente
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentItem = viewPager.getCurrentItem();
                
                if (currentItem == 2) { // Última página
                    if (validateAllFields()) {
                        registerUser();
                    }
                } else if (validateCurrentPage(currentItem)) {
                    viewPager.setCurrentItem(currentItem + 1);
                }
            }
        });

        // Configurar el ViewPager2
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                // Actualizar el progreso
                progressIndicator.setProgress((position + 1) * 33);
                
                // Actualizar el texto del botón
                if (position == 2) { // Última página
                    btnNext.setText("Registrar");
                } else {
                    btnNext.setText("Siguiente");
                }

                // Obtener referencias a los campos cuando se necesiten
                updateInputReferences();
            }
        });

        // Deshabilitar el deslizamiento manual del ViewPager2
        viewPager.setUserInputEnabled(false);
    }

    private void updateInputReferences() {
        int currentItem = viewPager.getCurrentItem();
        switch (currentItem) {
            case 0:
                documentInput = findViewById(R.id.documentInput);
                break;
            case 1:
                emailInput = findViewById(R.id.emailInput);
                passwordInput = findViewById(R.id.passwordInput);
                break;
            case 2:
                confirmPasswordInput = findViewById(R.id.confirmPasswordInput);
                break;
        }
    }

    private boolean validateCurrentPage(int page) {
        switch (page) {
            case 0:
                if (documentInput == null || documentInput.getText().toString().trim().isEmpty()) {
                    Toast.makeText(this, "Por favor, ingrese su documento de identidad", Toast.LENGTH_SHORT).show();
                    return false;
                }
                return true;

            case 1:
                if (emailInput == null || emailInput.getText().toString().trim().isEmpty()) {
                    Toast.makeText(this, "Por favor, ingrese su correo electrónico", Toast.LENGTH_SHORT).show();
                    return false;
                }
                if (passwordInput == null || passwordInput.getText().toString().trim().isEmpty()) {
                    Toast.makeText(this, "Por favor, ingrese su contraseña", Toast.LENGTH_SHORT).show();
                    return false;
                }
                return true;

            default:
                return true;
        }
    }

    private boolean validateAllFields() {
        if (!validateCurrentPage(0) || !validateCurrentPage(1)) {
            return false;
        }

        String password = passwordInput.getText().toString().trim();
        String confirmPassword = confirmPasswordInput.getText().toString().trim();

        if (confirmPassword.isEmpty()) {
            Toast.makeText(this, "Por favor, confirme su contraseña", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void registerUser() {
        btnNext.setEnabled(false);

        String ci = documentInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        RegisterRequest request = new RegisterRequest(ci, email, password);
        ApiClient.getClient().registerUser(request).enqueue(new Callback<RegisterResponse>() {
            @Override
            public void onResponse(Call<RegisterResponse> call, Response<RegisterResponse> response) {
                btnNext.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    RegisterResponse registerResponse = response.body();
                    if ("success".equals(registerResponse.getStatus())) {
                        // Mostrar mensaje de éxito
                        Toast.makeText(RegisterActivity.this, 
                            registerResponse.getMessage(), 
                            Toast.LENGTH_LONG).show();

                        // Iniciar la actividad de verificación
                        Intent intent = new Intent(RegisterActivity.this, VerifyOtpActivity.class);
                        intent.putExtra("user_id", registerResponse.getData().getUserId());
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(RegisterActivity.this, 
                            registerResponse.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(RegisterActivity.this, 
                        "Error al registrar el usuario", 
                        Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<RegisterResponse> call, Throwable t) {
                btnNext.setEnabled(true);
                Toast.makeText(RegisterActivity.this, 
                    "Error de conexión: " + t.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            }
        });
    }
} 