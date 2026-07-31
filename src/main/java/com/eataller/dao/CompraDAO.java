package com.eataller.dao;

import com.eataller.dto.PaginaResultado;
import com.eataller.entity.Compra;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Optional;

public interface CompraDAO {

    Compra crear(Connection connection, Compra compra) throws SQLException;

    void actualizarProveedor(Connection connection, Long idCompra, Long proveedorId) throws SQLException;

    void actualizarTotal(Connection connection, Long idCompra, BigDecimal total) throws SQLException;

    void confirmar(Connection connection, Long idCompra) throws SQLException;

    void anular(Connection connection, Long idCompra) throws SQLException;

    Optional<Compra> buscarPorId(Long idCompra) throws SQLException;

    PaginaResultado<Compra> listar(String textoBusqueda, String estadoFiltro,
                                    LocalDate fechaDesde, LocalDate fechaHasta,
                                    int pagina, int registrosPorPagina) throws SQLException;
}
