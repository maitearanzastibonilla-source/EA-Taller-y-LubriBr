package com.eataller.service;

import com.eataller.dto.ClienteDTO;
import com.eataller.dto.ClienteFormDTO;
import com.eataller.dto.PaginaResultado;
import com.eataller.entity.RolUsuario;
import com.eataller.exception.ClienteNoEncontradoException;
import com.eataller.exception.DatoDuplicadoException;
import com.eataller.exception.UsuarioSinPermisosException;
import com.eataller.exception.ValidacionException;

import java.sql.SQLException;

public interface ClienteService {

    ClienteDTO crear(ClienteFormDTO form, Long idUsuarioCreador)
            throws ValidacionException, DatoDuplicadoException, SQLException;

    ClienteDTO actualizar(ClienteFormDTO form, Long idUsuarioEditor, RolUsuario rolEditor)
            throws ValidacionException, DatoDuplicadoException, ClienteNoEncontradoException,
            UsuarioSinPermisosException, SQLException;

    ClienteDTO obtenerPorId(Long idCliente) throws ClienteNoEncontradoException, SQLException;

    PaginaResultado<ClienteDTO> listar(String textoBusqueda, String estadoFiltro,
                                        int pagina, int registrosPorPagina) throws SQLException;

    void cambiarEstado(Long idCliente, boolean activo, Long idUsuarioEditor, RolUsuario rolEditor)
            throws ClienteNoEncontradoException, UsuarioSinPermisosException, SQLException;
}
