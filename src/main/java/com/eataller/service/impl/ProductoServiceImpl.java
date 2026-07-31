package com.eataller.service.impl;

import com.eataller.config.DBConnectionManager;
import com.eataller.constants.AppConstants;
import com.eataller.dao.AuditoriaDAO;
import com.eataller.dao.ProductoDAO;
import com.eataller.dao.ProveedorDAO;
import com.eataller.dao.impl.AuditoriaDAOImpl;
import com.eataller.dao.impl.ProductoDAOImpl;
import com.eataller.dao.impl.ProveedorDAOImpl;
import com.eataller.dto.PaginaResultado;
import com.eataller.dto.ProductoDTO;
import com.eataller.dto.ProductoFormDTO;
import com.eataller.entity.Auditoria;
import com.eataller.entity.Estado;
import com.eataller.entity.Producto;
import com.eataller.entity.Proveedor;
import com.eataller.exception.ProductoNoEncontradoException;
import com.eataller.exception.ProveedorNoEncontradoException;
import com.eataller.exception.ValidacionException;
import com.eataller.service.ProductoService;
import com.eataller.utils.ProductoMapper;
import com.eataller.validator.ProductoValidator;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ProductoServiceImpl implements ProductoService {

    private final ProductoDAO productoDAO;
    private final ProveedorDAO proveedorDAO;
    private final AuditoriaDAO auditoriaDAO;

    public ProductoServiceImpl() {
        this.productoDAO = new ProductoDAOImpl();
        this.proveedorDAO = new ProveedorDAOImpl();
        this.auditoriaDAO = new AuditoriaDAOImpl();
    }

    @Override
    public ProductoDTO crear(ProductoFormDTO form, Long idUsuarioCreador)
            throws ValidacionException, ProveedorNoEncontradoException, SQLException {

        ProductoValidator.validar(form);

        Proveedor proveedor = proveedorDAO.buscarPorId(form.getProveedorId())
                .orElseThrow(() -> new ProveedorNoEncontradoException("No se encontro el proveedor seleccionado."));

        Producto producto = new Producto();
        producto.setProveedorId(proveedor.getIdProveedor());
        producto.setNombre(form.getNombre().trim());
        producto.setDescripcion(vacioComoNulo(form.getDescripcion()));
        producto.setCategoria(vacioComoNulo(form.getCategoria()));
        producto.setPrecioVenta(new BigDecimal(form.getPrecioVenta().trim()));
        producto.setPrecioCosto(parsearBigDecimalOpcional(form.getPrecioCosto()));
        producto.setStockActual(Integer.parseInt(form.getStockActual().trim()));
        producto.setStockMinimo(Integer.parseInt(form.getStockMinimo().trim()));
        producto.setEstado(Estado.ACTIVO);

        try (Connection connection = DBConnectionManager.getConnection()) {
            connection.setAutoCommit(false);
            try {
                productoDAO.crear(connection, producto);

                Auditoria auditoria = new Auditoria(idUsuarioCreador, AppConstants.MODULO_PRODUCTOS,
                        "ALTA", "productos", producto.getIdProducto(), null,
                        "nombre=" + producto.getNombre() + ", proveedor_id=" + producto.getProveedorId()
                                + ", stock_actual=" + producto.getStockActual(),
                        "EXITO", "Alta de producto.");
                auditoriaDAO.registrar(connection, auditoria);

                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        }

        return ProductoMapper.aDTO(producto, proveedor.getNombre());
    }

    @Override
    public ProductoDTO actualizar(ProductoFormDTO form, Long idUsuarioEditor)
            throws ValidacionException, ProductoNoEncontradoException, ProveedorNoEncontradoException, SQLException {

        ProductoValidator.validar(form);

        Producto existente = productoDAO.buscarPorId(form.getIdProducto())
                .orElseThrow(() -> new ProductoNoEncontradoException(
                        "No se encontro el producto solicitado (ID " + form.getIdProducto() + ")."));

        Proveedor proveedor = proveedorDAO.buscarPorId(form.getProveedorId())
                .orElseThrow(() -> new ProveedorNoEncontradoException("No se encontro el proveedor seleccionado."));

        String valoresAnteriores = "nombre=" + existente.getNombre() + ", precio_venta=" + existente.getPrecioVenta()
                + ", stock_minimo=" + existente.getStockMinimo();

        existente.setProveedorId(proveedor.getIdProveedor());
        existente.setNombre(form.getNombre().trim());
        existente.setDescripcion(vacioComoNulo(form.getDescripcion()));
        existente.setCategoria(vacioComoNulo(form.getCategoria()));
        existente.setPrecioVenta(new BigDecimal(form.getPrecioVenta().trim()));
        existente.setPrecioCosto(parsearBigDecimalOpcional(form.getPrecioCosto()));
        existente.setStockMinimo(Integer.parseInt(form.getStockMinimo().trim()));

        try (Connection connection = DBConnectionManager.getConnection()) {
            connection.setAutoCommit(false);
            try {
                productoDAO.actualizar(connection, existente);

                String valoresNuevos = "nombre=" + existente.getNombre() + ", precio_venta=" + existente.getPrecioVenta()
                        + ", stock_minimo=" + existente.getStockMinimo();
                Auditoria auditoria = new Auditoria(idUsuarioEditor, AppConstants.MODULO_PRODUCTOS,
                        "MODIFICACION", "productos", existente.getIdProducto(),
                        valoresAnteriores, valoresNuevos, "EXITO", "Modificacion de datos de producto.");
                auditoriaDAO.registrar(connection, auditoria);

                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        }

        return ProductoMapper.aDTO(existente, proveedor.getNombre());
    }

    @Override
    public ProductoDTO obtenerPorId(Long idProducto) throws ProductoNoEncontradoException, SQLException {
        Producto producto = productoDAO.buscarPorId(idProducto)
                .orElseThrow(() -> new ProductoNoEncontradoException(
                        "No se encontro el producto solicitado (ID " + idProducto + ")."));

        String proveedorNombre = proveedorDAO.buscarPorId(producto.getProveedorId())
                .map(Proveedor::getNombre)
                .orElse("(sin datos)");

        return ProductoMapper.aDTO(producto, proveedorNombre);
    }

    @Override
    public PaginaResultado<ProductoDTO> listar(String textoBusqueda, String estadoFiltro, Long proveedorId,
                                                int pagina, int registrosPorPagina) throws SQLException {

        PaginaResultado<Producto> resultado = productoDAO.listar(textoBusqueda, estadoFiltro, proveedorId,
                pagina, registrosPorPagina);

        Map<Long, String> nombresProveedores = new HashMap<>();

        List<ProductoDTO> dtos = resultado.getRegistros().stream()
                .map(producto -> {
                    String nombreProveedor = nombresProveedores.computeIfAbsent(producto.getProveedorId(),
                            this::buscarNombreProveedorSilencioso);
                    return ProductoMapper.aDTO(producto, nombreProveedor);
                })
                .collect(Collectors.toList());

        return new PaginaResultado<>(dtos, resultado.getTotalRegistros(), resultado.getPaginaActual(),
                resultado.getRegistrosPorPagina());
    }

    @Override
    public void cambiarEstado(Long idProducto, boolean activo, Long idUsuarioEditor)
            throws ProductoNoEncontradoException, ValidacionException, SQLException {

        Producto producto = productoDAO.buscarPorId(idProducto)
                .orElseThrow(() -> new ProductoNoEncontradoException(
                        "No se encontro el producto solicitado (ID " + idProducto + ")."));

        if (!activo && productoDAO.tieneItemsDeTrabajoAsociados(idProducto)) {
            throw new ValidacionException(Map.of("estado",
                    "No se puede dar de baja un producto utilizado en trabajos realizados."));
        }
        if (!activo && productoDAO.tieneItemsDeVentaAsociados(idProducto)) {
            throw new ValidacionException(Map.of("estado",
                    "No se puede dar de baja un producto utilizado en ventas directas."));
        }
        if (!activo && productoDAO.tieneItemsDeCompraAsociados(idProducto)) {
            throw new ValidacionException(Map.of("estado",
                    "No se puede dar de baja un producto utilizado en compras."));
        }

        try (Connection connection = DBConnectionManager.getConnection()) {
            connection.setAutoCommit(false);
            try {
                productoDAO.cambiarEstado(connection, idProducto, activo);

                Auditoria auditoria = new Auditoria(idUsuarioEditor, AppConstants.MODULO_PRODUCTOS,
                        activo ? "ACTIVACION" : "BAJA_LOGICA", "productos", idProducto,
                        "estado=" + producto.getEstado(), "estado=" + (activo ? "ACTIVO" : "INACTIVO"),
                        "EXITO", "Cambio de estado de producto.");
                auditoriaDAO.registrar(connection, auditoria);

                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        }
    }

    private String buscarNombreProveedorSilencioso(Long idProveedor) {
        try {
            return proveedorDAO.buscarPorId(idProveedor).map(Proveedor::getNombre).orElse("(sin datos)");
        } catch (SQLException e) {
            return "(sin datos)";
        }
    }

    private String vacioComoNulo(String valor) {
        return (valor == null || valor.isBlank()) ? null : valor.trim();
    }

    private BigDecimal parsearBigDecimalOpcional(String valor) {
        return (valor == null || valor.isBlank()) ? null : new BigDecimal(valor.trim());
    }
}
