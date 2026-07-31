package com.eataller.dao;

import com.eataller.dto.PaginaResultado;
import com.eataller.entity.Reporte;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Optional;

public interface ReporteDAO {

    Reporte crear(Connection connection, Reporte reporte) throws SQLException;

    void actualizarPdfUrl(Connection connection, Long idReporte, String pdfUrl) throws SQLException;

    Optional<Reporte> buscarPorId(Long idReporte) throws SQLException;

    PaginaResultado<Reporte> listar(String tipoFiltro, Long usuarioFiltro,
                                     LocalDate fechaDesde, LocalDate fechaHasta,
                                     int pagina, int registrosPorPagina) throws SQLException;
}
