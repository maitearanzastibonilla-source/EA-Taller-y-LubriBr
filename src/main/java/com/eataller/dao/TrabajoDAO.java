package com.eataller.dao;

import com.eataller.dto.PaginaResultado;
import com.eataller.entity.TrabajoRealizado;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

public interface TrabajoDAO {

    TrabajoRealizado crear(Connection connection, TrabajoRealizado trabajo) throws SQLException;

    void actualizar(Connection connection, TrabajoRealizado trabajo) throws SQLException;

    void finalizar(Connection connection, Long idTrabajo, java.time.LocalDate fechaEgreso) throws SQLException;

    void reabrir(Connection connection, Long idTrabajo) throws SQLException;

    void cambiarActivo(Connection connection, Long idTrabajo, boolean activo) throws SQLException;

    Optional<TrabajoRealizado> buscarPorId(Long idTrabajo) throws SQLException;

    PaginaResultado<TrabajoRealizado> listar(String textoBusqueda, String estadoFiltro, Long vehiculoId,
                                              int pagina, int registrosPorPagina) throws SQLException;
}
