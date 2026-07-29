package com.eataller.controller;

import com.eataller.dto.ClienteDTO;
import com.eataller.dto.ClienteFormDTO;
import com.eataller.dto.PaginaResultado;
import com.eataller.entity.RolUsuario;
import com.eataller.exception.ClienteNoEncontradoException;
import com.eataller.exception.DatoDuplicadoException;
import com.eataller.exception.UsuarioSinPermisosException;
import com.eataller.exception.ValidacionException;
import com.eataller.service.ClienteService;
import com.eataller.service.impl.ClienteServiceImpl;
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

@WebServlet(name = "ClienteServlet", urlPatterns = {"/clientes", "/clientes/*"})
public class ClienteServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(ClienteServlet.class.getName());
    private static final int REGISTROS_POR_PAGINA = 20;

    private final ClienteService clienteService = new ClienteServiceImpl();

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
            LOGGER.log(Level.SEVERE, "Error de base de datos en ClienteServlet (GET).", e);
            request.setAttribute("mensajeError", "No fue posible completar la operacion. Intente nuevamente.");
            request.getRequestDispatcher("/WEB-INF/jsp/error/500.jsp").forward(request, response);
        } catch (ClienteNoEncontradoException e) {
            response.sendRedirect(request.getContextPath() + "/clientes?error=no_encontrado");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!CsrfTokenUtils.esTokenValido(request)) {
            response.sendRedirect(request.getContextPath() + "/clientes?error=csrf");
            return;
        }

        String accion = request.getParameter("accion");
        try {
            if ("guardar".equals(accion)) {
                guardar(request, response);
            } else if ("cambiarEstado".equals(accion)) {
                cambiarEstado(request, response);
            } else {
                response.sendRedirect(request.getContextPath() + "/clientes");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error de base de datos en ClienteServlet (POST).", e);
            request.setAttribute("mensajeError", "No fue posible completar la operacion. Intente nuevamente.");
            request.getRequestDispatcher("/WEB-INF/jsp/error/500.jsp").forward(request, response);
        }
    }

    private void mostrarListado(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException {

        String textoBusqueda = request.getParameter("q");
        String estadoFiltro = request.getParameter("estado");
        int pagina = parsearEntero(request.getParameter("pagina"), 1);

        PaginaResultado<ClienteDTO> resultado = clienteService.listar(textoBusqueda, estadoFiltro,
                pagina, REGISTROS_POR_PAGINA);

        request.setAttribute("resultado", resultado);
        request.setAttribute("q", textoBusqueda);
        request.setAttribute("estado", estadoFiltro);
        request.setAttribute("csrfToken", CsrfTokenUtils.obtenerOGenerarToken(request));
        request.getRequestDispatcher("/WEB-INF/jsp/clientes/listado.jsp").forward(request, response);
    }

    private void mostrarFormularioAlta(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("form", new ClienteFormDTO());
        request.setAttribute("csrfToken", CsrfTokenUtils.obtenerOGenerarToken(request));
        request.getRequestDispatcher("/WEB-INF/jsp/clientes/form.jsp").forward(request, response);
    }

    private void mostrarFormularioEdicion(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException, ClienteNoEncontradoException {

        Long idCliente = parsearLong(request.getParameter("id"));
        ClienteDTO cliente = clienteService.obtenerPorId(idCliente);

        ClienteFormDTO form = new ClienteFormDTO();
        form.setIdCliente(cliente.getIdCliente());
        form.setNombre(cliente.getNombre());
        form.setApellido(cliente.getApellido());
        form.setDni(cliente.getDni());
        form.setEmail(cliente.getEmail());
        form.setTelefono(cliente.getTelefono());
        form.setDireccion(cliente.getDireccion());

        request.setAttribute("form", form);
        request.setAttribute("clienteActual", cliente);
        request.setAttribute("csrfToken", CsrfTokenUtils.obtenerOGenerarToken(request));
        request.getRequestDispatcher("/WEB-INF/jsp/clientes/form.jsp").forward(request, response);
    }

    private void guardar(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException {

        ClienteFormDTO form = new ClienteFormDTO();
        form.setIdCliente(parsearLong(request.getParameter("idCliente")));
        form.setNombre(request.getParameter("nombre"));
        form.setApellido(request.getParameter("apellido"));
        form.setDni(request.getParameter("dni"));
        form.setEmail(request.getParameter("email"));
        form.setTelefono(request.getParameter("telefono"));
        form.setDireccion(request.getParameter("direccion"));

        Long idUsuarioLogueado = SessionUtils.getUsuarioLogueado(request).getIdUsuario();
        RolUsuario rolLogueado = SessionUtils.getUsuarioLogueado(request).getRol();

        try {
            if (form.esAlta()) {
                clienteService.crear(form, idUsuarioLogueado);
                response.sendRedirect(request.getContextPath() + "/clientes?exito=creado");
            } else {
                clienteService.actualizar(form, idUsuarioLogueado, rolLogueado);
                response.sendRedirect(request.getContextPath() + "/clientes?exito=modificado");
            }
        } catch (ValidacionException e) {
            request.setAttribute("errores", e.getErrores());
            request.setAttribute("form", form);
            request.setAttribute("csrfToken", CsrfTokenUtils.obtenerOGenerarToken(request));
            request.getRequestDispatcher("/WEB-INF/jsp/clientes/form.jsp").forward(request, response);
        } catch (DatoDuplicadoException | UsuarioSinPermisosException e) {
            request.setAttribute("mensajeError", e.getMessage());
            request.setAttribute("form", form);
            request.setAttribute("csrfToken", CsrfTokenUtils.obtenerOGenerarToken(request));
            request.getRequestDispatcher("/WEB-INF/jsp/clientes/form.jsp").forward(request, response);
        } catch (ClienteNoEncontradoException e) {
            response.sendRedirect(request.getContextPath() + "/clientes?error=no_encontrado");
        }
    }

    private void cambiarEstado(HttpServletRequest request, HttpServletResponse response)
            throws IOException, SQLException {

        Long idCliente = parsearLong(request.getParameter("id"));
        boolean activo = Boolean.parseBoolean(request.getParameter("activo"));
        Long idUsuarioLogueado = SessionUtils.getUsuarioLogueado(request).getIdUsuario();
        RolUsuario rolLogueado = SessionUtils.getUsuarioLogueado(request).getRol();

        try {
            clienteService.cambiarEstado(idCliente, activo, idUsuarioLogueado, rolLogueado);
            response.sendRedirect(request.getContextPath() + "/clientes?exito="
                    + (activo ? "activado" : "desactivado"));
        } catch (ClienteNoEncontradoException e) {
            response.sendRedirect(request.getContextPath() + "/clientes?error=no_encontrado");
        } catch (UsuarioSinPermisosException e) {
            response.sendRedirect(request.getContextPath() + "/clientes?error=sin_permisos");
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
