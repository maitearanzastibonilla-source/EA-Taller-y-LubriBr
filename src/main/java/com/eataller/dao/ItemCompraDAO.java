package com.eataller.dao;

import com.eataller.entity.ItemCompra;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface ItemCompraDAO {

    ItemCompra crear(Connection connection, ItemCompra item) throws SQLException;

    void actualizar(Connection connection, ItemCompra item) throws SQLException;

    void eliminar(Connection connection, Long idItemCompra) throws SQLException;

    Optional<ItemCompra> buscarPorId(Long idItemCompra) throws SQLException;

    List<ItemCompra> listarPorCompra(Long compraId) throws SQLException;

    BigDecimal sumarTotalPorCompra(Connection connection, Long compraId) throws SQLException;
}
