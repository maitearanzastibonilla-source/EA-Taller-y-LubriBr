package com.eataller.controller;

import com.eataller.dto.PaginaResultado;
import com.eataller.dto.UsuarioDTO;
import com.eataller.dto.UsuarioFormDTO;
import com.eataller.entity.RolUsuario;
import com.eataller.exception.EmailDuplicadoException;
import com.eataller.exception.UsuarioNoEncontradoException;
import com.eataller.exception.UsuarioSinPermisosException;
import com.eataller.exception.ValidacionException;
import com.eataller.service.UsuarioService;
import com.eataller.service.impl.UsuarioServiceImpl;
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
 * Responsabilidad: recibir las solicitudes HTTP del Modulo de Usuarios
 * (listado, alta, modificacion y baja/activacion logica). Solo obtiene
 * parametros, valida permisos de acceso e invoca UsuarioService; no
 * contiene reglas de negocio ni acceso a datos.
 *
 * El acceso a este Servlet ya esta restringido a usuarios con rol
 * ADMINISTRADOR por AuthenticationFilter.
 * Autor: EA Taller y LubriBr - Equipo de Desarrollo
 */
@WebServlet(name = "UsuarioServlet", urlPatterns = {"/usuarios", "/usuarios/*"})
public class UsuarioServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(UsuarioServlet.class.getName());
    private static final int REGISTROS_POR_PAGINA = 20;

    private final UsuarioService usuarioService = new UsuarioServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String accion = request.getParameter("accion");
        try {
            if ("nuevo".equals(accion)) {
                mostrarFormularioAlta(request, response);
            } else if ("editar".equals(accion)) {
                mostrarFormularioEdicion(request, response);
            } else {
                mostrarListado(request, response);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error de base de datos en UsuarioServlet (GET).", e);
            request.setAttribute("mensajeError", "No fue posible completar la operacion. Intente nuevamente.");
            request.getRequestDispatcher("/WEB-INF/jsp/error/500.jsp").forward(request, response);
        } catch (UsuarioNoEncontradoException e) {
            response.sendRedirect(request.getContextPath() + "/usuarios?error=no_encontrado");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!CsrfTokenUtils.esTokenValido(request)) {
            response.sendRedirect(request.getContextPath() + "/usuarios?error=csrf");
            return;
        }

        String accion = request.getParameter("accion");
        try {
            if ("guardar".equals(accion)) {
                guardar(request, response);
            } else if ("cambiarEstado".equals(accion)) {
                cambiarEstado(request, response);
            } else {
                response.sendRedirect(request.getContextPath() + "/usuarios");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error de base de datos en UsuarioServlet (POST).", e);
            request.setAttribute("mensajeError", "No fue posible completar la operacion. Intente nuevamente.");
            request.getRequestDispatcher("/WEB-INF/jsp/error/500.jsp").forward(request, response);
        }
    }

    private void mostrarListado(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException {

        String textoBusqueda = request.getParameter("q");
        String rolFiltro = request.getParameter("rol");
        String activoParam = request.getParameter("activo");
        Boolean activoFiltro = (activoParam == null || activoParam.isBlank()) ? null : Boolean.valueOf(activoParam);

        int pagina = parsearEntero(request.getParameter("pagina"), 1);

        PaginaResultado<UsuarioDTO> resultado = usuarioService.listar(textoBusqueda, rolFiltro, activoFiltro,
                pagina, REGISTROS_POR_PAGINA);

        request.setAttribute("resultado", resultado);
        request.setAttribute("q", textoBusqueda);
        request.setAttribute("rol", rolFiltro);
        request.setAttribute("activo", activoParam);
        request.setAttribute("roles", RolUsuario.values());
        request.setAttribute("csrfToken", CsrfTokenUtils.obtenerOGenerarToken(request));
        request.getRequestDispatcher("/WEB-INF/jsp/usuarios/listado.jsp").forward(request, response);
    }

    private void mostrarFormularioAlta(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("form", new UsuarioFormDTO());
        request.setAttribute("roles", RolUsuario.values());
        request.setAttribute("csrfToken", CsrfTokenUtils.obtenerOGenerarToken(request));
        request.getRequestDispatcher("/WEB-INF/jsp/usuarios/form.jsp").forward(request, response);
    }

    private void mostrarFormularioEdicion(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException, UsuarioNoEncontradoException {

        Long idUsuario = parsearLong(request.getParameter("id"));
        UsuarioDTO usuario = usuarioService.obtenerPorId(idUsuario);

        UsuarioFormDTO form = new UsuarioFormDTO();
        form.setIdUsuario(usuario.getIdUsuario());
        form.setNombre(usuario.getNombre());
        form.setEmail(usuario.getEmail());
        form.setRol(usuario.getRol().name());

        request.setAttribute("form", form);
        request.setAttribute("usuarioActual", usuario);
        request.setAttribute("roles", RolUsuario.values());
        request.setAttribute("csrfToken", CsrfTokenUtils.obtenerOGenerarToken(request));
        request.getRequestDispatcher("/WEB-INF/jsp/usuarios/form.jsp").forward(request, response);
    }

    private void guardar(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException {

        UsuarioFormDTO form = new UsuarioFormDTO();
        form.setIdUsuario(parsearLong(request.getParameter("idUsuario")));
        form.setNombre(request.getParameter("nombre"));
        form.setEmail(request.getParameter("email"));
        form.setPassword(request.getParameter("password") == null ? "" : request.getParameter("password"));
        form.setConfirmarPassword(request.getParameter("confirmarPassword") == null ? "" : request.getParameter("confirmarPassword"));
        form.setRol(request.getParameter("rol"));

        Long idUsuarioLogueado = SessionUtils.getUsuarioLogueado(request).getIdUsuario();

        try {
            if (form.esAlta()) {
                usuarioService.crear(form, idUsuarioLogueado);
                response.sendRedirect(request.getContextPath() + "/usuarios?exito=creado");
            } else {
                usuarioService.actualizar(form, idUsuarioLogueado);
                response.sendRedirect(request.getContextPath() + "/usuarios?exito=modificado");
            }
        } catch (ValidacionException e) {
            request.setAttribute("errores", e.getErrores());
            request.setAttribute("form", form);
            request.setAttribute("roles", RolUsuario.values());
            request.setAttribute("csrfToken", CsrfTokenUtils.obtenerOGenerarToken(request));
            request.getRequestDispatcher("/WEB-INF/jsp/usuarios/form.jsp").forward(request, response);
        } catch (EmailDuplicadoException | UsuarioSinPermisosException e) {
            request.setAttribute("mensajeError", e.getMessage());
            request.setAttribute("form", form);
            request.setAttribute("roles", RolUsuario.values());
            request.setAttribute("csrfToken", CsrfTokenUtils.obtenerOGenerarToken(request));
            request.getRequestDispatcher("/WEB-INF/jsp/usuarios/form.jsp").forward(request, response);
        } catch (UsuarioNoEncontradoException e) {
            response.sendRedirect(request.getContextPath() + "/usuarios?error=no_encontrado");
        }
    }

    private void cambiarEstado(HttpServletRequest request, HttpServletResponse response)
            throws IOException, SQLException {

        Long idUsuario = parsearLong(request.getParameter("id"));
        boolean activo = Boolean.parseBoolean(request.getParameter("activo"));
        Long idUsuarioLogueado = SessionUtils.getUsuarioLogueado(request).getIdUsuario();

        try {
            usuarioService.cambiarEstado(idUsuario, activo, idUsuarioLogueado);
            response.sendRedirect(request.getContextPath() + "/usuarios?exito="
                    + (activo ? "activado" : "desactivado"));
        } catch (UsuarioNoEncontradoException e) {
            response.sendRedirect(request.getContextPath() + "/usuarios?error=no_encontrado");
        } catch (UsuarioSinPermisosException e) {
            response.sendRedirect(request.getContextPath() + "/usuarios?error=sin_permisos");
        }
    }

    private Long parsearLong(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        try {
            return Long.valueOf(valor);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private int parsearEntero(String valor, int porDefecto) {
        if (valor == null || valor.isBlank()) {
            return porDefecto;
        }
        try {
            return Integer.parseInt(valor);
        } catch (NumberFormatException e) {
            return porDefecto;
        }
    }
}
