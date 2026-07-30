package com.eataller.dao;

import com.eataller.dto.PaginaResultado;
import com.eataller.entity.Comprobante;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Optional;

public interface ComprobanteDAO {

    Comprobante crear(Connection connection, Comprobante comprobante) throws SQLException;

    void actualizarPdfUrl(Connection connection, Long idComprobante, String pdfUrl) throws SQLException;

    void anular(Connection connection, Long idComprobante) throws SQLException;

    Optional<Comprobante> buscarPorId(Long idComprobante) throws SQLException;

    Optional<Comprobante> buscarActivoPorTrabajo(Long trabajoId) throws SQLException;

    PaginaResultado<Comprobante> listar(String textoBusqueda, String estadoFiltro,
                                         LocalDate fechaDesde, LocalDate fechaHasta,
                                         int pagina, int registrosPorPagina) throws SQLException;
}
