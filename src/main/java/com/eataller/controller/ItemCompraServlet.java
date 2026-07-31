package com.eataller.controller;

import com.eataller.dto.ItemCompraFormDTO;
import com.eataller.exception.CompraNoEncontradaException;
import com.eataller.exception.ItemCompraNoEncontradoException;
import com.eataller.exception.ProductoNoEncontradoException;
import com.eataller.exception.ValidacionException;
import com.eataller.service.ItemCompraService;
import com.eataller.service.impl.ItemCompraServiceImpl;
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

@WebServlet(name = "ItemCompraServlet", urlPatterns = {"/items-compra"})
public class ItemCompraServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(ItemCompraServlet.class.getName());

    private final ItemCompraService itemCompraService = new ItemCompraServiceImpl();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Long compraId = parsearLong(request.getParameter("compraId"));

        if (!CsrfTokenUtils.esTokenValido(request)) {
            redirigirACompra(request, response, compraId, "error", "csrf");
            return;
        }

        String accion = request.getParameter("accion");
        Long idUsuarioLogueado = SessionUtils.getUsuarioLogueado(request).getIdUsuario();

        try {
            if ("guardar".equals(accion)) {
                ItemCompraFormDTO form = new ItemCompraFormDTO();
                form.setIdItemCompra(parsearLong(request.getParameter("idItemCompra")));
                form.setCompraId(compraId);
                form.setProductoId(parsearLong(request.getParameter("productoId")));
                form.setCantidad(request.getParameter("cantidad"));
                form.setPrecioUnitario(request.getParameter("precioUnitario"));

                if (form.esAlta()) {
                    itemCompraService.crear(form, idUsuarioLogueado);
                } else {
                    itemCompraService.actualizar(form, idUsuarioLogueado);
                }
                redirigirACompra(request, response, compraId, "exito", "item_guardado");
            } else if ("eliminar".equals(accion)) {
                Long idItemCompra = parsearLong(request.getParameter("idItemCompra"));
                itemCompraService.eliminar(idItemCompra, idUsuarioLogueado);
                redirigirACompra(request, response, compraId, "exito", "item_eliminado");
            } else {
                redirigirACompra(request, response, compraId, null, null);
            }
        } catch (ValidacionException e) {
            redirigirACompra(request, response, compraId, "error", "item_invalido");
        } catch (ItemCompraNoEncontradoException | CompraNoEncontradaException | ProductoNoEncontradoException e) {
            redirigirACompra(request, response, compraId, "error", "no_encontrado");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error de base de datos en ItemCompraServlet.", e);
            redirigirACompra(request, response, compraId, "error", "sql");
        }
    }

    private void redirigirACompra(HttpServletRequest request, HttpServletResponse response, Long compraId,
                                   String claveParametro, String valorParametro) throws IOException {
        StringBuilder url = new StringBuilder(request.getContextPath())
                .append("/compras?accion=detalle&id=").append(compraId);
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
