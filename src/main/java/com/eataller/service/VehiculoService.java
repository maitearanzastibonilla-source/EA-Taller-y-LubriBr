package com.eataller.service;

import com.eataller.dto.PaginaResultado;
import com.eataller.dto.VehiculoDTO;
import com.eataller.dto.VehiculoFormDTO;
import com.eataller.entity.RolUsuario;
import com.eataller.exception.ClienteNoEncontradoException;
import com.eataller.exception.DatoDuplicadoException;
import com.eataller.exception.UsuarioSinPermisosException;
import com.eataller.exception.ValidacionException;
import com.eataller.exception.VehiculoNoEncontradoException;

import java.sql.SQLException;

public interface VehiculoService {

    VehiculoDTO crear(VehiculoFormDTO form, Long idUsuarioCreador)
            throws ValidacionException, DatoDuplicadoException, ClienteNoEncontradoException, SQLException;

    VehiculoDTO actualizar(VehiculoFormDTO form, Long idUsuarioEditor, RolUsuario rolEditor)
            throws ValidacionException, DatoDuplicadoException, ClienteNoEncontradoException,
            VehiculoNoEncontradoException, UsuarioSinPermisosException, SQLException;

    VehiculoDTO obtenerPorId(Long idVehiculo) throws VehiculoNoEncontradoException, SQLException;

    PaginaResultado<VehiculoDTO> listar(String textoBusqueda, String estadoFiltro, Long clienteId,
                                         int pagina, int registrosPorPagina) throws SQLException;

    void cambiarEstado(Long idVehiculo, boolean activo, Long idUsuarioEditor)
            throws VehiculoNoEncontradoException, SQLException;
}
