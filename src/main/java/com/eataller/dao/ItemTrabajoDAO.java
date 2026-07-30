package com.eataller.dao;

import com.eataller.entity.ItemTrabajo;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface ItemTrabajoDAO {

    ItemTrabajo crear(Connection connection, ItemTrabajo item) throws SQLException;

    void actualizar(Connection connection, ItemTrabajo item) throws SQLException;

    void eliminar(Connection connection, Long idItem) throws SQLException;

    Optional<ItemTrabajo> buscarPorId(Long idItem) throws SQLException;

    List<ItemTrabajo> listarPorTrabajo(Long trabajoId) throws SQLException;
}
