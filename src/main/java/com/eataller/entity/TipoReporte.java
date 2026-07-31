package com.eataller.entity;

public enum TipoReporte {

    TRABAJOS_POR_PERIODO("Trabajos realizados por periodo"),
    INGRESOS("Ingresos por ventas y servicios"),
    MOVIMIENTOS_STOCK("Movimientos de stock"),
    COMPRAS_POR_PROVEEDOR("Compras realizadas por proveedor"),
    TURNOS_POR_ESTADO("Turnos por estado y periodo"),
    RANKING_CLIENTES("Ranking de clientes con mayor actividad");

    private final String etiqueta;

    TipoReporte(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public static TipoReporte desdeValorBD(String valor) {
        return TipoReporte.valueOf(valor.trim().toUpperCase());
    }
}
