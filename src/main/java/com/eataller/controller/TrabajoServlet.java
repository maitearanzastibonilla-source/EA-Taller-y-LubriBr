package com.eataller.controller;

import com.eataller.dto.ClienteDTO;
import com.eataller.dto.PaginaResultado;
import com.eataller.dto.TrabajoDTO;
import com.eataller.dto.TrabajoFormDTO;
import com.eataller.dto.VehiculoDTO;
import com.eataller.entity.RolUsuario;
import com.eataller.exception.TrabajoNoEncontradoException;
import com.eataller.exception.UsuarioSinPermisosException;
import com.eataller.exception.ValidacionException;
import com.eataller.exception.VehiculoNoEncontradoException;
import com.eataller.service.ClienteService;
import com.eataller.service.ItemTrabajoService;
import com.eataller.service.TrabajoService;
import com.eataller.service.TurnoService;
import com.eataller.service.VehiculoService;
import com.eataller.service.impl.ClienteServiceImpl;
import com.eataller.service.impl.ItemTrabajoServiceImpl;
import com.eataller.service.impl.TrabajoServiceImpl;
import com.eataller.service.impl.TurnoServiceImpl;
import com.eataller.service.impl.VehiculoServiceImpl;
import com.eataller.exception.TurnoNoEncontradoException;
import com.eataller.utils.CsrfTokenUtils;
import com.eataller.utils.SessionUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet(name = "TrabajoServlet", urlPatterns = {"/trabajos", "/trabajos/*"})
public class TrabajoServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(TrabajoServlet.class.getName());
    private static final int REGISTROS_POR_PAGINA = 20;
    private static final int MAX_SELECT = 1000;

    private final TrabajoService trabajoService = new TrabajoServiceImpl();
    private final ClienteService clienteService = new ClienteServiceImpl();
    private final VehiculoService vehiculoService = new VehiculoServiceImpl();
    private final TurnoService turnoService = new TurnoServiceImpl();
    private final ItemTrabajoService itemTrabajoService = new ItemTrabajoServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String accion = request.getParameter("accion");
        try {
            if ("nuevo".equals(accion)) {
                mostrarFormularioAlta(request, response);
            } else if ("editar".equals(accion)) {
                mostrarFormularioEdicion(request, response);
            } else if ("detalle".equals(accion)) {
                mostrarDetalle(request, response);
            } else {
                mostrarListado(request, response);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error de base de datos en TrabajoServlet (GET).", e);
            request.setAttribute("mensajeError", "No fue posible completar la operacion. Intente nuevamente.");
            request.getRequestDispatcher("/WEB-INF/jsp/error/500.jsp").forward(request, response);
        } catch (TrabajoNoEncontradoException e) {
            response.sendRedirect(request.getContextPath() + "/trabajos?error=no_encontrado");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!CsrfTokenUtils.esTokenValido(request)) {
            response.sendRedirect(request.getContextPath() + "/trabajos?error=csrf");
            return;
        }

        String accion = request.getParameter("accion");
        try {
            if ("guardar".equals(accion)) {
                guardar(request, response);
            } else if ("finalizar".equals(accion)) {
                finalizar(request, response);
            } else if ("reabrir".equals(accion)) {
                reabrir(request, response);
            } else if ("baja".equals(accion)) {
                darDeBaja(request, response);
            } else {
                response.sendRedirect(request.getContextPath() + "/trabajos");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error de base de datos en TrabajoServlet (POST).", e);
            request.setAttribute("mensajeError", "No fue posible completar la operacion. Intente nuevamente.");
            request.getRequestDispatcher("/WEB-INF/jsp/error/500.jsp").forward(request, response);
        }
    }

    private void mostrarListado(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException {

        String textoBusqueda = request.getParameter("q");
        String estadoFiltro = request.getParameter("estado");
        int pagina = parsearEntero(request.getParameter("pagina"), 1);

        PaginaResultado<TrabajoDTO> resultado = trabajoService.listar(textoBusqueda, estadoFiltro, null,
                pagina, REGISTROS_POR_PAGINA);

        request.setAttribute("resultado", resultado);
        request.setAttribute("q", textoBusqueda);
        request.setAttribute("estado", estadoFiltro);
        request.setAttribute("csrfToken", CsrfTokenUtils.obtenerOGenerarToken(request));
        request.getRequestDispatcher("/WEB-INF/jsp/trabajos/listado.jsp").forward(request, response);
    }

    private void mostrarFormularioAlta(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException {

        Long clienteId = parsearLong(request.getParameter("clienteId"));
        Long turnoId = parsearLong(request.getParameter("turnoId"));

        TrabajoFormDTO form = new TrabajoFormDTO();
        form.setFechaIngreso(LocalDate.now().toString());
        form.setTurnoId(turnoId);

        if (turnoId != null) {
            try {
                var turno = turnoService.obtenerPorId(turnoId);
                form.setVehiculoId(turno.getVehiculoId());
                request.setAttribute("vehiculoDesdeTurno", vehiculoService.obtenerPorId(turno.getVehiculoId()));
            } catch (TurnoNoEncontradoException | VehiculoNoEncontradoException e) {
                form.setTurnoId(null);
            }
        }

        request.setAttribute("form", form);
        request.setAttribute("clienteIdSeleccionado", clienteId);
        request.setAttribute("clientes", listarClientesParaSelect());
        request.setAttribute("vehiculos", clienteId == null ? List.of() : listarVehiculosDelCliente(clienteId));
        request.setAttribute("csrfToken", CsrfTokenUtils.obtenerOGenerarToken(request));
        request.getRequestDispatcher("/WEB-INF/jsp/trabajos/form.jsp").forward(request, response);
    }

    private void mostrarFormularioEdicion(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException, TrabajoNoEncontradoException {

        Long idTrabajo = parsearLong(request.getParameter("id"));
        TrabajoDTO trabajo = trabajoService.obtenerPorId(idTrabajo);

        TrabajoFormDTO form = new TrabajoFormDTO();
        form.setIdTrabajo(trabajo.getIdTrabajo());
        form.setVehiculoId(trabajo.getVehiculoId());
        form.setTurnoId(trabajo.getTurnoId());
        form.setFechaIngreso(trabajo.getFechaIngreso().toString());
        form.setDescripcion(trabajo.getDescripcion());

        request.setAttribute("form", form);
        request.setAttribute("trabajoActual", trabajo);
        request.setAttribute("csrfToken", CsrfTokenUtils.obtenerOGenerarToken(request));
        request.getRequestDispatcher("/WEB-INF/jsp/trabajos/form.jsp").forward(request, response);
    }

    private void mostrarDetalle(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException, TrabajoNoEncontradoException {

        Long idTrabajo = parsearLong(request.getParameter("id"));
        TrabajoDTO trabajo = trabajoService.obtenerPorId(idTrabajo);
        var items = itemTrabajoService.listarPorTrabajo(idTrabajo);

        java.math.BigDecimal total = items.stream()
                .map(com.eataller.dto.ItemTrabajoDTO::getSubtotal)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        Long idItemEditar = parsearLong(request.getParameter("editarItem"));
        if (idItemEditar != null) {
            items.stream()
                    .filter(i -> i.getIdItem().equals(idItemEditar))
                    .findFirst()
                    .ifPresent(i -> request.setAttribute("itemEnEdicion", i));
        }

        request.setAttribute("trabajo", trabajo);
        request.setAttribute("items", items);
        request.setAttribute("totalItems", total);
        request.setAttribute("csrfToken", CsrfTokenUtils.obtenerOGenerarToken(request));
        request.getRequestDispatcher("/WEB-INF/jsp/trabajos/detalle.jsp").forward(request, response);
    }

    private void guardar(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException {

        TrabajoFormDTO form = new TrabajoFormDTO();
        form.setIdTrabajo(parsearLong(request.getParameter("idTrabajo")));
        form.setVehiculoId(parsearLong(request.getParameter("vehiculoId")));
        form.setTurnoId(parsearLong(request.getParameter("turnoId")));
        form.setFechaIngreso(request.getParameter("fechaIngreso"));
        form.setDescripcion(request.getParameter("descripcion"));

        Long idUsuarioLogueado = SessionUtils.getUsuarioLogueado(request).getIdUsuario();

        try {
            if (form.esAlta()) {
                trabajoService.crear(form, idUsuarioLogueado);
                response.sendRedirect(request.getContextPath() + "/trabajos?exito=creado");
            } else {
                trabajoService.actualizar(form, idUsuarioLogueado);
                response.sendRedirect(request.getContextPath() + "/trabajos?exito=modificado");
            }
        } catch (ValidacionException e) {
            request.setAttribute("errores", e.getErrores());
            volverAFormulario(request, response, form);
        } catch (VehiculoNoEncontradoException e) {
            request.setAttribute("mensajeError", e.getMessage());
            volverAFormulario(request, response, form);
        } catch (TrabajoNoEncontradoException e) {
            response.sendRedirect(request.getContextPath() + "/trabajos?error=no_encontrado");
        }
    }

    private void volverAFormulario(HttpServletRequest request, HttpServletResponse response, TrabajoFormDTO form)
            throws ServletException, IOException, SQLException {

        request.setAttribute("form", form);
        request.setAttribute("csrfToken", CsrfTokenUtils.obtenerOGenerarToken(request));
        if (form.esAlta()) {
            request.setAttribute("clientes", listarClientesParaSelect());
            request.setAttribute("vehiculos", List.of());
            if (form.getVehiculoId() != null) {
                try {
                    request.setAttribute("vehiculoDesdeTurno", vehiculoService.obtenerPorId(form.getVehiculoId()));
                } catch (VehiculoNoEncontradoException ignored) {
                    // el vehiculo ya no existe; el formulario queda sin ese dato precargado
                }
            }
        }
        request.getRequestDispatcher("/WEB-INF/jsp/trabajos/form.jsp").forward(request, response);
    }

    private void finalizar(HttpServletRequest request, HttpServletResponse response)
            throws IOException, SQLException {

        Long idTrabajo = parsearLong(request.getParameter("id"));
        Long idUsuarioLogueado = SessionUtils.getUsuarioLogueado(request).getIdUsuario();
        LocalDate fechaEgreso = parsearFecha(request.getParameter("fechaEgreso"));

        try {
            trabajoService.finalizar(idTrabajo, fechaEgreso, idUsuarioLogueado);
            response.sendRedirect(request.getContextPath() + "/trabajos?exito=finalizado");
        } catch (TrabajoNoEncontradoException e) {
            response.sendRedirect(request.getContextPath() + "/trabajos?error=no_encontrado");
        } catch (ValidacionException e) {
            response.sendRedirect(request.getContextPath() + "/trabajos?error=fecha_egreso");
        }
    }

    private void reabrir(HttpServletRequest request, HttpServletResponse response)
            throws IOException, SQLException {

        Long idTrabajo = parsearLong(request.getParameter("id"));
        Long idUsuarioLogueado = SessionUtils.getUsuarioLogueado(request).getIdUsuario();
        RolUsuario rolLogueado = SessionUtils.getUsuarioLogueado(request).getRol();

        try {
            trabajoService.reabrir(idTrabajo, idUsuarioLogueado, rolLogueado);
            response.sendRedirect(request.getContextPath() + "/trabajos?exito=reabierto");
        } catch (TrabajoNoEncontradoException e) {
            response.sendRedirect(request.getContextPath() + "/trabajos?error=no_encontrado");
        } catch (ValidacionException e) {
            response.sendRedirect(request.getContextPath() + "/trabajos?error=no_finalizado");
        } catch (UsuarioSinPermisosException e) {
            response.sendRedirect(request.getContextPath() + "/trabajos?error=sin_permisos");
        }
    }

    private void darDeBaja(HttpServletRequest request, HttpServletResponse response)
            throws IOException, SQLException {

        Long idTrabajo = parsearLong(request.getParameter("id"));
        Long idUsuarioLogueado = SessionUtils.getUsuarioLogueado(request).getIdUsuario();
        RolUsuario rolLogueado = SessionUtils.getUsuarioLogueado(request).getRol();

        try {
            trabajoService.darDeBaja(idTrabajo, idUsuarioLogueado, rolLogueado);
            response.sendRedirect(request.getContextPath() + "/trabajos?exito=baja");
        } catch (TrabajoNoEncontradoException e) {
            response.sendRedirect(request.getContextPath() + "/trabajos?error=no_encontrado");
        } catch (UsuarioSinPermisosException e) {
            response.sendRedirect(request.getContextPath() + "/trabajos?error=sin_permisos");
        }
    }

    private PaginaResultado<ClienteDTO> listarClientesParaSelect() throws SQLException {
        return clienteService.listar(null, "ACTIVO", 1, MAX_SELECT);
    }

    private List<VehiculoDTO> listarVehiculosDelCliente(Long clienteId) throws SQLException {
        return vehiculoService.listar(null, "ACTIVO", clienteId, 1, MAX_SELECT).getRegistros();
    }

    private LocalDate parsearFecha(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(valor);
        } catch (DateTimeParseException e) {
            return null;
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
