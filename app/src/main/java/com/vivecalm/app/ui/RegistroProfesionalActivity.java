package com.vivecalm.app.ui;

import android.os.Bundle;
import android.util.Patterns;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
import com.vivecalm.app.R;
import com.vivecalm.app.data.Repositorio;
import com.vivecalm.app.model.Profesional;

/** Formulario para que psicólogos y psiquiatras se registren en el directorio. */
public class RegistroProfesionalActivity extends AppCompatActivity {

    private Repositorio repo;

    private TextInputLayout tilNombre;
    private TextInputLayout tilCedula;
    private TextInputLayout tilEspecialidades;
    private TextInputLayout tilTelefono;
    private TextInputLayout tilCorreo;
    private TextInputLayout tilCiudad;
    private TextInputLayout tilHorario;
    private TextInputLayout tilPrecio;
    private TextInputLayout tilDescripcion;
    private ChipGroup chipsProfesion;
    private CheckBox cbEnLinea;
    private CheckBox cbPresencial;
    private CheckBox cbAcepto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro_profesional);
        repo = new Repositorio(this);

        tilNombre = findViewById(R.id.tilNombre);
        tilCedula = findViewById(R.id.tilCedula);
        tilEspecialidades = findViewById(R.id.tilEspecialidades);
        tilTelefono = findViewById(R.id.tilTelefono);
        tilCorreo = findViewById(R.id.tilCorreo);
        tilCiudad = findViewById(R.id.tilCiudad);
        tilHorario = findViewById(R.id.tilHorario);
        tilPrecio = findViewById(R.id.tilPrecio);
        tilDescripcion = findViewById(R.id.tilDescripcion);
        chipsProfesion = findViewById(R.id.chipsProfesion);
        cbEnLinea = findViewById(R.id.cbEnLinea);
        cbPresencial = findViewById(R.id.cbPresencial);
        cbAcepto = findViewById(R.id.cbAcepto);

        findViewById(R.id.btnVolver).setOnClickListener(v -> finish());
        findViewById(R.id.btnRegistrar).setOnClickListener(v -> registrar());
    }

    private void registrar() {
        Profesional p = new Profesional();
        p.nombre = texto(tilNombre);
        p.profesion = chipsProfesion.getCheckedChipId() == R.id.chipPsiquiatra
                ? Profesional.PSIQUIATRIA : Profesional.PSICOLOGIA;
        p.cedula = texto(tilCedula);
        p.especialidades = texto(tilEspecialidades);
        p.telefono = texto(tilTelefono);
        p.correo = texto(tilCorreo);
        p.ciudad = texto(tilCiudad);
        p.enLinea = cbEnLinea.isChecked();
        p.presencial = cbPresencial.isChecked();
        p.horario = texto(tilHorario);
        p.precio = texto(tilPrecio);
        p.descripcion = texto(tilDescripcion);

        if (!validar(p)) {
            return;
        }

        if (repo.registrarProfesional(p) == -1) {
            Toast.makeText(this, "No se pudo guardar, intenta de nuevo", Toast.LENGTH_SHORT).show();
            return;
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle("¡Registro enviado!")
                .setMessage("Tu perfil ya aparece en el directorio como pendiente de verificación. "
                        + "Cuando revisemos tu cédula se mostrará la insignia de verificado.")
                .setPositiveButton("Listo", (d, w) -> finish())
                .setCancelable(false)
                .show();
    }

    /** Revisa cada campo y marca el error debajo del que esté mal. */
    private boolean validar(Profesional p) {
        boolean ok = true;
        limpiarErrores();

        if (p.nombre.length() < 5 || !p.nombre.contains(" ")) {
            tilNombre.setError("Escribe tu nombre y apellido");
            ok = false;
        }
        if (!p.cedula.matches("\\d{7,8}")) {
            tilCedula.setError("La cédula debe tener 7 u 8 números");
            ok = false;
        } else if (repo.existeCedula(p.cedula)) {
            tilCedula.setError("Esta cédula ya está registrada");
            ok = false;
        }
        if (p.especialidades.isEmpty()) {
            tilEspecialidades.setError("Agrega al menos una especialidad");
            ok = false;
        }
        if (!p.telefono.matches("\\d{10}")) {
            tilTelefono.setError("Escribe 10 números, sin espacios");
            ok = false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(p.correo).matches()) {
            tilCorreo.setError("Revisa tu correo");
            ok = false;
        }
        if (!p.enLinea && !p.presencial) {
            Toast.makeText(this, "Elige si atiendes en línea, presencial o ambos", Toast.LENGTH_SHORT).show();
            ok = false;
        }
        if (p.presencial && p.ciudad.isEmpty()) {
            tilCiudad.setError("Escribe dónde está tu consultorio");
            ok = false;
        }
        if (ok && !cbAcepto.isChecked()) {
            Toast.makeText(this, "Marca la casilla para confirmar tus datos", Toast.LENGTH_SHORT).show();
            ok = false;
        }
        return ok;
    }

    private void limpiarErrores() {
        TextInputLayout[] campos = {tilNombre, tilCedula, tilEspecialidades, tilTelefono,
                tilCorreo, tilCiudad, tilHorario, tilPrecio, tilDescripcion};
        for (TextInputLayout t : campos) {
            t.setError(null);
        }
    }

    private String texto(TextInputLayout til) {
        if (til.getEditText() == null || til.getEditText().getText() == null) {
            return "";
        }
        return til.getEditText().getText().toString().trim();
    }
}
