package com.eataller.entity;

public enum Estado {

    ACTIVO("Activo"),
    INACTIVO("Inactivo");

    private final String etiqueta;

    Estado(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public static Estado desdeValorBD(String valor) {
        return Estado.valueOf(valor.trim().toUpperCase());
    }
}
