package com.eataller.service;

import com.eataller.dto.PaginaResultado;
import com.eataller.dto.VentaDTO;
import com.eataller.dto.VentaFormDTO;
import com.eataller.entity.RolUsuario;
import com.eataller.exception.UsuarioSinPermisosException;
import com.eataller.exception.ValidacionException;
import com.eataller.exception.VentaNoEncontradaException;

import java.sql.SQLException;
import java.time.LocalDate;

public interface VentaService {

    VentaDTO crear(VentaFormDTO form, Long idUsuarioCreador) throws ValidacionException, SQLException;

    VentaDTO actualizar(VentaFormDTO form, Long idUsuarioEditor)
            throws ValidacionException, VentaNoEncontradaException, SQLException;

    VentaDTO obtenerPorId(Long idVenta) throws VentaNoEncontradaException, SQLException;

    PaginaResultado<VentaDTO> listar(String textoBusqueda, String estadoFiltro,
                                      LocalDate fechaDesde, LocalDate fechaHasta,
                                      int pagina, int registrosPorPagina) throws SQLException;

    void confirmar(Long idVenta, Long idUsuarioEditor) throws ValidacionException, VentaNoEncontradaException, SQLException;

    void anular(Long idVenta, Long idUsuarioEditor, RolUsuario rolEditor)
            throws ValidacionException, VentaNoEncontradaException, UsuarioSinPermisosException, SQLException;
}
