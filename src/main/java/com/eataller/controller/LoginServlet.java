package com.eataller.controller;

import com.eataller.dto.LoginRequestDTO;
import com.eataller.dto.UsuarioDTO;
import com.eataller.exception.CredencialesInvalidasException;
import com.eataller.exception.CuentaBloqueadaException;
import com.eataller.service.AuthService;
import com.eataller.service.impl.AuthServiceImpl;
import com.eataller.utils.CsrfTokenUtils;
import com.eataller.utils.SessionUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Responsabilidad: recibir las solicitudes HTTP de inicio y verificacion de
 * sesion. Unicamente obtiene parametros, invoca AuthService y redirige segun
 * el resultado; no contiene logica de negocio.
 * Autor: EA Taller y LubriBr - Equipo de Desarrollo
 */
@WebServlet(name = "LoginServlet", urlPatterns = {"/login"})
public class LoginServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(LoginServlet.class.getName());

    private final AuthService authService = new AuthServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (SessionUtils.haySesionActiva(request)) {
            response.sendRedirect(request.getContextPath() + "/dashboard");
            return;
        }

        String codigoError = request.getParameter("error");
        if ("sesion_requerida".equals(codigoError)) {
            request.setAttribute("mensajeError", "Debe iniciar sesion para continuar.");
        }

        request.setAttribute("csrfToken", CsrfTokenUtils.obtenerOGenerarToken(request));
        request.getRequestDispatcher("/WEB-INF/jsp/auth/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!CsrfTokenUtils.esTokenValido(request)) {
            request.setAttribute("mensajeError", "La sesion del formulario expiro. Intente nuevamente.");
            request.setAttribute("csrfToken", CsrfTokenUtils.obtenerOGenerarToken(request));
            request.getRequestDispatcher("/WEB-INF/jsp/auth/login.jsp").forward(request, response);
            return;
        }

        LoginRequestDTO loginRequest = new LoginRequestDTO(
                request.getParameter("email"),
                request.getParameter("password"));

        try {
            UsuarioDTO usuario = authService.autenticar(loginRequest);
            SessionUtils.iniciarSesion(request, usuario);
            response.sendRedirect(request.getContextPath() + "/dashboard");
        } catch (CredencialesInvalidasException | CuentaBloqueadaException e) {
            request.setAttribute("mensajeError", e.getMessage());
            request.setAttribute("emailIngresado", loginRequest.getEmail());
            request.setAttribute("csrfToken", CsrfTokenUtils.obtenerOGenerarToken(request));
            request.getRequestDispatcher("/WEB-INF/jsp/auth/login.jsp").forward(request, response);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error de base de datos durante el inicio de sesion.", e);
            request.setAttribute("mensajeError", "No fue posible conectar con el sistema. Intente nuevamente en unos minutos.");
            request.setAttribute("csrfToken", CsrfTokenUtils.obtenerOGenerarToken(request));
            request.getRequestDispatcher("/WEB-INF/jsp/auth/login.jsp").forward(request, response);
        }
    }
}
