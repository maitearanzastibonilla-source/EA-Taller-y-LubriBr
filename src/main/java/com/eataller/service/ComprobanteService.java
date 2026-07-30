package com.eataller.service;

import com.eataller.dto.ComprobanteDTO;
import com.eataller.dto.ComprobanteFormDTO;
import com.eataller.dto.PaginaResultado;
import com.eataller.entity.RolUsuario;
import com.eataller.exception.ComprobanteNoEncontradoException;
import com.eataller.exception.TrabajoNoEncontradoException;
import com.eataller.exception.UsuarioSinPermisosException;
import com.eataller.exception.ValidacionException;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Optional;

public interface ComprobanteService {

    ComprobanteDTO generar(ComprobanteFormDTO form, Long idUsuarioGenerador)
            throws ValidacionException, TrabajoNoEncontradoException, SQLException, IOException;

    ComprobanteDTO obtenerPorId(Long idComprobante) throws ComprobanteNoEncontradoException, SQLException;

    Optional<ComprobanteDTO> obtenerActivoPorTrabajo(Long idTrabajo) throws SQLException;

    PaginaResultado<ComprobanteDTO> listar(String textoBusqueda, String estadoFiltro,
                                            LocalDate fechaDesde, LocalDate fechaHasta,
                                            int pagina, int registrosPorPagina) throws SQLException;

    void anular(Long idComprobante, Long idUsuarioEditor, RolUsuario rolEditor)
            throws ComprobanteNoEncontradoException, ValidacionException, UsuarioSinPermisosException, SQLException;
}
