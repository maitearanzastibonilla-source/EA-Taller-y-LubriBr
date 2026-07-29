package com.eataller.dto;

/**
 * Responsabilidad: transportar los datos ingresados en los formularios de
 * alta y modificacion de usuario (vista -> Servlet -> Service). La
 * contrasenia en texto plano solo vive en este objeto durante el
 * procesamiento del request; nunca se persiste ni se loguea.
 * Autor: EA Taller y LubriBr - Equipo de Desarrollo
 */
public class UsuarioFormDTO {

    private Long idUsuario;
    private String nombre;
    private String email;
    private String password;
    private String confirmarPassword;
    private String rol;

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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmarPassword() {
        return confirmarPassword;
    }

    public void setConfirmarPassword(String confirmarPassword) {
        this.confirmarPassword = confirmarPassword;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public boolean esAlta() {
        return idUsuario == null;
    }
}
