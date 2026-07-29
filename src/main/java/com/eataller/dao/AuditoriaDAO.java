package com.eataller.dao;

import com.eataller.entity.Auditoria;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Responsabilidad: acceso a datos de la tabla auditoria (registro
 * transversal de acciones relevantes de todos los modulos del sistema).
 * Autor: EA Taller y LubriBr - Equipo de Desarrollo
 */
public interface AuditoriaDAO {

    /**
     * Registra un evento de auditoria utilizando una conexion existente,
     * de forma que quede incluido en la misma transaccion que la operacion
     * de negocio que lo origino (por ejemplo, alta de usuario + auditoria).
     */
    void registrar(Connection connection, Auditoria auditoria) throws SQLException;
}
