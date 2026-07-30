package com.eataller.controller;

import com.eataller.dto.ItemVentaFormDTO;
import com.eataller.exception.ItemVentaNoEncontradoException;
import com.eataller.exception.ProductoNoEncontradoException;
import com.eataller.exception.ValidacionException;
import com.eataller.exception.VentaNoEncontradaException;
import com.eataller.service.ItemVentaService;
import com.eataller.service.impl.ItemVentaServiceImpl;
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

@WebServlet(name = "ItemVentaServlet", urlPatterns = {"/items-venta"})
public class ItemVentaServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(ItemVentaServlet.class.getName());

    private final ItemVentaService itemVentaService = new ItemVentaServiceImpl();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Long ventaId = parsearLong(request.getParameter("ventaId"));

        if (!CsrfTokenUtils.esTokenValido(request)) {
            redirigirAVenta(request, response, ventaId, "error", "csrf");
            return;
        }

        String accion = request.getParameter("accion");
        Long idUsuarioLogueado = SessionUtils.getUsuarioLogueado(request).getIdUsuario();

        try {
            if ("guardar".equals(accion)) {
                ItemVentaFormDTO form = new ItemVentaFormDTO();
                form.setIdItemVenta(parsearLong(request.getParameter("idItemVenta")));
                form.setVentaId(ventaId);
                form.setProductoId(parsearLong(request.getParameter("productoId")));
                form.setCantidad(request.getParameter("cantidad"));
                form.setPrecioUnitario(request.getParameter("precioUnitario"));

                if (form.esAlta()) {
                    itemVentaService.crear(form, idUsuarioLogueado);
                } else {
                    itemVentaService.actualizar(form, idUsuarioLogueado);
                }
                redirigirAVenta(request, response, ventaId, "exito", "item_guardado");
            } else if ("eliminar".equals(accion)) {
                Long idItemVenta = parsearLong(request.getParameter("idItemVenta"));
                itemVentaService.eliminar(idItemVenta, idUsuarioLogueado);
                redirigirAVenta(request, response, ventaId, "exito", "item_eliminado");
            } else {
                redirigirAVenta(request, response, ventaId, null, null);
            }
        } catch (ValidacionException e) {
            redirigirAVenta(request, response, ventaId, "error", "item_invalido");
        } catch (ItemVentaNoEncontradoException | VentaNoEncontradaException | ProductoNoEncontradoException e) {
            redirigirAVenta(request, response, ventaId, "error", "no_encontrado");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error de base de datos en ItemVentaServlet.", e);
            redirigirAVenta(request, response, ventaId, "error", "sql");
        }
    }

    private void redirigirAVenta(HttpServletRequest request, HttpServletResponse response, Long ventaId,
                                  String claveParametro, String valorParametro) throws IOException {
        StringBuilder url = new StringBuilder(request.getContextPath())
                .append("/ventas?accion=detalle&id=").append(ventaId);
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
}
