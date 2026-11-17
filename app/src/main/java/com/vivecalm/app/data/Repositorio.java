package com.vivecalm.app.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.vivecalm.app.model.EntradaDiario;
import com.vivecalm.app.model.Profesional;
import com.vivecalm.app.model.RegistroEmocion;
import com.vivecalm.app.util.Fechas;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Todas las consultas a SQLite pasan por aquí, así las pantallas no escriben SQL.
 */
public class Repositorio {

    public static final String ACT_RESPIRACION = "respiracion";

    // puntos que da cada actividad
    public static final int PUNTOS_EMOCION = 5;
    public static final int PUNTOS_DIARIO = 10;
    public static final int PUNTOS_RESPIRACION = 10;

    private final ViveCalmDbHelper helper;

    public Repositorio(Context context) {
        helper = new ViveCalmDbHelper(context.getApplicationContext());
    }

    // ------------------------------------------------------------------ usuario

    public String obtenerNombreUsuario() {
        SQLiteDatabase db = helper.getReadableDatabase();
        try (Cursor c = db.rawQuery("SELECT nombre FROM usuario ORDER BY id LIMIT 1", null)) {
            return c.moveToFirst() ? c.getString(0) : null;
        }
    }

    public void guardarUsuario(String nombre) {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.delete(ViveCalmDbHelper.T_USUARIO, null, null);
        ContentValues v = new ContentValues();
        v.put("nombre", nombre);
        v.put("fecha_registro", Fechas.hoy());
        db.insert(ViveCalmDbHelper.T_USUARIO, null, v);
    }

    // ---------------------------------------------------------------- emociones

    public long guardarEmocion(int emocion, int intensidad, String temas, String nota) {
        ContentValues v = new ContentValues();
        v.put("fecha", Fechas.hoy());
        v.put("hora", Fechas.horaActual());
        v.put("emocion", emocion);
        v.put("intensidad", intensidad);
        v.put("temas", temas);
        v.put("nota", nota);
        return helper.getWritableDatabase().insert(ViveCalmDbHelper.T_EMOCIONES, null, v);
    }

    /** Último registro de un día, o null si ese día no se registró nada. */
    public RegistroEmocion ultimaEmocion(String fecha) {
        SQLiteDatabase db = helper.getReadableDatabase();
        try (Cursor c = db.rawQuery(
                "SELECT id, fecha, hora, emocion, intensidad, temas, nota FROM emociones "
                        + "WHERE fecha = ? ORDER BY id DESC LIMIT 1",
                new String[]{fecha})) {
            if (!c.moveToFirst()) {
                return null;
            }
            RegistroEmocion r = new RegistroEmocion();
            r.id = c.getLong(0);
            r.fecha = c.getString(1);
            r.hora = c.getString(2);
            r.emocion = c.getInt(3);
            r.intensidad = c.getInt(4);
            r.temas = c.getString(5);
            r.nota = c.getString(6);
            return r;
        }
    }

    /** Para el calendario: fecha → emoción (la última de ese día). */
    public Map<String, Integer> emocionesDelMes(String prefijoMes) {
        Map<String, Integer> mapa = new HashMap<>();
        SQLiteDatabase db = helper.getReadableDatabase();
        try (Cursor c = db.rawQuery(
                "SELECT fecha, emocion FROM emociones WHERE fecha LIKE ? ORDER BY id",
                new String[]{prefijoMes + "-%"})) {
            while (c.moveToNext()) {
                mapa.put(c.getString(0), c.getInt(1)); // el último registro sobrescribe
            }
        }
        return mapa;
    }

    public int contarEmociones() {
        return contar("SELECT COUNT(*) FROM emociones", null);
    }

    /** Días distintos con al menos un registro, en orden. */
    public List<String> fechasConRegistro() {
        List<String> fechas = new ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();
        try (Cursor c = db.rawQuery("SELECT DISTINCT fecha FROM emociones ORDER BY fecha", null)) {
            while (c.moveToNext()) {
                fechas.add(c.getString(0));
            }
        }
        return fechas;
    }

