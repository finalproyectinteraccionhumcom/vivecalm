package com.vivecalm.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.ChipGroup;
import com.vivecalm.app.R;
import com.vivecalm.app.data.Repositorio;
import com.vivecalm.app.model.Profesional;

import java.util.List;

/** Pestaña "Ayuda": directorio de psicólogos y psiquiatras con búsqueda y filtros. */
public class ProfesionalesFragment extends Fragment {

    private Repositorio repo;
    private ProfesionalAdapter adapter;
    private EditText etBuscar;
    private ChipGroup chipsFiltro;
    private TextView tvSinResultados;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profesionales, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View vista, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(vista, savedInstanceState);
        repo = new Repositorio(requireContext());

        etBuscar = vista.findViewById(R.id.etBuscar);
        chipsFiltro = vista.findViewById(R.id.chipsFiltro);
        tvSinResultados = vista.findViewById(R.id.tvSinResultados);

        RecyclerView rv = vista.findViewById(R.id.rvProfesionales);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new ProfesionalAdapter(p -> {
            Intent i = new Intent(requireContext(), ProfesionalDetalleActivity.class);
            i.putExtra(ProfesionalDetalleActivity.EXTRA_ID, p.id);
            startActivity(i);
        });
        rv.setAdapter(adapter);

        etBuscar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                buscar();
            }
        });
        chipsFiltro.setOnCheckedStateChangeListener((group, checkedIds) -> buscar());

        vista.findViewById(R.id.cardRegistroPro).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), RegistroProfesionalActivity.class)));
    }

    @Override
    public void onResume() {
        super.onResume();
        buscar(); // así aparece un profesional recién registrado
    }

    private void buscar() {
        String filtro = null;
        int chip = chipsFiltro.getCheckedChipId();
        if (chip == R.id.chipPsicologia) {
            filtro = Profesional.PSICOLOGIA;
        } else if (chip == R.id.chipPsiquiatria) {
            filtro = Profesional.PSIQUIATRIA;
        } else if (chip == R.id.chipEnLinea) {
            filtro = "en_linea";
        }
        List<Profesional> lista = repo.buscarProfesionales(etBuscar.getText().toString(), filtro);
        adapter.actualizar(lista);
        tvSinResultados.setVisibility(lista.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
