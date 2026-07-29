package com.eataller.service;

import com.eataller.dto.PaginaResultado;
import com.eataller.dto.UsuarioDTO;
import com.eataller.dto.UsuarioFormDTO;
import com.eataller.exception.EmailDuplicadoException;
import com.eataller.exception.UsuarioNoEncontradoException;
import com.eataller.exception.UsuarioSinPermisosException;
import com.eataller.exception.ValidacionException;

import java.sql.SQLException;

/**
 * Responsabilidad: implementar toda la logica de negocio del Modulo de
 * Usuarios (alta, modificacion, baja logica y consulta), segun las reglas
 * definidas en la Propuesta Tecnica.
 * Autor: EA Taller y LubriBr - Equipo de Desarrollo
 */
public interface UsuarioService {

    UsuarioDTO crear(UsuarioFormDTO form, Long idUsuarioCreador)
            throws ValidacionException, EmailDuplicadoException, SQLException;

    UsuarioDTO actualizar(UsuarioFormDTO form, Long idUsuarioEditor)
            throws ValidacionException, EmailDuplicadoException, UsuarioNoEncontradoException,
            UsuarioSinPermisosException, SQLException;

    UsuarioDTO obtenerPorId(Long idUsuario) throws UsuarioNoEncontradoException, SQLException;

    PaginaResultado<UsuarioDTO> listar(String textoBusqueda, String rolFiltro, Boolean activoFiltro,
                                        int pagina, int registrosPorPagina) throws SQLException;

    void cambiarEstado(Long idUsuario, boolean activo, Long idUsuarioEditor)
            throws UsuarioNoEncontradoException, UsuarioSinPermisosException, SQLException;
}
