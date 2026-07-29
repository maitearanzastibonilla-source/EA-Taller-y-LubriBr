package com.eataller.service.impl;

import com.eataller.config.DBConnectionManager;
import com.eataller.constants.AppConstants;
import com.eataller.dao.AuditoriaDAO;
import com.eataller.dao.UsuarioDAO;
import com.eataller.dao.impl.AuditoriaDAOImpl;
import com.eataller.dao.impl.UsuarioDAOImpl;
import com.eataller.dto.PaginaResultado;
import com.eataller.dto.UsuarioDTO;
import com.eataller.dto.UsuarioFormDTO;
import com.eataller.entity.Auditoria;
import com.eataller.entity.RolUsuario;
import com.eataller.entity.Usuario;
import com.eataller.exception.EmailDuplicadoException;
import com.eataller.exception.UsuarioNoEncontradoException;
import com.eataller.exception.UsuarioSinPermisosException;
import com.eataller.exception.ValidacionException;
import com.eataller.service.UsuarioService;
import com.eataller.utils.PasswordHasher;
import com.eataller.utils.UsuarioMapper;
import com.eataller.validator.UsuarioValidator;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Responsabilidad: implementar UsuarioService. Coordina las transacciones
 * (usuario + auditoria) y traduce los errores de persistencia en
 * excepciones de negocio especificas.
 * Autor: EA Taller y LubriBr - Equipo de Desarrollo
 */
public class UsuarioServiceImpl implements UsuarioService {

    private static final int CODIGO_MYSQL_ENTRADA_DUPLICADA = 1062;

    private final UsuarioDAO usuarioDAO;
    private final AuditoriaDAO auditoriaDAO;

    public UsuarioServiceImpl() {
        this.usuarioDAO = new UsuarioDAOImpl();
        this.auditoriaDAO = new AuditoriaDAOImpl();
    }

