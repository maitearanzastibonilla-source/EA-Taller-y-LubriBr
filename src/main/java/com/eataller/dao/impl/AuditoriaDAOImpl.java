package com.eataller.dao.impl;

import com.eataller.dao.AuditoriaDAO;
import com.eataller.entity.Auditoria;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Responsabilidad: implementar el acceso JDBC a la tabla auditoria.
 * Autor: EA Taller y LubriBr - Equipo de Desarrollo
 */
public class AuditoriaDAOImpl implements AuditoriaDAO {

    private static final String SQL_INSERT =
            "INSERT INTO auditoria (usuario_id, modulo, accion, entidad_afectada, id_registro_afectado, "
                    + "valores_anteriores, valores_nuevos, resultado, observaciones) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

    @Override
    public void registrar(Connection connection, Auditoria auditoria) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(SQL_INSERT)) {
            if (auditoria.getUsuarioId() != null) {
                stmt.setLong(1, auditoria.getUsuarioId());
            } else {
                stmt.setNull(1, java.sql.Types.BIGINT);
            }
            stmt.setString(2, auditoria.getModulo());
            stmt.setString(3, auditoria.getAccion());
            stmt.setString(4, auditoria.getEntidadAfectada());
            if (auditoria.getIdRegistroAfectado() != null) {
                stmt.setLong(5, auditoria.getIdRegistroAfectado());
            } else {
                stmt.setNull(5, java.sql.Types.BIGINT);
            }
            stmt.setString(6, auditoria.getValoresAnteriores());
            stmt.setString(7, auditoria.getValoresNuevos());
            stmt.setString(8, auditoria.getResultado());
            stmt.setString(9, auditoria.getObservaciones());
            stmt.executeUpdate();
        }
    }
}
