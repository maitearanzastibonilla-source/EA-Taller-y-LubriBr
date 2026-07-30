package com.eataller.service.impl;

import com.eataller.config.DBConnectionManager;
import com.eataller.constants.AppConstants;
import com.eataller.dao.AuditoriaDAO;
import com.eataller.dao.ItemVentaDAO;
import com.eataller.dao.ProductoDAO;
import com.eataller.dao.VentaDAO;
import com.eataller.dao.impl.AuditoriaDAOImpl;
import com.eataller.dao.impl.ItemVentaDAOImpl;
import com.eataller.dao.impl.ProductoDAOImpl;
import com.eataller.dao.impl.VentaDAOImpl;
import com.eataller.dto.ItemVentaDTO;
import com.eataller.dto.ItemVentaFormDTO;
import com.eataller.entity.Auditoria;
import com.eataller.entity.Estado;
import com.eataller.entity.EstadoVenta;
import com.eataller.entity.ItemVenta;
import com.eataller.entity.Producto;
import com.eataller.entity.VentaDirecta;
import com.eataller.exception.ItemVentaNoEncontradoException;
import com.eataller.exception.ProductoNoEncontradoException;
import com.eataller.exception.ValidacionException;
import com.eataller.exception.VentaNoEncontradaException;
import com.eataller.service.ItemVentaService;
import com.eataller.utils.ItemVentaMapper;
import com.eataller.validator.ItemVentaValidator;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ItemVentaServiceImpl implements ItemVentaService {

    private final ItemVentaDAO itemVentaDAO;
    private final VentaDAO ventaDAO;
    private final ProductoDAO productoDAO;
    private final AuditoriaDAO auditoriaDAO;

    public ItemVentaServiceImpl() {
        this.itemVentaDAO = new ItemVentaDAOImpl();
        this.ventaDAO = new VentaDAOImpl();
        this.productoDAO = new ProductoDAOImpl();
        this.auditoriaDAO = new AuditoriaDAOImpl();
    }

    @Override
    public ItemVentaDTO crear(ItemVentaFormDTO form, Long idUsuarioCreador)
            throws ValidacionException, VentaNoEncontradaException, ProductoNoEncontradoException, SQLException {

        ItemVentaValidator.validar(form);

        VentaDirecta venta = ventaDAO.buscarPorId(form.getVentaId())
                .orElseThrow(() -> new VentaNoEncontradaException("No se encontro la venta indicada."));

        if (venta.getEstado() != EstadoVenta.PENDIENTE) {
            throw new ValidacionException(Map.of("productoId", "Solo se pueden cargar items en una venta pendiente."));
        }

        Producto producto = productoDAO.buscarPorId(form.getProductoId())
                .orElseThrow(() -> new ProductoNoEncontradoException("No se encontro el producto seleccionado."));

        if (producto.getEstado() != Estado.ACTIVO) {
            throw new ValidacionException(Map.of("productoId", "El producto seleccionado no esta activo."));
        }

        int cantidad = Integer.parseInt(form.getCantidad().trim());
        if (cantidad > producto.getStockActual()) {
            throw new ValidacionException(Map.of("cantidad",
                    "La cantidad supera el stock disponible (" + producto.getStockActual() + " unidades)."));
        }

        ItemVenta item = new ItemVenta();
        item.setVentaId(venta.getIdVenta());
        item.setProductoId(producto.getIdProducto());
        item.setCantidad(cantidad);
        item.setPrecioUnitario(new BigDecimal(form.getPrecioUnitario().trim()));

        try (Connection connection = DBConnectionManager.getConnection()) {
            connection.setAutoCommit(false);
            try {
                itemVentaDAO.crear(connection, item);
                actualizarTotalVenta(connection, venta.getIdVenta());

                Auditoria auditoria = new Auditoria(idUsuarioCreador,
                        AppConstants.MODULO_ITEMS_VENTA, "ALTA", "items_venta", item.getIdItemVenta(), null,
                        "venta_id=" + item.getVentaId() + ", producto_id=" + item.getProductoId()
                                + ", cantidad=" + item.getCantidad() + ", precio_unitario=" + item.getPrecioUnitario(),
                        "EXITO", "Alta de item de venta.");
                auditoriaDAO.registrar(connection, auditoria);

                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        }

        return ItemVentaMapper.aDTO(item, producto.getNombre());
    }

    @Override
    public ItemVentaDTO actualizar(ItemVentaFormDTO form, Long idUsuarioEditor)
            throws ValidacionException, ItemVentaNoEncontradoException, VentaNoEncontradaException,
            ProductoNoEncontradoException, SQLException {

        ItemVentaValidator.validar(form);

        ItemVenta existente = itemVentaDAO.buscarPorId(form.getIdItemVenta())
                .orElseThrow(() -> new ItemVentaNoEncontradoException("No se encontro el item solicitado."));

        VentaDirecta venta = ventaDAO.buscarPorId(existente.getVentaId())
                .orElseThrow(() -> new VentaNoEncontradaException("No se encontro la venta asociada a este item."));

        if (venta.getEstado() != EstadoVenta.PENDIENTE) {
            throw new ValidacionException(Map.of("productoId", "Solo se pueden modificar items de una venta pendiente."));
        }

        Producto producto = productoDAO.buscarPorId(form.getProductoId())
                .orElseThrow(() -> new ProductoNoEncontradoException("No se encontro el producto seleccionado."));

        int cantidad = Integer.parseInt(form.getCantidad().trim());
        if (cantidad > producto.getStockActual()) {
            throw new ValidacionException(Map.of("cantidad",
                    "La cantidad supera el stock disponible (" + producto.getStockActual() + " unidades)."));
        }

        String valoresAnteriores = "producto_id=" + existente.getProductoId() + ", cantidad=" + existente.getCantidad()
                + ", precio_unitario=" + existente.getPrecioUnitario();

        existente.setProductoId(producto.getIdProducto());
        existente.setCantidad(cantidad);
        existente.setPrecioUnitario(new BigDecimal(form.getPrecioUnitario().trim()));

        try (Connection connection = DBConnectionManager.getConnection()) {
            connection.setAutoCommit(false);
            try {
                itemVentaDAO.actualizar(connection, existente);
                actualizarTotalVenta(connection, venta.getIdVenta());

                String valoresNuevos = "producto_id=" + existente.getProductoId() + ", cantidad=" + existente.getCantidad()
                        + ", precio_unitario=" + existente.getPrecioUnitario();
                Auditoria auditoria = new Auditoria(idUsuarioEditor,
                        AppConstants.MODULO_ITEMS_VENTA, "MODIFICACION", "items_venta", existente.getIdItemVenta(),
                        valoresAnteriores, valoresNuevos, "EXITO", "Modificacion de item de venta.");
                auditoriaDAO.registrar(connection, auditoria);

                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        }

        return ItemVentaMapper.aDTO(existente, producto.getNombre());
    }

    @Override
    public void eliminar(Long idItemVenta, Long idUsuarioEditor)
            throws ValidacionException, ItemVentaNoEncontradoException, VentaNoEncontradaException, SQLException {

        ItemVenta existente = itemVentaDAO.buscarPorId(idItemVenta)
                .orElseThrow(() -> new ItemVentaNoEncontradoException("No se encontro el item solicitado."));

        VentaDirecta venta = ventaDAO.buscarPorId(existente.getVentaId())
                .orElseThrow(() -> new VentaNoEncontradaException("No se encontro la venta asociada a este item."));

        if (venta.getEstado() != EstadoVenta.PENDIENTE) {
            throw new ValidacionException(Map.of("productoId", "Solo se pueden eliminar items de una venta pendiente."));
        }

        try (Connection connection = DBConnectionManager.getConnection()) {
            connection.setAutoCommit(false);
            try {
                itemVentaDAO.eliminar(connection, idItemVenta);
                actualizarTotalVenta(connection, venta.getIdVenta());

                Auditoria auditoria = new Auditoria(idUsuarioEditor,
                        AppConstants.MODULO_ITEMS_VENTA, "BAJA", "items_venta", idItemVenta,
                        "producto_id=" + existente.getProductoId() + ", cantidad=" + existente.getCantidad()
                                + ", precio_unitario=" + existente.getPrecioUnitario(),
                        null, "EXITO", "Eliminacion de item de venta.");
                auditoriaDAO.registrar(connection, auditoria);

                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        }
    }

    @Override
    public List<ItemVentaDTO> listarPorVenta(Long ventaId) throws SQLException {
        List<ItemVenta> items = itemVentaDAO.listarPorVenta(ventaId);

        Map<Long, String> nombresProductos = new HashMap<>();

        return items.stream()
                .map(item -> {
                    String nombreProducto = nombresProductos.computeIfAbsent(item.getProductoId(),
                            this::buscarNombreProductoSilencioso);
                    return ItemVentaMapper.aDTO(item, nombreProducto);
                })
                .collect(Collectors.toList());
    }

    private void actualizarTotalVenta(Connection connection, Long ventaId) throws SQLException {
        BigDecimal total = itemVentaDAO.sumarTotalPorVenta(connection, ventaId);
        ventaDAO.actualizarTotal(connection, ventaId, total);
    }

    private String buscarNombreProductoSilencioso(Long idProducto) {
        try {
            return productoDAO.buscarPorId(idProducto).map(Producto::getNombre).orElse("(sin datos)");
        } catch (SQLException e) {
            return "(sin datos)";
        }
    }
}
