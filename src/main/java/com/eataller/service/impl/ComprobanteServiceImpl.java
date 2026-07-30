package com.eataller.service.impl;

import com.eataller.config.DBConnectionManager;
import com.eataller.constants.AppConstants;
import com.eataller.dao.AuditoriaDAO;
import com.eataller.dao.ClienteDAO;
import com.eataller.dao.ComprobanteDAO;
import com.eataller.dao.ItemTrabajoDAO;
import com.eataller.dao.TrabajoDAO;
import com.eataller.dao.VehiculoDAO;
import com.eataller.dao.impl.AuditoriaDAOImpl;
import com.eataller.dao.impl.ClienteDAOImpl;
import com.eataller.dao.impl.ComprobanteDAOImpl;
import com.eataller.dao.impl.ItemTrabajoDAOImpl;
import com.eataller.dao.impl.TrabajoDAOImpl;
import com.eataller.dao.impl.VehiculoDAOImpl;
import com.eataller.dto.ComprobanteDTO;
import com.eataller.dto.ComprobanteFormDTO;
import com.eataller.dto.PaginaResultado;
import com.eataller.entity.Auditoria;
import com.eataller.entity.Cliente;
import com.eataller.entity.Comprobante;
import com.eataller.entity.EstadoComprobante;
import com.eataller.entity.EstadoTrabajo;
import com.eataller.entity.ItemTrabajo;
import com.eataller.entity.MetodoPago;
import com.eataller.entity.RolUsuario;
import com.eataller.entity.TrabajoRealizado;
import com.eataller.entity.Vehiculo;
import com.eataller.exception.ComprobanteNoEncontradoException;
import com.eataller.exception.TrabajoNoEncontradoException;
import com.eataller.exception.UsuarioSinPermisosException;
import com.eataller.exception.ValidacionException;
import com.eataller.service.ComprobanteService;
import com.eataller.utils.ComprobanteMapper;
import com.eataller.utils.ComprobantePdfGenerator;
import com.eataller.validator.ComprobanteValidator;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class ComprobanteServiceImpl implements ComprobanteService {

    private static final String NOMBRE_DIRECTORIO_PDF = "comprobantes-pdf";

    private final ComprobanteDAO comprobanteDAO;
    private final TrabajoDAO trabajoDAO;
    private final ItemTrabajoDAO itemTrabajoDAO;
    private final VehiculoDAO vehiculoDAO;
    private final ClienteDAO clienteDAO;
    private final AuditoriaDAO auditoriaDAO;

    public ComprobanteServiceImpl() {
        this.comprobanteDAO = new ComprobanteDAOImpl();
        this.trabajoDAO = new TrabajoDAOImpl();
        this.itemTrabajoDAO = new ItemTrabajoDAOImpl();
        this.vehiculoDAO = new VehiculoDAOImpl();
        this.clienteDAO = new ClienteDAOImpl();
        this.auditoriaDAO = new AuditoriaDAOImpl();
    }

    @Override
    public ComprobanteDTO generar(ComprobanteFormDTO form, Long idUsuarioGenerador)
            throws ValidacionException, TrabajoNoEncontradoException, SQLException, IOException {

        ComprobanteValidator.validar(form);

        TrabajoRealizado trabajo = trabajoDAO.buscarPorId(form.getTrabajoId())
                .orElseThrow(() -> new TrabajoNoEncontradoException("No se encontro el trabajo indicado."));

        if (trabajo.getEstado() != EstadoTrabajo.FINALIZADO) {
            throw new ValidacionException(Map.of("trabajoId", "Solo se puede generar un comprobante para un trabajo finalizado."));
        }

        List<ItemTrabajo> items = itemTrabajoDAO.listarPorTrabajo(trabajo.getIdTrabajo());
        if (items.isEmpty()) {
            throw new ValidacionException(Map.of("trabajoId", "No se puede generar un comprobante sobre un trabajo sin items."));
        }

        if (comprobanteDAO.buscarActivoPorTrabajo(trabajo.getIdTrabajo()).isPresent()) {
            throw new ValidacionException(Map.of("trabajoId", "Ya existe un comprobante activo para este trabajo."));
        }

        Vehiculo vehiculo = vehiculoDAO.buscarPorId(trabajo.getVehiculoId())
                .orElseThrow(() -> new TrabajoNoEncontradoException("No se encontro el vehiculo asociado al trabajo."));
        Cliente cliente = clienteDAO.buscarPorId(vehiculo.getClienteId())
                .orElseThrow(() -> new TrabajoNoEncontradoException("No se encontro el cliente asociado al trabajo."));

        BigDecimal total = items.stream()
                .map(ItemTrabajo::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Comprobante comprobante = new Comprobante();
        comprobante.setTrabajoId(trabajo.getIdTrabajo());
        comprobante.setFecha(LocalDate.now());
        comprobante.setTotal(total);
        comprobante.setMetodoPago(MetodoPago.desdeValorBD(form.getMetodoPago()));
        comprobante.setEstado(EstadoComprobante.desdeValorBD(form.getEstado()));

        try (Connection connection = DBConnectionManager.getConnection()) {
            connection.setAutoCommit(false);
            try {
                comprobanteDAO.crear(connection, comprobante);

                Path destino = resolverDirectorioPdf().resolve("comprobante-" + comprobante.getIdComprobante() + ".pdf");
                ComprobantePdfGenerator.generar(destino, comprobante, trabajo, vehiculo, cliente, items);
                comprobante.setPdfUrl(destino.toString());
                comprobanteDAO.actualizarPdfUrl(connection, comprobante.getIdComprobante(), destino.toString());

                trabajoDAO.marcarFacturado(connection, trabajo.getIdTrabajo());

                Auditoria auditoria = new Auditoria(idUsuarioGenerador, AppConstants.MODULO_COMPROBANTES,
                        "ALTA", "comprobantes", comprobante.getIdComprobante(), null,
                        "trabajo_id=" + comprobante.getTrabajoId() + ", total=" + comprobante.getTotal()
                                + ", metodo_pago=" + comprobante.getMetodoPago(),
                        "EXITO", "Generacion de comprobante.");
                auditoriaDAO.registrar(connection, auditoria);

                connection.commit();
            } catch (SQLException | IOException e) {
                connection.rollback();
                throw e;
            }
        }

        return ComprobanteMapper.aDTO(comprobante, vehiculo.getPatente(), cliente.getNombreCompleto());
    }

    @Override
    public ComprobanteDTO obtenerPorId(Long idComprobante) throws ComprobanteNoEncontradoException, SQLException {
        Comprobante comprobante = comprobanteDAO.buscarPorId(idComprobante)
                .orElseThrow(() -> new ComprobanteNoEncontradoException("No se encontro el comprobante solicitado."));
        return construirDTO(comprobante);
    }

    @Override
    public Optional<ComprobanteDTO> obtenerActivoPorTrabajo(Long idTrabajo) throws SQLException {
        Optional<Comprobante> comprobante = comprobanteDAO.buscarActivoPorTrabajo(idTrabajo);
        if (comprobante.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(construirDTO(comprobante.get()));
    }

    @Override
    public PaginaResultado<ComprobanteDTO> listar(String textoBusqueda, String estadoFiltro,
                                                   LocalDate fechaDesde, LocalDate fechaHasta,
                                                   int pagina, int registrosPorPagina) throws SQLException {

        PaginaResultado<Comprobante> resultado = comprobanteDAO.listar(textoBusqueda, estadoFiltro,
                fechaDesde, fechaHasta, pagina, registrosPorPagina);

        Map<Long, Vehiculo> vehiculosCache = new HashMap<>();
        Map<Long, Cliente> clientesCache = new HashMap<>();

        List<ComprobanteDTO> dtos = resultado.getRegistros().stream()
                .map(co -> {
                    try {
                        TrabajoRealizado trabajo = trabajoDAO.buscarPorId(co.getTrabajoId()).orElse(null);
                        if (trabajo == null) {
                            return ComprobanteMapper.aDTO(co, "(sin datos)", "(sin datos)");
                        }
                        Vehiculo vehiculo = vehiculosCache.computeIfAbsent(trabajo.getVehiculoId(), this::buscarVehiculoSilencioso);
                        Cliente cliente = vehiculo == null ? null
                                : clientesCache.computeIfAbsent(vehiculo.getClienteId(), this::buscarClienteSilencioso);
                        return ComprobanteMapper.aDTO(co,
                                vehiculo != null ? vehiculo.getPatente() : "(sin datos)",
                                cliente != null ? cliente.getNombreCompleto() : "(sin datos)");
                    } catch (SQLException e) {
                        return ComprobanteMapper.aDTO(co, "(sin datos)", "(sin datos)");
                    }
                })
                .collect(Collectors.toList());

        return new PaginaResultado<>(dtos, resultado.getTotalRegistros(), resultado.getPaginaActual(),
                resultado.getRegistrosPorPagina());
    }

    @Override
    public void anular(Long idComprobante, Long idUsuarioEditor, RolUsuario rolEditor)
            throws ComprobanteNoEncontradoException, ValidacionException, UsuarioSinPermisosException, SQLException {

        if (rolEditor != RolUsuario.ADMINISTRADOR) {
            throw new UsuarioSinPermisosException("Solo un administrador puede anular un comprobante.");
        }

        Comprobante comprobante = comprobanteDAO.buscarPorId(idComprobante)
                .orElseThrow(() -> new ComprobanteNoEncontradoException("No se encontro el comprobante solicitado."));

        if (comprobante.getEstado() == EstadoComprobante.ANULADO) {
            throw new ValidacionException(Map.of("estado", "El comprobante ya se encuentra anulado."));
        }

        try (Connection connection = DBConnectionManager.getConnection()) {
            connection.setAutoCommit(false);
            try {
                comprobanteDAO.anular(connection, idComprobante);
                trabajoDAO.desmarcarFacturado(connection, comprobante.getTrabajoId());

                Auditoria auditoria = new Auditoria(idUsuarioEditor, AppConstants.MODULO_COMPROBANTES,
                        "ANULACION", "comprobantes", idComprobante, "estado=" + comprobante.getEstado(),
                        "estado=ANULADO", "EXITO", "Anulacion administrativa de comprobante.");
                auditoriaDAO.registrar(connection, auditoria);

                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        }
    }

    private ComprobanteDTO construirDTO(Comprobante comprobante) throws SQLException {
        TrabajoRealizado trabajo = trabajoDAO.buscarPorId(comprobante.getTrabajoId()).orElse(null);
        if (trabajo == null) {
            return ComprobanteMapper.aDTO(comprobante, "(sin datos)", "(sin datos)");
        }
        Vehiculo vehiculo = vehiculoDAO.buscarPorId(trabajo.getVehiculoId()).orElse(null);
        Cliente cliente = vehiculo == null ? null : clienteDAO.buscarPorId(vehiculo.getClienteId()).orElse(null);
        return ComprobanteMapper.aDTO(comprobante,
                vehiculo != null ? vehiculo.getPatente() : "(sin datos)",
                cliente != null ? cliente.getNombreCompleto() : "(sin datos)");
    }

    private Vehiculo buscarVehiculoSilencioso(Long id) {
        try {
            return vehiculoDAO.buscarPorId(id).orElse(null);
        } catch (SQLException e) {
            return null;
        }
    }

    private Cliente buscarClienteSilencioso(Long id) {
        try {
            return clienteDAO.buscarPorId(id).orElse(null);
        } catch (SQLException e) {
            return null;
        }
    }

    private Path resolverDirectorioPdf() throws IOException {
        String base = System.getProperty("catalina.base");
        Path directorio = base != null
                ? Paths.get(base, NOMBRE_DIRECTORIO_PDF)
                : Paths.get(System.getProperty("java.io.tmpdir"), NOMBRE_DIRECTORIO_PDF);
        Files.createDirectories(directorio);
        return directorio;
    }
}
