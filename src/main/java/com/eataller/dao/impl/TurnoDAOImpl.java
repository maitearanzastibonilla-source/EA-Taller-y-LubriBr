package com.eataller.dao.impl;

import com.eataller.config.DBConnectionManager;
import com.eataller.dao.TurnoDAO;
import com.eataller.dto.PaginaResultado;
import com.eataller.entity.EstadoTurno;
import com.eataller.entity.TipoServicio;
import com.eataller.entity.Turno;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TurnoDAOImpl implements TurnoDAO {

    private static final String SQL_INSERT =
            "INSERT INTO turnos (cliente_id, vehiculo_id, usuario_id, fecha_hora, tipo_servicio, estado, notas) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?)";

    private static final String SQL_UPDATE =
            "UPDATE turnos SET fecha_hora = ?, tipo_servicio = ?, estado = ?, notas = ? WHERE id_turno = ?";

    private static final String SQL_CANCELAR =
            "UPDATE turnos SET estado = 'CANCELADO' WHERE id_turno = ?";

    private static final String SQL_BUSCAR_POR_ID =
            "SELECT * FROM turnos WHERE id_turno = ?";

    private static final String SQL_CONTAR_ACTIVOS_EN_HORARIO =
            "SELECT COUNT(*) FROM turnos WHERE fecha_hora = ? AND estado <> 'CANCELADO' AND id_turno <> ?";

    @Override
    public Turno crear(Connection connection, Turno turno) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, turno.getClienteId());
            stmt.setLong(2, turno.getVehiculoId());
            stmt.setLong(3, turno.getUsuarioId());
            stmt.setObject(4, turno.getFechaHora());
            stmt.setString(5, turno.getTipoServicio().name());
            stmt.setString(6, turno.getEstado().name());
            stmt.setString(7, turno.getNotas());
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    turno.setIdTurno(keys.getLong(1));
                }
            }
            return turno;
        }
    }

    @Override
    public void actualizar(Connection connection, Turno turno) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(SQL_UPDATE)) {
            stmt.setObject(1, turno.getFechaHora());
            stmt.setString(2, turno.getTipoServicio().name());
            stmt.setString(3, turno.getEstado().name());
            stmt.setString(4, turno.getNotas());
            stmt.setLong(5, turno.getIdTurno());
            stmt.executeUpdate();
        }
    }

    @Override
    public void cancelar(Connection connection, Long idTurno) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(SQL_CANCELAR)) {
            stmt.setLong(1, idTurno);
            stmt.executeUpdate();
        }
    }

    @Override
    public Optional<Turno> buscarPorId(Long idTurno) throws SQLException {
        try (Connection connection = DBConnectionManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(SQL_BUSCAR_POR_ID)) {

            stmt.setLong(1, idTurno);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        }
    }

    @Override
    public int contarActivosEnHorario(LocalDateTime fechaHora, Long idTurnoExcluido) throws SQLException {
        try (Connection connection = DBConnectionManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(SQL_CONTAR_ACTIVOS_EN_HORARIO)) {

            stmt.setObject(1, fechaHora);
            stmt.setLong(2, idTurnoExcluido == null ? 0L : idTurnoExcluido);
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    @Override
    public PaginaResultado<Turno> listar(String textoBusqueda, String estadoFiltro, String tipoServicioFiltro,
                                          LocalDate fechaDesde, LocalDate fechaHasta,
                                          int pagina, int registrosPorPagina) throws SQLException {

        StringBuilder where = new StringBuilder(" WHERE 1 = 1 ");
        List<Object> parametros = new ArrayList<>();

        if (textoBusqueda != null && !textoBusqueda.isBlank()) {
            where.append(" AND (c.nombre LIKE ? OR c.apellido LIKE ? OR v.patente LIKE ?) ");
            String comodin = "%" + textoBusqueda.trim() + "%";
            for (int i = 0; i < 3; i++) {
                parametros.add(comodin);
            }
        }
        if (estadoFiltro != null && !estadoFiltro.isBlank()) {
            where.append(" AND t.estado = ? ");
            parametros.add(estadoFiltro);
        }
        if (tipoServicioFiltro != null && !tipoServicioFiltro.isBlank()) {
            where.append(" AND t.tipo_servicio = ? ");
            parametros.add(tipoServicioFiltro);
        }
        if (fechaDesde != null) {
            where.append(" AND t.fecha_hora >= ? ");
            parametros.add(fechaDesde.atStartOfDay());
        }
        if (fechaHasta != null) {
            where.append(" AND t.fecha_hora < ? ");
            parametros.add(fechaHasta.plusDays(1).atStartOfDay());
        }

        String sqlBase = " FROM turnos t JOIN clientes c ON t.cliente_id = c.id_cliente "
                + "JOIN vehiculos v ON t.vehiculo_id = v.id_vehiculo" + where;
        String sqlConteo = "SELECT COUNT(*)" + sqlBase;
        String sqlDatos = "SELECT t.*" + sqlBase + " ORDER BY t.fecha_hora DESC LIMIT ? OFFSET ?";

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
        List<Turno> turnos = new ArrayList<>();

        try (Connection connection = DBConnectionManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sqlDatos)) {
            List<Object> parametrosDatos = new ArrayList<>(parametros);
            parametrosDatos.add(registrosPorPagina);
            parametrosDatos.add(offset);
            asignarParametros(stmt, parametrosDatos);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    turnos.add(mapRow(rs));
                }
            }
        }

        return new PaginaResultado<>(turnos, total, paginaSegura, registrosPorPagina);
    }

    private void asignarParametros(PreparedStatement stmt, List<Object> parametros) throws SQLException {
        for (int i = 0; i < parametros.size(); i++) {
            stmt.setObject(i + 1, parametros.get(i));
        }
    }

    private Turno mapRow(ResultSet rs) throws SQLException {
        Turno turno = new Turno();
        turno.setIdTurno(rs.getLong("id_turno"));
        turno.setClienteId(rs.getLong("cliente_id"));
        turno.setVehiculoId(rs.getLong("vehiculo_id"));
        turno.setUsuarioId(rs.getLong("usuario_id"));
        turno.setFechaHora(rs.getObject("fecha_hora", LocalDateTime.class));
        turno.setTipoServicio(TipoServicio.desdeValorBD(rs.getString("tipo_servicio")));
        turno.setEstado(EstadoTurno.desdeValorBD(rs.getString("estado")));
        turno.setNotas(rs.getString("notas"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        turno.setCreatedAt(createdAt == null ? null : createdAt.toLocalDateTime());

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        turno.setUpdatedAt(updatedAt == null ? null : updatedAt.toLocalDateTime());

        return turno;
    }
}
