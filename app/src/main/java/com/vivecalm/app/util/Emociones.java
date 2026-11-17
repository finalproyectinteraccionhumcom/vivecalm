package com.vivecalm.app.util;

import com.vivecalm.app.R;

/** Catálogo de emociones. El índice del arreglo es el valor que se guarda en la base de datos. */
public final class Emociones {

    public static final int FELIZ = 0;
    public static final int TRANQUI = 1;
    public static final int TRISTE = 2;
    public static final int ANSIOSO = 3;
    public static final int ENOJADO = 4;

    public static final String[] NOMBRES = {"Feliz", "Tranqui", "Triste", "Ansioso", "Enojado"};

    public static final int[] COLORES = {
            R.color.emo_feliz, R.color.emo_tranqui, R.color.emo_triste,
            R.color.emo_ansioso, R.color.emo_enojado
    };

    public static final int[] ICONOS = {
            R.drawable.emo_feliz, R.drawable.emo_tranqui, R.drawable.emo_triste,
            R.drawable.emo_ansioso, R.drawable.emo_enojado
    };

    public static final String[] TEMAS = {
            "Escuela", "Trabajo", "Familia", "Amigos", "Pareja", "Sueño", "Salud", "Dinero"
    };

    private Emociones() {
    }

    /** Las emociones difíciles se usan para sugerir respiración o hablar con alguien. */
    public static boolean esDificil(int emocion) {
        return emocion == TRISTE || emocion == ANSIOSO || emocion == ENOJADO;
    }
}
