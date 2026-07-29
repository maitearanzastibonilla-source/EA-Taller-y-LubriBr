package com.eataller.dao;

import com.eataller.dto.PaginaResultado;
import com.eataller.entity.Usuario;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Responsabilidad: acceso a datos de la tabla usuarios. No contiene reglas
 * de negocio, unicamente operaciones CRUD y consultas sobre la tabla.
 *
 * Las operaciones que modifican datos reciben la Connection activa desde la
 * capa de Service, de forma que puedan participar de la misma transaccion
 * que el registro de auditoria asociado (atomicidad: si la auditoria falla,
 * el cambio de negocio tambien se revierte).
 * Autor: EA Taller y LubriBr - Equipo de Desarrollo
 */
public interface UsuarioDAO {

    Usuario crear(Connection connection, Usuario usuario) throws SQLException;

    void actualizar(Connection connection, Usuario usuario) throws SQLException;

    void cambiarEstado(Connection connection, Long idUsuario, boolean activo) throws SQLException;

    void registrarIntentoFallido(Connection connection, Long idUsuario, int intentosFallidos,
                                  LocalDateTime bloqueadoHasta) throws SQLException;

    void resetearIntentosFallidos(Connection connection, Long idUsuario) throws SQLException;

    Optional<Usuario> buscarPorId(Long idUsuario) throws SQLException;

    Optional<Usuario> buscarPorEmail(String email) throws SQLException;

    boolean existeEmail(String email, Long idUsuarioExcluido) throws SQLException;

    PaginaResultado<Usuario> listar(String textoBusqueda, String rolFiltro, Boolean activoFiltro,
                                     int pagina, int registrosPorPagina) throws SQLException;
}
