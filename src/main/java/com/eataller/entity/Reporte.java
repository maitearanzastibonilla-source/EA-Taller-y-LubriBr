package com.eataller.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

public class Reporte {

    private Long idReporte;
    private TipoReporte tipoReporte;
    private LocalDateTime fechaGeneracion;
    private Long usuarioGeneradorId;
    private String formato;
    private LocalDate fechaDesde;
    private LocalDate fechaHasta;
    private String pdfUrl;

    public Reporte() {
    }

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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Reporte reporte)) {
            return false;
        }
        return Objects.equals(idReporte, reporte.idReporte);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idReporte);
    }
}
