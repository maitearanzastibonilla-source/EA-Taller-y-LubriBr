package com.eataller.exception;

/**
 * Responsabilidad: senializar que el email o la contrasenia ingresados en el
 * login no corresponden a un usuario valido del sistema.
 * Autor: EA Taller y LubriBr - Equipo de Desarrollo
 */
public class CredencialesInvalidasException extends Exception {

    public CredencialesInvalidasException(String mensaje) {
        super(mensaje);
    }
}
