package com.eataller.exception;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Responsabilidad: representar errores de validacion de datos de entrada,
 * agrupando todos los campos invalidos de un mismo formulario en un unico
 * objeto para que la vista pueda mostrarlos junto a cada campo.
 * Autor: EA Taller y LubriBr - Equipo de Desarrollo
 */
public class ValidacionException extends Exception {

    private final Map<String, String> errores;

    public ValidacionException(Map<String, String> errores) {
        super("Existen errores de validacion en el formulario.");
        this.errores = new LinkedHashMap<>(errores);
    }

    public Map<String, String> getErrores() {
        return errores;
    }
}
