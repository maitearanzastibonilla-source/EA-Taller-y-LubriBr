package com.eataller.service;

import com.eataller.dto.PaginaResultado;
import com.eataller.dto.ProductoDTO;
import com.eataller.dto.ProductoFormDTO;
import com.eataller.exception.ProductoNoEncontradoException;
import com.eataller.exception.ProveedorNoEncontradoException;
import com.eataller.exception.ValidacionException;

import java.sql.SQLException;

public interface ProductoService {

    ProductoDTO crear(ProductoFormDTO form, Long idUsuarioCreador)
            throws ValidacionException, ProveedorNoEncontradoException, SQLException;

    ProductoDTO actualizar(ProductoFormDTO form, Long idUsuarioEditor)
            throws ValidacionException, ProductoNoEncontradoException, ProveedorNoEncontradoException, SQLException;

    ProductoDTO obtenerPorId(Long idProducto) throws ProductoNoEncontradoException, SQLException;

    PaginaResultado<ProductoDTO> listar(String textoBusqueda, String estadoFiltro, Long proveedorId,
                                         int pagina, int registrosPorPagina) throws SQLException;

    void cambiarEstado(Long idProducto, boolean activo, Long idUsuarioEditor)
            throws ProductoNoEncontradoException, ValidacionException, SQLException;
}
