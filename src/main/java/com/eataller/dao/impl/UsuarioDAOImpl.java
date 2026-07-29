package com.eataller.dao.impl;

import com.eataller.config.DBConnectionManager;
import com.eataller.dao.UsuarioDAO;
import com.eataller.dto.PaginaResultado;
import com.eataller.entity.RolUsuario;
import com.eataller.entity.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Responsabilidad: implementar el acceso JDBC a la tabla usuarios utilizando
 * exclusivamente PreparedStatement, cerrando siempre los recursos mediante
 * try-with-resources. No contiene ninguna regla de negocio.
 * Autor: EA Taller y LubriBr - Equipo de Desarrollo
 */
public class UsuarioDAOImpl implements UsuarioDAO {

    private static final String SQL_INSERT =
            "INSERT INTO usuarios (nombre, email, password_hash, rol, activo, intentos_fallidos) "
                    + "VALUES (?, ?, ?, ?, ?, 0)";

    private static final String SQL_UPDATE =
            "UPDATE usuarios SET nombre = ?, email = ?, password_hash = ?, rol = ?, activo = ? "
                    + "WHERE id_usuario = ?";

    private static final String SQL_BUSCAR_POR_ID =
            "SELECT * FROM usuarios WHERE id_usuario = ?";

    private static final String SQL_BUSCAR_POR_EMAIL =
            "SELECT * FROM usuarios WHERE email = ?";

    private static final String SQL_EXISTE_EMAIL =
            "SELECT COUNT(*) FROM usuarios WHERE email = ? AND id_usuario <> ?";

    private static final String SQL_CAMBIAR_ESTADO =
            "UPDATE usuarios SET activo = ? WHERE id_usuario = ?";

    private static final String SQL_INTENTO_FALLIDO =
            "UPDATE usuarios SET intentos_fallidos = ?, bloqueado_hasta = ? WHERE id_usuario = ?";

    private static final String SQL_RESET_INTENTOS =
            "UPDATE usuarios SET intentos_fallidos = 0, bloqueado_hasta = NULL WHERE id_usuario = ?";

