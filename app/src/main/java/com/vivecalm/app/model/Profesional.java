package com.vivecalm.app.model;

/** Una fila de la tabla profesionales. */
public class Profesional {
    public static final String PSICOLOGIA = "Psicología";
    public static final String PSIQUIATRIA = "Psiquiatría";

    public long id;
    public String nombre;
    public String profesion;
    public String cedula;
    public String especialidades;
    public String telefono;
    public String correo;
    public String ciudad;
    public boolean enLinea;
    public boolean presencial;
    public String horario;
    public String precio;
    public String descripcion;
    public boolean verificado;

    /** "Psicóloga/o" o "Psiquiatra" para mostrar en pantalla. */
    public String titulo() {
        return PSIQUIATRIA.equals(profesion) ? "Psiquiatra" : "Psicólogo/a";
    }

    /** Ejemplo: "En línea y presencial · Guadalajara". */
    public String modalidad() {
        String texto;
        if (enLinea && presencial) {
            texto = "En línea y presencial";
        } else if (enLinea) {
            texto = "En línea";
        } else {
            texto = "Presencial";
        }
        if (presencial && ciudad != null && !ciudad.isEmpty()) {
            texto += " · " + ciudad;
        }
        return texto;
    }

    /** Primera especialidad de la lista, para la tarjeta del directorio. */
    public String especialidadPrincipal() {
        if (especialidades == null || especialidades.trim().isEmpty()) {
            return "";
        }
        return especialidades.split(",")[0].trim();
    }
}
