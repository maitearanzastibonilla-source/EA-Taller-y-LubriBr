package com.eataller.utils;

import com.eataller.constants.AppConstants;
import com.eataller.dto.UsuarioDTO;
import com.eataller.entity.RolUsuario;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * Responsabilidad: centralizar la lectura y escritura de datos de sesion
 * relacionados al usuario autenticado, evitando que cada Servlet repita la
 * logica de acceso a HttpSession.
 * Autor: EA Taller y LubriBr - Equipo de Desarrollo
 */
public final class SessionUtils {

    private SessionUtils() {
    }

    public static void iniciarSesion(HttpServletRequest request, UsuarioDTO usuario) {
        HttpSession session = request.getSession(true);
        session.setAttribute(AppConstants.SESSION_USUARIO, usuario);
        session.setAttribute(AppConstants.SESSION_ROL, usuario.getRol().name());
        session.setMaxInactiveInterval(AppConstants.SESSION_TIMEOUT_MINUTOS * 60);
    }

    public static void cerrarSesion(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }

    public static UsuarioDTO getUsuarioLogueado(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        return (UsuarioDTO) session.getAttribute(AppConstants.SESSION_USUARIO);
    }

    public static boolean haySesionActiva(HttpServletRequest request) {
        return getUsuarioLogueado(request) != null;
    }

    public static boolean esAdministrador(HttpServletRequest request) {
        UsuarioDTO usuario = getUsuarioLogueado(request);
        return usuario != null && usuario.getRol() == RolUsuario.ADMINISTRADOR;
    }
}
