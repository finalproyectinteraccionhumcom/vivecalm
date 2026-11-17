package com.vivecalm.app.util;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.widget.Toast;

import java.util.Calendar;

/** Funciones pequeñas que se usan en varias pantallas. */
public final class Util {

    private static final String[] COLORES_AVATAR = {"#FFE7A3", "#CFEFD8", "#D9E6FC", "#E6DCFB", "#FFD9C7"};

    private Util() {
    }

    public static int dp(Context c, float valor) {
        return Math.round(valor * c.getResources().getDisplayMetrics().density);
    }

    /** Círculo de un color, con borde opcional (grosor 0 = sin borde). */
    public static GradientDrawable circulo(int relleno, int grosorBorde, int colorBorde) {
        GradientDrawable g = new GradientDrawable();
        g.setShape(GradientDrawable.OVAL);
        g.setColor(relleno);
        if (grosorBorde > 0) {
            g.setStroke(grosorBorde, colorBorde);
        }
        return g;
    }

    /** Círculo con borde punteado (se usa para "hoy, sin registro"). */
    public static GradientDrawable circuloPunteado(Context c, int color) {
        GradientDrawable g = new GradientDrawable();
        g.setShape(GradientDrawable.OVAL);
        g.setColor(Color.TRANSPARENT);
        g.setStroke(dp(c, 3), color, dp(c, 5), dp(c, 4));
        return g;
    }

    /** Rectángulo redondeado con borde opcional. */
    public static GradientDrawable rectangulo(Context c, int relleno, float radioDp, float bordeDp, int colorBorde) {
        GradientDrawable g = new GradientDrawable();
        g.setShape(GradientDrawable.RECTANGLE);
        g.setCornerRadius(dp(c, radioDp));
        g.setColor(relleno);
        if (bordeDp > 0) {
            g.setStroke(dp(c, bordeDp), colorBorde);
        }
        return g;
    }

    public static int colorAvatar(long id) {
        return Color.parseColor(COLORES_AVATAR[(int) (Math.abs(id) % COLORES_AVATAR.length)]);
    }

    /** "Mariana Torres" → "MT". */
    public static String iniciales(String nombre) {
        if (nombre == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (String parte : nombre.trim().split("\\s+")) {
            if (!parte.isEmpty() && Character.isLetter(parte.charAt(0)) && sb.length() < 2) {
                sb.append(Character.toUpperCase(parte.charAt(0)));
            }
        }
        return sb.toString();
    }

    public static int contarPalabras(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            return 0;
        }
        return texto.trim().split("\\s+").length;
    }

    public static String saludoSegunHora() {
        int hora = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (hora < 12) {
            return "¡Buenos días!";
        } else if (hora < 19) {
            return "¡Buenas tardes!";
        }
        return "¡Buenas noches!";
    }

    /** Abre el marcador del teléfono con el número escrito (no llama directo). */
    public static void marcar(Context c, String telefono) {
        abrir(c, new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + telefono)));
    }

    public static void abrir(Context c, Intent intent) {
        try {
            c.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(c, "No hay una app para abrir esto", Toast.LENGTH_SHORT).show();
        }
    }

    public static String vacioSiNulo(String s) {
        return s == null ? "" : s;
    }
}
