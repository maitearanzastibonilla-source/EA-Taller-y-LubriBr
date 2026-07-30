package com.eataller.entity;

public enum TipoServicio {

    MECANICA("Mecanica"),
    LUBRICENTRO("Lubricentro"),
    OTRO("Otro");

    private final String etiqueta;

    TipoServicio(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public static TipoServicio desdeValorBD(String valor) {
        return TipoServicio.valueOf(valor.trim().toUpperCase());
    }
}
