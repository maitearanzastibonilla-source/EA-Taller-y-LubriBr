package com.eataller.entity;

public enum EstadoCliente {

    ACTIVO("Activo"),
    INACTIVO("Inactivo");

    private final String etiqueta;

    EstadoCliente(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public static EstadoCliente desdeValorBD(String valor) {
        return EstadoCliente.valueOf(valor.trim().toUpperCase());
    }
}
