package com.eataller.dao.impl;

import com.eataller.config.DBConnectionManager;
import com.eataller.dao.CompraDAO;
import com.eataller.dto.PaginaResultado;
import com.eataller.entity.Compra;
import com.eataller.entity.EstadoCompra;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CompraDAOImpl implements CompraDAO {

    private static final String SQL_INSERT =
            "INSERT INTO compras (proveedor_id, usuario_id, fecha, total, estado) VALUES (?, ?, ?, ?, 'PENDIENTE')";

    private static final String SQL_ACTUALIZAR_PROVEEDOR =
            "UPDATE compras SET proveedor_id = ? WHERE id_compra = ?";

    private static final String SQL_ACTUALIZAR_TOTAL =
            "UPDATE compras SET total = ? WHERE id_compra = ?";

    private static final String SQL_CONFIRMAR =
            "UPDATE compras SET estado = 'CONFIRMADA' WHERE id_compra = ?";

    private static final String SQL_ANULAR =
            "UPDATE compras SET estado = 'ANULADA' WHERE id_compra = ?";

    private static final String SQL_BUSCAR_POR_ID =
            "SELECT * FROM compras WHERE id_compra = ?";

    @Override
    public Compra crear(Connection connection, Compra compra) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, compra.getProveedorId());
            stmt.setLong(2, compra.getUsuarioId());
            stmt.setObject(3, compra.getFecha());
            stmt.setBigDecimal(4, compra.getTotal());
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    compra.setIdCompra(keys.getLong(1));
                }
            }
            return compra;
        }
    }

    @Override
    public void actualizarProveedor(Connection connection, Long idCompra, Long proveedorId) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(SQL_ACTUALIZAR_PROVEEDOR)) {
            stmt.setLong(1, proveedorId);
            stmt.setLong(2, idCompra);
            stmt.executeUpdate();
        }
    }

    @Override
    public void actualizarTotal(Connection connection, Long idCompra, BigDecimal total) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(SQL_ACTUALIZAR_TOTAL)) {
            stmt.setBigDecimal(1, total);
            stmt.setLong(2, idCompra);
            stmt.executeUpdate();
        }
    }

    @Override
    public void confirmar(Connection connection, Long idCompra) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(SQL_CONFIRMAR)) {
            stmt.setLong(1, idCompra);
            stmt.executeUpdate();
        }
    }

    @Override
    public void anular(Connection connection, Long idCompra) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(SQL_ANULAR)) {
            stmt.setLong(1, idCompra);
            stmt.executeUpdate();
        }
    }

    @Override
    public Optional<Compra> buscarPorId(Long idCompra) throws SQLException {
        try (Connection connection = DBConnectionManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(SQL_BUSCAR_POR_ID)) {

            stmt.setLong(1, idCompra);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        }
    }

    @Override
    public PaginaResultado<Compra> listar(String textoBusqueda, String estadoFiltro,
                                           LocalDate fechaDesde, LocalDate fechaHasta,
                                           int pagina, int registrosPorPagina) throws SQLException {

        StringBuilder where = new StringBuilder(" WHERE 1 = 1 ");
        List<Object> parametros = new ArrayList<>();

        if (textoBusqueda != null && !textoBusqueda.isBlank()) {
            where.append(" AND (c.id_compra = ? OR pr.nombre LIKE ?) ");
            String textoTrim = textoBusqueda.trim();
            String comodin = "%" + textoTrim + "%";
            long numeroBuscado;
            try {
                numeroBuscado = Long.parseLong(textoTrim);
            } catch (NumberFormatException e) {
                numeroBuscado = -1;
            }
            parametros.add(numeroBuscado);
            parametros.add(comodin);
        }
        if (estadoFiltro != null && !estadoFiltro.isBlank()) {
            where.append(" AND c.estado = ? ");
            parametros.add(estadoFiltro);
        }
        if (fechaDesde != null) {
            where.append(" AND c.fecha >= ? ");
            parametros.add(fechaDesde);
        }
        if (fechaHasta != null) {
            where.append(" AND c.fecha <= ? ");
            parametros.add(fechaHasta);
        }

        String sqlBase = " FROM compras c JOIN proveedores pr ON c.proveedor_id = pr.id_proveedor" + where;
        String sqlConteo = "SELECT COUNT(*)" + sqlBase;
        String sqlDatos = "SELECT c.*" + sqlBase + " ORDER BY c.id_compra DESC LIMIT ? OFFSET ?";

        long total;
        try (Connection connection = DBConnectionManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sqlConteo)) {
            asignarParametros(stmt, parametros);
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                total = rs.getLong(1);
            }
        }

        int paginaSegura = Math.max(pagina, 1);
        int offset = (paginaSegura - 1) * registrosPorPagina;
        List<Compra> compras = new ArrayList<>();

        try (Connection connection = DBConnectionManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sqlDatos)) {
            List<Object> parametrosDatos = new ArrayList<>(parametros);
            parametrosDatos.add(registrosPorPagina);
            parametrosDatos.add(offset);
            asignarParametros(stmt, parametrosDatos);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    compras.add(mapRow(rs));
                }
            }
        }

        return new PaginaResultado<>(compras, total, paginaSegura, registrosPorPagina);
    }

    private void asignarParametros(PreparedStatement stmt, List<Object> parametros) throws SQLException {
        for (int i = 0; i < parametros.size(); i++) {
            stmt.setObject(i + 1, parametros.get(i));
        }
    }

    private Compra mapRow(ResultSet rs) throws SQLException {
        Compra compra = new Compra();
        compra.setIdCompra(rs.getLong("id_compra"));
        compra.setProveedorId(rs.getLong("proveedor_id"));
        compra.setUsuarioId(rs.getLong("usuario_id"));
        compra.setFecha(rs.getObject("fecha", LocalDate.class));
        compra.setTotal(rs.getBigDecimal("total"));
        compra.setEstado(EstadoCompra.desdeValorBD(rs.getString("estado")));

        Timestamp createdAt = rs.getTimestamp("created_at");
        compra.setCreatedAt(createdAt == null ? null : createdAt.toLocalDateTime());

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        compra.setUpdatedAt(updatedAt == null ? null : updatedAt.toLocalDateTime());

        return compra;
    }
}
