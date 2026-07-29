package com.eataller.dao;

import com.eataller.dto.PaginaResultado;
import com.eataller.entity.Vehiculo;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

public interface VehiculoDAO {

    Vehiculo crear(Connection connection, Vehiculo vehiculo) throws SQLException;

    void actualizar(Connection connection, Vehiculo vehiculo) throws SQLException;

    void cambiarEstado(Connection connection, Long idVehiculo, boolean activo) throws SQLException;

    Optional<Vehiculo> buscarPorId(Long idVehiculo) throws SQLException;

    boolean existePatente(String patente, Long idVehiculoExcluido) throws SQLException;

    PaginaResultado<Vehiculo> listar(String textoBusqueda, String estadoFiltro, Long clienteId,
                                      int pagina, int registrosPorPagina) throws SQLException;
}
