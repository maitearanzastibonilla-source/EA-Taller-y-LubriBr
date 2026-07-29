package com.eataller.utils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.util.UUID;

/**
 * Responsabilidad: generar y validar el token anti-CSRF utilizado en todos
 * los formularios que ejecutan operaciones de escritura (POST), evitando que
 * un sitio externo pueda enviar solicitudes falsificadas en nombre de un
 * usuario autenticado.
 * Autor: EA Taller y LubriBr - Equipo de Desarrollo
 */
public final class CsrfTokenUtils {

    private static final String ATRIBUTO_TOKEN = "csrfToken";

    private CsrfTokenUtils() {
    }

    /**
     * Obtiene el token CSRF vigente de la sesion, generando uno nuevo si aun
     * no existe.
     */
    public static String obtenerOGenerarToken(HttpServletRequest request) {
        HttpSession session = request.getSession(true);
        String token = (String) session.getAttribute(ATRIBUTO_TOKEN);
        if (token == null) {
            token = UUID.randomUUID().toString();
            session.setAttribute(ATRIBUTO_TOKEN, token);
        }
        return token;
    }

    /**
     * Valida que el token recibido en el formulario coincida con el
     * almacenado en la sesion del usuario.
     */
    public static boolean esTokenValido(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return false;
        }
        String tokenSesion = (String) session.getAttribute(ATRIBUTO_TOKEN);
        String tokenRecibido = request.getParameter(ATRIBUTO_TOKEN);
        return tokenSesion != null && tokenSesion.equals(tokenRecibido);
    }
}
