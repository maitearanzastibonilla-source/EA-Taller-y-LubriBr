package com.eataller.entity;

public enum EstadoTurno {

    PENDIENTE("Pendiente"),
    CONFIRMADO("Confirmado"),
    EN_PROCESO("En proceso"),
    FINALIZADO("Finalizado"),
    CANCELADO("Cancelado");

    private final String etiqueta;

    EstadoTurno(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public boolean esTerminal() {
        return this == FINALIZADO || this == CANCELADO;
    }

    public static EstadoTurno desdeValorBD(String valor) {
        return EstadoTurno.valueOf(valor.trim().toUpperCase());
    }
}
