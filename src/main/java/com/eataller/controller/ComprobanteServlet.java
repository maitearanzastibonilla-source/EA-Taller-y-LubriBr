package com.eataller.controller;

import com.eataller.dto.ComprobanteDTO;
import com.eataller.dto.ComprobanteFormDTO;
import com.eataller.dto.PaginaResultado;
import com.eataller.entity.RolUsuario;
import com.eataller.exception.ComprobanteNoEncontradoException;
import com.eataller.exception.TrabajoNoEncontradoException;
import com.eataller.exception.UsuarioSinPermisosException;
import com.eataller.exception.ValidacionException;
import com.eataller.service.ComprobanteService;
import com.eataller.service.impl.ComprobanteServiceImpl;
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

@WebServlet(name = "ComprobanteServlet", urlPatterns = {"/comprobantes", "/comprobantes/*"})
public class ComprobanteServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(ComprobanteServlet.class.getName());
    private static final int REGISTROS_POR_PAGINA = 20;

    private final ComprobanteService comprobanteService = new ComprobanteServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String accion = request.getParameter("accion");
        try {
            if ("descargar".equals(accion)) {
                descargarPdf(request, response);
            } else {
                mostrarListado(request, response);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error de base de datos en ComprobanteServlet (GET).", e);
            request.setAttribute("mensajeError", "No fue posible completar la operacion. Intente nuevamente.");
            request.getRequestDispatcher("/WEB-INF/jsp/error/500.jsp").forward(request, response);
        } catch (ComprobanteNoEncontradoException e) {
            response.sendRedirect(request.getContextPath() + "/comprobantes?error=no_encontrado");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Long trabajoId = parsearLong(request.getParameter("trabajoId"));

        if (!CsrfTokenUtils.esTokenValido(request)) {
            redirigirATrabajo(request, response, trabajoId, "error", "csrf");
            return;
        }

        String accion = request.getParameter("accion");
        try {
            if ("generar".equals(accion)) {
                generar(request, response, trabajoId);
            } else if ("anular".equals(accion)) {
                anular(request, response, trabajoId);
            } else {
                response.sendRedirect(request.getContextPath() + "/comprobantes");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error de base de datos en ComprobanteServlet (POST).", e);
            request.setAttribute("mensajeError", "No fue posible completar la operacion. Intente nuevamente.");
            request.getRequestDispatcher("/WEB-INF/jsp/error/500.jsp").forward(request, response);
        }
    }

    private void mostrarListado(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException {

        String textoBusqueda = request.getParameter("q");
        String estadoFiltro = request.getParameter("estado");
        LocalDate fechaDesde = parsearFecha(request.getParameter("fechaDesde"));
        LocalDate fechaHasta = parsearFecha(request.getParameter("fechaHasta"));
        int pagina = parsearEntero(request.getParameter("pagina"), 1);

        PaginaResultado<ComprobanteDTO> resultado = comprobanteService.listar(textoBusqueda, estadoFiltro,
                fechaDesde, fechaHasta, pagina, REGISTROS_POR_PAGINA);

        request.setAttribute("resultado", resultado);
        request.setAttribute("q", textoBusqueda);
        request.setAttribute("estado", estadoFiltro);
        request.setAttribute("fechaDesde", request.getParameter("fechaDesde"));
        request.setAttribute("fechaHasta", request.getParameter("fechaHasta"));
        request.getRequestDispatcher("/WEB-INF/jsp/comprobantes/listado.jsp").forward(request, response);
    }

    private void descargarPdf(HttpServletRequest request, HttpServletResponse response)
            throws IOException, SQLException, ComprobanteNoEncontradoException {

        Long idComprobante = parsearLong(request.getParameter("id"));
        if (idComprobante == null) {
            response.sendRedirect(request.getContextPath() + "/comprobantes?error=no_encontrado");
            return;
        }

        ComprobanteDTO comprobante = comprobanteService.obtenerPorId(idComprobante);
        if (comprobante.getPdfUrl() == null || comprobante.getPdfUrl().isBlank()) {
            response.sendRedirect(request.getContextPath() + "/comprobantes?error=no_encontrado");
            return;
        }

        Path archivo = Paths.get(comprobante.getPdfUrl());
        if (!Files.exists(archivo)) {
            response.sendRedirect(request.getContextPath() + "/comprobantes?error=no_encontrado");
            return;
        }

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "inline; filename=\"comprobante-" + idComprobante + ".pdf\"");
        response.setContentLengthLong(Files.size(archivo));

        try (InputStream entrada = Files.newInputStream(archivo)) {
            entrada.transferTo(response.getOutputStream());
        }
    }

    private void generar(HttpServletRequest request, HttpServletResponse response, Long trabajoId)
            throws IOException, SQLException {

        ComprobanteFormDTO form = new ComprobanteFormDTO();
        form.setTrabajoId(trabajoId);
        form.setMetodoPago(request.getParameter("metodoPago"));
        form.setEstado(request.getParameter("estado"));

        Long idUsuarioLogueado = SessionUtils.getUsuarioLogueado(request).getIdUsuario();

        try {
            comprobanteService.generar(form, idUsuarioLogueado);
            redirigirATrabajo(request, response, trabajoId, "exito", "comprobante_generado");
        } catch (ValidacionException e) {
            redirigirATrabajo(request, response, trabajoId, "error", "comprobante_invalido");
        } catch (TrabajoNoEncontradoException e) {
            redirigirATrabajo(request, response, trabajoId, "error", "no_encontrado");
        }
    }

    private void anular(HttpServletRequest request, HttpServletResponse response, Long trabajoId)
            throws IOException, SQLException {

        Long idComprobante = parsearLong(request.getParameter("idComprobante"));
        Long idUsuarioLogueado = SessionUtils.getUsuarioLogueado(request).getIdUsuario();
        RolUsuario rolLogueado = SessionUtils.getUsuarioLogueado(request).getRol();

        try {
            comprobanteService.anular(idComprobante, idUsuarioLogueado, rolLogueado);
            redirigirATrabajo(request, response, trabajoId, "exito", "comprobante_anulado");
        } catch (ComprobanteNoEncontradoException e) {
            redirigirATrabajo(request, response, trabajoId, "error", "no_encontrado");
        } catch (ValidacionException e) {
            redirigirATrabajo(request, response, trabajoId, "error", "ya_anulado");
        } catch (UsuarioSinPermisosException e) {
            redirigirATrabajo(request, response, trabajoId, "error", "sin_permisos");
        }
    }

    private void redirigirATrabajo(HttpServletRequest request, HttpServletResponse response, Long trabajoId,
                                    String claveParametro, String valorParametro) throws IOException {
        StringBuilder url = new StringBuilder(request.getContextPath())
                .append("/trabajos?accion=detalle&id=").append(trabajoId);
        if (claveParametro != null) {
            url.append("&").append(claveParametro).append("=").append(valorParametro);
        }
        response.sendRedirect(url.toString());
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
