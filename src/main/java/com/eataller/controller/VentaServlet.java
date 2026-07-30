package com.eataller.controller;

import com.eataller.dto.ItemVentaDTO;
import com.eataller.dto.PaginaResultado;
import com.eataller.dto.ProductoDTO;
import com.eataller.dto.VentaDTO;
import com.eataller.dto.VentaFormDTO;
import com.eataller.entity.RolUsuario;
import com.eataller.exception.UsuarioSinPermisosException;
import com.eataller.exception.ValidacionException;
import com.eataller.exception.VentaNoEncontradaException;
import com.eataller.service.ItemVentaService;
import com.eataller.service.ProductoService;
import com.eataller.service.VentaService;
import com.eataller.service.impl.ItemVentaServiceImpl;
import com.eataller.service.impl.ProductoServiceImpl;
import com.eataller.service.impl.VentaServiceImpl;
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
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet(name = "VentaServlet", urlPatterns = {"/ventas", "/ventas/*"})
public class VentaServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(VentaServlet.class.getName());
    private static final int REGISTROS_POR_PAGINA = 20;
    private static final int MAX_SELECT = 1000;

    private final VentaService ventaService = new VentaServiceImpl();
    private final ItemVentaService itemVentaService = new ItemVentaServiceImpl();
    private final ProductoService productoService = new ProductoServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String accion = request.getParameter("accion");
        try {
            if ("nuevo".equals(accion)) {
                mostrarFormularioAlta(request, response);
            } else if ("detalle".equals(accion)) {
                mostrarDetalle(request, response);
            } else {
                mostrarListado(request, response);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error de base de datos en VentaServlet (GET).", e);
            request.setAttribute("mensajeError", "No fue posible completar la operacion. Intente nuevamente.");
            request.getRequestDispatcher("/WEB-INF/jsp/error/500.jsp").forward(request, response);
        } catch (VentaNoEncontradaException e) {
            response.sendRedirect(request.getContextPath() + "/ventas?error=no_encontrada");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!CsrfTokenUtils.esTokenValido(request)) {
            response.sendRedirect(request.getContextPath() + "/ventas?error=csrf");
            return;
        }

        String accion = request.getParameter("accion");
        try {
            if ("guardar".equals(accion)) {
                guardar(request, response);
            } else if ("confirmar".equals(accion)) {
                confirmar(request, response);
            } else if ("anular".equals(accion)) {
                anular(request, response);
            } else {
                response.sendRedirect(request.getContextPath() + "/ventas");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error de base de datos en VentaServlet (POST).", e);
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

        PaginaResultado<VentaDTO> resultado = ventaService.listar(textoBusqueda, estadoFiltro,
                fechaDesde, fechaHasta, pagina, REGISTROS_POR_PAGINA);

        request.setAttribute("resultado", resultado);
        request.setAttribute("q", textoBusqueda);
        request.setAttribute("estado", estadoFiltro);
        request.setAttribute("fechaDesde", request.getParameter("fechaDesde"));
        request.setAttribute("fechaHasta", request.getParameter("fechaHasta"));
        request.setAttribute("csrfToken", CsrfTokenUtils.obtenerOGenerarToken(request));
        request.getRequestDispatcher("/WEB-INF/jsp/ventas/listado.jsp").forward(request, response);
    }

    private void mostrarFormularioAlta(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("form", new VentaFormDTO());
        request.setAttribute("csrfToken", CsrfTokenUtils.obtenerOGenerarToken(request));
        request.getRequestDispatcher("/WEB-INF/jsp/ventas/form.jsp").forward(request, response);
    }

    private void mostrarDetalle(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException, VentaNoEncontradaException {

        Long idVenta = parsearLong(request.getParameter("id"));
        VentaDTO venta = ventaService.obtenerPorId(idVenta);
        var items = itemVentaService.listarPorVenta(idVenta);

        PaginaResultado<ProductoDTO> productos = productoService.listar(null, "ACTIVO", null, 1, MAX_SELECT);

        Long idItemEditar = parsearLong(request.getParameter("editarItem"));
        if (idItemEditar != null) {
            items.stream()
                    .filter(i -> i.getIdItemVenta().equals(idItemEditar))
                    .findFirst()
                    .ifPresent(i -> request.setAttribute("itemEnEdicion", i));
        }

        request.setAttribute("venta", venta);
        request.setAttribute("items", items);
        request.setAttribute("productos", productos.getRegistros());
        request.setAttribute("csrfToken", CsrfTokenUtils.obtenerOGenerarToken(request));
        request.getRequestDispatcher("/WEB-INF/jsp/ventas/detalle.jsp").forward(request, response);
    }

    private void guardar(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException {

        VentaFormDTO form = new VentaFormDTO();
        form.setIdVenta(parsearLong(request.getParameter("idVenta")));
        form.setMetodoPago(request.getParameter("metodoPago"));

        Long idUsuarioLogueado = SessionUtils.getUsuarioLogueado(request).getIdUsuario();

        try {
            if (form.esAlta()) {
                VentaDTO creada = ventaService.crear(form, idUsuarioLogueado);
                response.sendRedirect(request.getContextPath() + "/ventas?accion=detalle&id=" + creada.getIdVenta());
            } else {
                ventaService.actualizar(form, idUsuarioLogueado);
                response.sendRedirect(request.getContextPath() + "/ventas?accion=detalle&id=" + form.getIdVenta()
                        + "&exito=modificado");
            }
        } catch (ValidacionException e) {
            request.setAttribute("errores", e.getErrores());
            request.setAttribute("form", form);
            request.setAttribute("csrfToken", CsrfTokenUtils.obtenerOGenerarToken(request));
            request.getRequestDispatcher("/WEB-INF/jsp/ventas/form.jsp").forward(request, response);
        } catch (VentaNoEncontradaException e) {
            response.sendRedirect(request.getContextPath() + "/ventas?error=no_encontrada");
        }
    }

    private void confirmar(HttpServletRequest request, HttpServletResponse response)
            throws IOException, SQLException {

        Long idVenta = parsearLong(request.getParameter("id"));
        Long idUsuarioLogueado = SessionUtils.getUsuarioLogueado(request).getIdUsuario();

        try {
            ventaService.confirmar(idVenta, idUsuarioLogueado);
            response.sendRedirect(request.getContextPath() + "/ventas?accion=detalle&id=" + idVenta + "&exito=confirmada");
        } catch (VentaNoEncontradaException e) {
            response.sendRedirect(request.getContextPath() + "/ventas?error=no_encontrada");
        } catch (ValidacionException e) {
            response.sendRedirect(request.getContextPath() + "/ventas?accion=detalle&id=" + idVenta + "&error=confirmacion_invalida");
        }
    }

    private void anular(HttpServletRequest request, HttpServletResponse response)
            throws IOException, SQLException {

        Long idVenta = parsearLong(request.getParameter("id"));
        Long idUsuarioLogueado = SessionUtils.getUsuarioLogueado(request).getIdUsuario();
        RolUsuario rolLogueado = SessionUtils.getUsuarioLogueado(request).getRol();

        try {
            ventaService.anular(idVenta, idUsuarioLogueado, rolLogueado);
            response.sendRedirect(request.getContextPath() + "/ventas?accion=detalle&id=" + idVenta + "&exito=anulada");
        } catch (VentaNoEncontradaException e) {
            response.sendRedirect(request.getContextPath() + "/ventas?error=no_encontrada");
        } catch (ValidacionException e) {
            response.sendRedirect(request.getContextPath() + "/ventas?accion=detalle&id=" + idVenta + "&error=no_confirmada");
        } catch (UsuarioSinPermisosException e) {
            response.sendRedirect(request.getContextPath() + "/ventas?accion=detalle&id=" + idVenta + "&error=sin_permisos");
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
