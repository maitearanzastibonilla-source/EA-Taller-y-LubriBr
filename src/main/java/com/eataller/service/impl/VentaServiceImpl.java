package com.eataller.service.impl;

import com.eataller.config.DBConnectionManager;
import com.eataller.constants.AppConstants;
import com.eataller.dao.AuditoriaDAO;
import com.eataller.dao.ItemVentaDAO;
import com.eataller.dao.ProductoDAO;
import com.eataller.dao.UsuarioDAO;
import com.eataller.dao.VentaDAO;
import com.eataller.dao.impl.AuditoriaDAOImpl;
import com.eataller.dao.impl.ItemVentaDAOImpl;
import com.eataller.dao.impl.ProductoDAOImpl;
import com.eataller.dao.impl.UsuarioDAOImpl;
import com.eataller.dao.impl.VentaDAOImpl;
import com.eataller.dto.PaginaResultado;
import com.eataller.dto.VentaDTO;
import com.eataller.dto.VentaFormDTO;
import com.eataller.entity.Auditoria;
import com.eataller.entity.EstadoVenta;
import com.eataller.entity.ItemVenta;
import com.eataller.entity.MetodoPago;
import com.eataller.entity.Producto;
import com.eataller.entity.RolUsuario;
import com.eataller.entity.Usuario;
import com.eataller.entity.VentaDirecta;
import com.eataller.exception.UsuarioSinPermisosException;
import com.eataller.exception.ValidacionException;
import com.eataller.exception.VentaNoEncontradaException;
import com.eataller.service.VentaService;
import com.eataller.utils.VentaMapper;
import com.eataller.validator.VentaValidator;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class VentaServiceImpl implements VentaService {

    private final VentaDAO ventaDAO;
    private final ItemVentaDAO itemVentaDAO;
    private final ProductoDAO productoDAO;
    private final UsuarioDAO usuarioDAO;
    private final AuditoriaDAO auditoriaDAO;

    public VentaServiceImpl() {
        this.ventaDAO = new VentaDAOImpl();
        this.itemVentaDAO = new ItemVentaDAOImpl();
        this.productoDAO = new ProductoDAOImpl();
        this.usuarioDAO = new UsuarioDAOImpl();
        this.auditoriaDAO = new AuditoriaDAOImpl();
    }

    @Override
    public VentaDTO crear(VentaFormDTO form, Long idUsuarioCreador) throws ValidacionException, SQLException {
        VentaValidator.validar(form);

        VentaDirecta venta = new VentaDirecta();
        venta.setUsuarioId(idUsuarioCreador);
        venta.setFecha(LocalDate.now());
        venta.setTotal(BigDecimal.ZERO);
        venta.setMetodoPago(MetodoPago.valueOf(form.getMetodoPago().trim().toUpperCase()));
        venta.setEstado(EstadoVenta.PENDIENTE);

        try (Connection connection = DBConnectionManager.getConnection()) {
            connection.setAutoCommit(false);
            try {
                ventaDAO.crear(connection, venta);

                Auditoria auditoria = new Auditoria(idUsuarioCreador, AppConstants.MODULO_VENTAS_DIRECTAS,
                        "ALTA", "ventas_directas", venta.getIdVenta(), null,
                        "metodo_pago=" + venta.getMetodoPago(), "EXITO", "Alta de venta directa.");
                auditoriaDAO.registrar(connection, auditoria);

                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        }

        return construirDTO(venta);
    }

    @Override
    public VentaDTO actualizar(VentaFormDTO form, Long idUsuarioEditor)
            throws ValidacionException, VentaNoEncontradaException, SQLException {

        VentaValidator.validar(form);

        VentaDirecta existente = ventaDAO.buscarPorId(form.getIdVenta())
                .orElseThrow(() -> new VentaNoEncontradaException("No se encontro la venta solicitada."));

        if (existente.getEstado() != EstadoVenta.PENDIENTE) {
            throw new ValidacionException(Map.of("metodoPago", "Solo se puede modificar una venta pendiente."));
        }

        MetodoPago metodoPagoAnterior = existente.getMetodoPago();
        existente.setMetodoPago(MetodoPago.valueOf(form.getMetodoPago().trim().toUpperCase()));

        try (Connection connection = DBConnectionManager.getConnection()) {
            connection.setAutoCommit(false);
            try {
                ventaDAO.actualizarMetodoPago(connection, existente.getIdVenta(), existente.getMetodoPago().name());

                Auditoria auditoria = new Auditoria(idUsuarioEditor, AppConstants.MODULO_VENTAS_DIRECTAS,
                        "MODIFICACION", "ventas_directas", existente.getIdVenta(),
                        "metodo_pago=" + metodoPagoAnterior, "metodo_pago=" + existente.getMetodoPago(),
                        "EXITO", "Modificacion de venta directa.");
                auditoriaDAO.registrar(connection, auditoria);

                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        }

        return construirDTO(existente);
    }

    @Override
    public VentaDTO obtenerPorId(Long idVenta) throws VentaNoEncontradaException, SQLException {
        VentaDirecta venta = ventaDAO.buscarPorId(idVenta)
                .orElseThrow(() -> new VentaNoEncontradaException("No se encontro la venta solicitada."));
        return construirDTO(venta);
    }

    @Override
    public PaginaResultado<VentaDTO> listar(String textoBusqueda, String estadoFiltro,
                                             LocalDate fechaDesde, LocalDate fechaHasta,
                                             int pagina, int registrosPorPagina) throws SQLException {

        PaginaResultado<VentaDirecta> resultado = ventaDAO.listar(textoBusqueda, estadoFiltro,
                fechaDesde, fechaHasta, pagina, registrosPorPagina);

        Map<Long, String> nombresUsuarios = new HashMap<>();

        List<VentaDTO> dtos = resultado.getRegistros().stream()
                .map(venta -> {
                    String nombreUsuario = nombresUsuarios.computeIfAbsent(venta.getUsuarioId(),
                            this::buscarNombreUsuarioSilencioso);
                    return VentaMapper.aDTO(venta, nombreUsuario);
                })
                .collect(Collectors.toList());

        return new PaginaResultado<>(dtos, resultado.getTotalRegistros(), resultado.getPaginaActual(),
                resultado.getRegistrosPorPagina());
    }

    @Override
    public void confirmar(Long idVenta, Long idUsuarioEditor)
            throws ValidacionException, VentaNoEncontradaException, SQLException {

        VentaDirecta venta = ventaDAO.buscarPorId(idVenta)
                .orElseThrow(() -> new VentaNoEncontradaException("No se encontro la venta solicitada."));

        if (venta.getEstado() != EstadoVenta.PENDIENTE) {
            throw new ValidacionException(Map.of("estado", "Solo se pueden confirmar ventas pendientes."));
        }

        List<ItemVenta> items = itemVentaDAO.listarPorVenta(idVenta);
        if (items.isEmpty()) {
            throw new ValidacionException(Map.of("estado", "No se puede confirmar una venta sin items."));
        }

        for (ItemVenta item : items) {
            Producto producto = productoDAO.buscarPorId(item.getProductoId())
                    .orElseThrow(() -> new ValidacionException(Map.of("estado",
                            "Uno de los productos de la venta ya no existe en el catalogo.")));
            if (producto.getStockActual() < item.getCantidad()) {
                throw new ValidacionException(Map.of("estado", "Stock insuficiente para " + producto.getNombre()
                        + ": disponible " + producto.getStockActual() + ", solicitado " + item.getCantidad() + "."));
            }
        }

        try (Connection connection = DBConnectionManager.getConnection()) {
            connection.setAutoCommit(false);
            try {
                for (ItemVenta item : items) {
                    productoDAO.ajustarStock(connection, item.getProductoId(), -item.getCantidad());
                }
                ventaDAO.confirmar(connection, idVenta);

                Auditoria auditoria = new Auditoria(idUsuarioEditor, AppConstants.MODULO_VENTAS_DIRECTAS,
                        "CONFIRMACION", "ventas_directas", idVenta, "estado=" + venta.getEstado(),
                        "estado=CONFIRMADA", "EXITO", "Confirmacion de venta directa, descuento de stock aplicado.");
                auditoriaDAO.registrar(connection, auditoria);

                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        }
    }

    @Override
    public void anular(Long idVenta, Long idUsuarioEditor, RolUsuario rolEditor)
            throws ValidacionException, VentaNoEncontradaException, UsuarioSinPermisosException, SQLException {

        if (rolEditor != RolUsuario.ADMINISTRADOR) {
            throw new UsuarioSinPermisosException("Solo un administrador puede anular una venta confirmada.");
        }

        VentaDirecta venta = ventaDAO.buscarPorId(idVenta)
                .orElseThrow(() -> new VentaNoEncontradaException("No se encontro la venta solicitada."));

        if (venta.getEstado() != EstadoVenta.CONFIRMADA) {
            throw new ValidacionException(Map.of("estado", "Solo se pueden anular ventas confirmadas."));
        }

        List<ItemVenta> items = itemVentaDAO.listarPorVenta(idVenta);

        try (Connection connection = DBConnectionManager.getConnection()) {
            connection.setAutoCommit(false);
            try {
                for (ItemVenta item : items) {
                    productoDAO.ajustarStock(connection, item.getProductoId(), item.getCantidad());
                }
                ventaDAO.anular(connection, idVenta);

                Auditoria auditoria = new Auditoria(idUsuarioEditor, AppConstants.MODULO_VENTAS_DIRECTAS,
                        "ANULACION", "ventas_directas", idVenta, "estado=" + venta.getEstado(),
                        "estado=ANULADA", "EXITO", "Anulacion de venta directa, stock restaurado.");
                auditoriaDAO.registrar(connection, auditoria);

                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        }
    }

    private VentaDTO construirDTO(VentaDirecta venta) throws SQLException {
        String nombreUsuario = buscarNombreUsuarioSilencioso(venta.getUsuarioId());
        return VentaMapper.aDTO(venta, nombreUsuario);
    }

    private String buscarNombreUsuarioSilencioso(Long idUsuario) {
        try {
            return usuarioDAO.buscarPorId(idUsuario).map(Usuario::getNombre).orElse("(sin datos)");
        } catch (SQLException e) {
            return "(sin datos)";
        }
    }
}
