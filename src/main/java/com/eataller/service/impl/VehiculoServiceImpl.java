package com.eataller.service.impl;

import com.eataller.config.DBConnectionManager;
import com.eataller.constants.AppConstants;
import com.eataller.dao.AuditoriaDAO;
import com.eataller.dao.ClienteDAO;
import com.eataller.dao.VehiculoDAO;
import com.eataller.dao.impl.AuditoriaDAOImpl;
import com.eataller.dao.impl.ClienteDAOImpl;
import com.eataller.dao.impl.VehiculoDAOImpl;
import com.eataller.dto.PaginaResultado;
import com.eataller.dto.VehiculoDTO;
import com.eataller.dto.VehiculoFormDTO;
import com.eataller.entity.Auditoria;
import com.eataller.entity.Cliente;
import com.eataller.entity.Estado;
import com.eataller.entity.RolUsuario;
import com.eataller.entity.Vehiculo;
import com.eataller.exception.ClienteNoEncontradoException;
import com.eataller.exception.DatoDuplicadoException;
import com.eataller.exception.UsuarioSinPermisosException;
import com.eataller.exception.ValidacionException;
import com.eataller.exception.VehiculoNoEncontradoException;
import com.eataller.service.VehiculoService;
import com.eataller.utils.VehiculoMapper;
import com.eataller.validator.VehiculoValidator;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class VehiculoServiceImpl implements VehiculoService {

    private static final int CODIGO_MYSQL_ENTRADA_DUPLICADA = 1062;

    private final VehiculoDAO vehiculoDAO;
    private final ClienteDAO clienteDAO;
    private final AuditoriaDAO auditoriaDAO;

    public VehiculoServiceImpl() {
        this.vehiculoDAO = new VehiculoDAOImpl();
        this.clienteDAO = new ClienteDAOImpl();
        this.auditoriaDAO = new AuditoriaDAOImpl();
    }

    @Override
    public VehiculoDTO crear(VehiculoFormDTO form, Long idUsuarioCreador)
            throws ValidacionException, DatoDuplicadoException, ClienteNoEncontradoException, SQLException {

        VehiculoValidator.validar(form);

        Cliente propietario = clienteDAO.buscarPorId(form.getClienteId())
                .orElseThrow(() -> new ClienteNoEncontradoException("El cliente seleccionado no existe."));

        String patente = form.getPatente().trim().toUpperCase();
        if (vehiculoDAO.existePatente(patente, null)) {
            throw new DatoDuplicadoException("Ya existe un vehiculo registrado con esa patente.");
        }

        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setClienteId(propietario.getIdCliente());
        vehiculo.setPatente(patente);
        vehiculo.setMarca(form.getMarca().trim());
        vehiculo.setModelo(form.getModelo().trim());
        vehiculo.setAnio(Integer.parseInt(form.getAnio().trim()));
        vehiculo.setEstado(Estado.ACTIVO);

        try (Connection connection = DBConnectionManager.getConnection()) {
            connection.setAutoCommit(false);
            try {
                vehiculoDAO.crear(connection, vehiculo);

                Auditoria auditoria = new Auditoria(idUsuarioCreador, AppConstants.MODULO_VEHICULOS,
                        "ALTA", "vehiculos", vehiculo.getIdVehiculo(), null,
                        "patente=" + vehiculo.getPatente() + ", cliente_id=" + vehiculo.getClienteId(),
                        "EXITO", "Alta de vehiculo.");
                auditoriaDAO.registrar(connection, auditoria);

                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw traducirErrorDuplicado(e);
            }
        }

        return VehiculoMapper.aDTO(vehiculo, propietario.getNombreCompleto());
    }

    @Override
    public VehiculoDTO actualizar(VehiculoFormDTO form, Long idUsuarioEditor, RolUsuario rolEditor)
            throws ValidacionException, DatoDuplicadoException, ClienteNoEncontradoException,
            VehiculoNoEncontradoException, UsuarioSinPermisosException, SQLException {

        VehiculoValidator.validar(form);

        Vehiculo existente = vehiculoDAO.buscarPorId(form.getIdVehiculo())
                .orElseThrow(() -> new VehiculoNoEncontradoException(
                        "No se encontro el vehiculo solicitado (ID " + form.getIdVehiculo() + ")."));

        String patente = form.getPatente().trim().toUpperCase();
        if (!patente.equals(existente.getPatente()) && rolEditor != RolUsuario.ADMINISTRADOR) {
            throw new UsuarioSinPermisosException("Solo un administrador puede modificar la patente de un vehiculo.");
        }

        Cliente propietario = clienteDAO.buscarPorId(form.getClienteId())
                .orElseThrow(() -> new ClienteNoEncontradoException("El cliente seleccionado no existe."));

        if (vehiculoDAO.existePatente(patente, form.getIdVehiculo())) {
            throw new DatoDuplicadoException("Ya existe otro vehiculo registrado con esa patente.");
        }

        String valoresAnteriores = "patente=" + existente.getPatente() + ", marca=" + existente.getMarca()
                + ", modelo=" + existente.getModelo();

        existente.setClienteId(propietario.getIdCliente());
        existente.setPatente(patente);
        existente.setMarca(form.getMarca().trim());
        existente.setModelo(form.getModelo().trim());
        existente.setAnio(Integer.parseInt(form.getAnio().trim()));

        try (Connection connection = DBConnectionManager.getConnection()) {
            connection.setAutoCommit(false);
            try {
                vehiculoDAO.actualizar(connection, existente);

                String valoresNuevos = "patente=" + existente.getPatente() + ", marca=" + existente.getMarca()
                        + ", modelo=" + existente.getModelo();
                Auditoria auditoria = new Auditoria(idUsuarioEditor, AppConstants.MODULO_VEHICULOS,
                        "MODIFICACION", "vehiculos", existente.getIdVehiculo(),
                        valoresAnteriores, valoresNuevos, "EXITO", "Modificacion de datos de vehiculo.");
                auditoriaDAO.registrar(connection, auditoria);

                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw traducirErrorDuplicado(e);
            }
        }

        return VehiculoMapper.aDTO(existente, propietario.getNombreCompleto());
    }

    @Override
    public VehiculoDTO obtenerPorId(Long idVehiculo) throws VehiculoNoEncontradoException, SQLException {
        Vehiculo vehiculo = vehiculoDAO.buscarPorId(idVehiculo)
                .orElseThrow(() -> new VehiculoNoEncontradoException("No se encontro el vehiculo solicitado (ID " + idVehiculo + ")."));
        String propietarioNombre = obtenerNombrePropietario(vehiculo.getClienteId());
        return VehiculoMapper.aDTO(vehiculo, propietarioNombre);
    }

    @Override
    public PaginaResultado<VehiculoDTO> listar(String textoBusqueda, String estadoFiltro, Long clienteId,
                                                int pagina, int registrosPorPagina) throws SQLException {

        PaginaResultado<Vehiculo> resultado = vehiculoDAO.listar(textoBusqueda, estadoFiltro, clienteId,
                pagina, registrosPorPagina);

        Map<Long, String> nombresPorCliente = new HashMap<>();
        List<VehiculoDTO> dtos = resultado.getRegistros().stream()
                .map(v -> VehiculoMapper.aDTO(v, nombresPorCliente.computeIfAbsent(v.getClienteId(),
                        id -> obtenerNombrePropietarioSilencioso(id))))
                .collect(Collectors.toList());

        return new PaginaResultado<>(dtos, resultado.getTotalRegistros(), resultado.getPaginaActual(),
                resultado.getRegistrosPorPagina());
    }

    @Override
    public void cambiarEstado(Long idVehiculo, boolean activo, Long idUsuarioEditor)
            throws VehiculoNoEncontradoException, SQLException {

        Vehiculo vehiculo = vehiculoDAO.buscarPorId(idVehiculo)
                .orElseThrow(() -> new VehiculoNoEncontradoException("No se encontro el vehiculo solicitado (ID " + idVehiculo + ")."));

        try (Connection connection = DBConnectionManager.getConnection()) {
            connection.setAutoCommit(false);
            try {
                vehiculoDAO.cambiarEstado(connection, idVehiculo, activo);

                Auditoria auditoria = new Auditoria(idUsuarioEditor, AppConstants.MODULO_VEHICULOS,
                        activo ? "ACTIVACION" : "BAJA_LOGICA", "vehiculos", idVehiculo,
                        "estado=" + vehiculo.getEstado(), "estado=" + (activo ? "ACTIVO" : "INACTIVO"),
                        "EXITO", "Cambio de estado de vehiculo.");
                auditoriaDAO.registrar(connection, auditoria);

                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        }
    }

    private String obtenerNombrePropietario(Long clienteId) throws SQLException {
        return clienteDAO.buscarPorId(clienteId).map(Cliente::getNombreCompleto).orElse("(cliente eliminado)");
    }

    private String obtenerNombrePropietarioSilencioso(Long clienteId) {
        try {
            return obtenerNombrePropietario(clienteId);
        } catch (SQLException e) {
            return "(sin datos)";
        }
    }

    private SQLException traducirErrorDuplicado(SQLException e) {
        if (e.getErrorCode() == CODIGO_MYSQL_ENTRADA_DUPLICADA) {
            return new SQLException("La patente ingresada ya se encuentra registrada.", e);
        }
        return e;
    }
}
