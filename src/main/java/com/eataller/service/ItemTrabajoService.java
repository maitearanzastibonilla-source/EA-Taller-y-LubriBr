package com.eataller.service;

import com.eataller.dto.ItemTrabajoDTO;
import com.eataller.dto.ItemTrabajoFormDTO;
import com.eataller.exception.ItemTrabajoNoEncontradoException;
import com.eataller.exception.TrabajoNoEncontradoException;
import com.eataller.exception.ValidacionException;

import java.sql.SQLException;
import java.util.List;

public interface ItemTrabajoService {

    ItemTrabajoDTO crear(ItemTrabajoFormDTO form, Long idUsuarioCreador)
            throws ValidacionException, TrabajoNoEncontradoException, SQLException;

    ItemTrabajoDTO actualizar(ItemTrabajoFormDTO form, Long idUsuarioEditor)
            throws ValidacionException, ItemTrabajoNoEncontradoException, TrabajoNoEncontradoException, SQLException;

    void eliminar(Long idItem, Long idUsuarioEditor)
            throws ValidacionException, ItemTrabajoNoEncontradoException, TrabajoNoEncontradoException, SQLException;

    List<ItemTrabajoDTO> listarPorTrabajo(Long trabajoId) throws SQLException;
}
