package com.eataller.dao;

import com.eataller.dto.PaginaResultado;
import com.eataller.entity.Turno;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

public interface TurnoDAO {

    Turno crear(Connection connection, Turno turno) throws SQLException;

    void actualizar(Connection connection, Turno turno) throws SQLException;

    void cancelar(Connection connection, Long idTurno) throws SQLException;

    Optional<Turno> buscarPorId(Long idTurno) throws SQLException;

    int contarActivosEnHorario(LocalDateTime fechaHora, Long idTurnoExcluido) throws SQLException;

    PaginaResultado<Turno> listar(String textoBusqueda, String estadoFiltro, String tipoServicioFiltro,
                                   LocalDate fechaDesde, LocalDate fechaHasta,
                                   int pagina, int registrosPorPagina) throws SQLException;
}