    /**
     * Días seguidos registrando emoción. Si hoy todavía no registras pero ayer sí,
     * la racha sigue viva (cuenta desde ayer).
     */
    public int rachaActual() {
        Set<String> fechas = new HashSet<>(fechasConRegistro());
        Calendar dia = Calendar.getInstance();
        if (!fechas.contains(Fechas.formatear(dia))) {
            dia.add(Calendar.DAY_OF_YEAR, -1);
        }
        int racha = 0;
        while (fechas.contains(Fechas.formatear(dia))) {
            racha++;
            dia.add(Calendar.DAY_OF_YEAR, -1);
        }
        return racha;
    }

    public int mejorRacha() {
        int mejor = 0;
        int actual = 0;
        String esperada = null;
        for (String fecha : fechasConRegistro()) {
            actual = fecha.equals(esperada) ? actual + 1 : 1;
            mejor = Math.max(mejor, actual);
            esperada = Fechas.diaSiguiente(fecha);
        }
        return mejor;
    }

    // ------------------------------------------------------------------- diario

    /** Guarda la entrada del día; si ya existe, la actualiza. */
    public void guardarDiario(String fecha, String pregunta, String texto,
                              String gratitud1, String gratitud2, String gratitud3) {
        ContentValues v = new ContentValues();
        v.put("fecha", fecha);
        v.put("pregunta", pregunta);
        v.put("texto", texto);
        v.put("gratitud1", gratitud1);
        v.put("gratitud2", gratitud2);
        v.put("gratitud3", gratitud3);
        SQLiteDatabase db = helper.getWritableDatabase();
        int filas = db.update(ViveCalmDbHelper.T_DIARIO, v, "fecha = ?", new String[]{fecha});
        if (filas == 0) {
            db.insert(ViveCalmDbHelper.T_DIARIO, null, v);
        }
    }

    public EntradaDiario diarioDe(String fecha) {
        List<EntradaDiario> lista = consultarDiario("WHERE fecha = ?", new String[]{fecha}, 1);
        return lista.isEmpty() ? null : lista.get(0);
    }

    public List<EntradaDiario> ultimasEntradas(int limite) {
        return consultarDiario("", null, limite);
    }

    public int contarDiario() {
        return contar("SELECT COUNT(*) FROM diario", null);
    }

    private List<EntradaDiario> consultarDiario(String where, String[] args, int limite) {
        List<EntradaDiario> lista = new ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();
        String sql = "SELECT id, fecha, pregunta, texto, gratitud1, gratitud2, gratitud3 FROM diario "
                + where + " ORDER BY fecha DESC LIMIT " + limite;
        try (Cursor c = db.rawQuery(sql, args)) {
            while (c.moveToNext()) {
                EntradaDiario e = new EntradaDiario();
                e.id = c.getLong(0);
                e.fecha = c.getString(1);
                e.pregunta = c.getString(2);
                e.texto = c.getString(3);
                e.gratitud1 = c.getString(4);
                e.gratitud2 = c.getString(5);
                e.gratitud3 = c.getString(6);
                lista.add(e);
            }
        }
        return lista;
    }

    // -------------------------------------------------------------- actividades

    public void registrarActividad(String tipo) {
        ContentValues v = new ContentValues();
        v.put("fecha", Fechas.hoy());
        v.put("tipo", tipo);
        helper.getWritableDatabase().insert(ViveCalmDbHelper.T_ACTIVIDADES, null, v);
    }

    public boolean actividadHecha(String fecha, String tipo) {
        return contar("SELECT COUNT(*) FROM actividades WHERE fecha = ? AND tipo = ?",
                new String[]{fecha, tipo}) > 0;
    }

    public int contarActividades(String tipo) {
        return contar("SELECT COUNT(*) FROM actividades WHERE tipo = ?", new String[]{tipo});
    }

    public int puntos() {
        return contarEmociones() * PUNTOS_EMOCION
                + contarDiario() * PUNTOS_DIARIO
                + contarActividades(ACT_RESPIRACION) * PUNTOS_RESPIRACION;
    }

    // ------------------------------------------------------------ profesionales

