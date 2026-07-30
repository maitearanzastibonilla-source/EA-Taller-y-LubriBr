package com.eataller.controller;

import com.eataller.dto.PaginaResultado;
import com.eataller.dto.ProductoDTO;
import com.eataller.dto.ProductoFormDTO;
import com.eataller.dto.ProveedorDTO;
import com.eataller.exception.ProductoNoEncontradoException;
import com.eataller.exception.ProveedorNoEncontradoException;
import com.eataller.exception.ValidacionException;
import com.eataller.service.ProductoService;
import com.eataller.service.ProveedorService;
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
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet(name = "ProductoServlet", urlPatterns = {"/productos", "/productos/*"})
public class ProductoServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(ProductoServlet.class.getName());
    private static final int REGISTROS_POR_PAGINA = 20;
    private static final int MAX_SELECT = 1000;

    private final ProductoService productoService = new ProductoServiceImpl();
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
            LOGGER.log(Level.SEVERE, "Error de base de datos en ProductoServlet (GET).", e);
            request.setAttribute("mensajeError", "No fue posible completar la operacion. Intente nuevamente.");
            request.getRequestDispatcher("/WEB-INF/jsp/error/500.jsp").forward(request, response);
        } catch (ProductoNoEncontradoException e) {
            response.sendRedirect(request.getContextPath() + "/productos?error=no_encontrado");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!CsrfTokenUtils.esTokenValido(request)) {
            response.sendRedirect(request.getContextPath() + "/productos?error=csrf");
            return;
        }

        String accion = request.getParameter("accion");
        try {
            if ("guardar".equals(accion)) {
                guardar(request, response);
            } else if ("cambiarEstado".equals(accion)) {
                cambiarEstado(request, response);
            } else {
                response.sendRedirect(request.getContextPath() + "/productos");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error de base de datos en ProductoServlet (POST).", e);
            request.setAttribute("mensajeError", "No fue posible completar la operacion. Intente nuevamente.");
            request.getRequestDispatcher("/WEB-INF/jsp/error/500.jsp").forward(request, response);
        }
    }

    private void mostrarListado(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException {

        String textoBusqueda = request.getParameter("q");
        String estadoFiltro = request.getParameter("estado");
        int pagina = parsearEntero(request.getParameter("pagina"), 1);

        PaginaResultado<ProductoDTO> resultado = productoService.listar(textoBusqueda, estadoFiltro, null,
                pagina, REGISTROS_POR_PAGINA);

        request.setAttribute("resultado", resultado);
        request.setAttribute("q", textoBusqueda);
        request.setAttribute("estado", estadoFiltro);
        request.setAttribute("csrfToken", CsrfTokenUtils.obtenerOGenerarToken(request));
        request.getRequestDispatcher("/WEB-INF/jsp/productos/listado.jsp").forward(request, response);
    }

    private void mostrarFormularioAlta(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException {

        request.setAttribute("form", new ProductoFormDTO());
        request.setAttribute("proveedores", listarProveedoresParaSelect());
        request.setAttribute("csrfToken", CsrfTokenUtils.obtenerOGenerarToken(request));
        request.getRequestDispatcher("/WEB-INF/jsp/productos/form.jsp").forward(request, response);
    }

    private void mostrarFormularioEdicion(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException, ProductoNoEncontradoException {

        Long idProducto = parsearLong(request.getParameter("id"));
        ProductoDTO producto = productoService.obtenerPorId(idProducto);

        ProductoFormDTO form = new ProductoFormDTO();
        form.setIdProducto(producto.getIdProducto());
        form.setProveedorId(producto.getProveedorId());
        form.setNombre(producto.getNombre());
        form.setDescripcion(producto.getDescripcion());
        form.setCategoria(producto.getCategoria());
        form.setPrecioVenta(producto.getPrecioVenta() != null ? producto.getPrecioVenta().toPlainString() : null);
        form.setPrecioCosto(producto.getPrecioCosto() != null ? producto.getPrecioCosto().toPlainString() : null);
        form.setStockMinimo(String.valueOf(producto.getStockMinimo()));

        request.setAttribute("form", form);
        request.setAttribute("productoActual", producto);
        request.setAttribute("proveedores", listarProveedoresParaSelect());
        request.setAttribute("csrfToken", CsrfTokenUtils.obtenerOGenerarToken(request));
        request.getRequestDispatcher("/WEB-INF/jsp/productos/form.jsp").forward(request, response);
    }

    private void guardar(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException {

        ProductoFormDTO form = new ProductoFormDTO();
        form.setIdProducto(parsearLong(request.getParameter("idProducto")));
        form.setProveedorId(parsearLong(request.getParameter("proveedorId")));
        form.setNombre(request.getParameter("nombre"));
        form.setDescripcion(request.getParameter("descripcion"));
        form.setCategoria(request.getParameter("categoria"));
        form.setPrecioVenta(request.getParameter("precioVenta"));
        form.setPrecioCosto(request.getParameter("precioCosto"));
        form.setStockActual(request.getParameter("stockActual"));
        form.setStockMinimo(request.getParameter("stockMinimo"));

        Long idUsuarioLogueado = SessionUtils.getUsuarioLogueado(request).getIdUsuario();

        try {
            if (form.esAlta()) {
                productoService.crear(form, idUsuarioLogueado);
                response.sendRedirect(request.getContextPath() + "/productos?exito=creado");
            } else {
                productoService.actualizar(form, idUsuarioLogueado);
                response.sendRedirect(request.getContextPath() + "/productos?exito=modificado");
            }
        } catch (ValidacionException e) {
            request.setAttribute("errores", e.getErrores());
            volverAFormulario(request, response, form);
        } catch (ProveedorNoEncontradoException e) {
            request.setAttribute("mensajeError", e.getMessage());
            volverAFormulario(request, response, form);
        } catch (ProductoNoEncontradoException e) {
            response.sendRedirect(request.getContextPath() + "/productos?error=no_encontrado");
        }
    }

    private void volverAFormulario(HttpServletRequest request, HttpServletResponse response, ProductoFormDTO form)
            throws ServletException, IOException, SQLException {

        request.setAttribute("form", form);
        request.setAttribute("proveedores", listarProveedoresParaSelect());
        request.setAttribute("csrfToken", CsrfTokenUtils.obtenerOGenerarToken(request));
        request.getRequestDispatcher("/WEB-INF/jsp/productos/form.jsp").forward(request, response);
    }

    private void cambiarEstado(HttpServletRequest request, HttpServletResponse response)
            throws IOException, SQLException {

        Long idProducto = parsearLong(request.getParameter("id"));
        boolean activo = Boolean.parseBoolean(request.getParameter("activo"));
        Long idUsuarioLogueado = SessionUtils.getUsuarioLogueado(request).getIdUsuario();

        try {
            productoService.cambiarEstado(idProducto, activo, idUsuarioLogueado);
            response.sendRedirect(request.getContextPath() + "/productos?exito="
                    + (activo ? "activado" : "desactivado"));
        } catch (ProductoNoEncontradoException e) {
            response.sendRedirect(request.getContextPath() + "/productos?error=no_encontrado");
        } catch (ValidacionException e) {
            response.sendRedirect(request.getContextPath() + "/productos?error=en_uso");
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
