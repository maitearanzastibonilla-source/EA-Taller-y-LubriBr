package com.eataller.service;

import com.eataller.dto.PaginaResultado;
import com.eataller.dto.ReporteDTO;
import com.eataller.dto.ReporteDatos;
import com.eataller.dto.ReporteFormDTO;
import com.eataller.exception.ReporteNoEncontradoException;
import com.eataller.exception.ValidacionException;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;

public interface ReporteService {

    ReporteDTO generar(ReporteFormDTO form, Long idUsuarioGenerador)
            throws ValidacionException, SQLException, IOException;

    ReporteDTO obtenerPorId(Long idReporte) throws ReporteNoEncontradoException, SQLException;

    ReporteDatos obtenerDatosConsolidados(Long idReporte) throws ReporteNoEncontradoException, SQLException;

    PaginaResultado<ReporteDTO> listar(String tipoFiltro, Long usuarioFiltro,
                                        LocalDate fechaDesde, LocalDate fechaHasta,
                                        int pagina, int registrosPorPagina) throws SQLException;
}
