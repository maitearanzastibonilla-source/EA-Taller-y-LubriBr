package com.eataller.entity;

public enum MetodoPago {

    EFECTIVO("Efectivo"),
    TRANSFERENCIA("Transferencia"),
    TARJETA("Tarjeta"),
    OTRO("Otro");

    private final String etiqueta;

    MetodoPago(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public static MetodoPago desdeValorBD(String valor) {
        return MetodoPago.valueOf(valor.trim().toUpperCase());
    }
}
