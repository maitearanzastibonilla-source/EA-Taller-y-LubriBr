package com.eataller.service.impl;

import com.eataller.config.DBConnectionManager;
import com.eataller.constants.AppConstants;
import com.eataller.dao.AuditoriaDAO;
import com.eataller.dao.ReporteDAO;
import com.eataller.dao.UsuarioDAO;
import com.eataller.dao.impl.AuditoriaDAOImpl;
import com.eataller.dao.impl.ReporteDAOImpl;
import com.eataller.dao.impl.UsuarioDAOImpl;
import com.eataller.dto.PaginaResultado;
import com.eataller.dto.ReporteDTO;
import com.eataller.dto.ReporteDatos;
import com.eataller.dto.ReporteFormDTO;
import com.eataller.entity.Auditoria;
import com.eataller.entity.Reporte;
import com.eataller.entity.TipoReporte;
import com.eataller.entity.Usuario;
import com.eataller.exception.ReporteNoEncontradoException;
import com.eataller.exception.ValidacionException;
import com.eataller.service.ReporteService;
import com.eataller.utils.ReporteMapper;
import com.eataller.utils.ReportePdfGenerator;
import com.eataller.validator.ReporteValidator;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ReporteServiceImpl implements ReporteService {

    private static final String NOMBRE_DIRECTORIO_PDF = "reportes-pdf";
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final ReporteDAO reporteDAO;
    private final UsuarioDAO usuarioDAO;
    private final AuditoriaDAO auditoriaDAO;

    public ReporteServiceImpl() {
        this.reporteDAO = new ReporteDAOImpl();
        this.usuarioDAO = new UsuarioDAOImpl();
        this.auditoriaDAO = new AuditoriaDAOImpl();
    }

    @Override
    public ReporteDTO generar(ReporteFormDTO form, Long idUsuarioGenerador)
            throws ValidacionException, SQLException, IOException {

        ReporteValidator.validar(form);

        TipoReporte tipo = TipoReporte.valueOf(form.getTipoReporte().trim().toUpperCase());
        LocalDate fechaDesde = LocalDate.parse(form.getFechaDesde().trim());
        LocalDate fechaHasta = LocalDate.parse(form.getFechaHasta().trim());

        ReporteDatos datos = construirDatos(tipo, fechaDesde, fechaHasta);

        Reporte reporte = new Reporte();
        reporte.setTipoReporte(tipo);
        reporte.setUsuarioGeneradorId(idUsuarioGenerador);
        reporte.setFormato("PDF");
        reporte.setFechaDesde(fechaDesde);
        reporte.setFechaHasta(fechaHasta);

        try (Connection connection = DBConnectionManager.getConnection()) {
            connection.setAutoCommit(false);
            try {
                reporteDAO.crear(connection, reporte);

                Path destino = resolverDirectorioPdf().resolve("reporte-" + reporte.getIdReporte() + ".pdf");
                String usuarioNombre = usuarioDAO.buscarPorId(idUsuarioGenerador)
                        .map(Usuario::getNombre).orElse("(usuario desconocido)");
                ReportePdfGenerator.generar(destino, tipo, fechaDesde, fechaHasta,
                        LocalDateTime.now(), usuarioNombre, datos);
                reporte.setPdfUrl(destino.toString());
                reporteDAO.actualizarPdfUrl(connection, reporte.getIdReporte(), destino.toString());

                Auditoria auditoria = new Auditoria(idUsuarioGenerador, AppConstants.MODULO_REPORTES,
                        "ALTA", "reportes", reporte.getIdReporte(), null,
                        "tipo_reporte=" + tipo + ", periodo=" + fechaDesde.format(FORMATO_FECHA)
                                + " a " + fechaHasta.format(FORMATO_FECHA),
                        "EXITO", "Generacion de reporte.");
                auditoriaDAO.registrar(connection, auditoria);

                connection.commit();
            } catch (SQLException | IOException e) {
                connection.rollback();
                throw e;
            }
        }

        return ReporteMapper.aDTO(reporte, usuarioDAO.buscarPorId(idUsuarioGenerador).map(Usuario::getNombre).orElse("(sin datos)"));
    }

    @Override
    public ReporteDTO obtenerPorId(Long idReporte) throws ReporteNoEncontradoException, SQLException {
        Reporte reporte = reporteDAO.buscarPorId(idReporte)
                .orElseThrow(() -> new ReporteNoEncontradoException("No se encontro el reporte solicitado."));
        String usuarioNombre = usuarioDAO.buscarPorId(reporte.getUsuarioGeneradorId())
                .map(Usuario::getNombre).orElse("(sin datos)");
        return ReporteMapper.aDTO(reporte, usuarioNombre);
    }

    @Override
    public ReporteDatos obtenerDatosConsolidados(Long idReporte) throws ReporteNoEncontradoException, SQLException {
        Reporte reporte = reporteDAO.buscarPorId(idReporte)
                .orElseThrow(() -> new ReporteNoEncontradoException("No se encontro el reporte solicitado."));
        return construirDatos(reporte.getTipoReporte(), reporte.getFechaDesde(), reporte.getFechaHasta());
    }

    @Override
    public PaginaResultado<ReporteDTO> listar(String tipoFiltro, Long usuarioFiltro,
                                               LocalDate fechaDesde, LocalDate fechaHasta,
                                               int pagina, int registrosPorPagina) throws SQLException {

        PaginaResultado<Reporte> resultado = reporteDAO.listar(tipoFiltro, usuarioFiltro,
                fechaDesde, fechaHasta, pagina, registrosPorPagina);

        List<ReporteDTO> dtos = resultado.getRegistros().stream()
                .map(reporte -> {
                    String usuarioNombre;
                    try {
                        usuarioNombre = usuarioDAO.buscarPorId(reporte.getUsuarioGeneradorId())
                                .map(Usuario::getNombre).orElse("(sin datos)");
                    } catch (SQLException e) {
                        usuarioNombre = "(sin datos)";
                    }
                    return ReporteMapper.aDTO(reporte, usuarioNombre);
                })
                .collect(Collectors.toList());

        return new PaginaResultado<>(dtos, resultado.getTotalRegistros(), resultado.getPaginaActual(),
                resultado.getRegistrosPorPagina());
    }

    private Path resolverDirectorioPdf() throws IOException {
        String base = System.getProperty("catalina.base");
        Path directorio = base != null
                ? Paths.get(base, NOMBRE_DIRECTORIO_PDF)
                : Paths.get(System.getProperty("java.io.tmpdir"), NOMBRE_DIRECTORIO_PDF);
        Files.createDirectories(directorio);
        return directorio;
    }

    private ReporteDatos construirDatos(TipoReporte tipo, LocalDate fechaDesde, LocalDate fechaHasta) throws SQLException {
        return switch (tipo) {
            case TRABAJOS_POR_PERIODO -> datosTrabajosPorPeriodo(fechaDesde, fechaHasta);
            case INGRESOS -> datosIngresos(fechaDesde, fechaHasta);
            case MOVIMIENTOS_STOCK -> datosMovimientosStock(fechaDesde, fechaHasta);
            case COMPRAS_POR_PROVEEDOR -> datosComprasPorProveedor(fechaDesde, fechaHasta);
            case TURNOS_POR_ESTADO -> datosTurnosPorEstado(fechaDesde, fechaHasta);
            case RANKING_CLIENTES -> datosRankingClientes(fechaDesde, fechaHasta);
        };
    }

    private ReporteDatos datosTrabajosPorPeriodo(LocalDate fechaDesde, LocalDate fechaHasta) throws SQLException {
        String sql = "SELECT v.patente, CONCAT(c.nombre, ' ', c.apellido) AS cliente, "
                + "t.fecha_ingreso, t.fecha_egreso, t.estado "
                + "FROM trabajos_realizados t "
                + "JOIN vehiculos v ON t.vehiculo_id = v.id_vehiculo "
                + "JOIN clientes c ON v.cliente_id = c.id_cliente "
                + "WHERE t.fecha_ingreso BETWEEN ? AND ? "
                + "ORDER BY t.fecha_ingreso ASC";

        List<List<String>> filas = new ArrayList<>();
        int enProceso = 0;
        int finalizado = 0;
        int facturado = 0;

        try (Connection connection = DBConnectionManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setObject(1, fechaDesde);
            stmt.setObject(2, fechaHasta);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String estado = rs.getString("estado");
                    LocalDate fechaEgreso = rs.getObject("fecha_egreso", LocalDate.class);
                    filas.add(List.of(
                            rs.getString("patente"),
                            rs.getString("cliente"),
                            rs.getObject("fecha_ingreso", LocalDate.class).format(FORMATO_FECHA),
                            fechaEgreso == null ? "-" : fechaEgreso.format(FORMATO_FECHA),
                            estado
                    ));
                    switch (estado) {
                        case "EN_PROCESO" -> enProceso++;
                        case "FINALIZADO" -> finalizado++;
                        case "FACTURADO" -> facturado++;
                        default -> { }
                    }
                }
            }
        }

        List<String> resumen = List.of(
                "Total de trabajos: " + filas.size(),
                "En proceso: " + enProceso + " - Finalizados: " + finalizado + " - Facturados: " + facturado
        );

        return new ReporteDatos(List.of("Patente", "Cliente", "Ingreso", "Egreso", "Estado"), filas, resumen);
    }

    private ReporteDatos datosIngresos(LocalDate fechaDesde, LocalDate fechaHasta) throws SQLException {
        List<List<String>> filas = new ArrayList<>();
        BigDecimal totalVentas = BigDecimal.ZERO;
        BigDecimal totalComprobantes = BigDecimal.ZERO;

        String sqlVentas = "SELECT id_venta, fecha, total FROM ventas_directas "
                + "WHERE estado = 'CONFIRMADA' AND fecha BETWEEN ? AND ? ORDER BY fecha ASC";
        try (Connection connection = DBConnectionManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sqlVentas)) {
            stmt.setObject(1, fechaDesde);
            stmt.setObject(2, fechaHasta);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    BigDecimal total = rs.getBigDecimal("total");
                    totalVentas = totalVentas.add(total);
                    filas.add(List.of("Venta directa #" + rs.getLong("id_venta"),
                            rs.getObject("fecha", LocalDate.class).format(FORMATO_FECHA),
                            "$ " + total.toPlainString()));
                }
            }
        }

        String sqlComprobantes = "SELECT id_comprobante, fecha, total FROM comprobantes "
                + "WHERE estado = 'COBRADO' AND fecha BETWEEN ? AND ? ORDER BY fecha ASC";
        try (Connection connection = DBConnectionManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sqlComprobantes)) {
            stmt.setObject(1, fechaDesde);
            stmt.setObject(2, fechaHasta);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    BigDecimal total = rs.getBigDecimal("total");
                    totalComprobantes = totalComprobantes.add(total);
                    filas.add(List.of("Comprobante #" + rs.getLong("id_comprobante"),
                            rs.getObject("fecha", LocalDate.class).format(FORMATO_FECHA),
                            "$ " + total.toPlainString()));
                }
            }
        }

        BigDecimal totalGeneral = totalVentas.add(totalComprobantes);
        List<String> resumen = List.of(
                "Total ventas directas: $ " + totalVentas.toPlainString(),
                "Total comprobantes cobrados: $ " + totalComprobantes.toPlainString(),
                "Ingreso total del periodo: $ " + totalGeneral.toPlainString()
        );

        return new ReporteDatos(List.of("Origen", "Fecha", "Total"), filas, resumen);
    }

    private ReporteDatos datosMovimientosStock(LocalDate fechaDesde, LocalDate fechaHasta) throws SQLException {
        java.util.Map<String, int[]> movimientos = new java.util.LinkedHashMap<>();

        String sqlEntradas = "SELECT p.nombre, SUM(ic.cantidad) AS cantidad "
                + "FROM items_compra ic JOIN compras c ON ic.compra_id = c.id_compra "
                + "JOIN productos p ON ic.producto_id = p.id_producto "
                + "WHERE c.estado = 'CONFIRMADA' AND c.fecha BETWEEN ? AND ? "
                + "GROUP BY p.id_producto, p.nombre";
        try (Connection connection = DBConnectionManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sqlEntradas)) {
            stmt.setObject(1, fechaDesde);
            stmt.setObject(2, fechaHasta);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    movimientos.computeIfAbsent(rs.getString("nombre"), k -> new int[2])[0] += rs.getInt("cantidad");
                }
            }
        }

        String sqlSalidasVenta = "SELECT p.nombre, SUM(iv.cantidad) AS cantidad "
                + "FROM items_venta iv JOIN ventas_directas vd ON iv.venta_id = vd.id_venta "
                + "JOIN productos p ON iv.producto_id = p.id_producto "
                + "WHERE vd.estado = 'CONFIRMADA' AND vd.fecha BETWEEN ? AND ? "
                + "GROUP BY p.id_producto, p.nombre";
        try (Connection connection = DBConnectionManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sqlSalidasVenta)) {
            stmt.setObject(1, fechaDesde);
            stmt.setObject(2, fechaHasta);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    movimientos.computeIfAbsent(rs.getString("nombre"), k -> new int[2])[1] += rs.getInt("cantidad");
                }
            }
        }

        String sqlSalidasTrabajo = "SELECT p.nombre, SUM(it.cantidad) AS cantidad "
                + "FROM items_trabajo it JOIN trabajos_realizados t ON it.trabajo_id = t.id_trabajo "
                + "JOIN productos p ON it.producto_id = p.id_producto "
                + "WHERE it.producto_id IS NOT NULL AND t.fecha_ingreso BETWEEN ? AND ? "
                + "GROUP BY p.id_producto, p.nombre";
        try (Connection connection = DBConnectionManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sqlSalidasTrabajo)) {
            stmt.setObject(1, fechaDesde);
            stmt.setObject(2, fechaHasta);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    movimientos.computeIfAbsent(rs.getString("nombre"), k -> new int[2])[1] += rs.getInt("cantidad");
                }
            }
        }

        List<List<String>> filas = new ArrayList<>();
        int totalEntradas = 0;
        int totalSalidas = 0;
        for (var entrada : movimientos.entrySet()) {
            int[] valores = entrada.getValue();
            filas.add(List.of(entrada.getKey(), String.valueOf(valores[0]), String.valueOf(valores[1])));
            totalEntradas += valores[0];
            totalSalidas += valores[1];
        }

        List<String> resumen = List.of(
                "Total unidades ingresadas (compras confirmadas): " + totalEntradas,
                "Total unidades egresadas (ventas confirmadas + uso en trabajos): " + totalSalidas
        );

        return new ReporteDatos(List.of("Producto", "Entradas", "Salidas"), filas, resumen);
    }

    private ReporteDatos datosComprasPorProveedor(LocalDate fechaDesde, LocalDate fechaHasta) throws SQLException {
        String sql = "SELECT pr.nombre, COUNT(*) AS cantidad, COALESCE(SUM(c.total), 0) AS total "
                + "FROM compras c JOIN proveedores pr ON c.proveedor_id = pr.id_proveedor "
                + "WHERE c.estado = 'CONFIRMADA' AND c.fecha BETWEEN ? AND ? "
                + "GROUP BY pr.id_proveedor, pr.nombre ORDER BY total DESC";

        List<List<String>> filas = new ArrayList<>();
        BigDecimal totalGeneral = BigDecimal.ZERO;
        int cantidadTotal = 0;

        try (Connection connection = DBConnectionManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setObject(1, fechaDesde);
            stmt.setObject(2, fechaHasta);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    BigDecimal total = rs.getBigDecimal("total");
                    int cantidad = rs.getInt("cantidad");
                    totalGeneral = totalGeneral.add(total);
                    cantidadTotal += cantidad;
                    filas.add(List.of(rs.getString("nombre"), String.valueOf(cantidad), "$ " + total.toPlainString()));
                }
            }
        }

        List<String> resumen = List.of(
                "Total de compras confirmadas: " + cantidadTotal,
                "Monto total: $ " + totalGeneral.toPlainString()
        );

        return new ReporteDatos(List.of("Proveedor", "Cantidad de compras", "Total"), filas, resumen);
    }

    private ReporteDatos datosTurnosPorEstado(LocalDate fechaDesde, LocalDate fechaHasta) throws SQLException {
        String sql = "SELECT estado, COUNT(*) AS cantidad FROM turnos "
                + "WHERE fecha_hora BETWEEN ? AND ? GROUP BY estado ORDER BY cantidad DESC";

        List<List<String>> filas = new ArrayList<>();
        int totalTurnos = 0;

        try (Connection connection = DBConnectionManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setObject(1, fechaDesde.atStartOfDay());
            stmt.setObject(2, fechaHasta.atTime(23, 59, 59));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int cantidad = rs.getInt("cantidad");
                    totalTurnos += cantidad;
                    filas.add(List.of(rs.getString("estado"), String.valueOf(cantidad)));
                }
            }
        }

        List<String> resumen = List.of("Total de turnos en el periodo: " + totalTurnos);

        return new ReporteDatos(List.of("Estado", "Cantidad"), filas, resumen);
    }

    private ReporteDatos datosRankingClientes(LocalDate fechaDesde, LocalDate fechaHasta) throws SQLException {
        String sql = "SELECT c.id_cliente, CONCAT(c.nombre, ' ', c.apellido) AS cliente, "
                + "(SELECT COUNT(*) FROM turnos tu WHERE tu.cliente_id = c.id_cliente "
                + "  AND tu.fecha_hora BETWEEN ? AND ?) AS turnos, "
                + "(SELECT COUNT(*) FROM trabajos_realizados tr JOIN vehiculos v2 ON tr.vehiculo_id = v2.id_vehiculo "
                + "  WHERE v2.cliente_id = c.id_cliente AND tr.fecha_ingreso BETWEEN ? AND ?) AS trabajos "
                + "FROM clientes c "
                + "HAVING (turnos + trabajos) > 0 "
                + "ORDER BY (turnos + trabajos) DESC "
                + "LIMIT 20";

        List<List<String>> filas = new ArrayList<>();

        try (Connection connection = DBConnectionManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setObject(1, fechaDesde.atStartOfDay());
            stmt.setObject(2, fechaHasta.atTime(23, 59, 59));
            stmt.setObject(3, fechaDesde);
            stmt.setObject(4, fechaHasta);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int turnos = rs.getInt("turnos");
                    int trabajos = rs.getInt("trabajos");
                    filas.add(List.of(rs.getString("cliente"), String.valueOf(turnos), String.valueOf(trabajos),
                            String.valueOf(turnos + trabajos)));
                }
            }
        }

        List<String> resumen = List.of("Clientes con actividad en el periodo: " + filas.size());

        return new ReporteDatos(List.of("Cliente", "Turnos", "Trabajos", "Actividad total"), filas, resumen);
    }
}
