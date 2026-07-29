package com.eataller.utils;

import com.eataller.dto.VehiculoDTO;
import com.eataller.entity.Vehiculo;

public final class VehiculoMapper {

    private VehiculoMapper() {
    }

    public static VehiculoDTO aDTO(Vehiculo vehiculo, String propietarioNombre) {
        VehiculoDTO dto = new VehiculoDTO();
        dto.setIdVehiculo(vehiculo.getIdVehiculo());
        dto.setClienteId(vehiculo.getClienteId());
        dto.setPropietarioNombre(propietarioNombre);
        dto.setPatente(vehiculo.getPatente());
        dto.setMarca(vehiculo.getMarca());
        dto.setModelo(vehiculo.getModelo());
        dto.setAnio(vehiculo.getAnio());
        dto.setEstado(vehiculo.getEstado());
        dto.setCreatedAt(vehiculo.getCreatedAt());
        return dto;
    }
}
