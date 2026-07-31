package com.eataller.service.impl;

import com.eataller.config.DBConnectionManager;
import com.eataller.constants.AppConstants;
import com.eataller.dao.AuditoriaDAO;
import com.eataller.dao.CompraDAO;
import com.eataller.dao.ItemCompraDAO;
import com.eataller.dao.ProductoDAO;
import com.eataller.dao.impl.AuditoriaDAOImpl;
import com.eataller.dao.impl.CompraDAOImpl;
import com.eataller.dao.impl.ItemCompraDAOImpl;
import com.eataller.dao.impl.ProductoDAOImpl;
import com.eataller.dto.ItemCompraDTO;
import com.eataller.dto.ItemCompraFormDTO;
import com.eataller.entity.Auditoria;
import com.eataller.entity.Compra;
import com.eataller.entity.EstadoCompra;
import com.eataller.entity.ItemCompra;
import com.eataller.entity.Producto;
import com.eataller.exception.CompraNoEncontradaException;
import com.eataller.exception.ItemCompraNoEncontradoException;
import com.eataller.exception.ProductoNoEncontradoException;
import com.eataller.exception.ValidacionException;
import com.eataller.service.ItemCompraService;
import com.eataller.utils.ItemCompraMapper;
import com.eataller.validator.ItemCompraValidator;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ItemCompraServiceImpl implements ItemCompraService {

    private final ItemCompraDAO itemCompraDAO;
    private final CompraDAO compraDAO;
    private final ProductoDAO productoDAO;
    private final AuditoriaDAO auditoriaDAO;

    public ItemCompraServiceImpl() {
        this.itemCompraDAO = new ItemCompraDAOImpl();
        this.compraDAO = new CompraDAOImpl();
        this.productoDAO = new ProductoDAOImpl();
        this.auditoriaDAO = new AuditoriaDAOImpl();
    }

    @Override
    public ItemCompraDTO crear(ItemCompraFormDTO form, Long idUsuarioCreador)
            throws ValidacionException, CompraNoEncontradaException, ProductoNoEncontradoException, SQLException {

        ItemCompraValidator.validar(form);

        Compra compra = compraDAO.buscarPorId(form.getCompraId())
                .orElseThrow(() -> new CompraNoEncontradaException("No se encontro la compra indicada."));

        if (compra.getEstado() != EstadoCompra.PENDIENTE) {
            throw new ValidacionException(Map.of("productoId", "Solo se pueden cargar items en una compra pendiente."));
        }

        Producto producto = productoDAO.buscarPorId(form.getProductoId())
                .orElseThrow(() -> new ProductoNoEncontradoException("No se encontro el producto seleccionado."));

        ItemCompra item = new ItemCompra();
        item.setCompraId(compra.getIdCompra());
        item.setProductoId(producto.getIdProducto());
        item.setCantidad(Integer.parseInt(form.getCantidad().trim()));
        item.setPrecioUnitario(new BigDecimal(form.getPrecioUnitario().trim()));

        try (Connection connection = DBConnectionManager.getConnection()) {
            connection.setAutoCommit(false);
            try {
                itemCompraDAO.crear(connection, item);
                actualizarTotalCompra(connection, compra.getIdCompra());

                Auditoria auditoria = new Auditoria(idUsuarioCreador, AppConstants.MODULO_ITEMS_COMPRA,
                        "ALTA", "items_compra", item.getIdItemCompra(), null,
                        "compra_id=" + item.getCompraId() + ", producto_id=" + item.getProductoId()
                                + ", cantidad=" + item.getCantidad() + ", precio_unitario=" + item.getPrecioUnitario(),
                        "EXITO", "Alta de item de compra.");
                auditoriaDAO.registrar(connection, auditoria);

                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        }

        return ItemCompraMapper.aDTO(item, producto.getNombre());
    }

    @Override
    public ItemCompraDTO actualizar(ItemCompraFormDTO form, Long idUsuarioEditor)
            throws ValidacionException, ItemCompraNoEncontradoException, CompraNoEncontradaException,
            ProductoNoEncontradoException, SQLException {

        ItemCompraValidator.validar(form);

        ItemCompra existente = itemCompraDAO.buscarPorId(form.getIdItemCompra())
                .orElseThrow(() -> new ItemCompraNoEncontradoException("No se encontro el item solicitado."));

        Compra compra = compraDAO.buscarPorId(existente.getCompraId())
                .orElseThrow(() -> new CompraNoEncontradaException("No se encontro la compra asociada a este item."));

        if (compra.getEstado() != EstadoCompra.PENDIENTE) {
            throw new ValidacionException(Map.of("productoId", "Solo se pueden modificar items de una compra pendiente."));
        }

        Producto producto = productoDAO.buscarPorId(form.getProductoId())
                .orElseThrow(() -> new ProductoNoEncontradoException("No se encontro el producto seleccionado."));

        String valoresAnteriores = "producto_id=" + existente.getProductoId() + ", cantidad=" + existente.getCantidad()
                + ", precio_unitario=" + existente.getPrecioUnitario();

        existente.setProductoId(producto.getIdProducto());
        existente.setCantidad(Integer.parseInt(form.getCantidad().trim()));
        existente.setPrecioUnitario(new BigDecimal(form.getPrecioUnitario().trim()));

        try (Connection connection = DBConnectionManager.getConnection()) {
            connection.setAutoCommit(false);
            try {
                itemCompraDAO.actualizar(connection, existente);
                actualizarTotalCompra(connection, compra.getIdCompra());

                String valoresNuevos = "producto_id=" + existente.getProductoId() + ", cantidad=" + existente.getCantidad()
                        + ", precio_unitario=" + existente.getPrecioUnitario();
                Auditoria auditoria = new Auditoria(idUsuarioEditor, AppConstants.MODULO_ITEMS_COMPRA,
                        "MODIFICACION", "items_compra", existente.getIdItemCompra(),
                        valoresAnteriores, valoresNuevos, "EXITO", "Modificacion de item de compra.");
                auditoriaDAO.registrar(connection, auditoria);

                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        }

        return ItemCompraMapper.aDTO(existente, producto.getNombre());
    }

    @Override
    public void eliminar(Long idItemCompra, Long idUsuarioEditor)
            throws ValidacionException, ItemCompraNoEncontradoException, CompraNoEncontradaException, SQLException {

        ItemCompra existente = itemCompraDAO.buscarPorId(idItemCompra)
                .orElseThrow(() -> new ItemCompraNoEncontradoException("No se encontro el item solicitado."));

        Compra compra = compraDAO.buscarPorId(existente.getCompraId())
                .orElseThrow(() -> new CompraNoEncontradaException("No se encontro la compra asociada a este item."));

        if (compra.getEstado() != EstadoCompra.PENDIENTE) {
            throw new ValidacionException(Map.of("productoId", "Solo se pueden eliminar items de una compra pendiente."));
        }

        try (Connection connection = DBConnectionManager.getConnection()) {
            connection.setAutoCommit(false);
            try {
                itemCompraDAO.eliminar(connection, idItemCompra);
                actualizarTotalCompra(connection, compra.getIdCompra());

                Auditoria auditoria = new Auditoria(idUsuarioEditor, AppConstants.MODULO_ITEMS_COMPRA,
                        "BAJA", "items_compra", idItemCompra,
                        "producto_id=" + existente.getProductoId() + ", cantidad=" + existente.getCantidad()
                                + ", precio_unitario=" + existente.getPrecioUnitario(),
                        null, "EXITO", "Eliminacion de item de compra.");
                auditoriaDAO.registrar(connection, auditoria);

                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        }
    }

    @Override
    public List<ItemCompraDTO> listarPorCompra(Long compraId) throws SQLException {
        List<ItemCompra> items = itemCompraDAO.listarPorCompra(compraId);

        Map<Long, String> nombresProductos = new HashMap<>();

        return items.stream()
                .map(item -> {
                    String nombreProducto = nombresProductos.computeIfAbsent(item.getProductoId(),
                            this::buscarNombreProductoSilencioso);
                    return ItemCompraMapper.aDTO(item, nombreProducto);
                })
                .collect(Collectors.toList());
    }

    private void actualizarTotalCompra(Connection connection, Long compraId) throws SQLException {
        BigDecimal total = itemCompraDAO.sumarTotalPorCompra(connection, compraId);
        compraDAO.actualizarTotal(connection, compraId, total);
    }

    private String buscarNombreProductoSilencioso(Long idProducto) {
        try {
            return productoDAO.buscarPorId(idProducto).map(Producto::getNombre).orElse("(sin datos)");
        } catch (SQLException e) {
            return "(sin datos)";
        }
    }
}
