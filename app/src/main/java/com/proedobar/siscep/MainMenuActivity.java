package com.proedobar.siscep;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;
import com.proedobar.siscep.api.ApiClient;
import com.proedobar.siscep.models.VerificacionResponse;
import com.proedobar.siscep.models.FirmanteResponse;
import com.proedobar.siscep.models.NominasResponse;
import com.proedobar.siscep.models.DetallesNominaResponse;
import com.proedobar.siscep.session.SessionManager;
import com.proedobar.siscep.adapters.DetallesNominaAdapter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainMenuActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private SessionManager sessionManager;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private FloatingActionButton menuFab;
    
    // Referencias a las páginas
    private View homePage;
    private View constanciasPage;
    private View recibosPage;
    private View verificadorPage;
    private View settingsPage;

    // Variables para el lector QR
    private PreviewView previewView;
    private TextView qrResultTextView;
    private ExecutorService cameraExecutor;
    private static final int PERMISSION_REQUEST_CAMERA = 1001;

    private ProcessCameraProvider cameraProvider;
    private MaterialButton reiniciarEscaneoButton;

    // Inicializar vistas de la página de constancias
    private TextView constanciaPreviewText;

    // Referencias a las vistas de datos del firmante
    private TextView firmanteTipoText;
    private TextView firmanteNombreText;
    private TextView firmanteResolucionText;
    private TextView firmanteFechaResolucionText;
    private TextView firmanteGacetaText;
    private TextView firmanteFechaGacetaText;
    private TextView firmanteActivoText;

    private MaterialButton descargarConstanciaButton;

    // Referencias para la página de recibos
    private AutoCompleteTextView spinnerNomina;
    private AutoCompleteTextView spinnerMes;
    private AutoCompleteTextView spinnerAnio;
    private MaterialButton btnBuscarRecibo;

    // Mapa para almacenar la relación mes-número
    private Map<String, Integer> mesesMap = new LinkedHashMap<>();
    // Mapa para almacenar los años
    private Map<String, Integer> aniosMap = new LinkedHashMap<>();

    // Modificar el método configurarSpinnerNominas para guardar la lista de nóminas
    private List<NominasResponse.NominaData> nominasActuales;

    private RecyclerView detallesRecyclerView;
    private DetallesNominaAdapter detallesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        // Inicializar SessionManager
        sessionManager = SessionManager.getInstance(this);

        // Verificar si hay una sesión activa
        if (!sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        // Configurar Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // Inicializar vistas
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        menuFab = findViewById(R.id.menuFab);

        // Inicializar páginas
        homePage = findViewById(R.id.home_page);
        constanciasPage = findViewById(R.id.constancias_page);
        recibosPage = findViewById(R.id.recibos_page);
        verificadorPage = findViewById(R.id.verificador_page);
        settingsPage = findViewById(R.id.settings_page);

        // Inicializar componentes del lector QR
        previewView = findViewById(R.id.previewView);
        qrResultTextView = findViewById(R.id.qrResultTextView);
        cameraExecutor = Executors.newSingleThreadExecutor();

        // Inicializar el botón de reiniciar escaneo
        reiniciarEscaneoButton = findViewById(R.id.reiniciarEscaneoButton);
        reiniciarEscaneoButton.setOnClickListener(v -> {
            reiniciarEscaneoButton.setVisibility(View.GONE);
            qrResultTextView.setText("Escanea un código QR");
            startCamera();
        });

        // Inicializar vistas de la página de constancias
        constanciaPreviewText = findViewById(R.id.constanciaPreviewText);

        // Inicializar vistas de datos del firmante
        firmanteTipoText = findViewById(R.id.firmanteTipoText);
        firmanteNombreText = findViewById(R.id.firmanteNombreText);
        firmanteResolucionText = findViewById(R.id.firmanteResolucionText);
        firmanteFechaResolucionText = findViewById(R.id.firmanteFechaResolucionText);
        firmanteGacetaText = findViewById(R.id.firmanteGacetaText);
        firmanteFechaGacetaText = findViewById(R.id.firmanteFechaGacetaText);
        firmanteActivoText = findViewById(R.id.firmanteActivoText);

        // Inicializar botón de descarga
        descargarConstanciaButton = findViewById(R.id.descargarConstanciaButton);
        descargarConstanciaButton.setOnClickListener(v -> descargarConstancia());

        // Inicializar vistas de la página de recibos
        spinnerNomina = findViewById(R.id.spinnerNomina);
        spinnerMes = findViewById(R.id.spinnerMes);
        spinnerAnio = findViewById(R.id.spinnerAnio);
        btnBuscarRecibo = findViewById(R.id.btnBuscarRecibo);

        // Configurar los spinners
        configurarSpinnerMeses();
        configurarSpinnerAnios();

        // Configurar Navigation Drawer
        ActionBarDrawerToggle toggle;
        toggle = new ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Configurar listener para el Navigation View
        navigationView.setNavigationItemSelectedListener(this);

        // Configurar FAB para abrir drawer
        menuFab.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        // Mostrar información del usuario
        displayUserInfo();

        // Mostrar la página inicial (Home)
        showPage(homePage);

        // Inicializar RecyclerView
        detallesRecyclerView = findViewById(R.id.detallesRecyclerView);
        detallesRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Configurar el botón de búsqueda
        btnBuscarRecibo.setOnClickListener(v -> {
            buscarDetallesNomina();
        });
    }

    private void displayUserInfo() {
        TextView userName = findViewById(R.id.userName);
        TextView userCargo = findViewById(R.id.userCargo);
        TextView userCedula = findViewById(R.id.userCedula);
        TextView fechaIngreso = findViewById(R.id.fechaIngreso);

        // Obtener datos del usuario desde SessionManager
        userName.setText(sessionManager.getNombre());
        userCargo.setText(sessionManager.getCargo());
        userCedula.setText("C.I.: " + sessionManager.getCedula());
        
        // Formatear fecha y calcular antigüedad
        String fechaOriginal = sessionManager.getFechaIngreso();
        String fechaFormateada = formatearFecha(fechaOriginal);
        String antiguedad = calcularAntiguedad(fechaOriginal);
        fechaIngreso.setText(fechaFormateada + " (" + antiguedad + ")");
    }

    private String calcularAntiguedad(String fechaIngreso) {
        try {
            String[] partes = fechaIngreso.split("-");
            if (partes.length == 3) {
                int añoIngreso = Integer.parseInt(partes[0]);
                int mesIngreso = Integer.parseInt(partes[1]);
                int diaIngreso = Integer.parseInt(partes[2]);

                Calendar fechaActual = Calendar.getInstance();
                Calendar fechaInicio = Calendar.getInstance();
                fechaInicio.set(añoIngreso, mesIngreso - 1, diaIngreso);

                int años = fechaActual.get(Calendar.YEAR) - fechaInicio.get(Calendar.YEAR);
                int meses = fechaActual.get(Calendar.MONTH) - fechaInicio.get(Calendar.MONTH);
                int dias = fechaActual.get(Calendar.DAY_OF_MONTH) - fechaInicio.get(Calendar.DAY_OF_MONTH);

                if (dias < 0) {
                    meses--;
                    dias += fechaInicio.getActualMaximum(Calendar.DAY_OF_MONTH);
                }
                if (meses < 0) {
                    años--;
                    meses += 12;
                }

                StringBuilder antiguedad = new StringBuilder();
                if (años > 0) {
                    antiguedad.append(años).append(" año").append(años != 1 ? "s" : "");
                }
                if (meses > 0) {
                    if (antiguedad.length() > 0) antiguedad.append(", ");
                    antiguedad.append(meses).append(" mes").append(meses != 1 ? "es" : "");
                }
                if (dias > 0) {
                    if (antiguedad.length() > 0) antiguedad.append(", ");
                    antiguedad.append(dias).append(" día").append(dias != 1 ? "s" : "");
                }

                return antiguedad.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private void showPage(View pageToShow) {
        // Ocultar todas las páginas
        homePage.setVisibility(View.GONE);
        constanciasPage.setVisibility(View.GONE);
        recibosPage.setVisibility(View.GONE);
        verificadorPage.setVisibility(View.GONE);
        settingsPage.setVisibility(View.GONE);

        // Mostrar la página seleccionada
        pageToShow.setVisibility(View.VISIBLE);

        // Si es la página del verificador, iniciar la cámara
        if (pageToShow == verificadorPage) {
            reiniciarEscaneoButton.setVisibility(View.GONE);
            qrResultTextView.setText("Escanea un código QR");
            startCamera();
        } else {
            stopCamera();
        }

        // Si es la página de constancias, actualizar la información
        if (pageToShow == constanciasPage) {
            actualizarInformacionConstancia();
        }

        // Si es la página de recibos, cargar las nóminas
        if (pageToShow == recibosPage) {
            cargarNominas();
        }
    }

    private void stopCamera() {
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
        }
    }

    private void startCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) 
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, 
                new String[]{Manifest.permission.CAMERA}, 
                PERMISSION_REQUEST_CAMERA);
            return;
        }

        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = 
            ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                Toast.makeText(this, "Error al iniciar la cámara: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindPreview(ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder().build();
        CameraSelector cameraSelector = new CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build();

        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build();

        imageAnalysis.setAnalyzer(cameraExecutor, new QRCodeAnalyzer());

        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        cameraProvider.unbindAll();
        cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);
    }

    private class QRCodeAnalyzer implements ImageAnalysis.Analyzer {
        private final BarcodeScanner scanner;
        private boolean isProcessing = false;
        private String lastQRContent = null;
        private int consecutiveDetections = 0;
        private static final int REQUIRED_DETECTIONS = 10; // Número de detecciones consecutivas requeridas
        private long lastDetectionTime = 0;
        private static final long DETECTION_TIMEOUT = 500; // Tiempo máximo entre detecciones (ms)

        public QRCodeAnalyzer() {
            BarcodeScannerOptions options = new BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                .build();
            scanner = BarcodeScanning.getClient(options);
        }

        @OptIn(markerClass = ExperimentalGetImage.class) @Override
        public void analyze(@NonNull ImageProxy imageProxy) {
            if (isProcessing) {
                imageProxy.close();
                return;
            }

            InputImage image = InputImage.fromMediaImage(
                imageProxy.getImage(),
                imageProxy.getImageInfo().getRotationDegrees()
            );

            scanner.process(image)
                .addOnSuccessListener(barcodes -> {
                    if (!barcodes.isEmpty()) {
                        Barcode barcode = barcodes.get(0);
                        String qrContent = barcode.getRawValue();
                        
                        if (qrContent != null) {
                            // Verificar la calidad del escaneo
                            if (isQRWellPositioned(barcode)) {
                                long currentTime = System.currentTimeMillis();
                                
                                // Reiniciar el contador si ha pasado mucho tiempo o es un QR diferente
                                if (currentTime - lastDetectionTime > DETECTION_TIMEOUT || 
                                    !qrContent.equals(lastQRContent)) {
                                    consecutiveDetections = 0;
                                    lastQRContent = qrContent;
                                }
                                
                                lastDetectionTime = currentTime;
                                consecutiveDetections++;
                                
                                // Actualizar el mensaje de progreso
                                updateScanProgress();
                                
                                // Procesar el QR solo cuando tengamos suficientes detecciones consecutivas
                                if (consecutiveDetections >= REQUIRED_DETECTIONS) {
                                    processQRContent(qrContent);
                                }
                            } else {
                                // Reiniciar el contador si el QR no está bien posicionado
                                consecutiveDetections = 0;
                                runOnUiThread(() -> 
                                    qrResultTextView.setText("Centra el código QR en el marco"));
                            }
                        }
                    } else {
                        consecutiveDetections = 0;
                        runOnUiThread(() -> 
                            qrResultTextView.setText("Coloca el código QR dentro del marco"));
                    }
                })
                .addOnFailureListener(e -> {
                    consecutiveDetections = 0;
                    runOnUiThread(() -> 
                        Toast.makeText(MainMenuActivity.this,
                            "Error al escanear: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show());
                })
                .addOnCompleteListener(task -> imageProxy.close());
        }

        private boolean isQRWellPositioned(Barcode barcode) {
            // Obtener las coordenadas del QR en la imagen
            android.graphics.Rect box = barcode.getBoundingBox();
            if (box == null) return false;

            // Obtener las dimensiones de la vista previa
            int previewWidth = previewView.getWidth();
            int previewHeight = previewView.getHeight();

            // Calcular el centro del QR
            float qrCenterX = box.exactCenterX();
            float qrCenterY = box.exactCenterY();

            // Calcular el centro de la vista previa
            float centerX = previewWidth / 2f;
            float centerY = previewHeight / 2f;

            // Definir el área de tolerancia (15% del tamaño de la vista)
            float toleranceX = previewWidth * 0.15f;
            float toleranceY = previewHeight * 0.15f;

            // Verificar si el QR está centrado
            return Math.abs(qrCenterX - centerX) < toleranceX &&
                   Math.abs(qrCenterY - centerY) < toleranceY &&
                   box.width() >= previewWidth * 0.2f && // El QR debe ocupar al menos 20% del ancho
                   box.height() >= previewHeight * 0.2f; // Y 20% del alto
        }

        private void updateScanProgress() {
            if (!isProcessing) {
                runOnUiThread(() -> {
                    int progress = (consecutiveDetections * 100) / REQUIRED_DETECTIONS;
                    qrResultTextView.setText("Escaneando: " + progress + "%");
                });
            }
        }

        private void processQRContent(String qrContent) {
            if (isProcessing) return;
            isProcessing = true;

            try {
                JsonObject jsonObject = new Gson().fromJson(qrContent, JsonObject.class);
                int constanciaId = jsonObject.get("h").getAsInt();
                int empleadoId = jsonObject.get("e").getAsInt();
                int mes = jsonObject.get("m").getAsInt();
                int año = jsonObject.get("a").getAsInt();
                String fechaConstancia = jsonObject.get("c").getAsString();

                // Detener la cámara inmediatamente después de detectar un QR válido
                runOnUiThread(() -> {
                    stopCamera();
                    qrResultTextView.setText("Verificando constancia...");
                });

                verificarConstancia(constanciaId, empleadoId, mes, año, fechaConstancia);
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(MainMenuActivity.this,
                        "QR inválido: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
                    isProcessing = false;
                });
            }
        }

        private void verificarConstancia(int constanciaId, int empleadoId, int mes, int año, String fechaConstancia) {
            ApiClient.getClient().verificarConstancia(constanciaId).enqueue(new Callback<VerificacionResponse>() {
                @Override
                public void onResponse(Call<VerificacionResponse> call, Response<VerificacionResponse> response) {
                    isProcessing = false;
                    if (response.isSuccessful() && response.body() != null) {
                        VerificacionResponse verificacion = response.body();
                        if ("success".equals(verificacion.getStatus())) {
                            runOnUiThread(() -> {
                                mostrarDialogoVerificacion(
                                    constanciaId,
                                    verificacion.getData().getEmpleado().getNombre(),
                                    mes,
                                    año,
                                    fechaConstancia
                                );
                                reiniciarEscaneoButton.setVisibility(View.VISIBLE);
                                qrResultTextView.setText("Escaneo completado");
                            });
                        } else {
                            runOnUiThread(() -> {
                                Toast.makeText(MainMenuActivity.this,
                                    verificacion.getMessage(),
                                    Toast.LENGTH_LONG).show();
                                reiniciarEscaneoButton.setVisibility(View.VISIBLE);
                                qrResultTextView.setText("Error en la verificación");
                            });
                        }
                    } else {
                        runOnUiThread(() -> {
                            Toast.makeText(MainMenuActivity.this,
                                "Error al verificar la constancia",
                                Toast.LENGTH_SHORT).show();
                            reiniciarEscaneoButton.setVisibility(View.VISIBLE);
                            qrResultTextView.setText("Error en la verificación");
                        });
                    }
                }

                @Override
                public void onFailure(Call<VerificacionResponse> call, Throwable t) {
                    isProcessing = false;
                    runOnUiThread(() -> {
                        Toast.makeText(MainMenuActivity.this,
                            "Error de conexión: " + t.getMessage(),
                            Toast.LENGTH_SHORT).show();
                        reiniciarEscaneoButton.setVisibility(View.VISIBLE);
                        qrResultTextView.setText("Error de conexión");
                    });
                }
            });
        }
    }

    private void mostrarDialogoVerificacion(int constanciaId, String nombreEmpleado, int mes, int año, String fechaConstancia) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_verificacion_constancia);

        TextView constanciaIdText = dialog.findViewById(R.id.constanciaIdText);
        TextView empleadoText = dialog.findViewById(R.id.empleadoText);
        TextView periodoText = dialog.findViewById(R.id.periodoText);
        TextView fechaEmisionText = dialog.findViewById(R.id.fechaEmisionText);

        constanciaIdText.setText(String.valueOf(constanciaId));
        empleadoText.setText(nombreEmpleado);
        periodoText.setText(obtenerNombreMes(mes) + " " + año);
        fechaEmisionText.setText(formatearFecha(fechaConstancia));

        dialog.show();
    }

    private String obtenerNombreMes(int mes) {
        String[] meses = {"Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
                         "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};
        return meses[mes - 1];
    }

    private String formatearFecha(String fechaIngles) {
        try {
            SimpleDateFormat formatoEntrada = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
            SimpleDateFormat formatoSalida = new SimpleDateFormat("dd 'de' MMMM 'de' yyyy", new Locale("es", "ES"));
            Date fecha = formatoEntrada.parse(fechaIngles);
            return formatoSalida.format(fecha);
        } catch (ParseException e) {
            return fechaIngles;
        }
    }

    private void actualizarInformacionConstancia() {
        // Obtener datos del usuario desde SessionManager
        String nombre = sessionManager.getNombre();
        String cedula = sessionManager.getCedula();
        String cargo = sessionManager.getCargo();
        String tipoCargo = sessionManager.getTipoCargo();
        String fechaIngreso = sessionManager.getFechaIngreso();

        // Formatear la fecha de ingreso
        String fechaFormateada = formatearFecha(fechaIngreso);

        // Construir el texto de la constancia
        String textoConstancia = String.format(
            "Se generará una constancia para el empleado %s, " +
            "de Cédula de Identidad %s el cual ingresó a la institución como %s %s el %s.",
            nombre,
            cedula,
            cargo,
            tipoCargo,
            fechaFormateada
        );

        // Mostrar el texto en la vista
        constanciaPreviewText.setText(textoConstancia);

        // Obtener datos del firmante
        obtenerDatosFirmante();
    }

    private void obtenerDatosFirmante() {
        int userId = sessionManager.getUserId();
        
        ApiClient.getClient().getFirmante(userId).enqueue(new Callback<FirmanteResponse>() {
            @Override
            public void onResponse(Call<FirmanteResponse> call, Response<FirmanteResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    FirmanteResponse firmanteResponse = response.body();
                    if ("success".equals(firmanteResponse.getStatus())) {
                        mostrarDatosFirmante(firmanteResponse.getData());
                    } else {
                        Toast.makeText(MainMenuActivity.this,
                            firmanteResponse.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainMenuActivity.this,
                        "Error al obtener datos del firmante",
                        Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<FirmanteResponse> call, Throwable t) {
                Toast.makeText(MainMenuActivity.this,
                    "Error de conexión: " + t.getMessage(),
                    Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void mostrarDatosFirmante(FirmanteResponse.FirmanteData firmante) {
        // Mostrar tipo de firmante
        String tipo = firmante.isProcurador() ? 
            "PROCURADOR [MÁXIMA AUTORIDAD]" : 
            "DIRECTOR DE PERSONAL";
        firmanteTipoText.setText(tipo);

        // Mostrar nombre
        firmanteNombreText.setText(firmante.getNombre());

        // Mostrar resolución
        firmanteResolucionText.setText(firmante.getResolucion());

        // Mostrar fecha de resolución
        firmanteFechaResolucionText.setText(formatearFecha(firmante.getFechaResolucion()));

        // Mostrar gaceta
        firmanteGacetaText.setText(firmante.getGaceta());

        // Mostrar fecha de gaceta
        firmanteFechaGacetaText.setText(formatearFecha(firmante.getFechaGaceta()));

        // Mostrar estado con color
        firmanteActivoText.setText(firmante.isActivo() ? "ACTIVO" : "INACTIVO");
        firmanteActivoText.setTextColor(getResources().getColor(
            firmante.isActivo() ? R.color.success_color : R.color.danger_color,
            null
        ));
    }

    private void descargarConstancia() {
        // Mostrar progreso
        descargarConstanciaButton.setEnabled(false);
        Toast.makeText(this, "Descargando constancia...", Toast.LENGTH_SHORT).show();

        // Obtener el ID del usuario
        int userId = sessionManager.getUserId();

        // Realizar la llamada al API
        ApiClient.getClient().getConstancia(userId).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Crear el archivo en el directorio de descargas
                    String fileName = "constancia_" + System.currentTimeMillis() + ".pdf";
                    File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                    File pdfFile = new File(downloadsDir, fileName);

                    try {
                        FileOutputStream fos = new FileOutputStream(pdfFile);
                        fos.write(response.body().bytes());
                        fos.close();

                        // Notificar al usuario
                        Toast.makeText(MainMenuActivity.this,
                            "Constancia guardada en Descargas/" + fileName,
                            Toast.LENGTH_LONG).show();

                        // Abrir el PDF
                        Uri pdfUri = FileProvider.getUriForFile(MainMenuActivity.this,
                            getApplicationContext().getPackageName() + ".provider",
                            pdfFile);

                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setDataAndType(pdfUri, "application/pdf");
                        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        startActivity(intent);

                    } catch (IOException e) {
                        Toast.makeText(MainMenuActivity.this,
                            "Error al guardar el archivo: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainMenuActivity.this,
                        "Error al descargar la constancia",
                        Toast.LENGTH_SHORT).show();
                }
                descargarConstanciaButton.setEnabled(true);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(MainMenuActivity.this,
                    "Error de conexión: " + t.getMessage(),
                    Toast.LENGTH_SHORT).show();
                descargarConstanciaButton.setEnabled(true);
            }
        });
    }

    private void cargarNominas() {
        int userId = sessionManager.getUserId();
        
        ApiClient.getClient().getUserNominas(userId).enqueue(new Callback<NominasResponse>() {
            @Override
            public void onResponse(Call<NominasResponse> call, Response<NominasResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    NominasResponse nominasResponse = response.body();
                    if ("success".equals(nominasResponse.getStatus())) {
                        configurarSpinnerNominas(nominasResponse.getData());
                    } else {
                        Toast.makeText(MainMenuActivity.this,
                            "Error al cargar las nóminas",
                            Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainMenuActivity.this,
                        "Error al obtener las nóminas",
                        Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<NominasResponse> call, Throwable t) {
                Toast.makeText(MainMenuActivity.this,
                    "Error de conexión: " + t.getMessage(),
                    Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void configurarSpinnerNominas(List<NominasResponse.NominaData> nominas) {
        // Guardar la lista de nóminas
        this.nominasActuales = nominas;

        // Crear lista de nombres de nóminas con la opción por defecto
        List<String> nombresNominas = new ArrayList<>();
        nombresNominas.add("--SELECCIONE UNA NOMINA--");
        
        // Agregar las nóminas recibidas
        for (NominasResponse.NominaData nomina : nominas) {
            nombresNominas.add(nomina.getNombre());
        }

        // Crear y configurar el adaptador
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            this,
            android.R.layout.simple_dropdown_item_1line,
            nombresNominas
        );

        spinnerNomina.setAdapter(adapter);
        
        // Seleccionar la opción por defecto
        spinnerNomina.setText(nombresNominas.get(0), false);
    }

    private void configurarSpinnerMeses() {
        // Inicializar el mapa de meses
        mesesMap.put("--SELECCIONE UN MES--", 0);
        mesesMap.put("ENERO", 1);
        mesesMap.put("FEBRERO", 2);
        mesesMap.put("MARZO", 3);
        mesesMap.put("ABRIL", 4);
        mesesMap.put("MAYO", 5);
        mesesMap.put("JUNIO", 6);
        mesesMap.put("JULIO", 7);
        mesesMap.put("AGOSTO", 8);
        mesesMap.put("SEPTIEMBRE", 9);
        mesesMap.put("OCTUBRE", 10);
        mesesMap.put("NOVIEMBRE", 11);
        mesesMap.put("DICIEMBRE", 12);

        // Crear lista con los nombres de los meses
        List<String> nombresMeses = new ArrayList<>(mesesMap.keySet());

        // Crear y configurar el adaptador
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            this,
            android.R.layout.simple_dropdown_item_1line,
            nombresMeses
        );

        spinnerMes.setAdapter(adapter);
        
        // Seleccionar la opción por defecto
        spinnerMes.setText(nombresMeses.get(0), false);
    }

    private void configurarSpinnerAnios() {
        // Inicializar el mapa de años
        aniosMap.put("--SELECCIONE UN AÑO--", 0);
        for (int anio = 2025; anio <= 2050; anio++) {
            aniosMap.put(String.valueOf(anio), anio);
        }

        // Crear lista con los años
        List<String> listaAnios = new ArrayList<>(aniosMap.keySet());

        // Crear y configurar el adaptador
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            this,
            android.R.layout.simple_dropdown_item_1line,
            listaAnios
        );

        spinnerAnio.setAdapter(adapter);
        
        // Seleccionar la opción por defecto
        spinnerAnio.setText(listaAnios.get(0), false);
    }

    // Método helper para obtener el número del mes seleccionado
    private int obtenerNumeroMes() {
        String mesSeleccionado = spinnerMes.getText().toString();
        return mesesMap.getOrDefault(mesSeleccionado, 0);
    }

    // Método helper para obtener el año seleccionado
    private int obtenerAnioSeleccionado() {
        String anioSeleccionado = spinnerAnio.getText().toString();
        return aniosMap.getOrDefault(anioSeleccionado, 0);
    }

    private void buscarDetallesNomina() {
        int userId = sessionManager.getUserId();
        int mes = obtenerNumeroMes();
        int anio = obtenerAnioSeleccionado();
        String nominaId = "0";

        // Obtener ID de nómina seleccionada
        String nominaSeleccionada = spinnerNomina.getText().toString();
        for (NominasResponse.NominaData nomina : nominasActuales) {
            if (nomina.getNombre().equals(nominaSeleccionada)) {
                nominaId = nomina.getId();
                break;
            }
        }

        // Validar selecciones
        if (mes == 0 || anio == 0) {
            Toast.makeText(this, "Por favor seleccione mes y año", Toast.LENGTH_SHORT).show();
            return;
        }

        // Crear adaptador
        detallesAdapter = new DetallesNominaAdapter(
            this::onDescargarReciboClick,
            mes,
            anio
        );
        detallesRecyclerView.setAdapter(detallesAdapter);

        // Realizar búsqueda
        ApiClient.getClient().searchDetalles(userId, mes, anio, nominaId)
            .enqueue(new Callback<DetallesNominaResponse>() {
                @Override
                public void onResponse(Call<DetallesNominaResponse> call, 
                                     Response<DetallesNominaResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        DetallesNominaResponse detallesResponse = response.body();
                        if ("success".equals(detallesResponse.getStatus())) {
                            if (detallesResponse.getData().isEmpty()) {
                                Toast.makeText(MainMenuActivity.this,
                                    "No se encontraron detalles para los criterios seleccionados",
                                    Toast.LENGTH_SHORT).show();
                            }
                            detallesAdapter.setDetalles(detallesResponse.getData());
                        } else {
                            Toast.makeText(MainMenuActivity.this,
                                detallesResponse.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(MainMenuActivity.this,
                            "Error al obtener los detalles",
                            Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<DetallesNominaResponse> call, Throwable t) {
                    Toast.makeText(MainMenuActivity.this,
                        "Error de conexión: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void onDescargarReciboClick(String detalleId) {
        // Mostrar progreso
        Toast.makeText(this, "Descargando recibo...", Toast.LENGTH_SHORT).show();

        // Obtener el ID del usuario
        int userId = sessionManager.getUserId();

        // Realizar la llamada al API
        ApiClient.getClient().getRecibo(detalleId, userId).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Crear el archivo en el directorio de descargas
                    String fileName = "recibo_" + detalleId + "_" + System.currentTimeMillis() + ".pdf";
                    File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                    File pdfFile = new File(downloadsDir, fileName);

                    try {
                        FileOutputStream fos = new FileOutputStream(pdfFile);
                        fos.write(response.body().bytes());
                        fos.close();

                        // Notificar al usuario
                        Toast.makeText(MainMenuActivity.this,
                            "Recibo guardado en Descargas/" + fileName,
                            Toast.LENGTH_LONG).show();

                        // Abrir el PDF
                        Uri pdfUri = FileProvider.getUriForFile(MainMenuActivity.this,
                            getApplicationContext().getPackageName() + ".provider",
                            pdfFile);

                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setDataAndType(pdfUri, "application/pdf");
                        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        startActivity(intent);

                    } catch (IOException e) {
                        Toast.makeText(MainMenuActivity.this,
                            "Error al guardar el archivo: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainMenuActivity.this,
                        "Error al descargar el recibo",
                        Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(MainMenuActivity.this,
                    "Error de conexión: " + t.getMessage(),
                    Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            showPage(homePage);
        } else if (id == R.id.nav_constancias) {
            showPage(constanciasPage);
        } else if (id == R.id.nav_recibos) {
            showPage(recibosPage);
        } else if (id == R.id.nav_verificador) {
            showPage(verificadorPage);
        } else if (id == R.id.nav_settings) {
            showPage(settingsPage);
        } else if (id == R.id.nav_logout) {
            // Cerrar sesión
            sessionManager.logout();
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                         @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                Toast.makeText(this, "Se requiere permiso de cámara para escanear QR",
                    Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
} 