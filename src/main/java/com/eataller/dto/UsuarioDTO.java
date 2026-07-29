package com.eataller.dto;

import com.eataller.entity.RolUsuario;

import java.time.LocalDateTime;

/**
 * Responsabilidad: transportar datos de Usuario hacia la vista sin exponer
 * jamas el hash de la contrasenia. Utilizado en listados, detalle y
 * respuestas de las operaciones de alta/modificacion.
 * Autor: EA Taller y LubriBr - Equipo de Desarrollo
 */
public class UsuarioDTO {

    private Long idUsuario;
    private String nombre;
    private String email;
    private RolUsuario rol;
    private boolean activo;
    private boolean bloqueado;
    private LocalDateTime createdAt;

    public UsuarioDTO() {
    }

    public UsuarioDTO(Long idUsuario, String nombre, String email, RolUsuario rol,
                       boolean activo, boolean bloqueado, LocalDateTime createdAt) {
        this.idUsuario = idUsuario;
        this.nombre = nombre;
        this.email = email;
        this.rol = rol;
        this.activo = activo;
        this.bloqueado = bloqueado;
        this.createdAt = createdAt;
    }

    public Long getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Long idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public RolUsuario getRol() {
        return rol;
    }

    public void setRol(RolUsuario rol) {
        this.rol = rol;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public boolean isBloqueado() {
        return bloqueado;
    }

    public void setBloqueado(boolean bloqueado) {
        this.bloqueado = bloqueado;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
