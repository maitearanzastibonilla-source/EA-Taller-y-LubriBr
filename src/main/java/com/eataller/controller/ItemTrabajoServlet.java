package com.eataller.controller;

import com.eataller.dto.ItemTrabajoFormDTO;
import com.eataller.exception.ItemTrabajoNoEncontradoException;
import com.eataller.exception.TrabajoNoEncontradoException;
import com.eataller.exception.ValidacionException;
import com.eataller.service.ItemTrabajoService;
import com.eataller.service.impl.ItemTrabajoServiceImpl;
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

@WebServlet(name = "ItemTrabajoServlet", urlPatterns = {"/items-trabajo"})
public class ItemTrabajoServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(ItemTrabajoServlet.class.getName());

    private final ItemTrabajoService itemTrabajoService = new ItemTrabajoServiceImpl();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Long trabajoId = parsearLong(request.getParameter("trabajoId"));

        if (!CsrfTokenUtils.esTokenValido(request)) {
            redirigirATrabajo(request, response, trabajoId, "error", "csrf");
            return;
        }

        String accion = request.getParameter("accion");
        Long idUsuarioLogueado = SessionUtils.getUsuarioLogueado(request).getIdUsuario();

        try {
            if ("guardar".equals(accion)) {
                ItemTrabajoFormDTO form = new ItemTrabajoFormDTO();
                form.setIdItem(parsearLong(request.getParameter("idItem")));
                form.setTrabajoId(trabajoId);
                form.setDescripcionLibre(request.getParameter("descripcionLibre"));
                form.setCantidad(request.getParameter("cantidad"));
                form.setPrecioUnitario(request.getParameter("precioUnitario"));

                if (form.esAlta()) {
                    itemTrabajoService.crear(form, idUsuarioLogueado);
                } else {
                    itemTrabajoService.actualizar(form, idUsuarioLogueado);
                }
                redirigirATrabajo(request, response, trabajoId, "exito", "item_guardado");
            } else if ("eliminar".equals(accion)) {
                Long idItem = parsearLong(request.getParameter("idItem"));
                itemTrabajoService.eliminar(idItem, idUsuarioLogueado);
                redirigirATrabajo(request, response, trabajoId, "exito", "item_eliminado");
            } else {
                redirigirATrabajo(request, response, trabajoId, null, null);
            }
        } catch (ValidacionException e) {
            redirigirATrabajo(request, response, trabajoId, "error", "item_invalido");
        } catch (ItemTrabajoNoEncontradoException | TrabajoNoEncontradoException e) {
            redirigirATrabajo(request, response, trabajoId, "error", "no_encontrado");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error de base de datos en ItemTrabajoServlet.", e);
            redirigirATrabajo(request, response, trabajoId, "error", "sql");
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
}
