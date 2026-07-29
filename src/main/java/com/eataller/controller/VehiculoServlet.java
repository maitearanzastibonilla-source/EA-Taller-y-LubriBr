package com.eataller.controller;

import com.eataller.dto.ClienteDTO;
import com.eataller.dto.PaginaResultado;
import com.eataller.dto.VehiculoDTO;
import com.eataller.dto.VehiculoFormDTO;
import com.eataller.entity.RolUsuario;
import com.eataller.exception.ClienteNoEncontradoException;
import com.eataller.exception.DatoDuplicadoException;
import com.eataller.exception.UsuarioSinPermisosException;
import com.eataller.exception.ValidacionException;
import com.eataller.exception.VehiculoNoEncontradoException;
import com.eataller.service.ClienteService;
import com.eataller.service.VehiculoService;
import com.eataller.service.impl.ClienteServiceImpl;
import com.eataller.service.impl.VehiculoServiceImpl;
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

@WebServlet(name = "VehiculoServlet", urlPatterns = {"/vehiculos", "/vehiculos/*"})
public class VehiculoServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(VehiculoServlet.class.getName());
    private static final int REGISTROS_POR_PAGINA = 20;
    private static final int MAX_CLIENTES_SELECT = 1000;

    private final VehiculoService vehiculoService = new VehiculoServiceImpl();
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
            LOGGER.log(Level.SEVERE, "Error de base de datos en VehiculoServlet (GET).", e);
            request.setAttribute("mensajeError", "No fue posible completar la operacion. Intente nuevamente.");
            request.getRequestDispatcher("/WEB-INF/jsp/error/500.jsp").forward(request, response);
        } catch (VehiculoNoEncontradoException e) {
            response.sendRedirect(request.getContextPath() + "/vehiculos?error=no_encontrado");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!CsrfTokenUtils.esTokenValido(request)) {
            response.sendRedirect(request.getContextPath() + "/vehiculos?error=csrf");
            return;
        }

        String accion = request.getParameter("accion");
        try {
            if ("guardar".equals(accion)) {
                guardar(request, response);
            } else if ("cambiarEstado".equals(accion)) {
                cambiarEstado(request, response);
            } else {
                response.sendRedirect(request.getContextPath() + "/vehiculos");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error de base de datos en VehiculoServlet (POST).", e);
            request.setAttribute("mensajeError", "No fue posible completar la operacion. Intente nuevamente.");
            request.getRequestDispatcher("/WEB-INF/jsp/error/500.jsp").forward(request, response);
        }
    }

    private void mostrarListado(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException {

        String textoBusqueda = request.getParameter("q");
        String estadoFiltro = request.getParameter("estado");
        int pagina = parsearEntero(request.getParameter("pagina"), 1);

        PaginaResultado<VehiculoDTO> resultado = vehiculoService.listar(textoBusqueda, estadoFiltro, null,
                pagina, REGISTROS_POR_PAGINA);

        request.setAttribute("resultado", resultado);
        request.setAttribute("q", textoBusqueda);
        request.setAttribute("estado", estadoFiltro);
        request.setAttribute("csrfToken", CsrfTokenUtils.obtenerOGenerarToken(request));
        request.getRequestDispatcher("/WEB-INF/jsp/vehiculos/listado.jsp").forward(request, response);
    }

    private void mostrarFormularioAlta(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException {

        VehiculoFormDTO form = new VehiculoFormDTO();
        Long clienteIdPreseleccionado = parsearLong(request.getParameter("clienteId"));
        form.setClienteId(clienteIdPreseleccionado);

        request.setAttribute("form", form);
        request.setAttribute("clientes", listarClientesParaSelect());
        request.setAttribute("csrfToken", CsrfTokenUtils.obtenerOGenerarToken(request));
        request.getRequestDispatcher("/WEB-INF/jsp/vehiculos/form.jsp").forward(request, response);
    }

    private void mostrarFormularioEdicion(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException, VehiculoNoEncontradoException {

        Long idVehiculo = parsearLong(request.getParameter("id"));
        VehiculoDTO vehiculo = vehiculoService.obtenerPorId(idVehiculo);

        VehiculoFormDTO form = new VehiculoFormDTO();
        form.setIdVehiculo(vehiculo.getIdVehiculo());
        form.setClienteId(vehiculo.getClienteId());
        form.setPatente(vehiculo.getPatente());
        form.setMarca(vehiculo.getMarca());
        form.setModelo(vehiculo.getModelo());
        form.setAnio(String.valueOf(vehiculo.getAnio()));

        request.setAttribute("form", form);
        request.setAttribute("vehiculoActual", vehiculo);
        request.setAttribute("clientes", listarClientesParaSelect());
        request.setAttribute("csrfToken", CsrfTokenUtils.obtenerOGenerarToken(request));
        request.getRequestDispatcher("/WEB-INF/jsp/vehiculos/form.jsp").forward(request, response);
    }

    private void guardar(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException {

        VehiculoFormDTO form = new VehiculoFormDTO();
        form.setIdVehiculo(parsearLong(request.getParameter("idVehiculo")));
        form.setClienteId(parsearLong(request.getParameter("clienteId")));
        form.setPatente(request.getParameter("patente"));
        form.setMarca(request.getParameter("marca"));
        form.setModelo(request.getParameter("modelo"));
        form.setAnio(request.getParameter("anio"));

        Long idUsuarioLogueado = SessionUtils.getUsuarioLogueado(request).getIdUsuario();
        RolUsuario rolLogueado = SessionUtils.getUsuarioLogueado(request).getRol();

        try {
            if (form.esAlta()) {
                vehiculoService.crear(form, idUsuarioLogueado);
                response.sendRedirect(request.getContextPath() + "/vehiculos?exito=creado");
            } else {
                vehiculoService.actualizar(form, idUsuarioLogueado, rolLogueado);
                response.sendRedirect(request.getContextPath() + "/vehiculos?exito=modificado");
            }
        } catch (ValidacionException e) {
            request.setAttribute("errores", e.getErrores());
            request.setAttribute("form", form);
            request.setAttribute("clientes", listarClientesParaSelect());
            request.setAttribute("csrfToken", CsrfTokenUtils.obtenerOGenerarToken(request));
            request.getRequestDispatcher("/WEB-INF/jsp/vehiculos/form.jsp").forward(request, response);
        } catch (DatoDuplicadoException | UsuarioSinPermisosException | ClienteNoEncontradoException e) {
            request.setAttribute("mensajeError", e.getMessage());
            request.setAttribute("form", form);
            request.setAttribute("clientes", listarClientesParaSelect());
            request.setAttribute("csrfToken", CsrfTokenUtils.obtenerOGenerarToken(request));
            request.getRequestDispatcher("/WEB-INF/jsp/vehiculos/form.jsp").forward(request, response);
        } catch (VehiculoNoEncontradoException e) {
            response.sendRedirect(request.getContextPath() + "/vehiculos?error=no_encontrado");
        }
    }

    private void cambiarEstado(HttpServletRequest request, HttpServletResponse response)
            throws IOException, SQLException {

        Long idVehiculo = parsearLong(request.getParameter("id"));
        boolean activo = Boolean.parseBoolean(request.getParameter("activo"));
        Long idUsuarioLogueado = SessionUtils.getUsuarioLogueado(request).getIdUsuario();

        try {
            vehiculoService.cambiarEstado(idVehiculo, activo, idUsuarioLogueado);
            response.sendRedirect(request.getContextPath() + "/vehiculos?exito="
                    + (activo ? "activado" : "desactivado"));
        } catch (VehiculoNoEncontradoException e) {
            response.sendRedirect(request.getContextPath() + "/vehiculos?error=no_encontrado");
        }
    }

    private PaginaResultado<ClienteDTO> listarClientesParaSelect() throws SQLException {
        return clienteService.listar(null, "ACTIVO", 1, MAX_CLIENTES_SELECT);
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
