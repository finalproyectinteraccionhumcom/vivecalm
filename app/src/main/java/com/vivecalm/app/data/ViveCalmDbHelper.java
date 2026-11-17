package com.vivecalm.app.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.vivecalm.app.util.Fechas;

/**
 * Crea y actualiza la base de datos SQLite de ViveCalm.
 *
 * Tablas:
 *  - usuario        : nombre de la persona que usa la app
 *  - emociones      : registro emocional diario (emoción, intensidad, temas)
 *  - diario         : una entrada de diario por día (pregunta, texto y 3 agradecimientos)
 *  - actividades    : retos completados (por ahora: respiración 4-7-8)
 *  - profesionales  : psicólogos y psiquiatras que se registran en la app
 */
public class ViveCalmDbHelper extends SQLiteOpenHelper {

    public static final String NOMBRE_BD = "vivecalm.db";
    public static final int VERSION_BD = 1;

    // nombres de tablas
    public static final String T_USUARIO = "usuario";
    public static final String T_EMOCIONES = "emociones";
    public static final String T_DIARIO = "diario";
    public static final String T_ACTIVIDADES = "actividades";
    public static final String T_PROFESIONALES = "profesionales";

    public ViveCalmDbHelper(Context context) {
        super(context, NOMBRE_BD, null, VERSION_BD);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + T_USUARIO + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "nombre TEXT NOT NULL, "
                + "fecha_registro TEXT NOT NULL)");

        db.execSQL("CREATE TABLE " + T_EMOCIONES + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "fecha TEXT NOT NULL, "            // yyyy-MM-dd
                + "hora TEXT NOT NULL, "             // HH:mm
                + "emocion INTEGER NOT NULL, "       // 0 feliz, 1 tranqui, 2 triste, 3 ansioso, 4 enojado
                + "intensidad INTEGER NOT NULL, "    // 1 a 5
                + "temas TEXT, "                     // separados por coma
                + "nota TEXT)");

        db.execSQL("CREATE TABLE " + T_DIARIO + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "fecha TEXT NOT NULL UNIQUE, "
                + "pregunta TEXT NOT NULL, "
                + "texto TEXT, "
                + "gratitud1 TEXT, "
                + "gratitud2 TEXT, "
                + "gratitud3 TEXT)");

        db.execSQL("CREATE TABLE " + T_ACTIVIDADES + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "fecha TEXT NOT NULL, "
                + "tipo TEXT NOT NULL)");

        db.execSQL("CREATE TABLE " + T_PROFESIONALES + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "nombre TEXT NOT NULL, "
                + "profesion TEXT NOT NULL, "        // Psicología o Psiquiatría
                + "cedula TEXT NOT NULL UNIQUE, "
                + "especialidades TEXT, "
                + "telefono TEXT NOT NULL, "
                + "correo TEXT NOT NULL, "
                + "ciudad TEXT, "
                + "en_linea INTEGER NOT NULL DEFAULT 0, "
                + "presencial INTEGER NOT NULL DEFAULT 0, "
                + "horario TEXT, "
                + "precio TEXT, "
                + "descripcion TEXT, "
                + "verificado INTEGER NOT NULL DEFAULT 0, "
                + "fecha_registro TEXT NOT NULL)");

        db.execSQL("CREATE INDEX idx_emociones_fecha ON " + T_EMOCIONES + "(fecha)");
        db.execSQL("CREATE INDEX idx_actividades_fecha ON " + T_ACTIVIDADES + "(fecha, tipo)");

        insertarProfesionalesDeEjemplo(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // en una versión futura aquí se migran los datos; por ahora se recrean las tablas
        db.execSQL("DROP TABLE IF EXISTS " + T_USUARIO);
        db.execSQL("DROP TABLE IF EXISTS " + T_EMOCIONES);
        db.execSQL("DROP TABLE IF EXISTS " + T_DIARIO);
        db.execSQL("DROP TABLE IF EXISTS " + T_ACTIVIDADES);
        db.execSQL("DROP TABLE IF EXISTS " + T_PROFESIONALES);
        onCreate(db);
    }

    /** Perfiles de ejemplo para que el directorio no se vea vacío al probar la app. */
    private void insertarProfesionalesDeEjemplo(SQLiteDatabase db) {
        insertarEjemplo(db, "Mariana Torres (ejemplo)", "Psicología", "10000001",
                "Ansiedad, Estrés escolar, Autoestima", "Guadalajara", 1, 1,
                "Lunes a viernes, 9:00 a 18:00", "$600 por sesión",
                "Perfil de ejemplo. Psicóloga clínica con enfoque cognitivo-conductual.");
        insertarEjemplo(db, "Luis Herrera (ejemplo)", "Psiquiatría", "10000002",
                "Depresión, Trastornos del sueño", "Zapopan", 0, 1,
                "Martes y jueves, 10:00 a 15:00", "$1,000 por consulta",
                "Perfil de ejemplo. Psiquiatra de adultos.");
        insertarEjemplo(db, "Andrea Ruiz (ejemplo)", "Psicología", "10000003",
                "Adolescentes, Familia", "", 1, 0,
                "Lunes a sábado, 16:00 a 21:00", "$450 por sesión",
                "Perfil de ejemplo. Psicóloga especializada en adolescentes, atiende solo en línea.");
    }

    private void insertarEjemplo(SQLiteDatabase db, String nombre, String profesion, String cedula,
                                 String especialidades, String ciudad, int enLinea, int presencial,
                                 String horario, String precio, String descripcion) {
        ContentValues v = new ContentValues();
        v.put("nombre", nombre);
        v.put("profesion", profesion);
        v.put("cedula", cedula);
        v.put("especialidades", especialidades);
        v.put("telefono", "3300000000");
        v.put("correo", "ejemplo@vivecalm.app");
        v.put("ciudad", ciudad);
        v.put("en_linea", enLinea);
        v.put("presencial", presencial);
        v.put("horario", horario);
        v.put("precio", precio);
        v.put("descripcion", descripcion);
        v.put("verificado", 1);
        v.put("fecha_registro", Fechas.hoy());
        db.insert(T_PROFESIONALES, null, v);
    }
}
