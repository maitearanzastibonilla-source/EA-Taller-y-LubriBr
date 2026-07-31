package com.eataller.utils;

import com.eataller.dto.CompraDTO;
import com.eataller.entity.Compra;

public final class CompraMapper {

    private CompraMapper() {
    }

    public static CompraDTO aDTO(Compra compra, String proveedorNombre, String usuarioNombre) {
        CompraDTO dto = new CompraDTO();
        dto.setIdCompra(compra.getIdCompra());
        dto.setProveedorId(compra.getProveedorId());
        dto.setProveedorNombre(proveedorNombre);
        dto.setUsuarioId(compra.getUsuarioId());
        dto.setUsuarioNombre(usuarioNombre);
        dto.setFecha(compra.getFecha());
        dto.setTotal(compra.getTotal());
        dto.setEstado(compra.getEstado());
        return dto;
    }
}
