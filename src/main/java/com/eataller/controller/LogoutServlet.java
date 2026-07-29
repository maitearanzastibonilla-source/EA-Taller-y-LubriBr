package com.eataller.controller;

import com.eataller.utils.SessionUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Responsabilidad: cerrar la sesion activa del usuario e invalidarla en el
 * servidor.
 * Autor: EA Taller y LubriBr - Equipo de Desarrollo
 */
@WebServlet(name = "LogoutServlet", urlPatterns = {"/logout"})
public class LogoutServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        SessionUtils.cerrarSesion(request);
        response.sendRedirect(request.getContextPath() + "/login");
    }
}
