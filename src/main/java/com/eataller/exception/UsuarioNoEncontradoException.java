package com.eataller.exception;

/**
 * Responsabilidad: senializar que no existe un usuario con el identificador
 * o email solicitado.
 * Autor: EA Taller y LubriBr - Equipo de Desarrollo
 */
public class UsuarioNoEncontradoException extends Exception {

    public UsuarioNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
