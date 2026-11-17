package com.vivecalm.app.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.vivecalm.app.R;
import com.vivecalm.app.model.Profesional;
import com.vivecalm.app.util.Util;

import java.util.ArrayList;
import java.util.List;

/** Adaptador de la lista del directorio de profesionales. */
public class ProfesionalAdapter extends RecyclerView.Adapter<ProfesionalAdapter.ViewHolder> {

    public interface AlTocar {
        void alTocar(Profesional profesional);
    }

    private final List<Profesional> datos = new ArrayList<>();
    private final AlTocar listener;

    public ProfesionalAdapter(AlTocar listener) {
        this.listener = listener;
    }

    public void actualizar(List<Profesional> nuevos) {
        datos.clear();
        datos.addAll(nuevos);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_profesional, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Profesional p = datos.get(position);

        h.tvIniciales.setText(Util.iniciales(p.nombre));
        h.tvIniciales.setBackground(Util.rectangulo(h.itemView.getContext(), Util.colorAvatar(p.id), 18, 0, 0));
        h.tvNombre.setText(p.nombre);

        String rol = p.titulo();
        if (!p.especialidadPrincipal().isEmpty()) {
            rol += " · " + p.especialidadPrincipal();
        }
        h.tvRol.setText(rol);
        h.tvMeta.setText(p.modalidad());

        h.ivVerificado.setVisibility(p.verificado ? View.VISIBLE : View.GONE);
        h.tvPendiente.setVisibility(p.verificado ? View.GONE : View.VISIBLE);

        h.itemView.setOnClickListener(v -> listener.alTocar(p));
    }

    @Override
    public int getItemCount() {
        return datos.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvIniciales;
        final TextView tvNombre;
        final TextView tvRol;
        final TextView tvMeta;
        final TextView tvPendiente;
        final ImageView ivVerificado;

        ViewHolder(@NonNull View v) {
            super(v);
            tvIniciales = v.findViewById(R.id.tvIniciales);
            tvNombre = v.findViewById(R.id.tvNombre);
            tvRol = v.findViewById(R.id.tvRol);
            tvMeta = v.findViewById(R.id.tvMeta);
            tvPendiente = v.findViewById(R.id.tvPendiente);
            ivVerificado = v.findViewById(R.id.ivVerificado);
        }
    }
}