    /**
     * Busca en el directorio.
     *
     * @param texto  busca en nombre, ciudad, especialidades y profesión (puede ir vacío)
     * @param filtro null = todos, Profesional.PSICOLOGIA, Profesional.PSIQUIATRIA o "en_linea"
     */
    public List<Profesional> buscarProfesionales(String texto, String filtro) {
        StringBuilder where = new StringBuilder("WHERE 1 = 1");
        List<String> args = new ArrayList<>();

        if (texto != null && !texto.trim().isEmpty()) {
            String like = "%" + texto.trim() + "%";
            where.append(" AND (nombre LIKE ? OR ciudad LIKE ? OR especialidades LIKE ? OR profesion LIKE ?)");
            args.add(like);
            args.add(like);
            args.add(like);
            args.add(like);
        }
        if ("en_linea".equals(filtro)) {
            where.append(" AND en_linea = 1");
        } else if (filtro != null) {
            where.append(" AND profesion = ?");
            args.add(filtro);
        }

        List<Profesional> lista = new ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();
        String sql = "SELECT * FROM profesionales " + where + " ORDER BY verificado DESC, nombre";
        try (Cursor c = db.rawQuery(sql, args.toArray(new String[0]))) {
            while (c.moveToNext()) {
                lista.add(leerProfesional(c));
            }
        }
        return lista;
    }

    public Profesional obtenerProfesional(long id) {
        SQLiteDatabase db = helper.getReadableDatabase();
        try (Cursor c = db.rawQuery("SELECT * FROM profesionales WHERE id = ?",
                new String[]{String.valueOf(id)})) {
            return c.moveToFirst() ? leerProfesional(c) : null;
        }
    }

    public boolean existeCedula(String cedula) {
        return contar("SELECT COUNT(*) FROM profesionales WHERE cedula = ?", new String[]{cedula}) > 0;
    }

    /** Guarda un profesional nuevo. Queda como "pendiente de verificación". */
    public long registrarProfesional(Profesional p) {
        ContentValues v = new ContentValues();
        v.put("nombre", p.nombre);
        v.put("profesion", p.profesion);
        v.put("cedula", p.cedula);
        v.put("especialidades", p.especialidades);
        v.put("telefono", p.telefono);
        v.put("correo", p.correo);
        v.put("ciudad", p.ciudad);
        v.put("en_linea", p.enLinea ? 1 : 0);
        v.put("presencial", p.presencial ? 1 : 0);
        v.put("horario", p.horario);
        v.put("precio", p.precio);
        v.put("descripcion", p.descripcion);
        v.put("verificado", 0);
        v.put("fecha_registro", Fechas.hoy());
        return helper.getWritableDatabase().insert(ViveCalmDbHelper.T_PROFESIONALES, null, v);
    }

    private Profesional leerProfesional(Cursor c) {
        Profesional p = new Profesional();
        p.id = c.getLong(c.getColumnIndexOrThrow("id"));
        p.nombre = c.getString(c.getColumnIndexOrThrow("nombre"));
        p.profesion = c.getString(c.getColumnIndexOrThrow("profesion"));
        p.cedula = c.getString(c.getColumnIndexOrThrow("cedula"));
        p.especialidades = c.getString(c.getColumnIndexOrThrow("especialidades"));
        p.telefono = c.getString(c.getColumnIndexOrThrow("telefono"));
        p.correo = c.getString(c.getColumnIndexOrThrow("correo"));
        p.ciudad = c.getString(c.getColumnIndexOrThrow("ciudad"));
        p.enLinea = c.getInt(c.getColumnIndexOrThrow("en_linea")) == 1;
        p.presencial = c.getInt(c.getColumnIndexOrThrow("presencial")) == 1;
        p.horario = c.getString(c.getColumnIndexOrThrow("horario"));
        p.precio = c.getString(c.getColumnIndexOrThrow("precio"));
        p.descripcion = c.getString(c.getColumnIndexOrThrow("descripcion"));
        p.verificado = c.getInt(c.getColumnIndexOrThrow("verificado")) == 1;
        return p;
    }

    // ------------------------------------------------------------------ helpers

    private int contar(String sql, String[] args) {
        SQLiteDatabase db = helper.getReadableDatabase();
        try (Cursor c = db.rawQuery(sql, args)) {
            return c.moveToFirst() ? c.getInt(0) : 0;
        }
    }
}
