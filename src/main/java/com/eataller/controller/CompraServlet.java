package com.eataller.controller;

import com.eataller.dto.CompraDTO;
import com.eataller.dto.CompraFormDTO;
import com.eataller.dto.PaginaResultado;
import com.eataller.dto.ProductoDTO;
import com.eataller.dto.ProveedorDTO;
import com.eataller.entity.RolUsuario;
import com.eataller.exception.CompraNoEncontradaException;
import com.eataller.exception.ProveedorNoEncontradoException;
import com.eataller.exception.UsuarioSinPermisosException;
import com.eataller.exception.ValidacionException;
import com.eataller.service.CompraService;
import com.eataller.service.ItemCompraService;
import com.eataller.service.ProductoService;
import com.eataller.service.ProveedorService;
import com.eataller.service.impl.CompraServiceImpl;
import com.eataller.service.impl.ItemCompraServiceImpl;
import com.eataller.service.impl.ProductoServiceImpl;
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
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet(name = "CompraServlet", urlPatterns = {"/compras", "/compras/*"})
public class CompraServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(CompraServlet.class.getName());
    private static final int REGISTROS_POR_PAGINA = 20;
    private static final int MAX_SELECT = 1000;

    private final CompraService compraService = new CompraServiceImpl();
    private final ItemCompraService itemCompraService = new ItemCompraServiceImpl();
    private final ProductoService productoService = new ProductoServiceImpl();
    private final ProveedorService proveedorService = new ProveedorServiceImpl();

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
            LOGGER.log(Level.SEVERE, "Error de base de datos en CompraServlet (GET).", e);
            request.setAttribute("mensajeError", "No fue posible completar la operacion. Intente nuevamente.");
            request.getRequestDispatcher("/WEB-INF/jsp/error/500.jsp").forward(request, response);
        } catch (CompraNoEncontradaException e) {
            response.sendRedirect(request.getContextPath() + "/compras?error=no_encontrada");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!CsrfTokenUtils.esTokenValido(request)) {
            response.sendRedirect(request.getContextPath() + "/compras?error=csrf");
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
                response.sendRedirect(request.getContextPath() + "/compras");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error de base de datos en CompraServlet (POST).", e);
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

        PaginaResultado<CompraDTO> resultado = compraService.listar(textoBusqueda, estadoFiltro,
                fechaDesde, fechaHasta, pagina, REGISTROS_POR_PAGINA);

        request.setAttribute("resultado", resultado);
        request.setAttribute("q", textoBusqueda);
        request.setAttribute("estado", estadoFiltro);
        request.setAttribute("fechaDesde", request.getParameter("fechaDesde"));
        request.setAttribute("fechaHasta", request.getParameter("fechaHasta"));
        request.setAttribute("csrfToken", CsrfTokenUtils.obtenerOGenerarToken(request));
        request.getRequestDispatcher("/WEB-INF/jsp/compras/listado.jsp").forward(request, response);
    }

    private void mostrarFormularioAlta(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException {

        request.setAttribute("form", new CompraFormDTO());
        request.setAttribute("proveedores", listarProveedoresParaSelect());
        request.setAttribute("csrfToken", CsrfTokenUtils.obtenerOGenerarToken(request));
        request.getRequestDispatcher("/WEB-INF/jsp/compras/form.jsp").forward(request, response);
    }

    private void mostrarDetalle(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException, CompraNoEncontradaException {

        Long idCompra = parsearLong(request.getParameter("id"));
        CompraDTO compra = compraService.obtenerPorId(idCompra);
        var items = itemCompraService.listarPorCompra(idCompra);

        PaginaResultado<ProductoDTO> productos = productoService.listar(null, "ACTIVO", null, 1, MAX_SELECT);

        Long idItemEditar = parsearLong(request.getParameter("editarItem"));
        if (idItemEditar != null) {
            items.stream()
                    .filter(i -> i.getIdItemCompra().equals(idItemEditar))
                    .findFirst()
                    .ifPresent(i -> request.setAttribute("itemEnEdicion", i));
        }

        request.setAttribute("compra", compra);
        request.setAttribute("items", items);
        request.setAttribute("productos", productos.getRegistros());
        request.setAttribute("csrfToken", CsrfTokenUtils.obtenerOGenerarToken(request));
        request.getRequestDispatcher("/WEB-INF/jsp/compras/detalle.jsp").forward(request, response);
    }

    private void guardar(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException {

        CompraFormDTO form = new CompraFormDTO();
        form.setIdCompra(parsearLong(request.getParameter("idCompra")));
        form.setProveedorId(parsearLong(request.getParameter("proveedorId")));

        Long idUsuarioLogueado = SessionUtils.getUsuarioLogueado(request).getIdUsuario();

        try {
            if (form.esAlta()) {
                CompraDTO creada = compraService.crear(form, idUsuarioLogueado);
                response.sendRedirect(request.getContextPath() + "/compras?accion=detalle&id=" + creada.getIdCompra());
            } else {
                compraService.actualizar(form, idUsuarioLogueado);
                response.sendRedirect(request.getContextPath() + "/compras?accion=detalle&id=" + form.getIdCompra()
                        + "&exito=modificada");
            }
        } catch (ValidacionException e) {
            request.setAttribute("errores", e.getErrores());
            request.setAttribute("form", form);
            request.setAttribute("proveedores", listarProveedoresParaSelect());
            request.setAttribute("csrfToken", CsrfTokenUtils.obtenerOGenerarToken(request));
            request.getRequestDispatcher("/WEB-INF/jsp/compras/form.jsp").forward(request, response);
        } catch (ProveedorNoEncontradoException e) {
            request.setAttribute("mensajeError", e.getMessage());
            request.setAttribute("form", form);
            request.setAttribute("proveedores", listarProveedoresParaSelect());
            request.setAttribute("csrfToken", CsrfTokenUtils.obtenerOGenerarToken(request));
            request.getRequestDispatcher("/WEB-INF/jsp/compras/form.jsp").forward(request, response);
        } catch (CompraNoEncontradaException e) {
            response.sendRedirect(request.getContextPath() + "/compras?error=no_encontrada");
        }
    }

    private void confirmar(HttpServletRequest request, HttpServletResponse response)
            throws IOException, SQLException {

        Long idCompra = parsearLong(request.getParameter("id"));
        Long idUsuarioLogueado = SessionUtils.getUsuarioLogueado(request).getIdUsuario();

        try {
            compraService.confirmar(idCompra, idUsuarioLogueado);
            response.sendRedirect(request.getContextPath() + "/compras?accion=detalle&id=" + idCompra + "&exito=confirmada");
        } catch (CompraNoEncontradaException e) {
            response.sendRedirect(request.getContextPath() + "/compras?error=no_encontrada");
        } catch (ValidacionException e) {
            response.sendRedirect(request.getContextPath() + "/compras?accion=detalle&id=" + idCompra + "&error=confirmacion_invalida");
        }
    }

    private void anular(HttpServletRequest request, HttpServletResponse response)
            throws IOException, SQLException {

        Long idCompra = parsearLong(request.getParameter("id"));
        Long idUsuarioLogueado = SessionUtils.getUsuarioLogueado(request).getIdUsuario();
        RolUsuario rolLogueado = SessionUtils.getUsuarioLogueado(request).getRol();

        try {
            compraService.anular(idCompra, idUsuarioLogueado, rolLogueado);
            response.sendRedirect(request.getContextPath() + "/compras?accion=detalle&id=" + idCompra + "&exito=anulada");
        } catch (CompraNoEncontradaException e) {
            response.sendRedirect(request.getContextPath() + "/compras?error=no_encontrada");
        } catch (ValidacionException e) {
            response.sendRedirect(request.getContextPath() + "/compras?accion=detalle&id=" + idCompra + "&error=no_confirmada");
        } catch (UsuarioSinPermisosException e) {
            response.sendRedirect(request.getContextPath() + "/compras?accion=detalle&id=" + idCompra + "&error=sin_permisos");
        }
    }

    private PaginaResultado<ProveedorDTO> listarProveedoresParaSelect() throws SQLException {
        return proveedorService.listar(null, "ACTIVO", 1, MAX_SELECT);
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
