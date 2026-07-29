package com.eataller.entity;

import java.time.LocalDateTime;

/**
 * Responsabilidad: representar un registro de la tabla auditoria. Toda accion
 * relevante del sistema (altas, modificaciones, bajas logicas, cambios de
 * estado, inicios de sesion) queda trazada mediante esta entidad, conforme al
 * requerimiento transversal de auditoria de la Propuesta Tecnica.
 * Autor: EA Taller y LubriBr - Equipo de Desarrollo
 */
public class Auditoria {

    private Long idAuditoria;
    private Long usuarioId;
    private String modulo;
    private String accion;
    private String entidadAfectada;
    private Long idRegistroAfectado;
    private String valoresAnteriores;
    private String valoresNuevos;
    private String resultado;
    private String observaciones;
    private LocalDateTime fechaHora;

    public Auditoria() {
    }

    public Auditoria(Long usuarioId, String modulo, String accion, String entidadAfectada,
                      Long idRegistroAfectado, String valoresAnteriores, String valoresNuevos,
                      String resultado, String observaciones) {
        this.usuarioId = usuarioId;
        this.modulo = modulo;
        this.accion = accion;
        this.entidadAfectada = entidadAfectada;
        this.idRegistroAfectado = idRegistroAfectado;
        this.valoresAnteriores = valoresAnteriores;
        this.valoresNuevos = valoresNuevos;
        this.resultado = resultado;
        this.observaciones = observaciones;
    }

    public Long getIdAuditoria() {
        return idAuditoria;
    }

    public void setIdAuditoria(Long idAuditoria) {
        this.idAuditoria = idAuditoria;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getModulo() {
        return modulo;
    }

    public void setModulo(String modulo) {
        this.modulo = modulo;
    }

    public String getAccion() {
        return accion;
    }

    public void setAccion(String accion) {
        this.accion = accion;
    }

    public String getEntidadAfectada() {
        return entidadAfectada;
    }

    public void setEntidadAfectada(String entidadAfectada) {
        this.entidadAfectada = entidadAfectada;
    }

    public Long getIdRegistroAfectado() {
        return idRegistroAfectado;
    }

    public void setIdRegistroAfectado(Long idRegistroAfectado) {
        this.idRegistroAfectado = idRegistroAfectado;
    }

    public String getValoresAnteriores() {
        return valoresAnteriores;
    }

    public void setValoresAnteriores(String valoresAnteriores) {
        this.valoresAnteriores = valoresAnteriores;
    }

    public String getValoresNuevos() {
        return valoresNuevos;
    }

    public void setValoresNuevos(String valoresNuevos) {
        this.valoresNuevos = valoresNuevos;
    }

    public String getResultado() {
        return resultado;
    }

    public void setResultado(String resultado) {
        this.resultado = resultado;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }
}
