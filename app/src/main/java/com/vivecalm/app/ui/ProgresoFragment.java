package com.vivecalm.app.ui;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.vivecalm.app.R;
import com.vivecalm.app.data.Repositorio;
import com.vivecalm.app.util.Emociones;
import com.vivecalm.app.util.Fechas;
import com.vivecalm.app.util.Util;

import java.util.Calendar;
import java.util.Locale;
import java.util.Map;

/** Pestaña de progreso: racha, calendario de emociones del mes, estadísticas y logros. */
public class ProgresoFragment extends Fragment {

    private static final String[] ENCABEZADOS = {"L", "M", "M", "J", "V", "S", "D"};

    private Repositorio repo;
    private Calendar mesMostrado;

    private TextView tvRachaGrande;
    private TextView tvMejorRacha;
    private TextView tvMes;
    private TextView tvDiasRegistrados;
    private TextView tvPuntos;
    private TextView tvTotalRegistros;
    private TextView tvTotalDiario;
    private ImageButton btnMesSiguiente;
    private LinearLayout contenedorCalendario;
    private LinearLayout contenedorLogros;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_progreso, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View vista, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(vista, savedInstanceState);
        repo = new Repositorio(requireContext());

        tvRachaGrande = vista.findViewById(R.id.tvRachaGrande);
        tvMejorRacha = vista.findViewById(R.id.tvMejorRacha);
        tvMes = vista.findViewById(R.id.tvMes);
        tvDiasRegistrados = vista.findViewById(R.id.tvDiasRegistrados);
        tvPuntos = vista.findViewById(R.id.tvPuntos);
        tvTotalRegistros = vista.findViewById(R.id.tvTotalRegistros);
        tvTotalDiario = vista.findViewById(R.id.tvTotalDiario);
        btnMesSiguiente = vista.findViewById(R.id.btnMesSiguiente);
        contenedorCalendario = vista.findViewById(R.id.contenedorCalendario);
        contenedorLogros = vista.findViewById(R.id.contenedorLogros);

        mesMostrado = Calendar.getInstance();
        mesMostrado.set(Calendar.DAY_OF_MONTH, 1);

        vista.findViewById(R.id.btnMesAnterior).setOnClickListener(v -> {
            mesMostrado.add(Calendar.MONTH, -1);
            construirCalendario();
        });
        btnMesSiguiente.setOnClickListener(v -> {
            mesMostrado.add(Calendar.MONTH, 1);
            construirCalendario();
        });

