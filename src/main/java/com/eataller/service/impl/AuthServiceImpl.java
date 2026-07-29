package com.eataller.service.impl;

import com.eataller.config.DBConnectionManager;
import com.eataller.constants.AppConstants;
import com.eataller.dao.AuditoriaDAO;
import com.eataller.dao.UsuarioDAO;
import com.eataller.dao.impl.AuditoriaDAOImpl;
import com.eataller.dao.impl.UsuarioDAOImpl;
import com.eataller.dto.LoginRequestDTO;
import com.eataller.dto.UsuarioDTO;
import com.eataller.entity.Auditoria;
import com.eataller.entity.Usuario;
import com.eataller.exception.CredencialesInvalidasException;
import com.eataller.exception.CuentaBloqueadaException;
import com.eataller.service.AuthService;
import com.eataller.utils.PasswordHasher;
import com.eataller.utils.UsuarioMapper;
import com.eataller.utils.ValidationUtils;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * Responsabilidad: implementar AuthService. Verifica credenciales contra la
 * tabla usuarios, aplica la politica de bloqueo por intentos fallidos y
 * registra en auditoria cada intento de inicio de sesion (exitoso o no).
 * Autor: EA Taller y LubriBr - Equipo de Desarrollo
 */
public class AuthServiceImpl implements AuthService {

    private static final String MENSAJE_CREDENCIALES_INVALIDAS = "El email o la contrasenia ingresados son incorrectos.";
    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm");

    private final UsuarioDAO usuarioDAO;
    private final AuditoriaDAO auditoriaDAO;

    public AuthServiceImpl() {
        this.usuarioDAO = new UsuarioDAOImpl();
        this.auditoriaDAO = new AuditoriaDAOImpl();
    }

    @Override
    public UsuarioDTO autenticar(LoginRequestDTO loginRequest) throws CredencialesInvalidasException,
            CuentaBloqueadaException, SQLException {

        if (ValidationUtils.esVacio(loginRequest.getEmail()) || ValidationUtils.esVacio(loginRequest.getPassword())) {
            throw new CredencialesInvalidasException("Debe ingresar email y contrasenia.");
        }

        String emailNormalizado = loginRequest.getEmail().trim().toLowerCase();
        Optional<Usuario> usuarioOpt = usuarioDAO.buscarPorEmail(emailNormalizado);

        if (usuarioOpt.isEmpty()) {
            registrarAuditoriaLogin(null, "FALLO", "Email no registrado: " + emailNormalizado);
            throw new CredencialesInvalidasException(MENSAJE_CREDENCIALES_INVALIDAS);
        }

        Usuario usuario = usuarioOpt.get();

        if (usuario.estaBloqueado()) {
            String hora = usuario.getBloqueadoHasta().format(FORMATO_HORA);
            throw new CuentaBloqueadaException("La cuenta se encuentra bloqueada por intentos fallidos. "
                    + "Intente nuevamente despues de las " + hora + ".");
        }

        if (!usuario.isActivo()) {
            registrarAuditoriaLogin(usuario.getIdUsuario(), "FALLO", "Usuario inactivo.");
            throw new CredencialesInvalidasException("El usuario se encuentra inactivo. Contacte al administrador.");
        }

        if (!PasswordHasher.verificar(loginRequest.getPassword(), usuario.getPasswordHash())) {
            procesarIntentoFallido(usuario);
            throw new CredencialesInvalidasException(MENSAJE_CREDENCIALES_INVALIDAS);
        }

        try (Connection connection = DBConnectionManager.getConnection()) {
            connection.setAutoCommit(false);
            try {
                usuarioDAO.resetearIntentosFallidos(connection, usuario.getIdUsuario());
                registrarAuditoriaLogin(connection, usuario.getIdUsuario(), "EXITO", "Inicio de sesion correcto.");
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        }

        return UsuarioMapper.aDTO(usuario);
    }

    private void procesarIntentoFallido(Usuario usuario) throws SQLException {
        int nuevosIntentos = usuario.getIntentosFallidos() + 1;
        LocalDateTime bloqueadoHasta = null;
        String observacion = "Intento fallido numero " + nuevosIntentos + ".";

        if (nuevosIntentos >= AppConstants.MAX_INTENTOS_FALLIDOS) {
            bloqueadoHasta = LocalDateTime.now().plusMinutes(AppConstants.MINUTOS_BLOQUEO_CUENTA);
            observacion = "Cuenta bloqueada por " + AppConstants.MINUTOS_BLOQUEO_CUENTA
                    + " minutos tras " + nuevosIntentos + " intentos fallidos.";
        }

        try (Connection connection = DBConnectionManager.getConnection()) {
            connection.setAutoCommit(false);
            try {
                usuarioDAO.registrarIntentoFallido(connection, usuario.getIdUsuario(), nuevosIntentos, bloqueadoHasta);
                registrarAuditoriaLogin(connection, usuario.getIdUsuario(), "FALLO", observacion);
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        }
    }

    private void registrarAuditoriaLogin(Long idUsuario, String resultado, String observaciones) throws SQLException {
        try (Connection connection = DBConnectionManager.getConnection()) {
            registrarAuditoriaLogin(connection, idUsuario, resultado, observaciones);
        }
    }

    private void registrarAuditoriaLogin(Connection connection, Long idUsuario, String resultado, String observaciones)
            throws SQLException {
        Auditoria auditoria = new Auditoria(idUsuario, AppConstants.MODULO_AUTENTICACION,
                "LOGIN", "usuarios", idUsuario, null, null, resultado, observaciones);
        auditoriaDAO.registrar(connection, auditoria);
    }
}
