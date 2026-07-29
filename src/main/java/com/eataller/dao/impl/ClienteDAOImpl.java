package com.eataller.dao.impl;

import com.eataller.config.DBConnectionManager;
import com.eataller.dao.ClienteDAO;
import com.eataller.dto.PaginaResultado;
import com.eataller.entity.Cliente;
import com.eataller.entity.EstadoCliente;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ClienteDAOImpl implements ClienteDAO {

    private static final String SQL_INSERT =
            "INSERT INTO clientes (nombre, apellido, dni, email, telefono, direccion, estado) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?)";

    private static final String SQL_UPDATE =
            "UPDATE clientes SET nombre = ?, apellido = ?, dni = ?, email = ?, telefono = ?, direccion = ? "
                    + "WHERE id_cliente = ?";

    private static final String SQL_CAMBIAR_ESTADO =
            "UPDATE clientes SET estado = ? WHERE id_cliente = ?";

    private static final String SQL_BUSCAR_POR_ID =
            "SELECT * FROM clientes WHERE id_cliente = ?";

    private static final String SQL_EXISTE_DNI =
            "SELECT COUNT(*) FROM clientes WHERE dni = ? AND id_cliente <> ?";

    private static final String SQL_EXISTE_TELEFONO =
            "SELECT COUNT(*) FROM clientes WHERE telefono = ? AND id_cliente <> ?";

    @Override
    public Cliente crear(Connection connection, Cliente cliente) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, cliente.getNombre());
            stmt.setString(2, cliente.getApellido());
            stmt.setString(3, cliente.getDni());
            stmt.setString(4, cliente.getEmail());
            stmt.setString(5, cliente.getTelefono());
            stmt.setString(6, cliente.getDireccion());
            stmt.setString(7, cliente.getEstado().name());
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    cliente.setIdCliente(keys.getLong(1));
                }
            }
            return cliente;
        }
    }

    @Override
    public void actualizar(Connection connection, Cliente cliente) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(SQL_UPDATE)) {
            stmt.setString(1, cliente.getNombre());
            stmt.setString(2, cliente.getApellido());
            stmt.setString(3, cliente.getDni());
            stmt.setString(4, cliente.getEmail());
            stmt.setString(5, cliente.getTelefono());
            stmt.setString(6, cliente.getDireccion());
            stmt.setLong(7, cliente.getIdCliente());
            stmt.executeUpdate();
        }
    }

    @Override
    public void cambiarEstado(Connection connection, Long idCliente, boolean activo) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(SQL_CAMBIAR_ESTADO)) {
            stmt.setString(1, activo ? "ACTIVO" : "INACTIVO");
            stmt.setLong(2, idCliente);
            stmt.executeUpdate();
        }
    }

    @Override
    public Optional<Cliente> buscarPorId(Long idCliente) throws SQLException {
        try (Connection connection = DBConnectionManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(SQL_BUSCAR_POR_ID)) {

            stmt.setLong(1, idCliente);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        }
    }

    @Override
    public boolean existeDni(String dni, Long idClienteExcluido) throws SQLException {
        try (Connection connection = DBConnectionManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(SQL_EXISTE_DNI)) {

            stmt.setString(1, dni);
            stmt.setLong(2, idClienteExcluido == null ? 0L : idClienteExcluido);
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    @Override
    public boolean existeTelefono(String telefono, Long idClienteExcluido) throws SQLException {
        try (Connection connection = DBConnectionManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(SQL_EXISTE_TELEFONO)) {

            stmt.setString(1, telefono);
            stmt.setLong(2, idClienteExcluido == null ? 0L : idClienteExcluido);
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    @Override
    public PaginaResultado<Cliente> listar(String textoBusqueda, String estadoFiltro,
                                            int pagina, int registrosPorPagina) throws SQLException {

        StringBuilder where = new StringBuilder(" WHERE 1 = 1 ");
        List<Object> parametros = new ArrayList<>();

        if (textoBusqueda != null && !textoBusqueda.isBlank()) {
            where.append(" AND (nombre LIKE ? OR apellido LIKE ? OR dni LIKE ? OR telefono LIKE ? OR email LIKE ?) ");
            String comodin = "%" + textoBusqueda.trim() + "%";
            for (int i = 0; i < 5; i++) {
                parametros.add(comodin);
            }
        }
        if (estadoFiltro != null && !estadoFiltro.isBlank()) {
            where.append(" AND estado = ? ");
            parametros.add(estadoFiltro);
        }

        String sqlConteo = "SELECT COUNT(*) FROM clientes" + where;
        String sqlDatos = "SELECT * FROM clientes" + where + " ORDER BY apellido ASC, nombre ASC LIMIT ? OFFSET ?";

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
        List<Cliente> clientes = new ArrayList<>();

        try (Connection connection = DBConnectionManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sqlDatos)) {
            List<Object> parametrosDatos = new ArrayList<>(parametros);
            parametrosDatos.add(registrosPorPagina);
            parametrosDatos.add(offset);
            asignarParametros(stmt, parametrosDatos);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    clientes.add(mapRow(rs));
                }
            }
        }

        return new PaginaResultado<>(clientes, total, paginaSegura, registrosPorPagina);
    }

    private void asignarParametros(PreparedStatement stmt, List<Object> parametros) throws SQLException {
        for (int i = 0; i < parametros.size(); i++) {
            stmt.setObject(i + 1, parametros.get(i));
        }
    }

    private Cliente mapRow(ResultSet rs) throws SQLException {
        Cliente cliente = new Cliente();
        cliente.setIdCliente(rs.getLong("id_cliente"));
        cliente.setNombre(rs.getString("nombre"));
        cliente.setApellido(rs.getString("apellido"));
        cliente.setDni(rs.getString("dni"));
        cliente.setEmail(rs.getString("email"));
        cliente.setTelefono(rs.getString("telefono"));
        cliente.setDireccion(rs.getString("direccion"));
        cliente.setEstado(EstadoCliente.desdeValorBD(rs.getString("estado")));

        Timestamp createdAt = rs.getTimestamp("created_at");
        cliente.setCreatedAt(createdAt == null ? null : createdAt.toLocalDateTime());

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        cliente.setUpdatedAt(updatedAt == null ? null : updatedAt.toLocalDateTime());

        return cliente;
    }
}
