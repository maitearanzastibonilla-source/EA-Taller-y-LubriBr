package com.eataller.utils;

import com.eataller.dto.TrabajoDTO;
import com.eataller.entity.TrabajoRealizado;

public final class TrabajoMapper {

    private TrabajoMapper() {
    }

    public static TrabajoDTO aDTO(TrabajoRealizado trabajo, String vehiculoPatente, String propietarioNombre,
                                   String usuarioNombre) {
        TrabajoDTO dto = new TrabajoDTO();
        dto.setIdTrabajo(trabajo.getIdTrabajo());
        dto.setVehiculoId(trabajo.getVehiculoId());
        dto.setVehiculoPatente(vehiculoPatente);
        dto.setPropietarioNombre(propietarioNombre);
        dto.setTurnoId(trabajo.getTurnoId());
        dto.setUsuarioId(trabajo.getUsuarioId());
        dto.setUsuarioNombre(usuarioNombre);
        dto.setFechaIngreso(trabajo.getFechaIngreso());
        dto.setFechaEgreso(trabajo.getFechaEgreso());
        dto.setDescripcion(trabajo.getDescripcion());
        dto.setEstado(trabajo.getEstado());
        dto.setActivo(trabajo.isActivo());
        dto.setCreatedAt(trabajo.getCreatedAt());
        return dto;
    }
}
