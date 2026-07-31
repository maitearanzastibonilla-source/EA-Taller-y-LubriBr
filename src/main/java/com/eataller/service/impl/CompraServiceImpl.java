package com.eataller.service.impl;

import com.eataller.config.DBConnectionManager;
import com.eataller.constants.AppConstants;
import com.eataller.dao.AuditoriaDAO;
import com.eataller.dao.CompraDAO;
import com.eataller.dao.ItemCompraDAO;
import com.eataller.dao.ProductoDAO;
import com.eataller.dao.ProveedorDAO;
import com.eataller.dao.UsuarioDAO;
import com.eataller.dao.impl.AuditoriaDAOImpl;
import com.eataller.dao.impl.CompraDAOImpl;
import com.eataller.dao.impl.ItemCompraDAOImpl;
import com.eataller.dao.impl.ProductoDAOImpl;
import com.eataller.dao.impl.ProveedorDAOImpl;
import com.eataller.dao.impl.UsuarioDAOImpl;
import com.eataller.dto.CompraDTO;
import com.eataller.dto.CompraFormDTO;
import com.eataller.dto.PaginaResultado;
import com.eataller.entity.Auditoria;
import com.eataller.entity.Compra;
import com.eataller.entity.EstadoCompra;
import com.eataller.entity.ItemCompra;
import com.eataller.entity.Proveedor;
import com.eataller.entity.RolUsuario;
import com.eataller.entity.Usuario;
import com.eataller.exception.CompraNoEncontradaException;
import com.eataller.exception.ProveedorNoEncontradoException;
import com.eataller.exception.UsuarioSinPermisosException;
import com.eataller.exception.ValidacionException;
import com.eataller.service.CompraService;
import com.eataller.utils.CompraMapper;
import com.eataller.validator.CompraValidator;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CompraServiceImpl implements CompraService {

    private final CompraDAO compraDAO;
    private final ItemCompraDAO itemCompraDAO;
    private final ProductoDAO productoDAO;
    private final ProveedorDAO proveedorDAO;
    private final UsuarioDAO usuarioDAO;
    private final AuditoriaDAO auditoriaDAO;

    public CompraServiceImpl() {
        this.compraDAO = new CompraDAOImpl();
        this.itemCompraDAO = new ItemCompraDAOImpl();
        this.productoDAO = new ProductoDAOImpl();
        this.proveedorDAO = new ProveedorDAOImpl();
        this.usuarioDAO = new UsuarioDAOImpl();
        this.auditoriaDAO = new AuditoriaDAOImpl();
    }

    @Override
    public CompraDTO crear(CompraFormDTO form, Long idUsuarioCreador)
            throws ValidacionException, ProveedorNoEncontradoException, SQLException {

        CompraValidator.validar(form);

        Proveedor proveedor = proveedorDAO.buscarPorId(form.getProveedorId())
                .orElseThrow(() -> new ProveedorNoEncontradoException("No se encontro el proveedor seleccionado."));

        Compra compra = new Compra();
        compra.setProveedorId(proveedor.getIdProveedor());
        compra.setUsuarioId(idUsuarioCreador);
        compra.setFecha(LocalDate.now());
        compra.setTotal(BigDecimal.ZERO);
        compra.setEstado(EstadoCompra.PENDIENTE);

        try (Connection connection = DBConnectionManager.getConnection()) {
            connection.setAutoCommit(false);
            try {
                compraDAO.crear(connection, compra);

                Auditoria auditoria = new Auditoria(idUsuarioCreador, AppConstants.MODULO_COMPRAS,
                        "ALTA", "compras", compra.getIdCompra(), null,
                        "proveedor_id=" + compra.getProveedorId(), "EXITO", "Alta de compra.");
                auditoriaDAO.registrar(connection, auditoria);

                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        }

        return construirDTO(compra);
    }

    @Override
    public CompraDTO actualizar(CompraFormDTO form, Long idUsuarioEditor)
            throws ValidacionException, CompraNoEncontradaException, ProveedorNoEncontradoException, SQLException {

        CompraValidator.validar(form);

        Compra existente = compraDAO.buscarPorId(form.getIdCompra())
                .orElseThrow(() -> new CompraNoEncontradaException("No se encontro la compra solicitada."));

        if (existente.getEstado() != EstadoCompra.PENDIENTE) {
            throw new ValidacionException(Map.of("proveedorId", "Solo se puede modificar una compra pendiente."));
        }

        Proveedor proveedor = proveedorDAO.buscarPorId(form.getProveedorId())
                .orElseThrow(() -> new ProveedorNoEncontradoException("No se encontro el proveedor seleccionado."));

        Long proveedorAnterior = existente.getProveedorId();
        existente.setProveedorId(proveedor.getIdProveedor());

        try (Connection connection = DBConnectionManager.getConnection()) {
            connection.setAutoCommit(false);
            try {
                compraDAO.actualizarProveedor(connection, existente.getIdCompra(), existente.getProveedorId());

                Auditoria auditoria = new Auditoria(idUsuarioEditor, AppConstants.MODULO_COMPRAS,
                        "MODIFICACION", "compras", existente.getIdCompra(),
                        "proveedor_id=" + proveedorAnterior, "proveedor_id=" + existente.getProveedorId(),
                        "EXITO", "Modificacion de compra.");
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
    public CompraDTO obtenerPorId(Long idCompra) throws CompraNoEncontradaException, SQLException {
        Compra compra = compraDAO.buscarPorId(idCompra)
                .orElseThrow(() -> new CompraNoEncontradaException("No se encontro la compra solicitada."));
        return construirDTO(compra);
    }

    @Override
    public PaginaResultado<CompraDTO> listar(String textoBusqueda, String estadoFiltro,
                                              LocalDate fechaDesde, LocalDate fechaHasta,
                                              int pagina, int registrosPorPagina) throws SQLException {

        PaginaResultado<Compra> resultado = compraDAO.listar(textoBusqueda, estadoFiltro,
                fechaDesde, fechaHasta, pagina, registrosPorPagina);

        Map<Long, String> nombresProveedores = new HashMap<>();
        Map<Long, String> nombresUsuarios = new HashMap<>();

        List<CompraDTO> dtos = resultado.getRegistros().stream()
                .map(compra -> {
                    String nombreProveedor = nombresProveedores.computeIfAbsent(compra.getProveedorId(),
                            this::buscarNombreProveedorSilencioso);
                    String nombreUsuario = nombresUsuarios.computeIfAbsent(compra.getUsuarioId(),
                            this::buscarNombreUsuarioSilencioso);
                    return CompraMapper.aDTO(compra, nombreProveedor, nombreUsuario);
                })
                .collect(Collectors.toList());

        return new PaginaResultado<>(dtos, resultado.getTotalRegistros(), resultado.getPaginaActual(),
                resultado.getRegistrosPorPagina());
    }

    @Override
    public void confirmar(Long idCompra, Long idUsuarioEditor)
            throws ValidacionException, CompraNoEncontradaException, SQLException {

        Compra compra = compraDAO.buscarPorId(idCompra)
                .orElseThrow(() -> new CompraNoEncontradaException("No se encontro la compra solicitada."));

        if (compra.getEstado() != EstadoCompra.PENDIENTE) {
            throw new ValidacionException(Map.of("estado", "Solo se pueden confirmar compras pendientes."));
        }

        List<ItemCompra> items = itemCompraDAO.listarPorCompra(idCompra);
        if (items.isEmpty()) {
            throw new ValidacionException(Map.of("estado", "No se puede confirmar una compra sin items."));
        }

        try (Connection connection = DBConnectionManager.getConnection()) {
            connection.setAutoCommit(false);
            try {
                for (ItemCompra item : items) {
                    productoDAO.ajustarStock(connection, item.getProductoId(), item.getCantidad());
                }
                compraDAO.confirmar(connection, idCompra);

                Auditoria auditoria = new Auditoria(idUsuarioEditor, AppConstants.MODULO_COMPRAS,
                        "CONFIRMACION", "compras", idCompra, "estado=" + compra.getEstado(),
                        "estado=CONFIRMADA", "EXITO", "Confirmacion de compra, stock incrementado.");
                auditoriaDAO.registrar(connection, auditoria);

                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        }
    }

    @Override
    public void anular(Long idCompra, Long idUsuarioEditor, RolUsuario rolEditor)
            throws ValidacionException, CompraNoEncontradaException, UsuarioSinPermisosException, SQLException {

        if (rolEditor != RolUsuario.ADMINISTRADOR) {
            throw new UsuarioSinPermisosException("Solo un administrador puede anular una compra confirmada.");
        }

        Compra compra = compraDAO.buscarPorId(idCompra)
                .orElseThrow(() -> new CompraNoEncontradaException("No se encontro la compra solicitada."));

        if (compra.getEstado() != EstadoCompra.CONFIRMADA) {
            throw new ValidacionException(Map.of("estado", "Solo se pueden anular compras confirmadas."));
        }

        List<ItemCompra> items = itemCompraDAO.listarPorCompra(idCompra);

        try (Connection connection = DBConnectionManager.getConnection()) {
            connection.setAutoCommit(false);
            try {
                for (ItemCompra item : items) {
                    productoDAO.ajustarStock(connection, item.getProductoId(), -item.getCantidad());
                }
                compraDAO.anular(connection, idCompra);

                Auditoria auditoria = new Auditoria(idUsuarioEditor, AppConstants.MODULO_COMPRAS,
                        "ANULACION", "compras", idCompra, "estado=" + compra.getEstado(),
                        "estado=ANULADA", "EXITO", "Anulacion de compra, stock revertido.");
                auditoriaDAO.registrar(connection, auditoria);

                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        }
    }

    private CompraDTO construirDTO(Compra compra) throws SQLException {
        String nombreProveedor = buscarNombreProveedorSilencioso(compra.getProveedorId());
        String nombreUsuario = buscarNombreUsuarioSilencioso(compra.getUsuarioId());
        return CompraMapper.aDTO(compra, nombreProveedor, nombreUsuario);
    }

    private String buscarNombreProveedorSilencioso(Long idProveedor) {
        try {
            return proveedorDAO.buscarPorId(idProveedor).map(Proveedor::getNombre).orElse("(sin datos)");
        } catch (SQLException e) {
            return "(sin datos)";
        }
    }

    private String buscarNombreUsuarioSilencioso(Long idUsuario) {
        try {
            return usuarioDAO.buscarPorId(idUsuario).map(Usuario::getNombre).orElse("(sin datos)");
        } catch (SQLException e) {
            return "(sin datos)";
        }
    }
}
