package com.eataller.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public class ItemTrabajo {

    private Long idItem;
    private Long trabajoId;
    private Long productoId;
    private String descripcionLibre;
    private BigDecimal cantidad;
    private BigDecimal precioUnitario;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ItemTrabajo() {
    }

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

    public Long getProductoId() {
        return productoId;
    }

    public void setProductoId(Long productoId) {
        this.productoId = productoId;
    }

    public String getDescripcionLibre() {
        return descripcionLibre;
    }

    public void setDescripcionLibre(String descripcionLibre) {
        this.descripcionLibre = descripcionLibre;
    }

    public BigDecimal getCantidad() {
        return cantidad;
    }

    public void setCantidad(BigDecimal cantidad) {
        this.cantidad = cantidad;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public BigDecimal getSubtotal() {
        return cantidad.multiply(precioUnitario);
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
        if (!(o instanceof ItemTrabajo that)) {
            return false;
        }
        return Objects.equals(idItem, that.idItem);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idItem);
    }
}
