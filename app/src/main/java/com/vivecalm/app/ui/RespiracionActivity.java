package com.vivecalm.app.ui;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.vivecalm.app.R;
import com.vivecalm.app.data.Repositorio;

/**
 * Reto del día: respiración 4-7-8.
 * Inhala 4 s (el círculo crece), aguanta 7 s y exhala 8 s (el círculo se encoge). 4 ciclos.
 */
public class RespiracionActivity extends AppCompatActivity {

    private static final String[] FASES = {"Inhala", "Mantén", "Exhala"};
    private static final int[] SEGUNDOS = {4, 7, 8};
    private static final int CICLOS = 4;
    private static final float ESCALA_MIN = 0.6f;
    private static final float ESCALA_MAX = 1.0f;

    private final Handler handler = new Handler(Looper.getMainLooper());

    private View circulo;
    private TextView tvFase;
    private TextView tvContador;
    private TextView tvCiclo;
    private Button btnEmpezar;

    private boolean corriendo = false;
    private int ciclo = 0;
    private int fase = 0;
    private int segundo = 0;

    private final Runnable tic = new Runnable() {
        @Override
        public void run() {
            if (segundo == 0) {
                empezarFase();
            }
            tvContador.setText(String.valueOf(SEGUNDOS[fase] - segundo));
            segundo++;

            if (segundo >= SEGUNDOS[fase]) {
                segundo = 0;
                fase++;
                if (fase == FASES.length) {
                    fase = 0;
                    ciclo++;
                    if (ciclo == CICLOS) {
                        handler.postDelayed(RespiracionActivity.this::terminar, 1000);
                        return;
                    }
                }
            }
            handler.postDelayed(this, 1000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_respiracion);

        circulo = findViewById(R.id.circuloRespiracion);
        tvFase = findViewById(R.id.tvFase);
        tvContador = findViewById(R.id.tvContador);
        tvCiclo = findViewById(R.id.tvCiclo);
        btnEmpezar = findViewById(R.id.btnEmpezar);

        findViewById(R.id.btnCerrar).setOnClickListener(v -> finish());
        btnEmpezar.setOnClickListener(v -> {
            if (corriendo) {
                detener();
            } else {
                empezar();
            }
        });
    }

    private void empezar() {
        corriendo = true;
        ciclo = 0;
        fase = 0;
        segundo = 0;
        btnEmpezar.setText("Detener");
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        handler.post(tic);
    }

    private void detener() {
        corriendo = false;
        handler.removeCallbacksAndMessages(null);
        circulo.animate().cancel();
        circulo.animate().scaleX(ESCALA_MIN).scaleY(ESCALA_MIN).setDuration(400).start();
        tvFase.setText("¿Listo?");
        tvContador.setText("");
        tvCiclo.setText("4 ciclos · cerca de 1 minuto y medio");
        btnEmpezar.setText("Empezar");
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void empezarFase() {
        tvFase.setText(FASES[fase]);
        tvCiclo.setText("Ciclo " + (ciclo + 1) + " de " + CICLOS);
        long duracion = SEGUNDOS[fase] * 1000L;
        if (fase == 0) {
            circulo.animate().scaleX(ESCALA_MAX).scaleY(ESCALA_MAX).setDuration(duracion).start();
        } else if (fase == 2) {
            circulo.animate().scaleX(ESCALA_MIN).scaleY(ESCALA_MIN).setDuration(duracion).start();
        }
    }

    private void terminar() {
        Repositorio repo = new Repositorio(this);
        repo.registrarActividad(Repositorio.ACT_RESPIRACION);
        Toast.makeText(this, "¡Bien hecho! +" + Repositorio.PUNTOS_RESPIRACION + " puntos",
                Toast.LENGTH_LONG).show();
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (corriendo && !isFinishing()) {
            detener(); // si sales de la app se pausa el ejercicio
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}
