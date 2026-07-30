package com.eataller.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public class ItemVenta {

    private Long idItemVenta;
    private Long ventaId;
    private Long productoId;
    private int cantidad;
    private BigDecimal precioUnitario;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ItemVenta() {
    }

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

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public BigDecimal getSubtotal() {
        return precioUnitario.multiply(BigDecimal.valueOf(cantidad));
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ItemVenta that)) {
            return false;
        }
        return Objects.equals(idItemVenta, that.idItemVenta);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idItemVenta);
    }
}
