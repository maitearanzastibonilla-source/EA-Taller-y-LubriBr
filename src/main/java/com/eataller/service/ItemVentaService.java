package com.eataller.service;

import com.eataller.dto.ItemVentaDTO;
import com.eataller.dto.ItemVentaFormDTO;
import com.eataller.exception.ItemVentaNoEncontradoException;
import com.eataller.exception.ProductoNoEncontradoException;
import com.eataller.exception.ValidacionException;
import com.eataller.exception.VentaNoEncontradaException;

import java.sql.SQLException;
import java.util.List;

public interface ItemVentaService {

    ItemVentaDTO crear(ItemVentaFormDTO form, Long idUsuarioCreador)
            throws ValidacionException, VentaNoEncontradaException, ProductoNoEncontradoException, SQLException;

    ItemVentaDTO actualizar(ItemVentaFormDTO form, Long idUsuarioEditor)
            throws ValidacionException, ItemVentaNoEncontradoException, VentaNoEncontradaException,
            ProductoNoEncontradoException, SQLException;

    void eliminar(Long idItemVenta, Long idUsuarioEditor)
            throws ValidacionException, ItemVentaNoEncontradoException, VentaNoEncontradaException, SQLException;

    List<ItemVentaDTO> listarPorVenta(Long ventaId) throws SQLException;
}
