package com.eataller.entity;

public enum EstadoComprobante {

    PENDIENTE("Pendiente"),
    SENADO("Senado"),
    COBRADO("Cobrado"),
    ANULADO("Anulado");

    private final String etiqueta;

    EstadoComprobante(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public static EstadoComprobante desdeValorBD(String valor) {
        return EstadoComprobante.valueOf(valor.trim().toUpperCase());
    }
}
