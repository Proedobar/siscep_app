package com.proedobar.siscep;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

public class LoadingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        // Simular un tiempo de carga mínimo de 1 segundo
        new Handler().postDelayed(() -> {
            // Iniciar el menú principal
            Intent intent = new Intent(LoadingActivity.this, MainMenuActivity.class);
            startActivity(intent);
            finish(); // Cerrar esta actividad para que no se pueda volver atrás
        }, 1000);
    }
} 