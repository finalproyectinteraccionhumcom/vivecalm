package com.vivecalm.app.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.vivecalm.app.R;
import com.vivecalm.app.data.Repositorio;
import com.vivecalm.app.model.RegistroEmocion;
import com.vivecalm.app.util.Emociones;
import com.vivecalm.app.util.Fechas;
import com.vivecalm.app.util.Util;

import java.util.Calendar;

/** Pestaña de inicio: saludo, racha, registro rápido, últimos 7 días y reto del día. */
public class InicioFragment extends Fragment {

    private Repositorio repo;

    private TextView tvFecha;
    private TextView tvSaludo;
    private TextView tvRacha;
    private TextView tvMensajeMascota;
    private TextView tvEstadoSemana;
    private TextView tvRetoEstado;
    private TextView tvMeta;
    private Button btnRegistrarEmocion;
    private Button btnReto;
    private LinearLayout contenedorSemana;
    private LinearProgressIndicator progresoMeta;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_inicio, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View vista, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(vista, savedInstanceState);
        repo = new Repositorio(requireContext());

        tvFecha = vista.findViewById(R.id.tvFecha);
        tvSaludo = vista.findViewById(R.id.tvSaludo);
        tvRacha = vista.findViewById(R.id.tvRacha);
        tvMensajeMascota = vista.findViewById(R.id.tvMensajeMascota);
        tvEstadoSemana = vista.findViewById(R.id.tvEstadoSemana);
        tvRetoEstado = vista.findViewById(R.id.tvRetoEstado);
        tvMeta = vista.findViewById(R.id.tvMeta);
        btnRegistrarEmocion = vista.findViewById(R.id.btnRegistrarEmocion);
        btnReto = vista.findViewById(R.id.btnReto);
        contenedorSemana = vista.findViewById(R.id.contenedorSemana);
        progresoMeta = vista.findViewById(R.id.progresoMeta);
        progresoMeta.setMax(3);

        btnRegistrarEmocion.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), CheckInActivity.class)));
        btnReto.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), RespiracionActivity.class)));
        vista.findViewById(R.id.btnRacha).setOnClickListener(v -> irA(R.id.nav_progreso));
        vista.findViewById(R.id.cardDiario).setOnClickListener(v -> irA(R.id.nav_diario));
        vista.findViewById(R.id.cardProfesionales).setOnClickListener(v -> irA(R.id.nav_ayuda));
        vista.findViewById(R.id.cardCrisis).setOnClickListener(v ->
                Util.marcar(requireContext(), getString(R.string.linea_vida)));
    }

    @Override
    public void onResume() {
        super.onResume();
        cargarDatos(); // se recarga al volver del registro de emoción o de la respiración
    }

    private void irA(int idPestana) {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).irA(idPestana);
        }
    }

    private void cargarDatos() {
        String hoy = Fechas.hoy();

        tvFecha.setText(Fechas.fechaLarga(Calendar.getInstance()));
        tvSaludo.setText("Hola, " + Util.vacioSiNulo(repo.obtenerNombreUsuario()));
        tvRacha.setText(String.valueOf(repo.rachaActual()));

        RegistroEmocion emocionHoy = repo.ultimaEmocion(hoy);
        if (emocionHoy == null) {
            tvMensajeMascota.setText(Util.saludoSegunHora() + " ¿Cómo te sientes hoy?");
            btnRegistrarEmocion.setText("Registrar emoción");
        } else {
            String nombre = Emociones.NOMBRES[emocionHoy.emocion].toLowerCase();
            tvMensajeMascota.setText(Util.saludoSegunHora() + " Hoy te sientes " + nombre + ".");
            btnRegistrarEmocion.setText("Registrar otra vez");
        }
        tvEstadoSemana.setText(emocionHoy == null ? "Hoy falta tu registro" : "¡Hoy ya registraste!");

        construirSemana();

        boolean diarioHoy = repo.diarioDe(hoy) != null;
        boolean retoHoy = repo.actividadHecha(hoy, Repositorio.ACT_RESPIRACION);
        tvRetoEstado.setText(retoHoy ? "¡Completado hoy!" : "1.5 min · +" + Repositorio.PUNTOS_RESPIRACION + " puntos");
        btnReto.setText(retoHoy ? "Repetir" : "Empezar");

        int hechas = (emocionHoy != null ? 1 : 0) + (diarioHoy ? 1 : 0) + (retoHoy ? 1 : 0);
        tvMeta.setText(hechas + " de 3 actividades");
        progresoMeta.setProgressCompat(hechas, true);
    }

    /** Dibuja los últimos 7 días: círculo del color de la emoción o vacío. */
    private void construirSemana() {
        Context ctx = requireContext();
        contenedorSemana.removeAllViews();

        for (int i = 6; i >= 0; i--) {
            Calendar dia = Calendar.getInstance();
            dia.add(Calendar.DAY_OF_YEAR, -i);
            RegistroEmocion registro = repo.ultimaEmocion(Fechas.formatear(dia));

            LinearLayout columna = new LinearLayout(ctx);
            columna.setOrientation(LinearLayout.VERTICAL);
            columna.setGravity(Gravity.CENTER_HORIZONTAL);
            columna.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

            TextView etiqueta = new TextView(ctx);
            etiqueta.setText(i == 0 ? "Hoy" : Fechas.diaCorto(dia));
            etiqueta.setTextSize(12);
            etiqueta.setTypeface(Typeface.DEFAULT_BOLD);
            etiqueta.setTextColor(ContextCompat.getColor(ctx, R.color.texto_secundario));
            columna.addView(etiqueta);

            ImageView circulo = new ImageView(ctx);
            int tam = Util.dp(ctx, 34);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(tam, tam);
            lp.topMargin = Util.dp(ctx, 6);
            circulo.setLayoutParams(lp);
            int pad = Util.dp(ctx, 8);
            circulo.setPadding(pad, pad, pad, pad);

            if (registro != null) {
                int color = ContextCompat.getColor(ctx, Emociones.COLORES[registro.emocion]);
                circulo.setBackground(Util.circulo(color, 0, 0));
                circulo.setImageResource(R.drawable.ic_check);
                circulo.setContentDescription(Emociones.NOMBRES[registro.emocion]);
            } else if (i == 0) {
                circulo.setBackground(Util.circuloPunteado(ctx, ContextCompat.getColor(ctx, R.color.naranja)));
                circulo.setContentDescription("Hoy, sin registro");
            } else {
                circulo.setBackground(Util.circulo(Color.TRANSPARENT, Util.dp(ctx, 2),
                        ContextCompat.getColor(ctx, R.color.borde)));
                circulo.setContentDescription("Sin registro");
            }
            columna.addView(circulo);
            contenedorSemana.addView(columna);
        }
    }
}
