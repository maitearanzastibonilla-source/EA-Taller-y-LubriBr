package com.eataller.utils;

import com.eataller.dto.ProductoDTO;
import com.eataller.entity.Producto;

public final class ProductoMapper {

    private ProductoMapper() {
    }

    public static ProductoDTO aDTO(Producto producto, String proveedorNombre) {
        ProductoDTO dto = new ProductoDTO();
        dto.setIdProducto(producto.getIdProducto());
        dto.setProveedorId(producto.getProveedorId());
        dto.setProveedorNombre(proveedorNombre);
        dto.setNombre(producto.getNombre());
        dto.setDescripcion(producto.getDescripcion());
        dto.setCategoria(producto.getCategoria());
        dto.setPrecioVenta(producto.getPrecioVenta());
        dto.setPrecioCosto(producto.getPrecioCosto());
        dto.setStockActual(producto.getStockActual());
        dto.setStockMinimo(producto.getStockMinimo());
        dto.setEstado(producto.getEstado());
        dto.setCreatedAt(producto.getCreatedAt());
        return dto;
    }
}