    @Override
    public UsuarioDTO crear(UsuarioFormDTO form, Long idUsuarioCreador)
            throws ValidacionException, EmailDuplicadoException, SQLException {

        UsuarioValidator.validar(form, true);

        String emailNormalizado = form.getEmail().trim().toLowerCase();
        if (usuarioDAO.existeEmail(emailNormalizado, null)) {
            throw new EmailDuplicadoException("Ya existe un usuario registrado con ese correo electronico.");
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(form.getNombre().trim());
        usuario.setEmail(emailNormalizado);
        usuario.setPasswordHash(PasswordHasher.hash(form.getPassword()));
        usuario.setRol(RolUsuario.desdeValorBD(form.getRol()));
        usuario.setActivo(true);

        try (Connection connection = DBConnectionManager.getConnection()) {
            connection.setAutoCommit(false);
            try {
                usuarioDAO.crear(connection, usuario);

                Auditoria auditoria = new Auditoria(idUsuarioCreador, AppConstants.MODULO_USUARIOS,
                        "ALTA", "usuarios", usuario.getIdUsuario(), null,
                        "nombre=" + usuario.getNombre() + ", email=" + usuario.getEmail() + ", rol=" + usuario.getRol(),
                        "EXITO", "Alta de usuario del sistema.");
                auditoriaDAO.registrar(connection, auditoria);

                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw traducirErrorDuplicado(e);
            }
        }

        return UsuarioMapper.aDTO(usuario);
    }

    @Override
    public UsuarioDTO actualizar(UsuarioFormDTO form, Long idUsuarioEditor)
            throws ValidacionException, EmailDuplicadoException, UsuarioNoEncontradoException,
            UsuarioSinPermisosException, SQLException {

        UsuarioValidator.validar(form, false);

        Usuario usuarioExistente = usuarioDAO.buscarPorId(form.getIdUsuario())
                .orElseThrow(() -> new UsuarioNoEncontradoException(
                        "No se encontro el usuario solicitado (ID " + form.getIdUsuario() + ")."));

        RolUsuario nuevoRol = RolUsuario.desdeValorBD(form.getRol());
        if (form.getIdUsuario().equals(idUsuarioEditor) && nuevoRol != usuarioExistente.getRol()) {
            throw new UsuarioSinPermisosException("Un usuario no puede modificar su propio rol.");
        }

        String emailNormalizado = form.getEmail().trim().toLowerCase();
        if (usuarioDAO.existeEmail(emailNormalizado, form.getIdUsuario())) {
            throw new EmailDuplicadoException("Ya existe otro usuario registrado con ese correo electronico.");
        }

        String valoresAnteriores = "nombre=" + usuarioExistente.getNombre() + ", email=" + usuarioExistente.getEmail()
                + ", rol=" + usuarioExistente.getRol();

        usuarioExistente.setNombre(form.getNombre().trim());
        usuarioExistente.setEmail(emailNormalizado);
        usuarioExistente.setRol(nuevoRol);
        if (!form.getPassword().isBlank()) {
            usuarioExistente.setPasswordHash(PasswordHasher.hash(form.getPassword()));
        }

        try (Connection connection = DBConnectionManager.getConnection()) {
            connection.setAutoCommit(false);
            try {
                usuarioDAO.actualizar(connection, usuarioExistente);

                String valoresNuevos = "nombre=" + usuarioExistente.getNombre() + ", email=" + usuarioExistente.getEmail()
                        + ", rol=" + usuarioExistente.getRol();
                Auditoria auditoria = new Auditoria(idUsuarioEditor, AppConstants.MODULO_USUARIOS,
                        "MODIFICACION", "usuarios", usuarioExistente.getIdUsuario(),
                        valoresAnteriores, valoresNuevos, "EXITO", "Modificacion de datos de usuario.");
                auditoriaDAO.registrar(connection, auditoria);

                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw traducirErrorDuplicado(e);
            }
        }

        return UsuarioMapper.aDTO(usuarioExistente);
    }

    @Override
    public UsuarioDTO obtenerPorId(Long idUsuario) throws UsuarioNoEncontradoException, SQLException {
        Usuario usuario = usuarioDAO.buscarPorId(idUsuario)
                .orElseThrow(() -> new UsuarioNoEncontradoException("No se encontro el usuario solicitado (ID " + idUsuario + ")."));
        return UsuarioMapper.aDTO(usuario);
    }

    @Override
    public PaginaResultado<UsuarioDTO> listar(String textoBusqueda, String rolFiltro, Boolean activoFiltro,
                                               int pagina, int registrosPorPagina) throws SQLException {

        PaginaResultado<Usuario> resultado = usuarioDAO.listar(textoBusqueda, rolFiltro, activoFiltro,
                pagina, registrosPorPagina);

        List<UsuarioDTO> dtos = resultado.getRegistros().stream()
                .map(UsuarioMapper::aDTO)
                .collect(Collectors.toList());

        return new PaginaResultado<>(dtos, resultado.getTotalRegistros(), resultado.getPaginaActual(),
                resultado.getRegistrosPorPagina());
    }

    @Override
    public void cambiarEstado(Long idUsuario, boolean activo, Long idUsuarioEditor)
            throws UsuarioNoEncontradoException, UsuarioSinPermisosException, SQLException {

        Usuario usuario = usuarioDAO.buscarPorId(idUsuario)
                .orElseThrow(() -> new UsuarioNoEncontradoException("No se encontro el usuario solicitado (ID " + idUsuario + ")."));

        if (idUsuario.equals(idUsuarioEditor) && !activo) {
            throw new UsuarioSinPermisosException("Un usuario no puede desactivar su propia cuenta.");
        }

        try (Connection connection = DBConnectionManager.getConnection()) {
            connection.setAutoCommit(false);
            try {
                usuarioDAO.cambiarEstado(connection, idUsuario, activo);

                Auditoria auditoria = new Auditoria(idUsuarioEditor, AppConstants.MODULO_USUARIOS,
                        activo ? "ACTIVACION" : "BAJA_LOGICA", "usuarios", idUsuario,
                        "activo=" + usuario.isActivo(), "activo=" + activo, "EXITO",
                        "Cambio de estado de usuario.");
                auditoriaDAO.registrar(connection, auditoria);

                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        }
    }

    private SQLException traducirErrorDuplicado(SQLException e) {
        if (e.getErrorCode() == CODIGO_MYSQL_ENTRADA_DUPLICADA) {
            return new SQLException("El correo electronico ya se encuentra registrado.", e);
        }
        return e;
    }
}
