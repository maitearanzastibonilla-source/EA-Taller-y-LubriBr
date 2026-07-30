package com.eataller.dto;

public class ItemTrabajoFormDTO {

    private Long idItem;
    private Long trabajoId;
    private String descripcionLibre;
    private String cantidad;
    private String precioUnitario;

    public Long getIdItem() {
        return idItem;
    }

    public void setIdItem(Long idItem) {
        this.idItem = idItem;
    }

    public Long getTrabajoId() {
        return trabajoId;
    }

    public void setTrabajoId(Long trabajoId) {
        this.trabajoId = trabajoId;
    }

    public String getDescripcionLibre() {
        return descripcionLibre;
    }

    public void setDescripcionLibre(String descripcionLibre) {
        this.descripcionLibre = descripcionLibre;
    }

    public String getCantidad() {
        return cantidad;
    }

    public void setCantidad(String cantidad) {
        this.cantidad = cantidad;
    }

    public String getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(String precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public boolean esAlta() {
        return idItem == null;
    }
}
