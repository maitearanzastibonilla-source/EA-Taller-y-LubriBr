package com.eataller.utils;

import com.eataller.dto.ItemCompraDTO;
import com.eataller.entity.ItemCompra;

public final class ItemCompraMapper {

    private ItemCompraMapper() {
    }

    public static ItemCompraDTO aDTO(ItemCompra item, String productoNombre) {
        ItemCompraDTO dto = new ItemCompraDTO();
        dto.setIdItemCompra(item.getIdItemCompra());
        dto.setCompraId(item.getCompraId());
        dto.setProductoId(item.getProductoId());
        dto.setProductoNombre(productoNombre);
        dto.setCantidad(item.getCantidad());
        dto.setPrecioUnitario(item.getPrecioUnitario());
        dto.setSubtotal(item.getSubtotal());
        return dto;
    }
}
