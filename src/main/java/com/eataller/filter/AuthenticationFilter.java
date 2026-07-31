package com.eataller.filter;

import com.eataller.constants.AppConstants;
import com.eataller.dto.UsuarioDTO;
import com.eataller.entity.RolUsuario;
import com.eataller.utils.SessionUtils;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Responsabilidad: controlar el acceso a la aplicacion. Exige sesion activa
 * para cualquier recurso que no sea publico, y restringe las rutas de
 * administracion (/usuarios/*) al rol ADMINISTRADOR, conforme a la matriz de
 * permisos de la Propuesta Tecnica.
 * Registrado explicitamente en web.xml (despues de EncodingFilter) para
 * garantizar un orden de ejecucion deterministico.
 * Autor: EA Taller y LubriBr - Equipo de Desarrollo
 */
public class AuthenticationFilter implements Filter {

    private static final Logger LOGGER = Logger.getLogger(AuthenticationFilter.class.getName());

    private static final Set<String> RUTAS_PUBLICAS = Set.of(
            "/", "/login", "/index.jsp"
    );

    private static final Set<String> PREFIJOS_PUBLICOS = Set.of(
            "/assets/"
    );

    private static final Set<String> PREFIJOS_SOLO_ADMINISTRADOR = Set.of(
            "/usuarios", "/reportes"
    );

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        String rutaRelativa = request.getRequestURI().substring(request.getContextPath().length());
        if (rutaRelativa.isEmpty()) {
            rutaRelativa = "/";
        }

        if (esRutaPublica(rutaRelativa)) {
            chain.doFilter(req, res);
            return;
        }

        if (!SessionUtils.haySesionActiva(request)) {
            LOGGER.log(Level.INFO, "Acceso no autenticado bloqueado a {0}", rutaRelativa);
            response.sendRedirect(request.getContextPath() + "/login?error=sesion_requerida");
            return;
        }

        if (requiereRolAdministrador(rutaRelativa)) {
            UsuarioDTO usuario = SessionUtils.getUsuarioLogueado(request);
            if (usuario.getRol() != RolUsuario.ADMINISTRADOR) {
                LOGGER.log(Level.WARNING, "Usuario {0} intento acceder a {1} sin permisos de administrador.",
                        new Object[]{usuario.getEmail(), rutaRelativa});
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
        }

        chain.doFilter(req, res);
    }

    private boolean esRutaPublica(String ruta) {
        if (RUTAS_PUBLICAS.contains(ruta)) {
            return true;
        }
        return PREFIJOS_PUBLICOS.stream().anyMatch(ruta::startsWith);
    }

    private boolean requiereRolAdministrador(String ruta) {
        return PREFIJOS_SOLO_ADMINISTRADOR.stream().anyMatch(ruta::startsWith);
    }
}
