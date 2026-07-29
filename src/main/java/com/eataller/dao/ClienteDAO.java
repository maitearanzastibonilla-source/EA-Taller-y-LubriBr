package com.eataller.dao;

import com.eataller.dto.PaginaResultado;
import com.eataller.entity.Cliente;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

public interface ClienteDAO {

    Cliente crear(Connection connection, Cliente cliente) throws SQLException;

    void actualizar(Connection connection, Cliente cliente) throws SQLException;

    void cambiarEstado(Connection connection, Long idCliente, boolean activo) throws SQLException;

    Optional<Cliente> buscarPorId(Long idCliente) throws SQLException;

    boolean existeDni(String dni, Long idClienteExcluido) throws SQLException;

    boolean existeTelefono(String telefono, Long idClienteExcluido) throws SQLException;

    PaginaResultado<Cliente> listar(String textoBusqueda, String estadoFiltro,
                                     int pagina, int registrosPorPagina) throws SQLException;
}
