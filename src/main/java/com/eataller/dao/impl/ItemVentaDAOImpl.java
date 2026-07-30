package com.eataller.dao.impl;

import com.eataller.config.DBConnectionManager;
import com.eataller.dao.ItemVentaDAO;
import com.eataller.entity.ItemVenta;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ItemVentaDAOImpl implements ItemVentaDAO {

    private static final String SQL_INSERT =
            "INSERT INTO items_venta (venta_id, producto_id, cantidad, precio_unitario) VALUES (?, ?, ?, ?)";

    private static final String SQL_UPDATE =
            "UPDATE items_venta SET producto_id = ?, cantidad = ?, precio_unitario = ? WHERE id_item_venta = ?";

    private static final String SQL_DELETE =
            "DELETE FROM items_venta WHERE id_item_venta = ?";

    private static final String SQL_BUSCAR_POR_ID =
            "SELECT * FROM items_venta WHERE id_item_venta = ?";

    private static final String SQL_LISTAR_POR_VENTA =
            "SELECT * FROM items_venta WHERE venta_id = ? ORDER BY id_item_venta ASC";

    private static final String SQL_SUMAR_TOTAL =
            "SELECT COALESCE(SUM(cantidad * precio_unitario), 0) FROM items_venta WHERE venta_id = ?";

    @Override
    public ItemVenta crear(Connection connection, ItemVenta item) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, item.getVentaId());
            stmt.setLong(2, item.getProductoId());
            stmt.setInt(3, item.getCantidad());
            stmt.setBigDecimal(4, item.getPrecioUnitario());
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    item.setIdItemVenta(keys.getLong(1));
                }
            }
            return item;
        }
    }

    @Override
    public void actualizar(Connection connection, ItemVenta item) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(SQL_UPDATE)) {
            stmt.setLong(1, item.getProductoId());
            stmt.setInt(2, item.getCantidad());
            stmt.setBigDecimal(3, item.getPrecioUnitario());
            stmt.setLong(4, item.getIdItemVenta());
            stmt.executeUpdate();
        }
    }

    @Override
    public void eliminar(Connection connection, Long idItemVenta) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(SQL_DELETE)) {
            stmt.setLong(1, idItemVenta);
            stmt.executeUpdate();
        }
    }

    @Override
    public Optional<ItemVenta> buscarPorId(Long idItemVenta) throws SQLException {
        try (Connection connection = DBConnectionManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(SQL_BUSCAR_POR_ID)) {

            stmt.setLong(1, idItemVenta);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        }
    }

    @Override
    public List<ItemVenta> listarPorVenta(Long ventaId) throws SQLException {
        try (Connection connection = DBConnectionManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(SQL_LISTAR_POR_VENTA)) {

            stmt.setLong(1, ventaId);
            List<ItemVenta> items = new ArrayList<>();
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    items.add(mapRow(rs));
                }
            }
            return items;
        }
    }

    @Override
    public BigDecimal sumarTotalPorVenta(Connection connection, Long ventaId) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(SQL_SUMAR_TOTAL)) {
            stmt.setLong(1, ventaId);
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                return rs.getBigDecimal(1);
            }
        }
    }

    private ItemVenta mapRow(ResultSet rs) throws SQLException {
        ItemVenta item = new ItemVenta();
        item.setIdItemVenta(rs.getLong("id_item_venta"));
        item.setVentaId(rs.getLong("venta_id"));
        item.setProductoId(rs.getLong("producto_id"));
        item.setCantidad(rs.getInt("cantidad"));
        item.setPrecioUnitario(rs.getBigDecimal("precio_unitario"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        item.setCreatedAt(createdAt == null ? null : createdAt.toLocalDateTime());

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        item.setUpdatedAt(updatedAt == null ? null : updatedAt.toLocalDateTime());

        return item;
    }
}
