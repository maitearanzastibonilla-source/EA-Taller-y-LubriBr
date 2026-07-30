package com.eataller.dto;

import com.eataller.entity.EstadoComprobante;
import com.eataller.entity.MetodoPago;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ComprobanteDTO {

    private Long idComprobante;
    private Long trabajoId;
    private String vehiculoPatente;
    private String clienteNombre;
    private LocalDate fecha;
    private BigDecimal total;
    private MetodoPago metodoPago;
    private EstadoComprobante estado;
    private String pdfUrl;

    public Long getIdComprobante() {
        return idComprobante;
    }

    public void setIdComprobante(Long idComprobante) {
        this.idComprobante = idComprobante;
    }

    public Long getTrabajoId() {
        return trabajoId;
    }

    public void setTrabajoId(Long trabajoId) {
        this.trabajoId = trabajoId;
    }

    public String getVehiculoPatente() {
        return vehiculoPatente;
    }

    public void setVehiculoPatente(String vehiculoPatente) {
        this.vehiculoPatente = vehiculoPatente;
    }

    public String getClienteNombre() {
        return clienteNombre;
    }

    public void setClienteNombre(String clienteNombre) {
        this.clienteNombre = clienteNombre;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public MetodoPago getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(MetodoPago metodoPago) {
        this.metodoPago = metodoPago;
    }

    public EstadoComprobante getEstado() {
        return estado;
    }

    public void setEstado(EstadoComprobante estado) {
        this.estado = estado;
    }

    public String getPdfUrl() {
        return pdfUrl;
    }

    public void setPdfUrl(String pdfUrl) {
        this.pdfUrl = pdfUrl;
    }
}
