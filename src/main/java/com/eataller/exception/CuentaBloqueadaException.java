package com.eataller.exception;

/**
 * Responsabilidad: senializar que la cuenta del usuario se encuentra
 * bloqueada temporalmente por exceso de intentos fallidos de inicio de
 * sesion (control de intentos fallidos exigido por la Propuesta Tecnica).
 * Autor: EA Taller y LubriBr - Equipo de Desarrollo
 */
public class CuentaBloqueadaException extends Exception {

    public CuentaBloqueadaException(String mensaje) {
        super(mensaje);
    }
}
