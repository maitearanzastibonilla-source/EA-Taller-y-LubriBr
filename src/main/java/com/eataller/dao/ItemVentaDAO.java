package com.eataller.dao;

import com.eataller.entity.ItemVenta;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface ItemVentaDAO {

    ItemVenta crear(Connection connection, ItemVenta item) throws SQLException;

    void actualizar(Connection connection, ItemVenta item) throws SQLException;

    void eliminar(Connection connection, Long idItemVenta) throws SQLException;

    Optional<ItemVenta> buscarPorId(Long idItemVenta) throws SQLException;

    List<ItemVenta> listarPorVenta(Long ventaId) throws SQLException;

    BigDecimal sumarTotalPorVenta(Connection connection, Long ventaId) throws SQLException;
}
