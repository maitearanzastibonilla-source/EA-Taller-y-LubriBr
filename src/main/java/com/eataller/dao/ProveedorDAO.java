package com.eataller.dao;

import com.eataller.dto.PaginaResultado;
import com.eataller.entity.Proveedor;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

public interface ProveedorDAO {

    Proveedor crear(Connection connection, Proveedor proveedor) throws SQLException;

    void actualizar(Connection connection, Proveedor proveedor) throws SQLException;

    void cambiarEstado(Connection connection, Long idProveedor, boolean activo) throws SQLException;

    Optional<Proveedor> buscarPorId(Long idProveedor) throws SQLException;

    PaginaResultado<Proveedor> listar(String textoBusqueda, String estadoFiltro,
                                       int pagina, int registrosPorPagina) throws SQLException;
}
