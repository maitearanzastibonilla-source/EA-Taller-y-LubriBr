package com.eataller.dao.impl;

import com.eataller.config.DBConnectionManager;
import com.eataller.dao.ReporteDAO;
import com.eataller.dto.PaginaResultado;
import com.eataller.entity.Reporte;
import com.eataller.entity.TipoReporte;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ReporteDAOImpl implements ReporteDAO {

    private static final String SQL_INSERT =
            "INSERT INTO reportes (tipo_reporte, usuario_generador_id, formato, fecha_desde, fecha_hasta, pdf_url) "
                    + "VALUES (?, ?, 'PDF', ?, ?, '')";

    private static final String SQL_ACTUALIZAR_PDF =
            "UPDATE reportes SET pdf_url = ? WHERE id_reporte = ?";

    private static final String SQL_BUSCAR_POR_ID =
            "SELECT * FROM reportes WHERE id_reporte = ?";

    @Override
    public Reporte crear(Connection connection, Reporte reporte) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, reporte.getTipoReporte().name());
            stmt.setLong(2, reporte.getUsuarioGeneradorId());
            stmt.setObject(3, reporte.getFechaDesde());
            stmt.setObject(4, reporte.getFechaHasta());
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    reporte.setIdReporte(keys.getLong(1));
                }
            }
            return reporte;
        }
    }

    @Override
    public void actualizarPdfUrl(Connection connection, Long idReporte, String pdfUrl) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(SQL_ACTUALIZAR_PDF)) {
            stmt.setString(1, pdfUrl);
            stmt.setLong(2, idReporte);
            stmt.executeUpdate();
        }
    }

    @Override
    public Optional<Reporte> buscarPorId(Long idReporte) throws SQLException {
        try (Connection connection = DBConnectionManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(SQL_BUSCAR_POR_ID)) {

            stmt.setLong(1, idReporte);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        }
    }

    @Override
    public PaginaResultado<Reporte> listar(String tipoFiltro, Long usuarioFiltro,
                                            LocalDate fechaDesde, LocalDate fechaHasta,
                                            int pagina, int registrosPorPagina) throws SQLException {

        StringBuilder where = new StringBuilder(" WHERE 1 = 1 ");
        List<Object> parametros = new ArrayList<>();

        if (tipoFiltro != null && !tipoFiltro.isBlank()) {
            where.append(" AND tipo_reporte = ? ");
            parametros.add(tipoFiltro);
        }
        if (usuarioFiltro != null) {
            where.append(" AND usuario_generador_id = ? ");
            parametros.add(usuarioFiltro);
        }
        if (fechaDesde != null) {
            where.append(" AND DATE(fecha_generacion) >= ? ");
            parametros.add(fechaDesde);
        }
        if (fechaHasta != null) {
            where.append(" AND DATE(fecha_generacion) <= ? ");
            parametros.add(fechaHasta);
        }

        String sqlConteo = "SELECT COUNT(*) FROM reportes" + where;
        String sqlDatos = "SELECT * FROM reportes" + where + " ORDER BY fecha_generacion DESC LIMIT ? OFFSET ?";

        long total;
        try (Connection connection = DBConnectionManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sqlConteo)) {
            asignarParametros(stmt, parametros);
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                total = rs.getLong(1);
            }
        }

        int paginaSegura = Math.max(pagina, 1);
        int offset = (paginaSegura - 1) * registrosPorPagina;
        List<Reporte> reportes = new ArrayList<>();

        try (Connection connection = DBConnectionManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sqlDatos)) {
            List<Object> parametrosDatos = new ArrayList<>(parametros);
            parametrosDatos.add(registrosPorPagina);
            parametrosDatos.add(offset);
            asignarParametros(stmt, parametrosDatos);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    reportes.add(mapRow(rs));
                }
            }
        }

        return new PaginaResultado<>(reportes, total, paginaSegura, registrosPorPagina);
    }

    private void asignarParametros(PreparedStatement stmt, List<Object> parametros) throws SQLException {
        for (int i = 0; i < parametros.size(); i++) {
            stmt.setObject(i + 1, parametros.get(i));
        }
    }

    private Reporte mapRow(ResultSet rs) throws SQLException {
        Reporte reporte = new Reporte();
        reporte.setIdReporte(rs.getLong("id_reporte"));
        reporte.setTipoReporte(TipoReporte.desdeValorBD(rs.getString("tipo_reporte")));

        Timestamp fechaGeneracion = rs.getTimestamp("fecha_generacion");
        reporte.setFechaGeneracion(fechaGeneracion == null ? null : fechaGeneracion.toLocalDateTime());

        reporte.setUsuarioGeneradorId(rs.getLong("usuario_generador_id"));
        reporte.setFormato(rs.getString("formato"));
        reporte.setFechaDesde(rs.getObject("fecha_desde", LocalDate.class));
        reporte.setFechaHasta(rs.getObject("fecha_hasta", LocalDate.class));
        reporte.setPdfUrl(rs.getString("pdf_url"));
        return reporte;
    }
}
