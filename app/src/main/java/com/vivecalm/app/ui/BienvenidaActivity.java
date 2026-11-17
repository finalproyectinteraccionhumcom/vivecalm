package com.vivecalm.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.inputmethod.EditorInfo;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.vivecalm.app.R;
import com.vivecalm.app.data.Repositorio;

/** Se muestra solo la primera vez para guardar el nombre en la tabla usuario. */
public class BienvenidaActivity extends AppCompatActivity {

    private TextInputLayout tilNombre;
    private TextInputEditText etNombre;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bienvenida);

        tilNombre = findViewById(R.id.tilNombreUsuario);
        etNombre = findViewById(R.id.etNombreUsuario);

        findViewById(R.id.btnComenzar).setOnClickListener(v -> comenzar());
        etNombre.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                comenzar();
                return true;
            }
            return false;
        });
    }

    private void comenzar() {
        String nombre = etNombre.getText() == null ? "" : etNombre.getText().toString().trim();
        if (nombre.isEmpty()) {
            tilNombre.setError("Escribe tu nombre o un apodo");
            return;
        }
        new Repositorio(this).guardarUsuario(nombre);
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
