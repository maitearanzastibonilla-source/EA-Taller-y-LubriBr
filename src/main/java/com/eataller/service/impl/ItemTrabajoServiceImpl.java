package com.eataller.service.impl;

import com.eataller.config.DBConnectionManager;
import com.eataller.constants.AppConstants;
import com.eataller.dao.AuditoriaDAO;
import com.eataller.dao.ItemTrabajoDAO;
import com.eataller.dao.TrabajoDAO;
import com.eataller.dao.impl.AuditoriaDAOImpl;
import com.eataller.dao.impl.ItemTrabajoDAOImpl;
import com.eataller.dao.impl.TrabajoDAOImpl;
import com.eataller.dto.ItemTrabajoDTO;
import com.eataller.dto.ItemTrabajoFormDTO;
import com.eataller.entity.Auditoria;
import com.eataller.entity.EstadoTrabajo;
import com.eataller.entity.ItemTrabajo;
import com.eataller.entity.TrabajoRealizado;
import com.eataller.exception.ItemTrabajoNoEncontradoException;
import com.eataller.exception.TrabajoNoEncontradoException;
import com.eataller.exception.ValidacionException;
import com.eataller.service.ItemTrabajoService;
import com.eataller.utils.ItemTrabajoMapper;
import com.eataller.validator.ItemTrabajoValidator;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ItemTrabajoServiceImpl implements ItemTrabajoService {

    private final ItemTrabajoDAO itemDAO;
    private final TrabajoDAO trabajoDAO;
    private final AuditoriaDAO auditoriaDAO;

    public ItemTrabajoServiceImpl() {
        this.itemDAO = new ItemTrabajoDAOImpl();
        this.trabajoDAO = new TrabajoDAOImpl();
        this.auditoriaDAO = new AuditoriaDAOImpl();
    }

    @Override
    public ItemTrabajoDTO crear(ItemTrabajoFormDTO form, Long idUsuarioCreador)
            throws ValidacionException, TrabajoNoEncontradoException, SQLException {

        ItemTrabajoValidator.validar(form);

        TrabajoRealizado trabajo = trabajoDAO.buscarPorId(form.getTrabajoId())
                .orElseThrow(() -> new TrabajoNoEncontradoException("No se encontro el trabajo indicado."));

        if (trabajo.getEstado() == EstadoTrabajo.FACTURADO) {
            throw new ValidacionException(Map.of("descripcionLibre", "No se pueden cargar items sobre un trabajo ya facturado."));
        }

        ItemTrabajo item = new ItemTrabajo();
        item.setTrabajoId(trabajo.getIdTrabajo());
        item.setDescripcionLibre(form.getDescripcionLibre().trim());
        item.setCantidad(new BigDecimal(form.getCantidad().trim()));
        item.setPrecioUnitario(new BigDecimal(form.getPrecioUnitario().trim()));

        try (Connection connection = DBConnectionManager.getConnection()) {
            connection.setAutoCommit(false);
            try {
                itemDAO.crear(connection, item);

                Auditoria auditoria = new Auditoria(idUsuarioCreador, AppConstants.MODULO_ITEMS_TRABAJO,
                        "ALTA", "items_trabajo", item.getIdItem(), null,
                        "trabajo_id=" + item.getTrabajoId() + ", descripcion=" + item.getDescripcionLibre()
                                + ", cantidad=" + item.getCantidad() + ", precio_unitario=" + item.getPrecioUnitario(),
                        "EXITO", "Alta de item de trabajo.");
                auditoriaDAO.registrar(connection, auditoria);

                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        }

        return ItemTrabajoMapper.aDTO(item);
    }

    @Override
    public ItemTrabajoDTO actualizar(ItemTrabajoFormDTO form, Long idUsuarioEditor)
            throws ValidacionException, ItemTrabajoNoEncontradoException, TrabajoNoEncontradoException, SQLException {

        ItemTrabajoValidator.validar(form);

        ItemTrabajo existente = itemDAO.buscarPorId(form.getIdItem())
                .orElseThrow(() -> new ItemTrabajoNoEncontradoException("No se encontro el item solicitado."));

        TrabajoRealizado trabajo = trabajoDAO.buscarPorId(existente.getTrabajoId())
                .orElseThrow(() -> new TrabajoNoEncontradoException("No se encontro el trabajo asociado a este item."));

        if (trabajo.getEstado() != EstadoTrabajo.EN_PROCESO) {
            throw new ValidacionException(Map.of("descripcionLibre", "Los items solo se pueden modificar mientras el trabajo este en proceso."));
        }

        String valoresAnteriores = "descripcion=" + existente.getDescripcionLibre() + ", cantidad=" + existente.getCantidad()
                + ", precio_unitario=" + existente.getPrecioUnitario();

        existente.setDescripcionLibre(form.getDescripcionLibre().trim());
        existente.setCantidad(new BigDecimal(form.getCantidad().trim()));
        existente.setPrecioUnitario(new BigDecimal(form.getPrecioUnitario().trim()));

        try (Connection connection = DBConnectionManager.getConnection()) {
            connection.setAutoCommit(false);
            try {
                itemDAO.actualizar(connection, existente);

                String valoresNuevos = "descripcion=" + existente.getDescripcionLibre() + ", cantidad=" + existente.getCantidad()
                        + ", precio_unitario=" + existente.getPrecioUnitario();
                Auditoria auditoria = new Auditoria(idUsuarioEditor, AppConstants.MODULO_ITEMS_TRABAJO,
                        "MODIFICACION", "items_trabajo", existente.getIdItem(),
                        valoresAnteriores, valoresNuevos, "EXITO", "Modificacion de item de trabajo.");
                auditoriaDAO.registrar(connection, auditoria);

                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        }

        return ItemTrabajoMapper.aDTO(existente);
    }

    @Override
    public void eliminar(Long idItem, Long idUsuarioEditor)
            throws ValidacionException, ItemTrabajoNoEncontradoException, TrabajoNoEncontradoException, SQLException {

        ItemTrabajo existente = itemDAO.buscarPorId(idItem)
                .orElseThrow(() -> new ItemTrabajoNoEncontradoException("No se encontro el item solicitado."));

        TrabajoRealizado trabajo = trabajoDAO.buscarPorId(existente.getTrabajoId())
                .orElseThrow(() -> new TrabajoNoEncontradoException("No se encontro el trabajo asociado a este item."));

        if (trabajo.getEstado() == EstadoTrabajo.FACTURADO) {
            throw new ValidacionException(Map.of("descripcionLibre", "No se pueden eliminar items de un trabajo ya facturado."));
        }

        try (Connection connection = DBConnectionManager.getConnection()) {
            connection.setAutoCommit(false);
            try {
                itemDAO.eliminar(connection, idItem);

                Auditoria auditoria = new Auditoria(idUsuarioEditor, AppConstants.MODULO_ITEMS_TRABAJO,
                        "BAJA", "items_trabajo", idItem,
                        "descripcion=" + existente.getDescripcionLibre() + ", cantidad=" + existente.getCantidad()
                                + ", precio_unitario=" + existente.getPrecioUnitario(),
                        null, "EXITO", "Eliminacion de item de trabajo.");
                auditoriaDAO.registrar(connection, auditoria);

                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        }
    }

    @Override
    public List<ItemTrabajoDTO> listarPorTrabajo(Long trabajoId) throws SQLException {
        return itemDAO.listarPorTrabajo(trabajoId).stream()
                .map(ItemTrabajoMapper::aDTO)
                .collect(Collectors.toList());
    }
}
