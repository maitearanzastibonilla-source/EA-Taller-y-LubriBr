package com.eataller.listener;

import jakarta.servlet.annotation.WebListener;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Responsabilidad: registrar en el log la creacion y destruccion de
 * sesiones HTTP, como parte de los logs de seguridad exigidos por la
 * Propuesta Tecnica (control de sesiones).
 * Autor: EA Taller y LubriBr - Equipo de Desarrollo
 */
@WebListener
public class SessionActivityListener implements HttpSessionListener {

    private static final Logger LOGGER = Logger.getLogger(SessionActivityListener.class.getName());

    @Override
    public void sessionCreated(HttpSessionEvent se) {
        LOGGER.log(Level.FINE, "Sesion creada: {0}", se.getSession().getId());
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        LOGGER.log(Level.FINE, "Sesion finalizada: {0}", se.getSession().getId());
    }
}
