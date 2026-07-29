package com.eataller.dto;

import java.util.Collections;
import java.util.List;

/**
 * Responsabilidad: encapsular un resultado paginado generico (lista de la
 * pagina actual + metadatos de paginacion), reutilizable por los listados
 * de cualquier modulo del sistema (Usuarios, Clientes, Vehiculos, etc.).
 * Autor: EA Taller y LubriBr - Equipo de Desarrollo
 */
public class PaginaResultado<T> {

    private final List<T> registros;
    private final long totalRegistros;
    private final int paginaActual;
    private final int registrosPorPagina;

    public PaginaResultado(List<T> registros, long totalRegistros, int paginaActual, int registrosPorPagina) {
        this.registros = Collections.unmodifiableList(registros);
        this.totalRegistros = totalRegistros;
        this.paginaActual = paginaActual;
        this.registrosPorPagina = registrosPorPagina;
    }

    public List<T> getRegistros() {
        return registros;
    }

    public long getTotalRegistros() {
        return totalRegistros;
    }

    public int getPaginaActual() {
        return paginaActual;
    }

    public int getRegistrosPorPagina() {
        return registrosPorPagina;
    }

    public int getTotalPaginas() {
        if (registrosPorPagina <= 0) {
            return 1;
        }
        return (int) Math.max(1, Math.ceil((double) totalRegistros / registrosPorPagina));
    }

    public boolean tieneAnterior() {
        return paginaActual > 1;
    }

    public boolean tieneSiguiente() {
        return paginaActual < getTotalPaginas();
    }
}
