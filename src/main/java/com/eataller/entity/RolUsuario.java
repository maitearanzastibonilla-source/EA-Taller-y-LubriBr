package com.eataller.entity;

/**
 * Responsabilidad: representar los roles de acceso al sistema definidos en la
 * Propuesta Tecnica: administrador (acceso completo) y operador (funciones
 * del dia a dia, sin configuracion avanzada ni reportes financieros).
 * Autor: EA Taller y LubriBr - Equipo de Desarrollo
 */
public enum RolUsuario {

    ADMINISTRADOR("Administrador"),
    OPERADOR("Operador");

    private final String etiqueta;

    RolUsuario(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    /**
     * Convierte el valor almacenado en la columna rol de la tabla usuarios
     * a su enum correspondiente.
     *
     * @param valor valor textual almacenado en base de datos
     * @return el RolUsuario correspondiente
     * @throws IllegalArgumentException si el valor no coincide con ningun rol valido
     */
    public static RolUsuario desdeValorBD(String valor) {
        return RolUsuario.valueOf(valor.trim().toUpperCase());
    }
}
