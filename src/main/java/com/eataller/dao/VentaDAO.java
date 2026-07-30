package com.eataller.dao;

import com.eataller.dto.PaginaResultado;
import com.eataller.entity.VentaDirecta;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Optional;

public interface VentaDAO {

    VentaDirecta crear(Connection connection, VentaDirecta venta) throws SQLException;

    void actualizarMetodoPago(Connection connection, Long idVenta, String metodoPago) throws SQLException;

    void actualizarTotal(Connection connection, Long idVenta, BigDecimal total) throws SQLException;

    void confirmar(Connection connection, Long idVenta) throws SQLException;

    void anular(Connection connection, Long idVenta) throws SQLException;

    Optional<VentaDirecta> buscarPorId(Long idVenta) throws SQLException;

    PaginaResultado<VentaDirecta> listar(String textoBusqueda, String estadoFiltro,
                                          LocalDate fechaDesde, LocalDate fechaHasta,
                                          int pagina, int registrosPorPagina) throws SQLException;
}
