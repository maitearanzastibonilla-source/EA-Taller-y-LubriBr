package com.eataller.entity;

public enum EstadoCompra {

    PENDIENTE("Pendiente"),
    CONFIRMADA("Confirmada"),
    ANULADA("Anulada");

    private final String etiqueta;

    EstadoCompra(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public static EstadoCompra desdeValorBD(String valor) {
        return EstadoCompra.valueOf(valor.trim().toUpperCase());
    }
}