        construirEncabezadoSemana(vista.findViewById(R.id.filaDiasSemana));
    }

    @Override
    public void onResume() {
        super.onResume();
        int racha = repo.rachaActual();
        int mejor = repo.mejorRacha();
        tvRachaGrande.setText(racha == 1 ? "1 día" : racha + " días");
        tvMejorRacha.setText("Tu mejor racha: " + mejor + (mejor == 1 ? " día" : " días"));
        tvPuntos.setText(String.valueOf(repo.puntos()));
        tvTotalRegistros.setText(String.valueOf(repo.contarEmociones()));
        tvTotalDiario.setText(String.valueOf(repo.contarDiario()));
        construirCalendario();
        construirLogros(mejor);
    }

    private void construirEncabezadoSemana(LinearLayout fila) {
        Context ctx = requireContext();
        for (String letra : ENCABEZADOS) {
            TextView tv = new TextView(ctx);
            tv.setText(letra);
            tv.setGravity(Gravity.CENTER);
            tv.setTextSize(12);
            tv.setTypeface(Typeface.DEFAULT_BOLD);
            tv.setTextColor(ContextCompat.getColor(ctx, R.color.texto_secundario));
            tv.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            fila.addView(tv);
        }
    }

    /** Calendario del mes: cada día se pinta del color de la emoción registrada. */
    private void construirCalendario() {
        Context ctx = requireContext();
        contenedorCalendario.removeAllViews();

        Map<String, Integer> emociones = repo.emocionesDelMes(Fechas.prefijoMes(mesMostrado));
        tvMes.setText(Fechas.mesYAnio(mesMostrado));
        int dias = emociones.size();
        tvDiasRegistrados.setText(dias == 1 ? "1 día registrado" : dias + " días registrados");

        // no se puede avanzar más allá del mes actual
        Calendar hoy = Calendar.getInstance();
        boolean esMesActual = mesMostrado.get(Calendar.YEAR) == hoy.get(Calendar.YEAR)
                && mesMostrado.get(Calendar.MONTH) == hoy.get(Calendar.MONTH);
        btnMesSiguiente.setEnabled(!esMesActual);
        btnMesSiguiente.setAlpha(esMesActual ? 0.3f : 1f);

        Calendar c = (Calendar) mesMostrado.clone();
        c.set(Calendar.DAY_OF_MONTH, 1);
        // lunes = 0 ... domingo = 6
        int desfase = (c.get(Calendar.DAY_OF_WEEK) + 5) % 7;
        int diasDelMes = c.getActualMaximum(Calendar.DAY_OF_MONTH);
        int filas = (int) Math.ceil((desfase + diasDelMes) / 7.0);
        String prefijo = Fechas.prefijoMes(mesMostrado);
        String fechaHoy = Fechas.hoy();

        int tamCirculo = Util.dp(ctx, 36);
        for (int f = 0; f < filas; f++) {
            LinearLayout fila = new LinearLayout(ctx);
            fila.setOrientation(LinearLayout.HORIZONTAL);

            for (int col = 0; col < 7; col++) {
                FrameLayout celda = new FrameLayout(ctx);
                celda.setLayoutParams(new LinearLayout.LayoutParams(0, Util.dp(ctx, 42), 1f));

                int dia = f * 7 + col - desfase + 1;
                if (dia >= 1 && dia <= diasDelMes) {
                    String fecha = prefijo + String.format(Locale.US, "-%02d", dia);
                    TextView tv = new TextView(ctx);
                    FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(tamCirculo, tamCirculo, Gravity.CENTER);
                    tv.setLayoutParams(lp);
                    tv.setGravity(Gravity.CENTER);
                    tv.setText(String.valueOf(dia));
                    tv.setTextSize(13);
                    tv.setTypeface(Typeface.DEFAULT_BOLD);

                    Integer emocion = emociones.get(fecha);
                    if (emocion != null) {
                        tv.setBackground(Util.circulo(ContextCompat.getColor(ctx, Emociones.COLORES[emocion]), 0, 0));
                        tv.setTextColor(ContextCompat.getColor(ctx, R.color.texto));
                        tv.setContentDescription(dia + ", " + Emociones.NOMBRES[emocion]);
                    } else {
                        tv.setTextColor(ContextCompat.getColor(ctx, R.color.texto_secundario));
                    }
                    if (fecha.equals(fechaHoy)) {
                        if (emocion == null) {
                            tv.setBackground(Util.circuloPunteado(ctx, ContextCompat.getColor(ctx, R.color.naranja)));
                        }
                        tv.setTextColor(ContextCompat.getColor(ctx, R.color.texto));
                    }
                    celda.addView(tv);
                }
                fila.addView(celda);
            }
            contenedorCalendario.addView(fila);
        }
    }

    private void construirLogros(int mejorRacha) {
        contenedorLogros.removeAllViews();
        int emociones = repo.contarEmociones();
        int diario = repo.contarDiario();
        int respiraciones = repo.contarActividades(Repositorio.ACT_RESPIRACION);

        agregarLogro(R.drawable.ic_hoja, "Primer paso", "Registra tu primera emoción", emociones >= 1);
        agregarLogro(R.drawable.ic_flama, "Primera semana", "Racha de 7 días seguidos", mejorRacha >= 7);
        agregarLogro(R.drawable.ic_diario, "Escritor de diario", "Escribe 10 entradas (" + Math.min(diario, 10) + "/10)", diario >= 10);
        agregarLogro(R.drawable.ic_respirar, "Respira hondo", "Completa 5 respiraciones (" + Math.min(respiraciones, 5) + "/5)", respiraciones >= 5);
        agregarLogro(R.drawable.ic_estrella, "Imparable", "Racha de 30 días seguidos", mejorRacha >= 30);
    }

    private void agregarLogro(int icono, String titulo, String descripcion, boolean desbloqueado) {
        Context ctx = requireContext();
        View item = LayoutInflater.from(ctx).inflate(R.layout.item_logro, contenedorLogros, false);

        ImageView ivLogro = item.findViewById(R.id.ivLogro);
        ivLogro.setImageResource(icono);
        int fondo = ContextCompat.getColor(ctx, desbloqueado ? R.color.verde_claro : R.color.gris_claro);
        ivLogro.setBackground(Util.rectangulo(ctx, fondo, 14, 0, 0));

        ((TextView) item.findViewById(R.id.tvLogroTitulo)).setText(titulo);
        ((TextView) item.findViewById(R.id.tvLogroDescripcion)).setText(descripcion);

        ImageView ivEstado = item.findViewById(R.id.ivLogroEstado);
        ivEstado.setImageResource(desbloqueado ? R.drawable.ic_verificado : R.drawable.ic_candado);
        ivEstado.setContentDescription(desbloqueado ? "Desbloqueado" : "Bloqueado");

        if (!desbloqueado) {
            item.setBackground(Util.rectangulo(ctx, ContextCompat.getColor(ctx, R.color.gris_claro), 22, 0, 0));
            ivLogro.setImageAlpha(110);
        }
        contenedorLogros.addView(item);
    }
}
