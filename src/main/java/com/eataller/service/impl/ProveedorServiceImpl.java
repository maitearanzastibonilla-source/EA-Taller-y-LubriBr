package com.eataller.service.impl;

import com.eataller.config.DBConnectionManager;
import com.eataller.constants.AppConstants;
import com.eataller.dao.AuditoriaDAO;
import com.eataller.dao.ProveedorDAO;
import com.eataller.dao.impl.AuditoriaDAOImpl;
import com.eataller.dao.impl.ProveedorDAOImpl;
import com.eataller.dto.PaginaResultado;
import com.eataller.dto.ProveedorDTO;
import com.eataller.dto.ProveedorFormDTO;
import com.eataller.entity.Auditoria;
import com.eataller.entity.Estado;
import com.eataller.entity.Proveedor;
import com.eataller.exception.ProveedorNoEncontradoException;
import com.eataller.exception.ValidacionException;
import com.eataller.service.ProveedorService;
import com.eataller.utils.ProveedorMapper;
import com.eataller.validator.ProveedorValidator;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class ProveedorServiceImpl implements ProveedorService {

    private final ProveedorDAO proveedorDAO;
    private final AuditoriaDAO auditoriaDAO;

    public ProveedorServiceImpl() {
        this.proveedorDAO = new ProveedorDAOImpl();
        this.auditoriaDAO = new AuditoriaDAOImpl();
    }

    @Override
    public ProveedorDTO crear(ProveedorFormDTO form, Long idUsuarioCreador) throws ValidacionException, SQLException {
        ProveedorValidator.validar(form);

        Proveedor proveedor = new Proveedor();
        proveedor.setNombre(form.getNombre().trim());
        proveedor.setContacto(vacioComoNulo(form.getContacto()));
        proveedor.setTelefono(form.getTelefono().trim());
        proveedor.setEmail(vacioComoNulo(form.getEmail()));
        proveedor.setEstado(Estado.ACTIVO);

        try (Connection connection = DBConnectionManager.getConnection()) {
            connection.setAutoCommit(false);
            try {
                proveedorDAO.crear(connection, proveedor);

                Auditoria auditoria = new Auditoria(idUsuarioCreador, AppConstants.MODULO_PROVEEDORES,
                        "ALTA", "proveedores", proveedor.getIdProveedor(), null,
                        "nombre=" + proveedor.getNombre() + ", telefono=" + proveedor.getTelefono(),
                        "EXITO", "Alta de proveedor.");
                auditoriaDAO.registrar(connection, auditoria);

                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        }

        return ProveedorMapper.aDTO(proveedor);
    }

    @Override
    public ProveedorDTO actualizar(ProveedorFormDTO form, Long idUsuarioEditor)
            throws ValidacionException, ProveedorNoEncontradoException, SQLException {

        ProveedorValidator.validar(form);

        Proveedor existente = proveedorDAO.buscarPorId(form.getIdProveedor())
                .orElseThrow(() -> new ProveedorNoEncontradoException(
                        "No se encontro el proveedor solicitado (ID " + form.getIdProveedor() + ")."));

        String valoresAnteriores = "nombre=" + existente.getNombre() + ", telefono=" + existente.getTelefono();

        existente.setNombre(form.getNombre().trim());
        existente.setContacto(vacioComoNulo(form.getContacto()));
        existente.setTelefono(form.getTelefono().trim());
        existente.setEmail(vacioComoNulo(form.getEmail()));

        try (Connection connection = DBConnectionManager.getConnection()) {
            connection.setAutoCommit(false);
            try {
                proveedorDAO.actualizar(connection, existente);

                String valoresNuevos = "nombre=" + existente.getNombre() + ", telefono=" + existente.getTelefono();
                Auditoria auditoria = new Auditoria(idUsuarioEditor, AppConstants.MODULO_PROVEEDORES,
                        "MODIFICACION", "proveedores", existente.getIdProveedor(),
                        valoresAnteriores, valoresNuevos, "EXITO", "Modificacion de datos de proveedor.");
                auditoriaDAO.registrar(connection, auditoria);

                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        }

        return ProveedorMapper.aDTO(existente);
    }

    @Override
    public ProveedorDTO obtenerPorId(Long idProveedor) throws ProveedorNoEncontradoException, SQLException {
        Proveedor proveedor = proveedorDAO.buscarPorId(idProveedor)
                .orElseThrow(() -> new ProveedorNoEncontradoException(
                        "No se encontro el proveedor solicitado (ID " + idProveedor + ")."));
        return ProveedorMapper.aDTO(proveedor);
    }

    @Override
    public PaginaResultado<ProveedorDTO> listar(String textoBusqueda, String estadoFiltro,
                                                 int pagina, int registrosPorPagina) throws SQLException {

        PaginaResultado<Proveedor> resultado = proveedorDAO.listar(textoBusqueda, estadoFiltro, pagina, registrosPorPagina);

        List<ProveedorDTO> dtos = resultado.getRegistros().stream()
                .map(ProveedorMapper::aDTO)
                .collect(Collectors.toList());

        return new PaginaResultado<>(dtos, resultado.getTotalRegistros(), resultado.getPaginaActual(),
                resultado.getRegistrosPorPagina());
    }

    @Override
    public void cambiarEstado(Long idProveedor, boolean activo, Long idUsuarioEditor)
            throws ProveedorNoEncontradoException, SQLException {

        Proveedor proveedor = proveedorDAO.buscarPorId(idProveedor)
                .orElseThrow(() -> new ProveedorNoEncontradoException(
                        "No se encontro el proveedor solicitado (ID " + idProveedor + ")."));

        try (Connection connection = DBConnectionManager.getConnection()) {
            connection.setAutoCommit(false);
            try {
                proveedorDAO.cambiarEstado(connection, idProveedor, activo);

                Auditoria auditoria = new Auditoria(idUsuarioEditor, AppConstants.MODULO_PROVEEDORES,
                        activo ? "ACTIVACION" : "BAJA_LOGICA", "proveedores", idProveedor,
                        "estado=" + proveedor.getEstado(), "estado=" + (activo ? "ACTIVO" : "INACTIVO"),
                        "EXITO", "Cambio de estado de proveedor.");
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
}
