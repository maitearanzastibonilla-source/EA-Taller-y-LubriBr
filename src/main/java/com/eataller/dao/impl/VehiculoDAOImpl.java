package com.eataller.dao.impl;

import com.eataller.config.DBConnectionManager;
import com.eataller.dao.VehiculoDAO;
import com.eataller.dto.PaginaResultado;
import com.eataller.entity.Estado;
import com.eataller.entity.Vehiculo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class VehiculoDAOImpl implements VehiculoDAO {

    private static final String SQL_INSERT =
            "INSERT INTO vehiculos (cliente_id, patente, marca, modelo, anio, estado) VALUES (?, ?, ?, ?, ?, ?)";

    private static final String SQL_UPDATE =
            "UPDATE vehiculos SET cliente_id = ?, patente = ?, marca = ?, modelo = ?, anio = ? WHERE id_vehiculo = ?";

    private static final String SQL_CAMBIAR_ESTADO =
            "UPDATE vehiculos SET estado = ? WHERE id_vehiculo = ?";

    private static final String SQL_BUSCAR_POR_ID =
            "SELECT * FROM vehiculos WHERE id_vehiculo = ?";

    private static final String SQL_EXISTE_PATENTE =
            "SELECT COUNT(*) FROM vehiculos WHERE patente = ? AND id_vehiculo <> ?";

    @Override
    public Vehiculo crear(Connection connection, Vehiculo vehiculo) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, vehiculo.getClienteId());
            stmt.setString(2, vehiculo.getPatente());
            stmt.setString(3, vehiculo.getMarca());
            stmt.setString(4, vehiculo.getModelo());
            stmt.setInt(5, vehiculo.getAnio());
            stmt.setString(6, vehiculo.getEstado().name());
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    vehiculo.setIdVehiculo(keys.getLong(1));
                }
            }
            return vehiculo;
        }
    }

    @Override
    public void actualizar(Connection connection, Vehiculo vehiculo) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(SQL_UPDATE)) {
            stmt.setLong(1, vehiculo.getClienteId());
            stmt.setString(2, vehiculo.getPatente());
            stmt.setString(3, vehiculo.getMarca());
            stmt.setString(4, vehiculo.getModelo());
            stmt.setInt(5, vehiculo.getAnio());
            stmt.setLong(6, vehiculo.getIdVehiculo());
            stmt.executeUpdate();
        }
    }

    @Override
    public void cambiarEstado(Connection connection, Long idVehiculo, boolean activo) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(SQL_CAMBIAR_ESTADO)) {
            stmt.setString(1, activo ? "ACTIVO" : "INACTIVO");
            stmt.setLong(2, idVehiculo);
            stmt.executeUpdate();
        }
    }

    @Override
    public Optional<Vehiculo> buscarPorId(Long idVehiculo) throws SQLException {
        try (Connection connection = DBConnectionManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(SQL_BUSCAR_POR_ID)) {

            stmt.setLong(1, idVehiculo);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        }
    }

    @Override
    public boolean existePatente(String patente, Long idVehiculoExcluido) throws SQLException {
        try (Connection connection = DBConnectionManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(SQL_EXISTE_PATENTE)) {

            stmt.setString(1, patente);
            stmt.setLong(2, idVehiculoExcluido == null ? 0L : idVehiculoExcluido);
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    @Override
    public PaginaResultado<Vehiculo> listar(String textoBusqueda, String estadoFiltro, Long clienteId,
                                             int pagina, int registrosPorPagina) throws SQLException {

        StringBuilder where = new StringBuilder(" WHERE 1 = 1 ");
        List<Object> parametros = new ArrayList<>();

        if (textoBusqueda != null && !textoBusqueda.isBlank()) {
            where.append(" AND (v.patente LIKE ? OR v.marca LIKE ? OR v.modelo LIKE ? "
                    + "OR c.nombre LIKE ? OR c.apellido LIKE ?) ");
            String comodin = "%" + textoBusqueda.trim() + "%";
            for (int i = 0; i < 5; i++) {
                parametros.add(comodin);
            }
        }
        if (estadoFiltro != null && !estadoFiltro.isBlank()) {
            where.append(" AND v.estado = ? ");
            parametros.add(estadoFiltro);
        }
        if (clienteId != null) {
            where.append(" AND v.cliente_id = ? ");
            parametros.add(clienteId);
        }

        String sqlBase = " FROM vehiculos v JOIN clientes c ON v.cliente_id = c.id_cliente" + where;
        String sqlConteo = "SELECT COUNT(*)" + sqlBase;
        String sqlDatos = "SELECT v.*" + sqlBase + " ORDER BY v.created_at DESC LIMIT ? OFFSET ?";

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
        List<Vehiculo> vehiculos = new ArrayList<>();

        try (Connection connection = DBConnectionManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sqlDatos)) {
            List<Object> parametrosDatos = new ArrayList<>(parametros);
            parametrosDatos.add(registrosPorPagina);
            parametrosDatos.add(offset);
            asignarParametros(stmt, parametrosDatos);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    vehiculos.add(mapRow(rs));
                }
            }
        }

        return new PaginaResultado<>(vehiculos, total, paginaSegura, registrosPorPagina);
    }

    private void asignarParametros(PreparedStatement stmt, List<Object> parametros) throws SQLException {
        for (int i = 0; i < parametros.size(); i++) {
            stmt.setObject(i + 1, parametros.get(i));
        }
    }

    private Vehiculo mapRow(ResultSet rs) throws SQLException {
        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setIdVehiculo(rs.getLong("id_vehiculo"));
        vehiculo.setClienteId(rs.getLong("cliente_id"));
        vehiculo.setPatente(rs.getString("patente"));
        vehiculo.setMarca(rs.getString("marca"));
        vehiculo.setModelo(rs.getString("modelo"));
        vehiculo.setAnio(rs.getInt("anio"));
        vehiculo.setEstado(Estado.desdeValorBD(rs.getString("estado")));

        Timestamp createdAt = rs.getTimestamp("created_at");
        vehiculo.setCreatedAt(createdAt == null ? null : createdAt.toLocalDateTime());

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        vehiculo.setUpdatedAt(updatedAt == null ? null : updatedAt.toLocalDateTime());

        return vehiculo;
    }
}
