package com.eataller.entity;

public enum EstadoVenta {

    PENDIENTE("Pendiente"),
    CONFIRMADA("Confirmada"),
    ANULADA("Anulada");

    private final String etiqueta;

    EstadoVenta(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public static EstadoVenta desdeValorBD(String valor) {
        return EstadoVenta.valueOf(valor.trim().toUpperCase());
    }
}
