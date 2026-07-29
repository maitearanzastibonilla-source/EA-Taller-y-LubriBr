package com.eataller.service;

import com.eataller.dto.LoginRequestDTO;
import com.eataller.dto.UsuarioDTO;
import com.eataller.exception.CredencialesInvalidasException;
import com.eataller.exception.CuentaBloqueadaException;

import java.sql.SQLException;

/**
 * Responsabilidad: implementar la logica de autenticacion (verificacion de
 * credenciales, control de intentos fallidos y bloqueo temporal de cuentas).
 * Autor: EA Taller y LubriBr - Equipo de Desarrollo
 */
public interface AuthService {

    UsuarioDTO autenticar(LoginRequestDTO loginRequest) throws CredencialesInvalidasException,
            CuentaBloqueadaException, SQLException;
}
