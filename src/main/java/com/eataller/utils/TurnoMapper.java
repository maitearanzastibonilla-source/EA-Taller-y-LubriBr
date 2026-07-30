package com.eataller.utils;

import com.eataller.dto.TurnoDTO;
import com.eataller.entity.Turno;

public final class TurnoMapper {

    private TurnoMapper() {
    }

    public static TurnoDTO aDTO(Turno turno, String clienteNombre, String vehiculoPatente, String usuarioNombre) {
        TurnoDTO dto = new TurnoDTO();
        dto.setIdTurno(turno.getIdTurno());
        dto.setClienteId(turno.getClienteId());
        dto.setClienteNombre(clienteNombre);
        dto.setVehiculoId(turno.getVehiculoId());
        dto.setVehiculoPatente(vehiculoPatente);
        dto.setUsuarioId(turno.getUsuarioId());
        dto.setUsuarioNombre(usuarioNombre);
        dto.setFechaHora(turno.getFechaHora());
        dto.setTipoServicio(turno.getTipoServicio());
        dto.setEstado(turno.getEstado());
        dto.setNotas(turno.getNotas());
        dto.setCreatedAt(turno.getCreatedAt());
        return dto;
    }
}
