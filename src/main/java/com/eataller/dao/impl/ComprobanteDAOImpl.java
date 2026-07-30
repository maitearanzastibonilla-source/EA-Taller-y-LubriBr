package com.eataller.dao.impl;

import com.eataller.config.DBConnectionManager;
import com.eataller.dao.ComprobanteDAO;
import com.eataller.dto.PaginaResultado;
import com.eataller.entity.Comprobante;
import com.eataller.entity.EstadoComprobante;
import com.eataller.entity.MetodoPago;

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

public class ComprobanteDAOImpl implements ComprobanteDAO {

    private static final String SQL_INSERT =
            "INSERT INTO comprobantes (trabajo_id, fecha, total, metodo_pago, estado, pdf_url) "
                    + "VALUES (?, ?, ?, ?, ?, '')";

    private static final String SQL_ACTUALIZAR_PDF =
            "UPDATE comprobantes SET pdf_url = ? WHERE id_comprobante = ?";

    private static final String SQL_ANULAR =
            "UPDATE comprobantes SET estado = 'ANULADO' WHERE id_comprobante = ?";

    private static final String SQL_BUSCAR_POR_ID =
            "SELECT * FROM comprobantes WHERE id_comprobante = ?";

    private static final String SQL_BUSCAR_ACTIVO_POR_TRABAJO =
            "SELECT * FROM comprobantes WHERE trabajo_id = ? AND estado <> 'ANULADO' LIMIT 1";

    @Override
    public Comprobante crear(Connection connection, Comprobante comprobante) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, comprobante.getTrabajoId());
            stmt.setObject(2, comprobante.getFecha());
            stmt.setBigDecimal(3, comprobante.getTotal());
            stmt.setString(4, comprobante.getMetodoPago().name());
            stmt.setString(5, comprobante.getEstado().name());
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    comprobante.setIdComprobante(keys.getLong(1));
                }
            }
            return comprobante;
        }
    }

    @Override
    public void actualizarPdfUrl(Connection connection, Long idComprobante, String pdfUrl) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(SQL_ACTUALIZAR_PDF)) {
            stmt.setString(1, pdfUrl);
            stmt.setLong(2, idComprobante);
            stmt.executeUpdate();
        }
    }

    @Override
    public void anular(Connection connection, Long idComprobante) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(SQL_ANULAR)) {
            stmt.setLong(1, idComprobante);
            stmt.executeUpdate();
        }
    }

    @Override
    public Optional<Comprobante> buscarPorId(Long idComprobante) throws SQLException {
        try (Connection connection = DBConnectionManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(SQL_BUSCAR_POR_ID)) {

            stmt.setLong(1, idComprobante);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        }
    }

    @Override
    public Optional<Comprobante> buscarActivoPorTrabajo(Long trabajoId) throws SQLException {
        try (Connection connection = DBConnectionManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(SQL_BUSCAR_ACTIVO_POR_TRABAJO)) {

            stmt.setLong(1, trabajoId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        }
    }

    @Override
    public PaginaResultado<Comprobante> listar(String textoBusqueda, String estadoFiltro,
                                                LocalDate fechaDesde, LocalDate fechaHasta,
                                                int pagina, int registrosPorPagina) throws SQLException {

        StringBuilder where = new StringBuilder(" WHERE 1 = 1 ");
        List<Object> parametros = new ArrayList<>();

        if (textoBusqueda != null && !textoBusqueda.isBlank()) {
            where.append(" AND (co.id_comprobante = ? OR v.patente LIKE ? OR c.nombre LIKE ? OR c.apellido LIKE ?) ");
            String textoTrim = textoBusqueda.trim();
            String comodin = "%" + textoTrim + "%";
            long numeroBuscado;
            try {
                numeroBuscado = Long.parseLong(textoTrim);
            } catch (NumberFormatException e) {
                numeroBuscado = -1;
            }
            parametros.add(numeroBuscado);
            parametros.add(comodin);
            parametros.add(comodin);
            parametros.add(comodin);
        }
        if (estadoFiltro != null && !estadoFiltro.isBlank()) {
            where.append(" AND co.estado = ? ");
            parametros.add(estadoFiltro);
        }
        if (fechaDesde != null) {
            where.append(" AND co.fecha >= ? ");
            parametros.add(fechaDesde);
        }
        if (fechaHasta != null) {
            where.append(" AND co.fecha <= ? ");
            parametros.add(fechaHasta);
        }

        String sqlBase = " FROM comprobantes co JOIN trabajos_realizados t ON co.trabajo_id = t.id_trabajo "
                + "JOIN vehiculos v ON t.vehiculo_id = v.id_vehiculo JOIN clientes c ON v.cliente_id = c.id_cliente"
                + where;
        String sqlConteo = "SELECT COUNT(*)" + sqlBase;
        String sqlDatos = "SELECT co.*" + sqlBase + " ORDER BY co.id_comprobante DESC LIMIT ? OFFSET ?";

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
        List<Comprobante> comprobantes = new ArrayList<>();

        try (Connection connection = DBConnectionManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sqlDatos)) {
            List<Object> parametrosDatos = new ArrayList<>(parametros);
            parametrosDatos.add(registrosPorPagina);
            parametrosDatos.add(offset);
            asignarParametros(stmt, parametrosDatos);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    comprobantes.add(mapRow(rs));
                }
            }
        }

        return new PaginaResultado<>(comprobantes, total, paginaSegura, registrosPorPagina);
    }

    private void asignarParametros(PreparedStatement stmt, List<Object> parametros) throws SQLException {
        for (int i = 0; i < parametros.size(); i++) {
            stmt.setObject(i + 1, parametros.get(i));
        }
    }

    private Comprobante mapRow(ResultSet rs) throws SQLException {
        Comprobante comprobante = new Comprobante();
        comprobante.setIdComprobante(rs.getLong("id_comprobante"));
        comprobante.setTrabajoId(rs.getLong("trabajo_id"));
        comprobante.setFecha(rs.getObject("fecha", LocalDate.class));
        comprobante.setTotal(rs.getBigDecimal("total"));
        comprobante.setMetodoPago(MetodoPago.desdeValorBD(rs.getString("metodo_pago")));
        comprobante.setEstado(EstadoComprobante.desdeValorBD(rs.getString("estado")));
        comprobante.setPdfUrl(rs.getString("pdf_url"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        comprobante.setCreatedAt(createdAt == null ? null : createdAt.toLocalDateTime());

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        comprobante.setUpdatedAt(updatedAt == null ? null : updatedAt.toLocalDateTime());

        return comprobante;
    }
}
