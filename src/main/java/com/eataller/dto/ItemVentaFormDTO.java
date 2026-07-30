package com.eataller.dto;

public class ItemVentaFormDTO {

    private Long idItemVenta;
    private Long ventaId;
    private Long productoId;
    private String cantidad;
    private String precioUnitario;

    public Long getIdItemVenta() {
        return idItemVenta;
    }

    public void setIdItemVenta(Long idItemVenta) {
        this.idItemVenta = idItemVenta;
    }

    public Long getVentaId() {
        return ventaId;
    }

    public void setVentaId(Long ventaId) {
        this.ventaId = ventaId;
    }

    public Long getProductoId() {
        return productoId;
    }

    public void setProductoId(Long productoId) {
        this.productoId = productoId;
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
        return idItemVenta == null;
    }
}
