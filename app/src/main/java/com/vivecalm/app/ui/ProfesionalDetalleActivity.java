package com.vivecalm.app.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.vivecalm.app.R;
import com.vivecalm.app.data.Repositorio;
import com.vivecalm.app.model.Profesional;
import com.vivecalm.app.util.Util;

/** Perfil de un profesional con botones para llamar, mandar mensaje o correo. */
public class ProfesionalDetalleActivity extends AppCompatActivity {

    public static final String EXTRA_ID = "id_profesional";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profesional_detalle);

        Profesional p = new Repositorio(this).obtenerProfesional(getIntent().getLongExtra(EXTRA_ID, -1));
        if (p == null) {
            finish();
            return;
        }

        findViewById(R.id.btnVolver).setOnClickListener(v -> finish());

        TextView tvAvatar = findViewById(R.id.tvAvatar);
        tvAvatar.setText(Util.iniciales(p.nombre));
        tvAvatar.setBackground(Util.rectangulo(this, Util.colorAvatar(p.id), 32, 0, 0));

        ((TextView) findViewById(R.id.tvNombreDetalle)).setText(p.nombre);
        ((TextView) findViewById(R.id.tvProfesionDetalle)).setText(p.titulo());

        // insignia de cédula
        TextView tvCedula = findViewById(R.id.tvCedula);
        if (p.verificado) {
            tvCedula.setText("Cédula verificada · " + p.cedula);
            tvCedula.setTextColor(ContextCompat.getColor(this, R.color.verde_oscuro));
            tvCedula.setBackground(Util.rectangulo(this, ContextCompat.getColor(this, R.color.verde_claro), 999, 0, 0));
            tvCedula.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_verificado, 0, 0, 0);
        } else {
            tvCedula.setText("Pendiente de verificación · cédula " + p.cedula);
            tvCedula.setTextColor(ContextCompat.getColor(this, R.color.cafe));
            tvCedula.setBackground(Util.rectangulo(this, ContextCompat.getColor(this, R.color.naranja_claro), 999, 0, 0));
        }

        // sobre mí y especialidades
        TextView tvDescripcion = findViewById(R.id.tvDescripcion);
        if (p.descripcion == null || p.descripcion.trim().isEmpty()) {
            tvDescripcion.setText("Este profesional todavía no agrega una descripción.");
        } else {
            tvDescripcion.setText(p.descripcion);
        }
        ChipGroup chips = findViewById(R.id.chipsEspecialidades);
        if (p.especialidades != null) {
            for (String esp : p.especialidades.split(",")) {
                if (esp.trim().isEmpty()) {
                    continue;
                }
                Chip chip = (Chip) LayoutInflater.from(this).inflate(R.layout.item_chip_info, chips, false);
                chip.setText(esp.trim());
                chips.addView(chip);
            }
        }

        // datos
        ((TextView) findViewById(R.id.tvModalidad)).setText(p.modalidad());
        mostrarOpcional(findViewById(R.id.tvHorario), p.horario);
        mostrarOpcional(findViewById(R.id.tvPrecio), p.precio);
        ((TextView) findViewById(R.id.tvTelefonoCorreo)).setText(p.telefono + "\n" + p.correo);

        // contacto
        findViewById(R.id.btnLlamar).setOnClickListener(v -> Util.marcar(this, p.telefono));
        findViewById(R.id.btnMensaje).setOnClickListener(v ->
                Util.abrir(this, new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + p.telefono))));
        findViewById(R.id.btnCorreo).setOnClickListener(v -> {
            Intent correo = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + p.correo));
            correo.putExtra(Intent.EXTRA_SUBJECT, "Consulta desde ViveCalm");
            Util.abrir(this, correo);
        });
    }

    private void mostrarOpcional(TextView tv, String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            tv.setVisibility(View.GONE);
        } else {
            tv.setText(valor);
        }
    }
}
