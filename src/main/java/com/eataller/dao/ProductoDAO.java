package com.eataller.dao;

import com.eataller.dto.PaginaResultado;
import com.eataller.entity.Producto;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

public interface ProductoDAO {

    Producto crear(Connection connection, Producto producto) throws SQLException;

    void actualizar(Connection connection, Producto producto) throws SQLException;

    void cambiarEstado(Connection connection, Long idProducto, boolean activo) throws SQLException;

    Optional<Producto> buscarPorId(Long idProducto) throws SQLException;

    boolean tieneItemsDeTrabajoAsociados(Long idProducto) throws SQLException;

    PaginaResultado<Producto> listar(String textoBusqueda, String estadoFiltro, Long proveedorId,
                                      int pagina, int registrosPorPagina) throws SQLException;
}
