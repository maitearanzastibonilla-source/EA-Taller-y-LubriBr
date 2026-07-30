package com.eataller.dao.impl;

import com.eataller.config.DBConnectionManager;
import com.eataller.dao.TrabajoDAO;
import com.eataller.dto.PaginaResultado;
import com.eataller.entity.EstadoTrabajo;
import com.eataller.entity.TrabajoRealizado;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TrabajoDAOImpl implements TrabajoDAO {

    private static final String SQL_INSERT =
            "INSERT INTO trabajos_realizados (vehiculo_id, turno_id, usuario_id, fecha_ingreso, descripcion, estado) "
                    + "VALUES (?, ?, ?, ?, ?, 'EN_PROCESO')";

    private static final String SQL_UPDATE =
            "UPDATE trabajos_realizados SET fecha_ingreso = ?, descripcion = ? WHERE id_trabajo = ?";

    private static final String SQL_FINALIZAR =
            "UPDATE trabajos_realizados SET estado = 'FINALIZADO', fecha_egreso = ? WHERE id_trabajo = ?";

    private static final String SQL_REABRIR =
            "UPDATE trabajos_realizados SET estado = 'EN_PROCESO', fecha_egreso = NULL WHERE id_trabajo = ?";

    private static final String SQL_MARCAR_FACTURADO =
            "UPDATE trabajos_realizados SET estado = 'FACTURADO' WHERE id_trabajo = ?";

    private static final String SQL_DESMARCAR_FACTURADO =
            "UPDATE trabajos_realizados SET estado = 'FINALIZADO' WHERE id_trabajo = ?";

    private static final String SQL_CAMBIAR_ACTIVO =
            "UPDATE trabajos_realizados SET activo = ? WHERE id_trabajo = ?";

    private static final String SQL_BUSCAR_POR_ID =
            "SELECT * FROM trabajos_realizados WHERE id_trabajo = ?";

    @Override
    public TrabajoRealizado crear(Connection connection, TrabajoRealizado trabajo) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, trabajo.getVehiculoId());
            if (trabajo.getTurnoId() != null) {
                stmt.setLong(2, trabajo.getTurnoId());
            } else {
                stmt.setNull(2, java.sql.Types.BIGINT);
            }
            stmt.setLong(3, trabajo.getUsuarioId());
            stmt.setObject(4, trabajo.getFechaIngreso());
            stmt.setString(5, trabajo.getDescripcion());
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    trabajo.setIdTrabajo(keys.getLong(1));
                }
            }
            return trabajo;
        }
    }

    @Override
    public void actualizar(Connection connection, TrabajoRealizado trabajo) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(SQL_UPDATE)) {
            stmt.setObject(1, trabajo.getFechaIngreso());
            stmt.setString(2, trabajo.getDescripcion());
            stmt.setLong(3, trabajo.getIdTrabajo());
            stmt.executeUpdate();
        }
    }

    @Override
    public void finalizar(Connection connection, Long idTrabajo, java.time.LocalDate fechaEgreso) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(SQL_FINALIZAR)) {
            stmt.setObject(1, fechaEgreso);
            stmt.setLong(2, idTrabajo);
            stmt.executeUpdate();
        }
    }

    @Override
    public void reabrir(Connection connection, Long idTrabajo) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(SQL_REABRIR)) {
            stmt.setLong(1, idTrabajo);
            stmt.executeUpdate();
        }
    }

    @Override
    public void marcarFacturado(Connection connection, Long idTrabajo) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(SQL_MARCAR_FACTURADO)) {
            stmt.setLong(1, idTrabajo);
            stmt.executeUpdate();
        }
    }

    @Override
    public void desmarcarFacturado(Connection connection, Long idTrabajo) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(SQL_DESMARCAR_FACTURADO)) {
            stmt.setLong(1, idTrabajo);
            stmt.executeUpdate();
        }
    }

    @Override
    public void cambiarActivo(Connection connection, Long idTrabajo, boolean activo) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(SQL_CAMBIAR_ACTIVO)) {
            stmt.setBoolean(1, activo);
            stmt.setLong(2, idTrabajo);
            stmt.executeUpdate();
        }
    }

    @Override
    public Optional<TrabajoRealizado> buscarPorId(Long idTrabajo) throws SQLException {
        try (Connection connection = DBConnectionManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(SQL_BUSCAR_POR_ID)) {

            stmt.setLong(1, idTrabajo);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        }
    }

    @Override
    public PaginaResultado<TrabajoRealizado> listar(String textoBusqueda, String estadoFiltro, Long vehiculoId,
                                                     int pagina, int registrosPorPagina) throws SQLException {

        StringBuilder where = new StringBuilder(" WHERE t.activo = TRUE ");
        List<Object> parametros = new ArrayList<>();

        if (textoBusqueda != null && !textoBusqueda.isBlank()) {
            where.append(" AND (v.patente LIKE ? OR c.nombre LIKE ? OR c.apellido LIKE ? OR t.descripcion LIKE ?) ");
            String comodin = "%" + textoBusqueda.trim() + "%";
            for (int i = 0; i < 4; i++) {
                parametros.add(comodin);
            }
        }
        if (estadoFiltro != null && !estadoFiltro.isBlank()) {
            where.append(" AND t.estado = ? ");
            parametros.add(estadoFiltro);
        }
        if (vehiculoId != null) {
            where.append(" AND t.vehiculo_id = ? ");
            parametros.add(vehiculoId);
        }

        String sqlBase = " FROM trabajos_realizados t JOIN vehiculos v ON t.vehiculo_id = v.id_vehiculo "
                + "JOIN clientes c ON v.cliente_id = c.id_cliente" + where;
        String sqlConteo = "SELECT COUNT(*)" + sqlBase;
        String sqlDatos = "SELECT t.*" + sqlBase + " ORDER BY t.fecha_ingreso DESC, t.id_trabajo DESC LIMIT ? OFFSET ?";

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
        List<TrabajoRealizado> trabajos = new ArrayList<>();

        try (Connection connection = DBConnectionManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sqlDatos)) {
            List<Object> parametrosDatos = new ArrayList<>(parametros);
            parametrosDatos.add(registrosPorPagina);
            parametrosDatos.add(offset);
            asignarParametros(stmt, parametrosDatos);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    trabajos.add(mapRow(rs));
                }
            }
        }

        return new PaginaResultado<>(trabajos, total, paginaSegura, registrosPorPagina);
    }

    private void asignarParametros(PreparedStatement stmt, List<Object> parametros) throws SQLException {
        for (int i = 0; i < parametros.size(); i++) {
            stmt.setObject(i + 1, parametros.get(i));
        }
    }

    private TrabajoRealizado mapRow(ResultSet rs) throws SQLException {
        TrabajoRealizado trabajo = new TrabajoRealizado();
        trabajo.setIdTrabajo(rs.getLong("id_trabajo"));
        trabajo.setVehiculoId(rs.getLong("vehiculo_id"));

        long turnoId = rs.getLong("turno_id");
        trabajo.setTurnoId(rs.wasNull() ? null : turnoId);

        trabajo.setUsuarioId(rs.getLong("usuario_id"));
        trabajo.setFechaIngreso(rs.getObject("fecha_ingreso", java.time.LocalDate.class));
        trabajo.setFechaEgreso(rs.getObject("fecha_egreso", java.time.LocalDate.class));
        trabajo.setDescripcion(rs.getString("descripcion"));
        trabajo.setEstado(EstadoTrabajo.desdeValorBD(rs.getString("estado")));
        trabajo.setActivo(rs.getBoolean("activo"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        trabajo.setCreatedAt(createdAt == null ? null : createdAt.toLocalDateTime());

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        trabajo.setUpdatedAt(updatedAt == null ? null : updatedAt.toLocalDateTime());

        return trabajo;
    }
}
