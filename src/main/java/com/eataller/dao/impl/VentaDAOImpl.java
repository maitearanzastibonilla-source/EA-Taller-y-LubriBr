package com.eataller.dao.impl;

import com.eataller.config.DBConnectionManager;
import com.eataller.dao.VentaDAO;
import com.eataller.dto.PaginaResultado;
import com.eataller.entity.EstadoVenta;
import com.eataller.entity.MetodoPago;
import com.eataller.entity.VentaDirecta;

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

public class VentaDAOImpl implements VentaDAO {

    private static final String SQL_INSERT =
            "INSERT INTO ventas_directas (usuario_id, fecha, total, metodo_pago, estado) VALUES (?, ?, ?, ?, 'PENDIENTE')";

    private static final String SQL_ACTUALIZAR_METODO_PAGO =
            "UPDATE ventas_directas SET metodo_pago = ? WHERE id_venta = ?";

    private static final String SQL_ACTUALIZAR_TOTAL =
            "UPDATE ventas_directas SET total = ? WHERE id_venta = ?";

    private static final String SQL_CONFIRMAR =
            "UPDATE ventas_directas SET estado = 'CONFIRMADA' WHERE id_venta = ?";

    private static final String SQL_ANULAR =
            "UPDATE ventas_directas SET estado = 'ANULADA' WHERE id_venta = ?";

    private static final String SQL_BUSCAR_POR_ID =
            "SELECT * FROM ventas_directas WHERE id_venta = ?";

    @Override
    public VentaDirecta crear(Connection connection, VentaDirecta venta) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, venta.getUsuarioId());
            stmt.setObject(2, venta.getFecha());
            stmt.setBigDecimal(3, venta.getTotal());
            stmt.setString(4, venta.getMetodoPago().name());
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    venta.setIdVenta(keys.getLong(1));
                }
            }
            return venta;
        }
    }

    @Override
    public void actualizarMetodoPago(Connection connection, Long idVenta, String metodoPago) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(SQL_ACTUALIZAR_METODO_PAGO)) {
            stmt.setString(1, metodoPago);
            stmt.setLong(2, idVenta);
            stmt.executeUpdate();
        }
    }

    @Override
    public void actualizarTotal(Connection connection, Long idVenta, BigDecimal total) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(SQL_ACTUALIZAR_TOTAL)) {
            stmt.setBigDecimal(1, total);
            stmt.setLong(2, idVenta);
            stmt.executeUpdate();
        }
    }

    @Override
    public void confirmar(Connection connection, Long idVenta) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(SQL_CONFIRMAR)) {
            stmt.setLong(1, idVenta);
            stmt.executeUpdate();
        }
    }

    @Override
    public void anular(Connection connection, Long idVenta) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(SQL_ANULAR)) {
            stmt.setLong(1, idVenta);
            stmt.executeUpdate();
        }
    }

    @Override
    public Optional<VentaDirecta> buscarPorId(Long idVenta) throws SQLException {
        try (Connection connection = DBConnectionManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(SQL_BUSCAR_POR_ID)) {

            stmt.setLong(1, idVenta);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        }
    }

    @Override
    public PaginaResultado<VentaDirecta> listar(String textoBusqueda, String estadoFiltro,
                                                 LocalDate fechaDesde, LocalDate fechaHasta,
                                                 int pagina, int registrosPorPagina) throws SQLException {

        StringBuilder where = new StringBuilder(" WHERE 1 = 1 ");
        List<Object> parametros = new ArrayList<>();

        if (textoBusqueda != null && !textoBusqueda.isBlank()) {
            where.append(" AND (v.id_venta = ? OR u.nombre LIKE ?) ");
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
            where.append(" AND v.estado = ? ");
            parametros.add(estadoFiltro);
        }
        if (fechaDesde != null) {
            where.append(" AND v.fecha >= ? ");
            parametros.add(fechaDesde);
        }
        if (fechaHasta != null) {
            where.append(" AND v.fecha <= ? ");
            parametros.add(fechaHasta);
        }

        String sqlBase = " FROM ventas_directas v JOIN usuarios u ON v.usuario_id = u.id_usuario" + where;
        String sqlConteo = "SELECT COUNT(*)" + sqlBase;
        String sqlDatos = "SELECT v.*" + sqlBase + " ORDER BY v.id_venta DESC LIMIT ? OFFSET ?";

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
        List<VentaDirecta> ventas = new ArrayList<>();

        try (Connection connection = DBConnectionManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sqlDatos)) {
            List<Object> parametrosDatos = new ArrayList<>(parametros);
            parametrosDatos.add(registrosPorPagina);
            parametrosDatos.add(offset);
            asignarParametros(stmt, parametrosDatos);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ventas.add(mapRow(rs));
                }
            }
        }

        return new PaginaResultado<>(ventas, total, paginaSegura, registrosPorPagina);
    }

    private void asignarParametros(PreparedStatement stmt, List<Object> parametros) throws SQLException {
        for (int i = 0; i < parametros.size(); i++) {
            stmt.setObject(i + 1, parametros.get(i));
        }
    }

    private VentaDirecta mapRow(ResultSet rs) throws SQLException {
        VentaDirecta venta = new VentaDirecta();
        venta.setIdVenta(rs.getLong("id_venta"));
        venta.setUsuarioId(rs.getLong("usuario_id"));
        venta.setFecha(rs.getObject("fecha", LocalDate.class));
        venta.setTotal(rs.getBigDecimal("total"));
        venta.setMetodoPago(MetodoPago.desdeValorBD(rs.getString("metodo_pago")));
        venta.setEstado(EstadoVenta.desdeValorBD(rs.getString("estado")));

        Timestamp createdAt = rs.getTimestamp("created_at");
        venta.setCreatedAt(createdAt == null ? null : createdAt.toLocalDateTime());

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        venta.setUpdatedAt(updatedAt == null ? null : updatedAt.toLocalDateTime());

        return venta;
    }
}
