package com.proedobar.siscep;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.proedobar.siscep.api.ApiClient;
import com.proedobar.siscep.models.LoginResponse;
import com.proedobar.siscep.session.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private TextInputEditText emailInput;
    private TextInputEditText passwordInput;
    private MaterialButton loginButton;
    private MaterialButton createAccountButton;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Inicializar SessionManager
        sessionManager = SessionManager.getInstance(this);
        
        // Si ya hay una sesión activa, ir directamente al menú principal
        if (sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, MainMenuActivity.class));
            finish();
            return;
        }
        
        setContentView(R.layout.activity_main);

        // Inicializar vistas
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginButton = findViewById(R.id.loginButton);
        createAccountButton = findViewById(R.id.createAccountButton);

        // Configurar click listener para el botón de inicio de sesión
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailInput.getText().toString().trim();
                String password = passwordInput.getText().toString().trim();

                // Validar campos
                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Deshabilitar el botón mientras se procesa
                loginButton.setEnabled(false);

                // Realizar la llamada a la API
                ApiClient.getClient().login(email, password).enqueue(new Callback<LoginResponse>() {
                    @Override
                    public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                        loginButton.setEnabled(true);

                        if (response.isSuccessful() && response.body() != null) {
                            LoginResponse loginResponse = response.body();
                            if ("success".equals(loginResponse.getStatus())) {
                                // Guardar datos de sesión
                                sessionManager.saveUserSession(loginResponse);
                                
                                // Iniciar la pantalla de carga
                                Intent intent = new Intent(MainActivity.this, LoadingActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        } else {
                            Toast.makeText(MainActivity.this, 
                                "Error en las credenciales", 
                                Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<LoginResponse> call, Throwable t) {
                        loginButton.setEnabled(true);
                        Toast.makeText(MainActivity.this, 
                            "Error de conexión: " + t.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        // Configurar click listener para el botón de crear cuenta
        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }
}