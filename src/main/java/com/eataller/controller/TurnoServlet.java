package com.eataller.controller;

import com.eataller.dto.ClienteDTO;
import com.eataller.dto.PaginaResultado;
import com.eataller.dto.TurnoDTO;
import com.eataller.dto.TurnoFormDTO;
import com.eataller.dto.VehiculoDTO;
import com.eataller.exception.ClienteNoEncontradoException;
import com.eataller.exception.TurnoNoEncontradoException;
import com.eataller.exception.TurnoOcupadoException;
import com.eataller.exception.ValidacionException;
import com.eataller.exception.VehiculoNoEncontradoException;
import com.eataller.service.ClienteService;
import com.eataller.service.TurnoService;
import com.eataller.service.VehiculoService;
import com.eataller.service.impl.ClienteServiceImpl;
import com.eataller.service.impl.TurnoServiceImpl;
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
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet(name = "TurnoServlet", urlPatterns = {"/turnos", "/turnos/*"})
public class TurnoServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(TurnoServlet.class.getName());
    private static final int REGISTROS_POR_PAGINA = 20;
    private static final int MAX_CLIENTES_SELECT = 1000;

    private final TurnoService turnoService = new TurnoServiceImpl();
    private final ClienteService clienteService = new ClienteServiceImpl();
    private final VehiculoService vehiculoService = new VehiculoServiceImpl();

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
            LOGGER.log(Level.SEVERE, "Error de base de datos en TurnoServlet (GET).", e);
            request.setAttribute("mensajeError", "No fue posible completar la operacion. Intente nuevamente.");
            request.getRequestDispatcher("/WEB-INF/jsp/error/500.jsp").forward(request, response);
        } catch (TurnoNoEncontradoException e) {
            response.sendRedirect(request.getContextPath() + "/turnos?error=no_encontrado");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!CsrfTokenUtils.esTokenValido(request)) {
            response.sendRedirect(request.getContextPath() + "/turnos?error=csrf");
            return;
        }

        String accion = request.getParameter("accion");
        try {
            if ("guardar".equals(accion)) {
                guardar(request, response);
            } else if ("cancelar".equals(accion)) {
                cancelar(request, response);
            } else {
                response.sendRedirect(request.getContextPath() + "/turnos");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error de base de datos en TurnoServlet (POST).", e);
            request.setAttribute("mensajeError", "No fue posible completar la operacion. Intente nuevamente.");
            request.getRequestDispatcher("/WEB-INF/jsp/error/500.jsp").forward(request, response);
        }
    }

    private void mostrarListado(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException {

        String textoBusqueda = request.getParameter("q");
        String estadoFiltro = request.getParameter("estado");
        String tipoServicioFiltro = request.getParameter("tipoServicio");
        LocalDate fechaDesde = parsearFecha(request.getParameter("fechaDesde"));
        LocalDate fechaHasta = parsearFecha(request.getParameter("fechaHasta"));
        int pagina = parsearEntero(request.getParameter("pagina"), 1);

        PaginaResultado<TurnoDTO> resultado = turnoService.listar(textoBusqueda, estadoFiltro, tipoServicioFiltro,
                fechaDesde, fechaHasta, pagina, REGISTROS_POR_PAGINA);

        request.setAttribute("resultado", resultado);
        request.setAttribute("q", textoBusqueda);
        request.setAttribute("estado", estadoFiltro);
        request.setAttribute("tipoServicio", tipoServicioFiltro);
        request.setAttribute("fechaDesde", request.getParameter("fechaDesde"));
        request.setAttribute("fechaHasta", request.getParameter("fechaHasta"));
        request.setAttribute("csrfToken", CsrfTokenUtils.obtenerOGenerarToken(request));
        request.getRequestDispatcher("/WEB-INF/jsp/turnos/listado.jsp").forward(request, response);
    }

    private void mostrarFormularioAlta(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException {

        Long clienteId = parsearLong(request.getParameter("clienteId"));

        TurnoFormDTO form = new TurnoFormDTO();
        form.setClienteId(clienteId);
        form.setEstado("PENDIENTE");

        request.setAttribute("form", form);
        request.setAttribute("clientes", listarClientesParaSelect());
        request.setAttribute("vehiculos", clienteId == null ? List.of() : listarVehiculosDelCliente(clienteId));
        request.setAttribute("csrfToken", CsrfTokenUtils.obtenerOGenerarToken(request));
        request.getRequestDispatcher("/WEB-INF/jsp/turnos/form.jsp").forward(request, response);
    }

    private void mostrarFormularioEdicion(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException, TurnoNoEncontradoException {

        Long idTurno = parsearLong(request.getParameter("id"));
        TurnoDTO turno = turnoService.obtenerPorId(idTurno);

        TurnoFormDTO form = new TurnoFormDTO();
        form.setIdTurno(turno.getIdTurno());
        form.setClienteId(turno.getClienteId());
        form.setVehiculoId(turno.getVehiculoId());
        form.setFechaHora(turno.getFechaHora().toString());
        form.setTipoServicio(turno.getTipoServicio().name());
        form.setEstado(turno.getEstado().name());
        form.setNotas(turno.getNotas());

        request.setAttribute("form", form);
        request.setAttribute("turnoActual", turno);
        request.setAttribute("csrfToken", CsrfTokenUtils.obtenerOGenerarToken(request));
        request.getRequestDispatcher("/WEB-INF/jsp/turnos/form.jsp").forward(request, response);
    }

    private void guardar(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException {

        TurnoFormDTO form = new TurnoFormDTO();
        form.setIdTurno(parsearLong(request.getParameter("idTurno")));
        form.setClienteId(parsearLong(request.getParameter("clienteId")));
        form.setVehiculoId(parsearLong(request.getParameter("vehiculoId")));
        form.setFechaHora(request.getParameter("fechaHora"));
        form.setTipoServicio(request.getParameter("tipoServicio"));
        form.setEstado(request.getParameter("estado"));
        form.setNotas(request.getParameter("notas"));

        Long idUsuarioLogueado = SessionUtils.getUsuarioLogueado(request).getIdUsuario();

        try {
            if (form.esAlta()) {
                turnoService.crear(form, idUsuarioLogueado);
                response.sendRedirect(request.getContextPath() + "/turnos?exito=creado");
            } else {
                turnoService.actualizar(form, idUsuarioLogueado);
                response.sendRedirect(request.getContextPath() + "/turnos?exito=modificado");
            }
        } catch (ValidacionException e) {
            volverAFormularioConError(request, response, form, e.getErrores(), null);
        } catch (TurnoOcupadoException | ClienteNoEncontradoException | VehiculoNoEncontradoException e) {
            volverAFormularioConError(request, response, form, null, e.getMessage());
        } catch (TurnoNoEncontradoException e) {
            response.sendRedirect(request.getContextPath() + "/turnos?error=no_encontrado");
        }
    }

    private void volverAFormularioConError(HttpServletRequest request, HttpServletResponse response,
                                            TurnoFormDTO form, java.util.Map<String, String> errores, String mensajeError)
            throws ServletException, IOException, SQLException {

        if (errores != null) {
            request.setAttribute("errores", errores);
        }
        if (mensajeError != null) {
            request.setAttribute("mensajeError", mensajeError);
        }
        request.setAttribute("form", form);
        request.setAttribute("csrfToken", CsrfTokenUtils.obtenerOGenerarToken(request));

        if (form.esAlta()) {
            request.setAttribute("clientes", listarClientesParaSelect());
            request.setAttribute("vehiculos", form.getClienteId() == null ? List.of() : listarVehiculosDelCliente(form.getClienteId()));
        } else {
            request.setAttribute("turnoActual", turnoServiceObtenerSilencioso(form.getIdTurno()));
        }
        request.getRequestDispatcher("/WEB-INF/jsp/turnos/form.jsp").forward(request, response);
    }

    private TurnoDTO turnoServiceObtenerSilencioso(Long idTurno) throws SQLException {
        try {
            return turnoService.obtenerPorId(idTurno);
        } catch (TurnoNoEncontradoException e) {
            return null;
        }
    }

    private void cancelar(HttpServletRequest request, HttpServletResponse response)
            throws IOException, SQLException {

        Long idTurno = parsearLong(request.getParameter("id"));
        Long idUsuarioLogueado = SessionUtils.getUsuarioLogueado(request).getIdUsuario();

        try {
            turnoService.cancelar(idTurno, idUsuarioLogueado);
            response.sendRedirect(request.getContextPath() + "/turnos?exito=cancelado");
        } catch (TurnoNoEncontradoException e) {
            response.sendRedirect(request.getContextPath() + "/turnos?error=no_encontrado");
        } catch (ValidacionException e) {
            response.sendRedirect(request.getContextPath() + "/turnos?error=ya_finalizado");
        }
    }

    private PaginaResultado<ClienteDTO> listarClientesParaSelect() throws SQLException {
        return clienteService.listar(null, "ACTIVO", 1, MAX_CLIENTES_SELECT);
    }

    private List<VehiculoDTO> listarVehiculosDelCliente(Long clienteId) throws SQLException {
        return vehiculoService.listar(null, "ACTIVO", clienteId, 1, MAX_CLIENTES_SELECT).getRegistros();
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
