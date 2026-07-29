package com.eataller.constants;

/**
 * Constantes globales del sistema: nombres de atributos de sesion, nombre de
 * la fuente de datos JNDI y parametros de seguridad de autenticacion.
 * Autor: EA Taller y LubriBr - Equipo de Desarrollo
 */
public final class AppConstants {

    private AppConstants() {
    }

    // JNDI
    public static final String JNDI_DATASOURCE = "java:comp/env/jdbc/eaTallerDB";

    // Atributos de sesion
    public static final String SESSION_USUARIO = "usuarioLogueado";
    public static final String SESSION_ROL = "rolUsuario";

    // Atributos de request para mensajes flash
    public static final String FLASH_SUCCESS = "mensajeExito";
    public static final String FLASH_ERROR = "mensajeError";
    public static final String FLASH_WARNING = "mensajeAdvertencia";

    // Seguridad de autenticacion
    public static final int MAX_INTENTOS_FALLIDOS = 5;
    public static final int MINUTOS_BLOQUEO_CUENTA = 15;
    public static final int SESSION_TIMEOUT_MINUTOS = 30;
    public static final int BCRYPT_ROUNDS = 12;

    // Paginacion
    public static final int REGISTROS_POR_PAGINA = 20;

    // Roles
    public static final String ROL_ADMINISTRADOR = "ADMINISTRADOR";
    public static final String ROL_OPERADOR = "OPERADOR";

    // Modulo (para auditoria)
    public static final String MODULO_USUARIOS = "USUARIOS";
    public static final String MODULO_AUTENTICACION = "AUTENTICACION";
}
