package com.eataller.dto;

public class CompraFormDTO {

    private Long idCompra;
    private Long proveedorId;

    public Long getIdCompra() {
        return idCompra;
    }

    public void setIdCompra(Long idCompra) {
        this.idCompra = idCompra;
    }

    public Long getProveedorId() {
        return proveedorId;
    }

    public void setProveedorId(Long proveedorId) {
        this.proveedorId = proveedorId;
    }

    public boolean esAlta() {
        return idCompra == null;
    }
}
