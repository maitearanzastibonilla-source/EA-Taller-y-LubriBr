package com.eataller.dto;

import com.eataller.entity.TipoReporte;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ReporteDTO {

    private Long idReporte;
    private TipoReporte tipoReporte;
    private LocalDateTime fechaGeneracion;
    private Long usuarioGeneradorId;
    private String usuarioGeneradorNombre;
    private String formato;
    private LocalDate fechaDesde;
    private LocalDate fechaHasta;
    private String pdfUrl;

    public Long getIdReporte() {
        return idReporte;
    }

    public void setIdReporte(Long idReporte) {
        this.idReporte = idReporte;
    }

    public TipoReporte getTipoReporte() {
        return tipoReporte;
    }

    public void setTipoReporte(TipoReporte tipoReporte) {
        this.tipoReporte = tipoReporte;
    }

    public LocalDateTime getFechaGeneracion() {
        return fechaGeneracion;
    }

    public void setFechaGeneracion(LocalDateTime fechaGeneracion) {
        this.fechaGeneracion = fechaGeneracion;
    }

    public Long getUsuarioGeneradorId() {
        return usuarioGeneradorId;
    }

    public void setUsuarioGeneradorId(Long usuarioGeneradorId) {
        this.usuarioGeneradorId = usuarioGeneradorId;
    }

    public String getUsuarioGeneradorNombre() {
        return usuarioGeneradorNombre;
    }

    public void setUsuarioGeneradorNombre(String usuarioGeneradorNombre) {
        this.usuarioGeneradorNombre = usuarioGeneradorNombre;
    }

    public String getFormato() {
        return formato;
    }

    public void setFormato(String formato) {
        this.formato = formato;
    }

    public LocalDate getFechaDesde() {
        return fechaDesde;
    }

    public void setFechaDesde(LocalDate fechaDesde) {
        this.fechaDesde = fechaDesde;
    }

    public LocalDate getFechaHasta() {
        return fechaHasta;
    }

    public void setFechaHasta(LocalDate fechaHasta) {
        this.fechaHasta = fechaHasta;
    }

    public String getPdfUrl() {
        return pdfUrl;
    }

    public void setPdfUrl(String pdfUrl) {
        this.pdfUrl = pdfUrl;
    }
}
