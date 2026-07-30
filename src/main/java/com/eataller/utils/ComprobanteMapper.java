package com.eataller.utils;

import com.eataller.dto.ComprobanteDTO;
import com.eataller.entity.Comprobante;

public final class ComprobanteMapper {

    private ComprobanteMapper() {
    }

    public static ComprobanteDTO aDTO(Comprobante comprobante, String vehiculoPatente, String clienteNombre) {
        ComprobanteDTO dto = new ComprobanteDTO();
        dto.setIdComprobante(comprobante.getIdComprobante());
        dto.setTrabajoId(comprobante.getTrabajoId());
        dto.setVehiculoPatente(vehiculoPatente);
        dto.setClienteNombre(clienteNombre);
        dto.setFecha(comprobante.getFecha());
        dto.setTotal(comprobante.getTotal());
        dto.setMetodoPago(comprobante.getMetodoPago());
        dto.setEstado(comprobante.getEstado());
        dto.setPdfUrl(comprobante.getPdfUrl());
        return dto;
    }
}
