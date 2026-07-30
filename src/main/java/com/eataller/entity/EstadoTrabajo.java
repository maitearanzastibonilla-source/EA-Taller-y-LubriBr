package com.eataller.entity;

public enum EstadoTrabajo {

    EN_PROCESO("En proceso"),
    FINALIZADO("Finalizado"),
    FACTURADO("Facturado");

    private final String etiqueta;

    EstadoTrabajo(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public static EstadoTrabajo desdeValorBD(String valor) {
        return EstadoTrabajo.valueOf(valor.trim().toUpperCase());
    }
}
