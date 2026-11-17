package com.vivecalm.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.slider.Slider;
import com.google.android.material.textfield.TextInputEditText;
import com.vivecalm.app.R;
import com.vivecalm.app.data.Repositorio;
import com.vivecalm.app.util.Emociones;
import com.vivecalm.app.util.Util;

import java.util.ArrayList;
import java.util.List;

/** Registro emocional diario: emoción, intensidad, temas y una nota opcional. */
public class CheckInActivity extends AppCompatActivity {

    private final View[] botonesEmocion = new View[Emociones.NOMBRES.length];
    private int emocionElegida = -1;
    private int intensidad = 3;

    private TextView tvIntensidad;
    private TextView tvPuntos;
    private Button btnGuardar;
    private ChipGroup chipsTemas;
    private TextInputEditText etNota;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_in);

        tvIntensidad = findViewById(R.id.tvIntensidad);
        tvPuntos = findViewById(R.id.tvPuntosCheckIn);
        btnGuardar = findViewById(R.id.btnGuardarEmocion);
        chipsTemas = findViewById(R.id.chipsTemas);
        etNota = findViewById(R.id.etNota);

        findViewById(R.id.btnCerrar).setOnClickListener(v -> finish());

        crearBotonesDeEmocion();
        crearChipsDeTemas();

        Slider slider = findViewById(R.id.sliderIntensidad);
        slider.addOnChangeListener((s, valor, delUsuario) -> {
            intensidad = (int) valor;
            tvIntensidad.setText(intensidad + " / 5");
        });

        btnGuardar.setOnClickListener(v -> guardar());
    }

    private void crearBotonesDeEmocion() {
        LinearLayout contenedor = findViewById(R.id.contenedorEmociones);
        LayoutInflater inflater = LayoutInflater.from(this);

        for (int i = 0; i < Emociones.NOMBRES.length; i++) {
            View boton = inflater.inflate(R.layout.item_emocion, contenedor, false);
            ((ImageView) boton.findViewById(R.id.ivEmocion)).setImageResource(Emociones.ICONOS[i]);
            ((TextView) boton.findViewById(R.id.tvEmocion)).setText(Emociones.NOMBRES[i]);
            boton.setContentDescription(Emociones.NOMBRES[i]);

            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) boton.getLayoutParams();
            int margen = Util.dp(this, 3);
            lp.setMargins(margen, 0, margen, 0);

            final int indice = i;
            boton.setOnClickListener(v -> seleccionarEmocion(indice));
            botonesEmocion[i] = boton;
            contenedor.addView(boton);
        }
        pintarBotones();
    }

    private void crearChipsDeTemas() {
        LayoutInflater inflater = LayoutInflater.from(this);
        for (String tema : Emociones.TEMAS) {
            Chip chip = (Chip) inflater.inflate(R.layout.item_chip, chipsTemas, false);
            chip.setText(tema);
            chipsTemas.addView(chip);
        }
    }

    private void seleccionarEmocion(int indice) {
        emocionElegida = indice;
        pintarBotones();
        btnGuardar.setEnabled(true);
        tvPuntos.setVisibility(View.VISIBLE);
    }

    /** La emoción elegida se marca con borde verde; las demás quedan con borde gris. */
    private void pintarBotones() {
        int verde = ContextCompat.getColor(this, R.color.verde);
        int verdeClaro = ContextCompat.getColor(this, R.color.verde_claro);
        int blanco = ContextCompat.getColor(this, R.color.tarjeta);
        int borde = ContextCompat.getColor(this, R.color.borde);

        for (int i = 0; i < botonesEmocion.length; i++) {
            boolean elegida = i == emocionElegida;
            botonesEmocion[i].setBackground(Util.rectangulo(this,
                    elegida ? verdeClaro : blanco, 18, elegida ? 3 : 2, elegida ? verde : borde));
            botonesEmocion[i].setSelected(elegida);
        }
    }

    private void guardar() {
        if (emocionElegida < 0) {
            return;
        }
        List<String> temas = new ArrayList<>();
        for (int id : chipsTemas.getCheckedChipIds()) {
            Chip chip = chipsTemas.findViewById(id);
            temas.add(chip.getText().toString());
        }
        String nota = etNota.getText() == null ? "" : etNota.getText().toString().trim();

        Repositorio repo = new Repositorio(this);
        repo.guardarEmocion(emocionElegida, intensidad, String.join(", ", temas), nota);
        int racha = repo.rachaActual();

        mostrarResultado(racha);
    }

    private void mostrarResultado(int racha) {
        String mensaje = "Tu racha: " + racha + (racha == 1 ? " día" : " días") + " seguidos. ¡Sigue así!";
        boolean diaDificil = Emociones.esDificil(emocionElegida) && intensidad >= 4;
        if (diaDificil) {
            mensaje += "\n\nParece un día pesado. Puedes probar el ejercicio de respiración "
                    + "o buscar a un profesional en la pestaña Ayuda.";
        }

        MaterialAlertDialogBuilder dialogo = new MaterialAlertDialogBuilder(this)
                .setTitle("¡Registro guardado!")
                .setMessage(mensaje)
                .setCancelable(false)
                .setPositiveButton("Escribir en el diario", (d, w) -> abrirPestana(R.id.nav_diario))
                .setNegativeButton("Volver al inicio", (d, w) -> finish());
        if (diaDificil) {
            dialogo.setNeutralButton("Respirar", (d, w) -> {
                startActivity(new Intent(this, RespiracionActivity.class));
                finish();
            });
        }
        dialogo.show();
    }

    private void abrirPestana(int idPestana) {
        Intent i = new Intent(this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        i.putExtra(MainActivity.EXTRA_PESTANA, idPestana);
        startActivity(i);
        finish();
    }
}
