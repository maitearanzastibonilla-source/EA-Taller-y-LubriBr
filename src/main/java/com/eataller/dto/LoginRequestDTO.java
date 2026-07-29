package com.eataller.dto;

/**
 * Responsabilidad: transportar las credenciales enviadas desde el formulario
 * de inicio de sesion hacia la capa de servicio de autenticacion.
 * Autor: EA Taller y LubriBr - Equipo de Desarrollo
 */
public class LoginRequestDTO {

    private String email;
    private String password;

    public LoginRequestDTO() {
    }

    public LoginRequestDTO(String email, String password) {
        this.email = email;
        this.password = password;
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
}
