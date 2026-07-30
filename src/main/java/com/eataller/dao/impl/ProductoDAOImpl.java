package com.eataller.dao.impl;

import com.eataller.config.DBConnectionManager;
import com.eataller.dao.ProductoDAO;
import com.eataller.dto.PaginaResultado;
import com.eataller.entity.Estado;
import com.eataller.entity.Producto;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProductoDAOImpl implements ProductoDAO {

    private static final String SQL_INSERT =
            "INSERT INTO productos (proveedor_id, nombre, descripcion, categoria, precio_venta, precio_costo, "
                    + "stock_actual, stock_minimo, estado) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String SQL_UPDATE =
            "UPDATE productos SET proveedor_id = ?, nombre = ?, descripcion = ?, categoria = ?, "
                    + "precio_venta = ?, precio_costo = ?, stock_minimo = ? WHERE id_producto = ?";

    private static final String SQL_CAMBIAR_ESTADO =
            "UPDATE productos SET estado = ? WHERE id_producto = ?";

    private static final String SQL_AJUSTAR_STOCK =
            "UPDATE productos SET stock_actual = stock_actual + ? WHERE id_producto = ?";

    private static final String SQL_BUSCAR_POR_ID =
            "SELECT * FROM productos WHERE id_producto = ?";

    private static final String SQL_TIENE_ITEMS_TRABAJO =
            "SELECT COUNT(*) FROM items_trabajo WHERE producto_id = ?";

    private static final String SQL_TIENE_ITEMS_VENTA =
            "SELECT COUNT(*) FROM items_venta WHERE producto_id = ?";

    @Override
    public Producto crear(Connection connection, Producto producto) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, producto.getProveedorId());
            stmt.setString(2, producto.getNombre());
            stmt.setString(3, producto.getDescripcion());
            stmt.setString(4, producto.getCategoria());
            stmt.setBigDecimal(5, producto.getPrecioVenta());
            if (producto.getPrecioCosto() != null) {
                stmt.setBigDecimal(6, producto.getPrecioCosto());
            } else {
                stmt.setNull(6, java.sql.Types.DECIMAL);
            }
            stmt.setInt(7, producto.getStockActual());
            stmt.setInt(8, producto.getStockMinimo());
            stmt.setString(9, producto.getEstado().name());
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    producto.setIdProducto(keys.getLong(1));
                }
            }
            return producto;
        }
    }

    @Override
    public void actualizar(Connection connection, Producto producto) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(SQL_UPDATE)) {
            stmt.setLong(1, producto.getProveedorId());
            stmt.setString(2, producto.getNombre());
            stmt.setString(3, producto.getDescripcion());
            stmt.setString(4, producto.getCategoria());
            stmt.setBigDecimal(5, producto.getPrecioVenta());
            if (producto.getPrecioCosto() != null) {
                stmt.setBigDecimal(6, producto.getPrecioCosto());
            } else {
                stmt.setNull(6, java.sql.Types.DECIMAL);
            }
            stmt.setInt(7, producto.getStockMinimo());
            stmt.setLong(8, producto.getIdProducto());
            stmt.executeUpdate();
        }
    }

    @Override
    public void cambiarEstado(Connection connection, Long idProducto, boolean activo) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(SQL_CAMBIAR_ESTADO)) {
            stmt.setString(1, activo ? "ACTIVO" : "INACTIVO");
            stmt.setLong(2, idProducto);
            stmt.executeUpdate();
        }
    }

    @Override
    public void ajustarStock(Connection connection, Long idProducto, int delta) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(SQL_AJUSTAR_STOCK)) {
            stmt.setInt(1, delta);
            stmt.setLong(2, idProducto);
            stmt.executeUpdate();
        }
    }

    @Override
    public Optional<Producto> buscarPorId(Long idProducto) throws SQLException {
        try (Connection connection = DBConnectionManager.getConnection()) {
            return buscarPorId(connection, idProducto);
        }
    }

    @Override
    public Optional<Producto> buscarPorId(Connection connection, Long idProducto) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(SQL_BUSCAR_POR_ID)) {
            stmt.setLong(1, idProducto);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        }
    }

    @Override
    public boolean tieneItemsDeVentaAsociados(Long idProducto) throws SQLException {
        try (Connection connection = DBConnectionManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(SQL_TIENE_ITEMS_VENTA)) {

            stmt.setLong(1, idProducto);
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    @Override
    public boolean tieneItemsDeTrabajoAsociados(Long idProducto) throws SQLException {
        try (Connection connection = DBConnectionManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(SQL_TIENE_ITEMS_TRABAJO)) {

            stmt.setLong(1, idProducto);
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    @Override
    public PaginaResultado<Producto> listar(String textoBusqueda, String estadoFiltro, Long proveedorId,
                                             int pagina, int registrosPorPagina) throws SQLException {

        StringBuilder where = new StringBuilder(" WHERE 1 = 1 ");
        List<Object> parametros = new ArrayList<>();

        if (textoBusqueda != null && !textoBusqueda.isBlank()) {
            where.append(" AND (p.nombre LIKE ? OR p.categoria LIKE ? OR pr.nombre LIKE ?) ");
            String comodin = "%" + textoBusqueda.trim() + "%";
            for (int i = 0; i < 3; i++) {
                parametros.add(comodin);
            }
        }
        if (estadoFiltro != null && !estadoFiltro.isBlank()) {
            where.append(" AND p.estado = ? ");
            parametros.add(estadoFiltro);
        }
        if (proveedorId != null) {
            where.append(" AND p.proveedor_id = ? ");
            parametros.add(proveedorId);
        }

        String sqlBase = " FROM productos p JOIN proveedores pr ON p.proveedor_id = pr.id_proveedor" + where;
        String sqlConteo = "SELECT COUNT(*)" + sqlBase;
        String sqlDatos = "SELECT p.*" + sqlBase + " ORDER BY p.nombre ASC LIMIT ? OFFSET ?";

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
        List<Producto> productos = new ArrayList<>();

        try (Connection connection = DBConnectionManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sqlDatos)) {
            List<Object> parametrosDatos = new ArrayList<>(parametros);
            parametrosDatos.add(registrosPorPagina);
            parametrosDatos.add(offset);
            asignarParametros(stmt, parametrosDatos);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    productos.add(mapRow(rs));
                }
            }
        }

        return new PaginaResultado<>(productos, total, paginaSegura, registrosPorPagina);
    }

    private void asignarParametros(PreparedStatement stmt, List<Object> parametros) throws SQLException {
        for (int i = 0; i < parametros.size(); i++) {
            stmt.setObject(i + 1, parametros.get(i));
        }
    }

    private Producto mapRow(ResultSet rs) throws SQLException {
        Producto producto = new Producto();
        producto.setIdProducto(rs.getLong("id_producto"));
        producto.setProveedorId(rs.getLong("proveedor_id"));
        producto.setNombre(rs.getString("nombre"));
        producto.setDescripcion(rs.getString("descripcion"));
        producto.setCategoria(rs.getString("categoria"));
        producto.setPrecioVenta(rs.getBigDecimal("precio_venta"));

        BigDecimal precioCosto = rs.getBigDecimal("precio_costo");
        producto.setPrecioCosto(rs.wasNull() ? null : precioCosto);

        producto.setStockActual(rs.getInt("stock_actual"));
        producto.setStockMinimo(rs.getInt("stock_minimo"));
        producto.setEstado(Estado.desdeValorBD(rs.getString("estado")));

        Timestamp createdAt = rs.getTimestamp("created_at");
        producto.setCreatedAt(createdAt == null ? null : createdAt.toLocalDateTime());

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        producto.setUpdatedAt(updatedAt == null ? null : updatedAt.toLocalDateTime());

        return producto;
    }
}
