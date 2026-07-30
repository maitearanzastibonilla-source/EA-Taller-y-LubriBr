package com.eataller.dao.impl;

import com.eataller.config.DBConnectionManager;
import com.eataller.dao.ItemTrabajoDAO;
import com.eataller.entity.ItemTrabajo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ItemTrabajoDAOImpl implements ItemTrabajoDAO {

    private static final String SQL_INSERT =
            "INSERT INTO items_trabajo (trabajo_id, producto_id, descripcion_libre, cantidad, precio_unitario) "
                    + "VALUES (?, ?, ?, ?, ?)";

    private static final String SQL_UPDATE =
            "UPDATE items_trabajo SET descripcion_libre = ?, cantidad = ?, precio_unitario = ? WHERE id_item = ?";

    private static final String SQL_ELIMINAR =
            "DELETE FROM items_trabajo WHERE id_item = ?";

    private static final String SQL_BUSCAR_POR_ID =
            "SELECT * FROM items_trabajo WHERE id_item = ?";

    private static final String SQL_LISTAR_POR_TRABAJO =
            "SELECT * FROM items_trabajo WHERE trabajo_id = ? ORDER BY id_item ASC";

    @Override
    public ItemTrabajo crear(Connection connection, ItemTrabajo item) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, item.getTrabajoId());
            if (item.getProductoId() != null) {
                stmt.setLong(2, item.getProductoId());
            } else {
                stmt.setNull(2, Types.BIGINT);
            }
            stmt.setString(3, item.getDescripcionLibre());
            stmt.setBigDecimal(4, item.getCantidad());
            stmt.setBigDecimal(5, item.getPrecioUnitario());
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    item.setIdItem(keys.getLong(1));
                }
            }
            return item;
        }
    }

    @Override
    public void actualizar(Connection connection, ItemTrabajo item) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(SQL_UPDATE)) {
            stmt.setString(1, item.getDescripcionLibre());
            stmt.setBigDecimal(2, item.getCantidad());
            stmt.setBigDecimal(3, item.getPrecioUnitario());
            stmt.setLong(4, item.getIdItem());
            stmt.executeUpdate();
        }
    }

    @Override
    public void eliminar(Connection connection, Long idItem) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(SQL_ELIMINAR)) {
            stmt.setLong(1, idItem);
            stmt.executeUpdate();
        }
    }

    @Override
    public Optional<ItemTrabajo> buscarPorId(Long idItem) throws SQLException {
        try (Connection connection = DBConnectionManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(SQL_BUSCAR_POR_ID)) {

            stmt.setLong(1, idItem);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        }
    }

    @Override
    public List<ItemTrabajo> listarPorTrabajo(Long trabajoId) throws SQLException {
        try (Connection connection = DBConnectionManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(SQL_LISTAR_POR_TRABAJO)) {

            stmt.setLong(1, trabajoId);
            List<ItemTrabajo> items = new ArrayList<>();
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    items.add(mapRow(rs));
                }
            }
            return items;
        }
    }

    private ItemTrabajo mapRow(ResultSet rs) throws SQLException {
        ItemTrabajo item = new ItemTrabajo();
        item.setIdItem(rs.getLong("id_item"));
        item.setTrabajoId(rs.getLong("trabajo_id"));

        long productoId = rs.getLong("producto_id");
        item.setProductoId(rs.wasNull() ? null : productoId);

        item.setDescripcionLibre(rs.getString("descripcion_libre"));
        item.setCantidad(rs.getBigDecimal("cantidad"));
        item.setPrecioUnitario(rs.getBigDecimal("precio_unitario"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        item.setCreatedAt(createdAt == null ? null : createdAt.toLocalDateTime());

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        item.setUpdatedAt(updatedAt == null ? null : updatedAt.toLocalDateTime());

        return item;
    }
}
