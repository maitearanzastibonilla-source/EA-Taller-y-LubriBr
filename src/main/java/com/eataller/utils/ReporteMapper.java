package com.eataller.utils;

import com.eataller.dto.ReporteDTO;
import com.eataller.entity.Reporte;

public final class ReporteMapper {

    private ReporteMapper() {
    }

    public static ReporteDTO aDTO(Reporte reporte, String usuarioGeneradorNombre) {
        ReporteDTO dto = new ReporteDTO();
        dto.setIdReporte(reporte.getIdReporte());
        dto.setTipoReporte(reporte.getTipoReporte());
        dto.setFechaGeneracion(reporte.getFechaGeneracion());
        dto.setUsuarioGeneradorId(reporte.getUsuarioGeneradorId());
        dto.setUsuarioGeneradorNombre(usuarioGeneradorNombre);
        dto.setFormato(reporte.getFormato());
        dto.setFechaDesde(reporte.getFechaDesde());
        dto.setFechaHasta(reporte.getFechaHasta());
        dto.setPdfUrl(reporte.getPdfUrl());
        return dto;
    }
}
