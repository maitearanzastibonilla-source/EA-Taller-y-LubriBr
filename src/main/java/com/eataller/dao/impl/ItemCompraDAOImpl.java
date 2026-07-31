package com.eataller.dao.impl;

import com.eataller.config.DBConnectionManager;
import com.eataller.dao.ItemCompraDAO;
import com.eataller.entity.ItemCompra;

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

public class ItemCompraDAOImpl implements ItemCompraDAO {

    private static final String SQL_INSERT =
            "INSERT INTO items_compra (compra_id, producto_id, cantidad, precio_unitario) VALUES (?, ?, ?, ?)";

    private static final String SQL_UPDATE =
            "UPDATE items_compra SET producto_id = ?, cantidad = ?, precio_unitario = ? WHERE id_item_compra = ?";

    private static final String SQL_DELETE =
            "DELETE FROM items_compra WHERE id_item_compra = ?";

    private static final String SQL_BUSCAR_POR_ID =
            "SELECT * FROM items_compra WHERE id_item_compra = ?";

    private static final String SQL_LISTAR_POR_COMPRA =
            "SELECT * FROM items_compra WHERE compra_id = ? ORDER BY id_item_compra ASC";

    private static final String SQL_SUMAR_TOTAL =
            "SELECT COALESCE(SUM(cantidad * precio_unitario), 0) FROM items_compra WHERE compra_id = ?";

    @Override
    public ItemCompra crear(Connection connection, ItemCompra item) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, item.getCompraId());
            stmt.setLong(2, item.getProductoId());
            stmt.setInt(3, item.getCantidad());
            stmt.setBigDecimal(4, item.getPrecioUnitario());
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    item.setIdItemCompra(keys.getLong(1));
                }
            }
            return item;
        }
    }

    @Override
    public void actualizar(Connection connection, ItemCompra item) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(SQL_UPDATE)) {
            stmt.setLong(1, item.getProductoId());
            stmt.setInt(2, item.getCantidad());
            stmt.setBigDecimal(3, item.getPrecioUnitario());
            stmt.setLong(4, item.getIdItemCompra());
            stmt.executeUpdate();
        }
    }

    @Override
    public void eliminar(Connection connection, Long idItemCompra) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(SQL_DELETE)) {
            stmt.setLong(1, idItemCompra);
            stmt.executeUpdate();
        }
    }

    @Override
    public Optional<ItemCompra> buscarPorId(Long idItemCompra) throws SQLException {
        try (Connection connection = DBConnectionManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(SQL_BUSCAR_POR_ID)) {

            stmt.setLong(1, idItemCompra);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        }
    }

    @Override
    public List<ItemCompra> listarPorCompra(Long compraId) throws SQLException {
        try (Connection connection = DBConnectionManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(SQL_LISTAR_POR_COMPRA)) {

            stmt.setLong(1, compraId);
            List<ItemCompra> items = new ArrayList<>();
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    items.add(mapRow(rs));
                }
            }
            return items;
        }
    }

    @Override
    public BigDecimal sumarTotalPorCompra(Connection connection, Long compraId) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(SQL_SUMAR_TOTAL)) {
            stmt.setLong(1, compraId);
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                return rs.getBigDecimal(1);
            }
        }
    }

    private ItemCompra mapRow(ResultSet rs) throws SQLException {
        ItemCompra item = new ItemCompra();
        item.setIdItemCompra(rs.getLong("id_item_compra"));
        item.setCompraId(rs.getLong("compra_id"));
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
