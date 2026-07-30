package com.eataller.utils;

import com.eataller.dto.ProveedorDTO;
import com.eataller.entity.Proveedor;

public final class ProveedorMapper {

    private ProveedorMapper() {
    }

    public static ProveedorDTO aDTO(Proveedor proveedor) {
        ProveedorDTO dto = new ProveedorDTO();
        dto.setIdProveedor(proveedor.getIdProveedor());
        dto.setNombre(proveedor.getNombre());
        dto.setContacto(proveedor.getContacto());
        dto.setTelefono(proveedor.getTelefono());
        dto.setEmail(proveedor.getEmail());
        dto.setEstado(proveedor.getEstado());
        dto.setCreatedAt(proveedor.getCreatedAt());
        return dto;
    }
}
