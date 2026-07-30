package com.eataller.service;

import com.eataller.dto.PaginaResultado;
import com.eataller.dto.TrabajoDTO;
import com.eataller.dto.TrabajoFormDTO;
import com.eataller.entity.RolUsuario;
import com.eataller.exception.TrabajoNoEncontradoException;
import com.eataller.exception.UsuarioSinPermisosException;
import com.eataller.exception.ValidacionException;
import com.eataller.exception.VehiculoNoEncontradoException;

import java.sql.SQLException;
import java.time.LocalDate;

public interface TrabajoService {

    TrabajoDTO crear(TrabajoFormDTO form, Long idUsuarioCreador)
            throws ValidacionException, VehiculoNoEncontradoException, SQLException;

    TrabajoDTO actualizar(TrabajoFormDTO form, Long idUsuarioEditor)
            throws ValidacionException, TrabajoNoEncontradoException, SQLException;

    TrabajoDTO obtenerPorId(Long idTrabajo) throws TrabajoNoEncontradoException, SQLException;

    PaginaResultado<TrabajoDTO> listar(String textoBusqueda, String estadoFiltro, Long vehiculoId,
                                        int pagina, int registrosPorPagina) throws SQLException;

    void finalizar(Long idTrabajo, LocalDate fechaEgreso, Long idUsuarioEditor)
            throws TrabajoNoEncontradoException, ValidacionException, SQLException;

    void reabrir(Long idTrabajo, Long idUsuarioEditor, RolUsuario rolEditor)
            throws TrabajoNoEncontradoException, ValidacionException, UsuarioSinPermisosException, SQLException;

    void darDeBaja(Long idTrabajo, Long idUsuarioEditor, RolUsuario rolEditor)
            throws TrabajoNoEncontradoException, UsuarioSinPermisosException, SQLException;
}