    @Override
    public Usuario crear(Connection connection, Usuario usuario) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, usuario.getNombre());
            stmt.setString(2, usuario.getEmail());
            stmt.setString(3, usuario.getPasswordHash());
            stmt.setString(4, usuario.getRol().name());
            stmt.setBoolean(5, usuario.isActivo());
            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    usuario.setIdUsuario(generatedKeys.getLong(1));
                }
            }
            return usuario;
        }
    }

    @Override
    public void actualizar(Connection connection, Usuario usuario) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(SQL_UPDATE)) {
            stmt.setString(1, usuario.getNombre());
            stmt.setString(2, usuario.getEmail());
            stmt.setString(3, usuario.getPasswordHash());
            stmt.setString(4, usuario.getRol().name());
            stmt.setBoolean(5, usuario.isActivo());
            stmt.setLong(6, usuario.getIdUsuario());
            stmt.executeUpdate();
        }
    }

    @Override
    public void cambiarEstado(Connection connection, Long idUsuario, boolean activo) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(SQL_CAMBIAR_ESTADO)) {
            stmt.setBoolean(1, activo);
            stmt.setLong(2, idUsuario);
            stmt.executeUpdate();
        }
    }

    @Override
    public void registrarIntentoFallido(Connection connection, Long idUsuario, int intentosFallidos,
                                         LocalDateTime bloqueadoHasta) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(SQL_INTENTO_FALLIDO)) {
            stmt.setInt(1, intentosFallidos);
            stmt.setTimestamp(2, bloqueadoHasta == null ? null : Timestamp.valueOf(bloqueadoHasta));
            stmt.setLong(3, idUsuario);
            stmt.executeUpdate();
        }
    }

    @Override
    public void resetearIntentosFallidos(Connection connection, Long idUsuario) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(SQL_RESET_INTENTOS)) {
            stmt.setLong(1, idUsuario);
            stmt.executeUpdate();
        }
    }

    @Override
    public Optional<Usuario> buscarPorId(Long idUsuario) throws SQLException {
        try (Connection connection = DBConnectionManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(SQL_BUSCAR_POR_ID)) {

            stmt.setLong(1, idUsuario);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        }
    }

    @Override
    public Optional<Usuario> buscarPorEmail(String email) throws SQLException {
        try (Connection connection = DBConnectionManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(SQL_BUSCAR_POR_EMAIL)) {

            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        }
    }

    @Override
    public boolean existeEmail(String email, Long idUsuarioExcluido) throws SQLException {
        try (Connection connection = DBConnectionManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(SQL_EXISTE_EMAIL)) {

            stmt.setString(1, email);
            stmt.setLong(2, idUsuarioExcluido == null ? 0L : idUsuarioExcluido);
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    @Override
    public PaginaResultado<Usuario> listar(String textoBusqueda, String rolFiltro, Boolean activoFiltro,
                                            int pagina, int registrosPorPagina) throws SQLException {

        StringBuilder where = new StringBuilder(" WHERE 1 = 1 ");
        List<Object> parametros = new ArrayList<>();

        if (textoBusqueda != null && !textoBusqueda.isBlank()) {
            where.append(" AND (nombre LIKE ? OR email LIKE ?) ");
            String comodin = "%" + textoBusqueda.trim() + "%";
            parametros.add(comodin);
            parametros.add(comodin);
        }
        if (rolFiltro != null && !rolFiltro.isBlank()) {
            where.append(" AND rol = ? ");
            parametros.add(rolFiltro);
        }
        if (activoFiltro != null) {
            where.append(" AND activo = ? ");
            parametros.add(activoFiltro);
        }

        String sqlConteo = "SELECT COUNT(*) FROM usuarios" + where;
        String sqlDatos = "SELECT * FROM usuarios" + where
                + " ORDER BY nombre ASC LIMIT ? OFFSET ?";

        long total;
        try (Connection connection = DBConnectionManager.getConnection();
             PreparedStatement stmtConteo = connection.prepareStatement(sqlConteo)) {
            asignarParametros(stmtConteo, parametros);
            try (ResultSet rs = stmtConteo.executeQuery()) {
                rs.next();
                total = rs.getLong(1);
            }
        }

        List<Usuario> usuarios = new ArrayList<>();
        int paginaSegura = Math.max(pagina, 1);
        int offset = (paginaSegura - 1) * registrosPorPagina;

        try (Connection connection = DBConnectionManager.getConnection();
             PreparedStatement stmtDatos = connection.prepareStatement(sqlDatos)) {
            List<Object> parametrosDatos = new ArrayList<>(parametros);
            parametrosDatos.add(registrosPorPagina);
            parametrosDatos.add(offset);
            asignarParametros(stmtDatos, parametrosDatos);

            try (ResultSet rs = stmtDatos.executeQuery()) {
                while (rs.next()) {
                    usuarios.add(mapRow(rs));
                }
            }
        }

        return new PaginaResultado<>(usuarios, total, paginaSegura, registrosPorPagina);
    }

    private void asignarParametros(PreparedStatement stmt, List<Object> parametros) throws SQLException {
        for (int i = 0; i < parametros.size(); i++) {
            Object valor = parametros.get(i);
            if (valor instanceof Boolean booleano) {
                stmt.setBoolean(i + 1, booleano);
            } else if (valor instanceof Integer entero) {
                stmt.setInt(i + 1, entero);
            } else if (valor == null) {
                stmt.setNull(i + 1, Types.VARCHAR);
            } else {
                stmt.setObject(i + 1, valor);
            }
        }
    }

    private Usuario mapRow(ResultSet rs) throws SQLException {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(rs.getLong("id_usuario"));
        usuario.setNombre(rs.getString("nombre"));
        usuario.setEmail(rs.getString("email"));
        usuario.setPasswordHash(rs.getString("password_hash"));
        usuario.setRol(RolUsuario.desdeValorBD(rs.getString("rol")));
        usuario.setActivo(rs.getBoolean("activo"));
        usuario.setIntentosFallidos(rs.getInt("intentos_fallidos"));

        Timestamp bloqueadoHasta = rs.getTimestamp("bloqueado_hasta");
        usuario.setBloqueadoHasta(bloqueadoHasta == null ? null : bloqueadoHasta.toLocalDateTime());

        Timestamp createdAt = rs.getTimestamp("created_at");
        usuario.setCreatedAt(createdAt == null ? null : createdAt.toLocalDateTime());

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        usuario.setUpdatedAt(updatedAt == null ? null : updatedAt.toLocalDateTime());

        return usuario;
    }
}
