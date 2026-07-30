package com.eataller.dao.impl;

import com.eataller.config.DBConnectionManager;
import com.eataller.dao.ProveedorDAO;
import com.eataller.dto.PaginaResultado;
import com.eataller.entity.Estado;
import com.eataller.entity.Proveedor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProveedorDAOImpl implements ProveedorDAO {

    private static final String SQL_INSERT =
            "INSERT INTO proveedores (nombre, contacto, telefono, email, estado) VALUES (?, ?, ?, ?, ?)";

    private static final String SQL_UPDATE =
            "UPDATE proveedores SET nombre = ?, contacto = ?, telefono = ?, email = ? WHERE id_proveedor = ?";

    private static final String SQL_CAMBIAR_ESTADO =
            "UPDATE proveedores SET estado = ? WHERE id_proveedor = ?";

    private static final String SQL_BUSCAR_POR_ID =
            "SELECT * FROM proveedores WHERE id_proveedor = ?";

    @Override
    public Proveedor crear(Connection connection, Proveedor proveedor) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, proveedor.getNombre());
            stmt.setString(2, proveedor.getContacto());
            stmt.setString(3, proveedor.getTelefono());
            stmt.setString(4, proveedor.getEmail());
            stmt.setString(5, proveedor.getEstado().name());
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    proveedor.setIdProveedor(keys.getLong(1));
                }
            }
            return proveedor;
        }
    }

    @Override
    public void actualizar(Connection connection, Proveedor proveedor) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(SQL_UPDATE)) {
            stmt.setString(1, proveedor.getNombre());
            stmt.setString(2, proveedor.getContacto());
            stmt.setString(3, proveedor.getTelefono());
            stmt.setString(4, proveedor.getEmail());
            stmt.setLong(5, proveedor.getIdProveedor());
            stmt.executeUpdate();
        }
    }

    @Override
    public void cambiarEstado(Connection connection, Long idProveedor, boolean activo) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(SQL_CAMBIAR_ESTADO)) {
            stmt.setString(1, activo ? "ACTIVO" : "INACTIVO");
            stmt.setLong(2, idProveedor);
            stmt.executeUpdate();
        }
    }

    @Override
    public Optional<Proveedor> buscarPorId(Long idProveedor) throws SQLException {
        try (Connection connection = DBConnectionManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(SQL_BUSCAR_POR_ID)) {

            stmt.setLong(1, idProveedor);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        }
    }

    @Override
    public PaginaResultado<Proveedor> listar(String textoBusqueda, String estadoFiltro,
                                              int pagina, int registrosPorPagina) throws SQLException {

        StringBuilder where = new StringBuilder(" WHERE 1 = 1 ");
        List<Object> parametros = new ArrayList<>();

        if (textoBusqueda != null && !textoBusqueda.isBlank()) {
            where.append(" AND (nombre LIKE ? OR contacto LIKE ? OR telefono LIKE ? OR email LIKE ?) ");
            String comodin = "%" + textoBusqueda.trim() + "%";
            for (int i = 0; i < 4; i++) {
                parametros.add(comodin);
            }
        }
        if (estadoFiltro != null && !estadoFiltro.isBlank()) {
            where.append(" AND estado = ? ");
            parametros.add(estadoFiltro);
        }

        String sqlConteo = "SELECT COUNT(*) FROM proveedores" + where;
        String sqlDatos = "SELECT * FROM proveedores" + where + " ORDER BY nombre ASC LIMIT ? OFFSET ?";

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
        List<Proveedor> proveedores = new ArrayList<>();

        try (Connection connection = DBConnectionManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sqlDatos)) {
            List<Object> parametrosDatos = new ArrayList<>(parametros);
            parametrosDatos.add(registrosPorPagina);
            parametrosDatos.add(offset);
            asignarParametros(stmt, parametrosDatos);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    proveedores.add(mapRow(rs));
                }
            }
        }

        return new PaginaResultado<>(proveedores, total, paginaSegura, registrosPorPagina);
    }

    private void asignarParametros(PreparedStatement stmt, List<Object> parametros) throws SQLException {
        for (int i = 0; i < parametros.size(); i++) {
            stmt.setObject(i + 1, parametros.get(i));
        }
    }

    private Proveedor mapRow(ResultSet rs) throws SQLException {
        Proveedor proveedor = new Proveedor();
        proveedor.setIdProveedor(rs.getLong("id_proveedor"));
        proveedor.setNombre(rs.getString("nombre"));
        proveedor.setContacto(rs.getString("contacto"));
        proveedor.setTelefono(rs.getString("telefono"));
        proveedor.setEmail(rs.getString("email"));
        proveedor.setEstado(Estado.desdeValorBD(rs.getString("estado")));

        Timestamp createdAt = rs.getTimestamp("created_at");
        proveedor.setCreatedAt(createdAt == null ? null : createdAt.toLocalDateTime());

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        proveedor.setUpdatedAt(updatedAt == null ? null : updatedAt.toLocalDateTime());

        return proveedor;
    }
}
