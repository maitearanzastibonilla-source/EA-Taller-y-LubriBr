package com.eataller.exception;

/**
 * Responsabilidad: senializar que el usuario autenticado no posee el rol
 * requerido para ejecutar la operacion solicitada (por ejemplo, un operador
 * intentando administrar usuarios, funcion exclusiva de administradores).
 * Autor: EA Taller y LubriBr - Equipo de Desarrollo
 */
public class UsuarioSinPermisosException extends Exception {

    public UsuarioSinPermisosException(String mensaje) {
        super(mensaje);
    }
}
