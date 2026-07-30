package com.eataller.utils;

import com.eataller.dto.ItemVentaDTO;
import com.eataller.entity.ItemVenta;

public final class ItemVentaMapper {

    private ItemVentaMapper() {
    }

    public static ItemVentaDTO aDTO(ItemVenta item, String productoNombre) {
        ItemVentaDTO dto = new ItemVentaDTO();
        dto.setIdItemVenta(item.getIdItemVenta());
        dto.setVentaId(item.getVentaId());
        dto.setProductoId(item.getProductoId());
        dto.setProductoNombre(productoNombre);
        dto.setCantidad(item.getCantidad());
        dto.setPrecioUnitario(item.getPrecioUnitario());
        dto.setSubtotal(item.getSubtotal());
        return dto;
    }
}
