package com.eataller.controller;

import com.eataller.dto.UsuarioDTO;
import com.eataller.utils.SessionUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Responsabilidad: mostrar la pantalla principal posterior al inicio de
 * sesion. Los indicadores operativos (turnos, trabajos, ventas) se
 * incorporaran a medida que se desarrollen sus respectivos modulos; por
 * ahora unicamente presenta la bienvenida y la navegacion disponible.
 * Autor: EA Taller y LubriBr - Equipo de Desarrollo
 */
@WebServlet(name = "DashboardServlet", urlPatterns = {"/dashboard"})
public class DashboardServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        UsuarioDTO usuario = SessionUtils.getUsuarioLogueado(request);
        request.setAttribute("usuario", usuario);
        request.getRequestDispatcher("/WEB-INF/jsp/dashboard/dashboard.jsp").forward(request, response);
    }
}
