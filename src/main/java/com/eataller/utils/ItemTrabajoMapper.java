package com.eataller.utils;

import com.eataller.dto.ItemTrabajoDTO;
import com.eataller.entity.ItemTrabajo;

public final class ItemTrabajoMapper {

    private ItemTrabajoMapper() {
    }

    public static ItemTrabajoDTO aDTO(ItemTrabajo item) {
        ItemTrabajoDTO dto = new ItemTrabajoDTO();
        dto.setIdItem(item.getIdItem());
        dto.setTrabajoId(item.getTrabajoId());
        dto.setProductoId(item.getProductoId());
        dto.setDescripcionLibre(item.getDescripcionLibre());
        dto.setCantidad(item.getCantidad());
        dto.setPrecioUnitario(item.getPrecioUnitario());
        dto.setSubtotal(item.getSubtotal());
        return dto;
    }
}
