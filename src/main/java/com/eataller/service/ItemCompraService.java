package com.eataller.service;

import com.eataller.dto.ItemCompraDTO;
import com.eataller.dto.ItemCompraFormDTO;
import com.eataller.exception.CompraNoEncontradaException;
import com.eataller.exception.ItemCompraNoEncontradoException;
import com.eataller.exception.ProductoNoEncontradoException;
import com.eataller.exception.ValidacionException;

import java.sql.SQLException;
import java.util.List;

public interface ItemCompraService {

    ItemCompraDTO crear(ItemCompraFormDTO form, Long idUsuarioCreador)
            throws ValidacionException, CompraNoEncontradaException, ProductoNoEncontradoException, SQLException;

    ItemCompraDTO actualizar(ItemCompraFormDTO form, Long idUsuarioEditor)
            throws ValidacionException, ItemCompraNoEncontradoException, CompraNoEncontradaException,
            ProductoNoEncontradoException, SQLException;

    void eliminar(Long idItemCompra, Long idUsuarioEditor)
            throws ValidacionException, ItemCompraNoEncontradoException, CompraNoEncontradaException, SQLException;

    List<ItemCompraDTO> listarPorCompra(Long compraId) throws SQLException;
}
