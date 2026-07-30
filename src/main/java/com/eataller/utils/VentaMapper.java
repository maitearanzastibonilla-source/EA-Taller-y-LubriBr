package com.eataller.utils;

import com.eataller.dto.VentaDTO;
import com.eataller.entity.VentaDirecta;

public final class VentaMapper {

    private VentaMapper() {
    }

    public static VentaDTO aDTO(VentaDirecta venta, String usuarioNombre) {
        VentaDTO dto = new VentaDTO();
        dto.setIdVenta(venta.getIdVenta());
        dto.setUsuarioId(venta.getUsuarioId());
        dto.setUsuarioNombre(usuarioNombre);
        dto.setFecha(venta.getFecha());
        dto.setTotal(venta.getTotal());
        dto.setMetodoPago(venta.getMetodoPago());
        dto.setEstado(venta.getEstado());
        return dto;
    }
}
