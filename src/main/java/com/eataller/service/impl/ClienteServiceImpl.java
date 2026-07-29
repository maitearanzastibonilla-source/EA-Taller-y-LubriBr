package com.eataller.service.impl;

import com.eataller.config.DBConnectionManager;
import com.eataller.constants.AppConstants;
import com.eataller.dao.AuditoriaDAO;
import com.eataller.dao.ClienteDAO;
import com.eataller.dao.impl.AuditoriaDAOImpl;
import com.eataller.dao.impl.ClienteDAOImpl;
import com.eataller.dto.ClienteDTO;
import com.eataller.dto.ClienteFormDTO;
import com.eataller.dto.PaginaResultado;
import com.eataller.entity.Auditoria;
import com.eataller.entity.Cliente;
import com.eataller.entity.EstadoCliente;
import com.eataller.entity.RolUsuario;
import com.eataller.exception.ClienteNoEncontradoException;
import com.eataller.exception.DatoDuplicadoException;
import com.eataller.exception.UsuarioSinPermisosException;
import com.eataller.exception.ValidacionException;
import com.eataller.service.ClienteService;
import com.eataller.utils.ClienteMapper;
import com.eataller.validator.ClienteValidator;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class ClienteServiceImpl implements ClienteService {

    private static final int CODIGO_MYSQL_ENTRADA_DUPLICADA = 1062;

    private final ClienteDAO clienteDAO;
    private final AuditoriaDAO auditoriaDAO;

    public ClienteServiceImpl() {
        this.clienteDAO = new ClienteDAOImpl();
        this.auditoriaDAO = new AuditoriaDAOImpl();
    }

    @Override
    public ClienteDTO crear(ClienteFormDTO form, Long idUsuarioCreador)
            throws ValidacionException, DatoDuplicadoException, SQLException {

        ClienteValidator.validar(form);

        String dni = form.getDni().trim();
        String telefono = form.getTelefono().trim();

        if (clienteDAO.existeDni(dni, null)) {
            throw new DatoDuplicadoException("Ya existe un cliente registrado con ese DNI.");
        }
        if (clienteDAO.existeTelefono(telefono, null)) {
            throw new DatoDuplicadoException("Ya existe un cliente registrado con ese telefono.");
        }

        Cliente cliente = new Cliente();
        cliente.setNombre(form.getNombre().trim());
        cliente.setApellido(form.getApellido().trim());
        cliente.setDni(dni);
        cliente.setEmail(vacioComoNulo(form.getEmail()));
        cliente.setTelefono(telefono);
        cliente.setDireccion(vacioComoNulo(form.getDireccion()));
        cliente.setEstado(EstadoCliente.ACTIVO);

        try (Connection connection = DBConnectionManager.getConnection()) {
            connection.setAutoCommit(false);
            try {
                clienteDAO.crear(connection, cliente);

                Auditoria auditoria = new Auditoria(idUsuarioCreador, AppConstants.MODULO_CLIENTES,
                        "ALTA", "clientes", cliente.getIdCliente(), null,
                        "nombre=" + cliente.getNombreCompleto() + ", dni=" + cliente.getDni(),
                        "EXITO", "Alta de cliente.");
                auditoriaDAO.registrar(connection, auditoria);

                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw traducirErrorDuplicado(e);
            }
        }

        return ClienteMapper.aDTO(cliente);
    }

    @Override
    public ClienteDTO actualizar(ClienteFormDTO form, Long idUsuarioEditor, RolUsuario rolEditor)
            throws ValidacionException, DatoDuplicadoException, ClienteNoEncontradoException,
            UsuarioSinPermisosException, SQLException {

        ClienteValidator.validar(form);

        Cliente existente = clienteDAO.buscarPorId(form.getIdCliente())
                .orElseThrow(() -> new ClienteNoEncontradoException(
                        "No se encontro el cliente solicitado (ID " + form.getIdCliente() + ")."));

        String dni = form.getDni().trim();
        if (!dni.equals(existente.getDni()) && rolEditor != RolUsuario.ADMINISTRADOR) {
            throw new UsuarioSinPermisosException("Solo un administrador puede modificar el DNI de un cliente.");
        }

        String telefono = form.getTelefono().trim();
        if (clienteDAO.existeDni(dni, form.getIdCliente())) {
            throw new DatoDuplicadoException("Ya existe otro cliente registrado con ese DNI.");
        }
        if (clienteDAO.existeTelefono(telefono, form.getIdCliente())) {
            throw new DatoDuplicadoException("Ya existe otro cliente registrado con ese telefono.");
        }

        String valoresAnteriores = "nombre=" + existente.getNombreCompleto() + ", dni=" + existente.getDni()
                + ", telefono=" + existente.getTelefono();

        existente.setNombre(form.getNombre().trim());
        existente.setApellido(form.getApellido().trim());
        existente.setDni(dni);
        existente.setEmail(vacioComoNulo(form.getEmail()));
        existente.setTelefono(telefono);
        existente.setDireccion(vacioComoNulo(form.getDireccion()));

        try (Connection connection = DBConnectionManager.getConnection()) {
            connection.setAutoCommit(false);
            try {
                clienteDAO.actualizar(connection, existente);

                String valoresNuevos = "nombre=" + existente.getNombreCompleto() + ", dni=" + existente.getDni()
                        + ", telefono=" + existente.getTelefono();
                Auditoria auditoria = new Auditoria(idUsuarioEditor, AppConstants.MODULO_CLIENTES,
                        "MODIFICACION", "clientes", existente.getIdCliente(),
                        valoresAnteriores, valoresNuevos, "EXITO", "Modificacion de datos de cliente.");
                auditoriaDAO.registrar(connection, auditoria);

                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw traducirErrorDuplicado(e);
            }
        }

        return ClienteMapper.aDTO(existente);
    }

    @Override
    public ClienteDTO obtenerPorId(Long idCliente) throws ClienteNoEncontradoException, SQLException {
        Cliente cliente = clienteDAO.buscarPorId(idCliente)
                .orElseThrow(() -> new ClienteNoEncontradoException("No se encontro el cliente solicitado (ID " + idCliente + ")."));
        return ClienteMapper.aDTO(cliente);
    }

    @Override
    public PaginaResultado<ClienteDTO> listar(String textoBusqueda, String estadoFiltro,
                                               int pagina, int registrosPorPagina) throws SQLException {

        PaginaResultado<Cliente> resultado = clienteDAO.listar(textoBusqueda, estadoFiltro, pagina, registrosPorPagina);

        List<ClienteDTO> dtos = resultado.getRegistros().stream()
                .map(ClienteMapper::aDTO)
                .collect(Collectors.toList());

        return new PaginaResultado<>(dtos, resultado.getTotalRegistros(), resultado.getPaginaActual(),
                resultado.getRegistrosPorPagina());
    }

    @Override
    public void cambiarEstado(Long idCliente, boolean activo, Long idUsuarioEditor, RolUsuario rolEditor)
            throws ClienteNoEncontradoException, UsuarioSinPermisosException, SQLException {

        if (rolEditor != RolUsuario.ADMINISTRADOR) {
            throw new UsuarioSinPermisosException("Solo un administrador puede dar de baja a un cliente.");
        }

        Cliente cliente = clienteDAO.buscarPorId(idCliente)
                .orElseThrow(() -> new ClienteNoEncontradoException("No se encontro el cliente solicitado (ID " + idCliente + ")."));

        try (Connection connection = DBConnectionManager.getConnection()) {
            connection.setAutoCommit(false);
            try {
                clienteDAO.cambiarEstado(connection, idCliente, activo);

                Auditoria auditoria = new Auditoria(idUsuarioEditor, AppConstants.MODULO_CLIENTES,
                        activo ? "ACTIVACION" : "BAJA_LOGICA", "clientes", idCliente,
                        "estado=" + cliente.getEstado(), "estado=" + (activo ? "ACTIVO" : "INACTIVO"),
                        "EXITO", "Cambio de estado de cliente.");
                auditoriaDAO.registrar(connection, auditoria);

                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        }
    }

    private String vacioComoNulo(String valor) {
        return (valor == null || valor.isBlank()) ? null : valor.trim();
    }

    private SQLException traducirErrorDuplicado(SQLException e) {
        if (e.getErrorCode() == CODIGO_MYSQL_ENTRADA_DUPLICADA) {
            return new SQLException("El DNI ingresado ya se encuentra registrado.", e);
        }
        return e;
    }
}
