package com.eataller.dto;

import java.util.List;

public class ReporteDatos {

    private final List<String> columnas;
    private final List<List<String>> filas;
    private final List<String> resumen;

    public ReporteDatos(List<String> columnas, List<List<String>> filas, List<String> resumen) {
        this.columnas = columnas;
        this.filas = filas;
        this.resumen = resumen;
    }

    public List<String> getColumnas() {
        return columnas;
    }

    public List<List<String>> getFilas() {
        return filas;
    }

    public List<String> getResumen() {
        return resumen;
    }
}
