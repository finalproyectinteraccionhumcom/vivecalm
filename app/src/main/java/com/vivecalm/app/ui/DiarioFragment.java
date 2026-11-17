package com.vivecalm.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.vivecalm.app.R;
import com.vivecalm.app.data.Repositorio;
import com.vivecalm.app.model.EntradaDiario;
import com.vivecalm.app.model.RegistroEmocion;
import com.vivecalm.app.util.Emociones;
import com.vivecalm.app.util.Fechas;
import com.vivecalm.app.util.Util;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/** Pestaña de diario: pregunta del día, texto libre, 3 agradecimientos y entradas anteriores. */
public class DiarioFragment extends Fragment {

    private Repositorio repo;
    private String pregunta;

    private ImageView ivEmocionHoy;
    private TextView tvEmocionHoy;
    private TextView tvPalabras;
    private TextView tvSinEntradas;
    private EditText etEntrada;
    private EditText etGratitud1;
    private EditText etGratitud2;
    private EditText etGratitud3;
    private LinearLayout contenedorEntradas;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_diario, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View vista, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(vista, savedInstanceState);
        repo = new Repositorio(requireContext());

        TextView tvFecha = vista.findViewById(R.id.tvFechaDiario);
        TextView tvPregunta = vista.findViewById(R.id.tvPregunta);
        ivEmocionHoy = vista.findViewById(R.id.ivEmocionHoy);
        tvEmocionHoy = vista.findViewById(R.id.tvEmocionHoy);
        tvPalabras = vista.findViewById(R.id.tvPalabras);
        tvSinEntradas = vista.findViewById(R.id.tvSinEntradas);
        etEntrada = vista.findViewById(R.id.etEntrada);
        etGratitud1 = vista.findViewById(R.id.etGratitud1);
        etGratitud2 = vista.findViewById(R.id.etGratitud2);
        etGratitud3 = vista.findViewById(R.id.etGratitud3);
        contenedorEntradas = vista.findViewById(R.id.contenedorEntradas);

        tvFecha.setText(Fechas.fechaLarga(Calendar.getInstance()));

        // si ya escribiste hoy, se cargan tus respuestas para poder editarlas
        EntradaDiario deHoy = repo.diarioDe(Fechas.hoy());
        if (deHoy != null) {
            pregunta = deHoy.pregunta;
            etEntrada.setText(deHoy.texto);
            etGratitud1.setText(deHoy.gratitud1);
            etGratitud2.setText(deHoy.gratitud2);
            etGratitud3.setText(deHoy.gratitud3);
        } else {
            pregunta = preguntaDelDia();
        }
        tvPregunta.setText(pregunta);
        actualizarPalabras();

        etEntrada.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                actualizarPalabras();
            }
        });

        vista.findViewById(R.id.filaEmocionHoy).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), CheckInActivity.class)));
        vista.findViewById(R.id.btnGuardarDiario).setOnClickListener(v -> guardar());
    }

    @Override
    public void onResume() {
        super.onResume();
        mostrarEmocionDeHoy();
        cargarEntradasAnteriores();
    }

    /** Cambia la pregunta cada día del año. */
    private String preguntaDelDia() {
        String[] preguntas = getResources().getStringArray(R.array.preguntas_diario);
        int dia = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
        return preguntas[dia % preguntas.length];
    }

    private void actualizarPalabras() {
        int n = Util.contarPalabras(etEntrada.getText().toString());
        tvPalabras.setText(n == 1 ? "1 palabra" : n + " palabras");
    }

    private void mostrarEmocionDeHoy() {
        RegistroEmocion r = repo.ultimaEmocion(Fechas.hoy());
        if (r == null) {
            ivEmocionHoy.setImageResource(R.drawable.mascota);
            tvEmocionHoy.setText("Aún no registras cómo te sientes. Toca aquí");
            return;
        }
        ivEmocionHoy.setImageResource(Emociones.ICONOS[r.emocion]);
        String texto = "Hoy te sientes " + Emociones.NOMBRES[r.emocion].toLowerCase();
        if (r.temas != null && !r.temas.isEmpty()) {
            texto += " · " + r.temas;
        }
        tvEmocionHoy.setText(texto);
    }

    private void guardar() {
        String texto = etEntrada.getText().toString().trim();
        String g1 = etGratitud1.getText().toString().trim();
        String g2 = etGratitud2.getText().toString().trim();
        String g3 = etGratitud3.getText().toString().trim();

        if (texto.isEmpty() && g1.isEmpty() && g2.isEmpty() && g3.isEmpty()) {
            Toast.makeText(requireContext(), "Escribe algo antes de guardar", Toast.LENGTH_SHORT).show();
            return;
        }
        boolean eraNueva = repo.diarioDe(Fechas.hoy()) == null;
        repo.guardarDiario(Fechas.hoy(), pregunta, texto, g1, g2, g3);

        String mensaje = eraNueva
                ? "Entrada guardada · +" + Repositorio.PUNTOS_DIARIO + " puntos"
                : "Entrada actualizada";
        Toast.makeText(requireContext(), mensaje, Toast.LENGTH_SHORT).show();
        cargarEntradasAnteriores();
    }

    private void cargarEntradasAnteriores() {
        contenedorEntradas.removeAllViews();
        List<EntradaDiario> entradas = repo.ultimasEntradas(15);
        tvSinEntradas.setVisibility(entradas.isEmpty() ? View.VISIBLE : View.GONE);

        LayoutInflater inflater = LayoutInflater.from(requireContext());
        for (EntradaDiario e : entradas) {
            View item = inflater.inflate(R.layout.item_entrada_diario, contenedorEntradas, false);
            ((TextView) item.findViewById(R.id.tvFechaEntrada)).setText(Fechas.fechaLarga(Fechas.parsear(e.fecha)));
            ((TextView) item.findViewById(R.id.tvPreguntaEntrada)).setText(e.pregunta);

            TextView tvTexto = item.findViewById(R.id.tvTextoEntrada);
            tvTexto.setText(e.texto);
            tvTexto.setVisibility(e.texto == null || e.texto.isEmpty() ? View.GONE : View.VISIBLE);

            List<String> gratitudes = new ArrayList<>();
            for (String g : new String[]{e.gratitud1, e.gratitud2, e.gratitud3}) {
                if (g != null && !g.isEmpty()) {
                    gratitudes.add(g);
                }
            }
            TextView tvGratitud = item.findViewById(R.id.tvGratitudEntrada);
            if (gratitudes.isEmpty()) {
                tvGratitud.setVisibility(View.GONE);
            } else {
                tvGratitud.setText("Agradeciste: " + String.join(", ", gratitudes));
            }
            contenedorEntradas.addView(item);
        }
    }
}
