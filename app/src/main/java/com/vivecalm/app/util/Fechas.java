package com.vivecalm.app.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/** Ayudas para manejar fechas. En la base de datos se guardan como texto "yyyy-MM-dd". */
public final class Fechas {

    private static final Locale ES = Locale.forLanguageTag("es-MX");
    private static final String[] DIAS_CORTOS = {"Do", "Lu", "Ma", "Mi", "Ju", "Vi", "Sá"};

    private Fechas() {
    }

    public static String formatear(Calendar c) {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(c.getTime());
    }

    public static Calendar parsear(String fecha) {
        Calendar c = Calendar.getInstance();
        try {
            c.setTime(new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(fecha));
        } catch (ParseException | NullPointerException e) {
            // si la fecha viene mal se regresa hoy
        }
        return c;
    }

    public static String hoy() {
        return formatear(Calendar.getInstance());
    }

    public static String horaActual() {
        return new SimpleDateFormat("HH:mm", Locale.US).format(Calendar.getInstance().getTime());
    }

    /** Fecha del día siguiente, ej. "2026-11-30" → "2026-12-01". */
    public static String diaSiguiente(String fecha) {
        Calendar c = parsear(fecha);
        c.add(Calendar.DAY_OF_YEAR, 1);
        return formatear(c);
    }

    /** "2026-11" para buscar todos los registros de un mes con LIKE. */
    public static String prefijoMes(Calendar c) {
        return new SimpleDateFormat("yyyy-MM", Locale.US).format(c.getTime());
    }

    /** "Lunes 16 de noviembre". */
    public static String fechaLarga(Calendar c) {
        return capitalizar(new SimpleDateFormat("EEEE d 'de' MMMM", ES).format(c.getTime()));
    }

    /** "Noviembre 2026". */
    public static String mesYAnio(Calendar c) {
        return capitalizar(new SimpleDateFormat("MMMM yyyy", ES).format(c.getTime()));
    }

    /** "Lu", "Ma", ... */
    public static String diaCorto(Calendar c) {
        return DIAS_CORTOS[c.get(Calendar.DAY_OF_WEEK) - 1];
    }

    public static String capitalizar(String s) {
        if (s == null || s.isEmpty()) {
            return s;
        }
        return s.substring(0, 1).toUpperCase(ES) + s.substring(1);
    }
}
