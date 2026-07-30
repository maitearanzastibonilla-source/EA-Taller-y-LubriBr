package com.eataller.service;

import com.eataller.dto.PaginaResultado;
import com.eataller.dto.ProveedorDTO;
import com.eataller.dto.ProveedorFormDTO;
import com.eataller.exception.ProveedorNoEncontradoException;
import com.eataller.exception.ValidacionException;

import java.sql.SQLException;

public interface ProveedorService {

    ProveedorDTO crear(ProveedorFormDTO form, Long idUsuarioCreador) throws ValidacionException, SQLException;

    ProveedorDTO actualizar(ProveedorFormDTO form, Long idUsuarioEditor)
            throws ValidacionException, ProveedorNoEncontradoException, SQLException;

    ProveedorDTO obtenerPorId(Long idProveedor) throws ProveedorNoEncontradoException, SQLException;

    PaginaResultado<ProveedorDTO> listar(String textoBusqueda, String estadoFiltro,
                                          int pagina, int registrosPorPagina) throws SQLException;

    void cambiarEstado(Long idProveedor, boolean activo, Long idUsuarioEditor)
            throws ProveedorNoEncontradoException, SQLException;
}
