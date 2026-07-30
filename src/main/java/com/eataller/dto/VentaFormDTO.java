package com.eataller.dto;

public class VentaFormDTO {

    private Long idVenta;
    private String metodoPago;

    public Long getIdVenta() {
        return idVenta;
    }

    public void setIdVenta(Long idVenta) {
        this.idVenta = idVenta;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }

    public boolean esAlta() {
        return idVenta == null;
    }
}
