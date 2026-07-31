package com.eataller.controller;

import com.eataller.dto.PaginaResultado;
import com.eataller.dto.ReporteDTO;
import com.eataller.dto.ReporteDatos;
import com.eataller.dto.ReporteFormDTO;
import com.eataller.exception.ReporteNoEncontradoException;
import com.eataller.exception.ValidacionException;
import com.eataller.service.ReporteService;
import com.eataller.service.impl.ReporteServiceImpl;
import com.eataller.utils.CsrfTokenUtils;
import com.eataller.utils.SessionUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet(name = "ReporteServlet", urlPatterns = {"/reportes", "/reportes/*"})
public class ReporteServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(ReporteServlet.class.getName());
    private static final int REGISTROS_POR_PAGINA = 20;

    private final ReporteService reporteService = new ReporteServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String accion = request.getParameter("accion");
        try {
            if ("nuevo".equals(accion)) {
                mostrarFormularioAlta(request, response);
            } else if ("detalle".equals(accion)) {
                mostrarDetalle(request, response);
            } else if ("descargar".equals(accion)) {
                descargarPdf(request, response);
            } else {
                mostrarListado(request, response);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error de base de datos en ReporteServlet (GET).", e);
            request.setAttribute("mensajeError", "No fue posible completar la operacion. Intente nuevamente.");
            request.getRequestDispatcher("/WEB-INF/jsp/error/500.jsp").forward(request, response);
        } catch (ReporteNoEncontradoException e) {
            response.sendRedirect(request.getContextPath() + "/reportes?error=no_encontrado");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!CsrfTokenUtils.esTokenValido(request)) {
            response.sendRedirect(request.getContextPath() + "/reportes?error=csrf");
            return;
        }

        String accion = request.getParameter("accion");
        try {
            if ("generar".equals(accion)) {
                generar(request, response);
            } else {
                response.sendRedirect(request.getContextPath() + "/reportes");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error de base de datos en ReporteServlet (POST).", e);
            request.setAttribute("mensajeError", "No fue posible completar la operacion. Intente nuevamente.");
            request.getRequestDispatcher("/WEB-INF/jsp/error/500.jsp").forward(request, response);
        }
    }

    private void mostrarListado(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException {

        String tipoFiltro = request.getParameter("tipo");
        LocalDate fechaDesde = parsearFecha(request.getParameter("fechaDesde"));
        LocalDate fechaHasta = parsearFecha(request.getParameter("fechaHasta"));
        int pagina = parsearEntero(request.getParameter("pagina"), 1);

        PaginaResultado<ReporteDTO> resultado = reporteService.listar(tipoFiltro, null,
                fechaDesde, fechaHasta, pagina, REGISTROS_POR_PAGINA);

        request.setAttribute("resultado", resultado);
        request.setAttribute("tipo", tipoFiltro);
        request.setAttribute("fechaDesde", request.getParameter("fechaDesde"));
        request.setAttribute("fechaHasta", request.getParameter("fechaHasta"));
        request.setAttribute("csrfToken", CsrfTokenUtils.obtenerOGenerarToken(request));
        request.getRequestDispatcher("/WEB-INF/jsp/reportes/listado.jsp").forward(request, response);
    }

    private void mostrarFormularioAlta(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("form", new ReporteFormDTO());
        request.setAttribute("csrfToken", CsrfTokenUtils.obtenerOGenerarToken(request));
        request.getRequestDispatcher("/WEB-INF/jsp/reportes/form.jsp").forward(request, response);
    }

    private void mostrarDetalle(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException, ReporteNoEncontradoException {

        Long idReporte = parsearLong(request.getParameter("id"));
        ReporteDTO reporte = reporteService.obtenerPorId(idReporte);
        ReporteDatos datos = reporteService.obtenerDatosConsolidados(idReporte);

        request.setAttribute("reporte", reporte);
        request.setAttribute("datos", datos);
        request.getRequestDispatcher("/WEB-INF/jsp/reportes/detalle.jsp").forward(request, response);
    }

    private void descargarPdf(HttpServletRequest request, HttpServletResponse response)
            throws IOException, SQLException, ReporteNoEncontradoException {

        Long idReporte = parsearLong(request.getParameter("id"));
        if (idReporte == null) {
            response.sendRedirect(request.getContextPath() + "/reportes?error=no_encontrado");
            return;
        }

        ReporteDTO reporte = reporteService.obtenerPorId(idReporte);
        if (reporte.getPdfUrl() == null || reporte.getPdfUrl().isBlank()) {
            response.sendRedirect(request.getContextPath() + "/reportes?error=no_encontrado");
            return;
        }

        Path archivo = Paths.get(reporte.getPdfUrl());
        if (!Files.exists(archivo)) {
            response.sendRedirect(request.getContextPath() + "/reportes?error=no_encontrado");
            return;
        }

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "inline; filename=\"reporte-" + idReporte + ".pdf\"");
        response.setContentLengthLong(Files.size(archivo));

        try (InputStream entrada = Files.newInputStream(archivo)) {
            entrada.transferTo(response.getOutputStream());
        }
    }

    private void generar(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException {

        ReporteFormDTO form = new ReporteFormDTO();
        form.setTipoReporte(request.getParameter("tipoReporte"));
        form.setFechaDesde(request.getParameter("fechaDesde"));
        form.setFechaHasta(request.getParameter("fechaHasta"));

        Long idUsuarioLogueado = SessionUtils.getUsuarioLogueado(request).getIdUsuario();

        try {
            ReporteDTO creado = reporteService.generar(form, idUsuarioLogueado);
            response.sendRedirect(request.getContextPath() + "/reportes?accion=detalle&id=" + creado.getIdReporte());
        } catch (ValidacionException e) {
            request.setAttribute("errores", e.getErrores());
            request.setAttribute("form", form);
            request.setAttribute("csrfToken", CsrfTokenUtils.obtenerOGenerarToken(request));
            request.getRequestDispatcher("/WEB-INF/jsp/reportes/form.jsp").forward(request, response);
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
