package com.eataller.controller;

import com.eataller.dto.PaginaResultado;
import com.eataller.dto.ProveedorDTO;
import com.eataller.dto.ProveedorFormDTO;
import com.eataller.exception.ProveedorNoEncontradoException;
import com.eataller.exception.ValidacionException;
import com.eataller.service.ProveedorService;
import com.eataller.service.impl.ProveedorServiceImpl;
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

@WebServlet(name = "ProveedorServlet", urlPatterns = {"/proveedores", "/proveedores/*"})
public class ProveedorServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(ProveedorServlet.class.getName());
    private static final int REGISTROS_POR_PAGINA = 20;

    private final ProveedorService proveedorService = new ProveedorServiceImpl();

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
            LOGGER.log(Level.SEVERE, "Error de base de datos en ProveedorServlet (GET).", e);
            request.setAttribute("mensajeError", "No fue posible completar la operacion. Intente nuevamente.");
            request.getRequestDispatcher("/WEB-INF/jsp/error/500.jsp").forward(request, response);
        } catch (ProveedorNoEncontradoException e) {
            response.sendRedirect(request.getContextPath() + "/proveedores?error=no_encontrado");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!CsrfTokenUtils.esTokenValido(request)) {
            response.sendRedirect(request.getContextPath() + "/proveedores?error=csrf");
            return;
        }

        String accion = request.getParameter("accion");
        try {
            if ("guardar".equals(accion)) {
                guardar(request, response);
            } else if ("cambiarEstado".equals(accion)) {
                cambiarEstado(request, response);
            } else {
                response.sendRedirect(request.getContextPath() + "/proveedores");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error de base de datos en ProveedorServlet (POST).", e);
            request.setAttribute("mensajeError", "No fue posible completar la operacion. Intente nuevamente.");
            request.getRequestDispatcher("/WEB-INF/jsp/error/500.jsp").forward(request, response);
        }
    }

    private void mostrarListado(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException {

        String textoBusqueda = request.getParameter("q");
        String estadoFiltro = request.getParameter("estado");
        int pagina = parsearEntero(request.getParameter("pagina"), 1);

        PaginaResultado<ProveedorDTO> resultado = proveedorService.listar(textoBusqueda, estadoFiltro,
                pagina, REGISTROS_POR_PAGINA);

        request.setAttribute("resultado", resultado);
        request.setAttribute("q", textoBusqueda);
        request.setAttribute("estado", estadoFiltro);
        request.setAttribute("csrfToken", CsrfTokenUtils.obtenerOGenerarToken(request));
        request.getRequestDispatcher("/WEB-INF/jsp/proveedores/listado.jsp").forward(request, response);
    }

    private void mostrarFormularioAlta(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("form", new ProveedorFormDTO());
        request.setAttribute("csrfToken", CsrfTokenUtils.obtenerOGenerarToken(request));
        request.getRequestDispatcher("/WEB-INF/jsp/proveedores/form.jsp").forward(request, response);
    }

    private void mostrarFormularioEdicion(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException, ProveedorNoEncontradoException {

        Long idProveedor = parsearLong(request.getParameter("id"));
        ProveedorDTO proveedor = proveedorService.obtenerPorId(idProveedor);

        ProveedorFormDTO form = new ProveedorFormDTO();
        form.setIdProveedor(proveedor.getIdProveedor());
        form.setNombre(proveedor.getNombre());
        form.setContacto(proveedor.getContacto());
        form.setTelefono(proveedor.getTelefono());
        form.setEmail(proveedor.getEmail());

        request.setAttribute("form", form);
        request.setAttribute("proveedorActual", proveedor);
        request.setAttribute("csrfToken", CsrfTokenUtils.obtenerOGenerarToken(request));
        request.getRequestDispatcher("/WEB-INF/jsp/proveedores/form.jsp").forward(request, response);
    }

    private void guardar(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException {

        ProveedorFormDTO form = new ProveedorFormDTO();
        form.setIdProveedor(parsearLong(request.getParameter("idProveedor")));
        form.setNombre(request.getParameter("nombre"));
        form.setContacto(request.getParameter("contacto"));
        form.setTelefono(request.getParameter("telefono"));
        form.setEmail(request.getParameter("email"));

        Long idUsuarioLogueado = SessionUtils.getUsuarioLogueado(request).getIdUsuario();

        try {
            if (form.esAlta()) {
                proveedorService.crear(form, idUsuarioLogueado);
                response.sendRedirect(request.getContextPath() + "/proveedores?exito=creado");
            } else {
                proveedorService.actualizar(form, idUsuarioLogueado);
                response.sendRedirect(request.getContextPath() + "/proveedores?exito=modificado");
            }
        } catch (ValidacionException e) {
            request.setAttribute("errores", e.getErrores());
            request.setAttribute("form", form);
            request.setAttribute("csrfToken", CsrfTokenUtils.obtenerOGenerarToken(request));
            request.getRequestDispatcher("/WEB-INF/jsp/proveedores/form.jsp").forward(request, response);
        } catch (ProveedorNoEncontradoException e) {
            response.sendRedirect(request.getContextPath() + "/proveedores?error=no_encontrado");
        }
    }

    private void cambiarEstado(HttpServletRequest request, HttpServletResponse response)
            throws IOException, SQLException {

        Long idProveedor = parsearLong(request.getParameter("id"));
        boolean activo = Boolean.parseBoolean(request.getParameter("activo"));
        Long idUsuarioLogueado = SessionUtils.getUsuarioLogueado(request).getIdUsuario();

        try {
            proveedorService.cambiarEstado(idProveedor, activo, idUsuarioLogueado);
            response.sendRedirect(request.getContextPath() + "/proveedores?exito="
                    + (activo ? "activado" : "desactivado"));
        } catch (ProveedorNoEncontradoException e) {
            response.sendRedirect(request.getContextPath() + "/proveedores?error=no_encontrado");
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
