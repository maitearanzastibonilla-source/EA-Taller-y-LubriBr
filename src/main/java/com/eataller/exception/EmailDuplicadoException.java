package com.eataller.exception;

/**
 * Responsabilidad: senializar que el email indicado ya se encuentra
 * registrado para otro usuario del sistema (restriccion UNIQUE de negocio).
 * Autor: EA Taller y LubriBr - Equipo de Desarrollo
 */
public class EmailDuplicadoException extends Exception {

    public EmailDuplicadoException(String mensaje) {
        super(mensaje);
    }
}
