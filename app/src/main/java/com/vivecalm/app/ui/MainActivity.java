package com.vivecalm.app.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.vivecalm.app.R;
import com.vivecalm.app.data.Repositorio;

/**
 * Pantalla principal con la barra de navegación de abajo.
 * Cada pestaña es un Fragment: Inicio, Diario, Progreso y Ayuda (profesionales).
 */
public class MainActivity extends AppCompatActivity {

    /** Extra para abrir una pestaña específica, ej. R.id.nav_diario. */
    public static final String EXTRA_PESTANA = "pestana";

    private BottomNavigationView navegacion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // la primera vez se pide el nombre
        if (new Repositorio(this).obtenerNombreUsuario() == null) {
            startActivity(new Intent(this, BienvenidaActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);
        navegacion = findViewById(R.id.navegacion);

        navegacion.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            Fragment fragment;
            if (id == R.id.nav_diario) {
                fragment = new DiarioFragment();
            } else if (id == R.id.nav_progreso) {
                fragment = new ProgresoFragment();
            } else if (id == R.id.nav_ayuda) {
                fragment = new ProfesionalesFragment();
            } else {
                fragment = new InicioFragment();
            }
            mostrar(fragment);
            return true;
        });

        if (savedInstanceState == null) {
            mostrar(new InicioFragment());
            abrirPestanaSolicitada(getIntent());
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        abrirPestanaSolicitada(intent);
    }

    /** Cambia de pestaña desde un fragment (ej. el botón "Escribe tu diario" del inicio). */
    public void irA(int idPestana) {
        navegacion.setSelectedItemId(idPestana);
    }

    private void abrirPestanaSolicitada(Intent intent) {
        int pestana = intent.getIntExtra(EXTRA_PESTANA, 0);
        if (pestana != 0) {
            navegacion.setSelectedItemId(pestana);
        }
    }

    private void mostrar(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.contenedor, fragment)
                .commit();
    }
}
