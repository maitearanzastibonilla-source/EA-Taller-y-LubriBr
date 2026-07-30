package com.eataller.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Responsabilidad: exponer funciones EL reutilizables por las vistas JSP de
 * cualquier modulo (formato de fechas), registradas mediante el descriptor
 * WEB-INF/tags/ea-functions.tld.
 * Autor: EA Taller y LubriBr - Equipo de Desarrollo
 */
public final class JspFunctions {

    private static final DateTimeFormatter FORMATO_FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private JspFunctions() {
    }

    public static String fechaHora(LocalDateTime fecha) {
        return fecha == null ? "-" : fecha.format(FORMATO_FECHA_HORA);
    }

    public static String fecha(LocalDateTime fecha) {
        return fecha == null ? "-" : fecha.format(FORMATO_FECHA);
    }

    public static String fechaDia(LocalDate fecha) {
        return fecha == null ? "-" : fecha.format(FORMATO_FECHA);
    }
}
