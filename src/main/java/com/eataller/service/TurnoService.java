package com.eataller.service;

import com.eataller.dto.PaginaResultado;
import com.eataller.dto.TurnoDTO;
import com.eataller.dto.TurnoFormDTO;
import com.eataller.exception.ClienteNoEncontradoException;
import com.eataller.exception.TurnoNoEncontradoException;
import com.eataller.exception.TurnoOcupadoException;
import com.eataller.exception.ValidacionException;
import com.eataller.exception.VehiculoNoEncontradoException;

import java.sql.SQLException;
import java.time.LocalDate;

public interface TurnoService {

    TurnoDTO crear(TurnoFormDTO form, Long idUsuarioCreador)
            throws ValidacionException, ClienteNoEncontradoException, VehiculoNoEncontradoException,
            TurnoOcupadoException, SQLException;

    TurnoDTO actualizar(TurnoFormDTO form, Long idUsuarioEditor)
            throws ValidacionException, TurnoNoEncontradoException, TurnoOcupadoException, SQLException;

    TurnoDTO obtenerPorId(Long idTurno) throws TurnoNoEncontradoException, SQLException;

    PaginaResultado<TurnoDTO> listar(String textoBusqueda, String estadoFiltro, String tipoServicioFiltro,
                                      LocalDate fechaDesde, LocalDate fechaHasta,
                                      int pagina, int registrosPorPagina) throws SQLException;

    void cancelar(Long idTurno, Long idUsuarioEditor) throws TurnoNoEncontradoException, ValidacionException, SQLException;
}
