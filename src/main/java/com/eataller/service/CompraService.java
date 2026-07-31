package com.eataller.service;

import com.eataller.dto.CompraDTO;
import com.eataller.dto.CompraFormDTO;
import com.eataller.dto.PaginaResultado;
import com.eataller.entity.RolUsuario;
import com.eataller.exception.CompraNoEncontradaException;
import com.eataller.exception.ProveedorNoEncontradoException;
import com.eataller.exception.UsuarioSinPermisosException;
import com.eataller.exception.ValidacionException;

import java.sql.SQLException;
import java.time.LocalDate;

public interface CompraService {

    CompraDTO crear(CompraFormDTO form, Long idUsuarioCreador)
            throws ValidacionException, ProveedorNoEncontradoException, SQLException;

    CompraDTO actualizar(CompraFormDTO form, Long idUsuarioEditor)
            throws ValidacionException, CompraNoEncontradaException, ProveedorNoEncontradoException, SQLException;

    CompraDTO obtenerPorId(Long idCompra) throws CompraNoEncontradaException, SQLException;

    PaginaResultado<CompraDTO> listar(String textoBusqueda, String estadoFiltro,
                                       LocalDate fechaDesde, LocalDate fechaHasta,
                                       int pagina, int registrosPorPagina) throws SQLException;

    void confirmar(Long idCompra, Long idUsuarioEditor) throws ValidacionException, CompraNoEncontradaException, SQLException;

    void anular(Long idCompra, Long idUsuarioEditor, RolUsuario rolEditor)
            throws ValidacionException, CompraNoEncontradaException, UsuarioSinPermisosException, SQLException;
}
