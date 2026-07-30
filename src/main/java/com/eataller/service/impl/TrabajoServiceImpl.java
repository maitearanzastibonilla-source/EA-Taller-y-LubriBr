package com.eataller.service.impl;

import com.eataller.config.DBConnectionManager;
import com.eataller.constants.AppConstants;
import com.eataller.dao.AuditoriaDAO;
import com.eataller.dao.ClienteDAO;
import com.eataller.dao.TrabajoDAO;
import com.eataller.dao.TurnoDAO;
import com.eataller.dao.UsuarioDAO;
import com.eataller.dao.VehiculoDAO;
import com.eataller.dao.impl.AuditoriaDAOImpl;
import com.eataller.dao.impl.ClienteDAOImpl;
import com.eataller.dao.impl.TrabajoDAOImpl;
import com.eataller.dao.impl.TurnoDAOImpl;
import com.eataller.dao.impl.UsuarioDAOImpl;
import com.eataller.dao.impl.VehiculoDAOImpl;
import com.eataller.dto.PaginaResultado;
import com.eataller.dto.TrabajoDTO;
import com.eataller.dto.TrabajoFormDTO;
import com.eataller.entity.Cliente;
import com.eataller.entity.EstadoTrabajo;
import com.eataller.entity.RolUsuario;
import com.eataller.entity.TrabajoRealizado;
import com.eataller.entity.Turno;
import com.eataller.entity.Usuario;
import com.eataller.entity.Vehiculo;
import com.eataller.entity.Auditoria;
import com.eataller.exception.TrabajoNoEncontradoException;
import com.eataller.exception.UsuarioSinPermisosException;
import com.eataller.exception.ValidacionException;
import com.eataller.exception.VehiculoNoEncontradoException;
import com.eataller.service.TrabajoService;
import com.eataller.utils.TrabajoMapper;
import com.eataller.validator.TrabajoValidator;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TrabajoServiceImpl implements TrabajoService {

    private final TrabajoDAO trabajoDAO;
    private final VehiculoDAO vehiculoDAO;
    private final ClienteDAO clienteDAO;
    private final TurnoDAO turnoDAO;
    private final UsuarioDAO usuarioDAO;
    private final AuditoriaDAO auditoriaDAO;

    public TrabajoServiceImpl() {
        this.trabajoDAO = new TrabajoDAOImpl();
        this.vehiculoDAO = new VehiculoDAOImpl();
        this.clienteDAO = new ClienteDAOImpl();
        this.turnoDAO = new TurnoDAOImpl();
        this.usuarioDAO = new UsuarioDAOImpl();
        this.auditoriaDAO = new AuditoriaDAOImpl();
    }

    @Override
    public TrabajoDTO crear(TrabajoFormDTO form, Long idUsuarioCreador)
            throws ValidacionException, VehiculoNoEncontradoException, SQLException {

        TrabajoValidator.validar(form);

        Vehiculo vehiculo = vehiculoDAO.buscarPorId(form.getVehiculoId())
                .orElseThrow(() -> new VehiculoNoEncontradoException("El vehiculo seleccionado no existe."));

        if (form.getTurnoId() != null) {
            Turno turno = turnoDAO.buscarPorId(form.getTurnoId()).orElse(null);
            if (turno == null || !turno.getVehiculoId().equals(vehiculo.getIdVehiculo())) {
                throw new ValidacionException(Map.of("turnoId", "El turno indicado no corresponde a este vehiculo."));
            }
        }

        TrabajoRealizado trabajo = new TrabajoRealizado();
        trabajo.setVehiculoId(vehiculo.getIdVehiculo());
        trabajo.setTurnoId(form.getTurnoId());
        trabajo.setUsuarioId(idUsuarioCreador);
        trabajo.setFechaIngreso(LocalDate.parse(form.getFechaIngreso()));
        trabajo.setDescripcion(form.getDescripcion().trim());
        trabajo.setEstado(EstadoTrabajo.EN_PROCESO);
        trabajo.setActivo(true);

        try (Connection connection = DBConnectionManager.getConnection()) {
            connection.setAutoCommit(false);
            try {
                trabajoDAO.crear(connection, trabajo);

                Auditoria auditoria = new Auditoria(idUsuarioCreador, AppConstants.MODULO_TRABAJOS,
                        "ALTA", "trabajos_realizados", trabajo.getIdTrabajo(), null,
                        "vehiculo_id=" + trabajo.getVehiculoId() + ", fecha_ingreso=" + trabajo.getFechaIngreso(),
                        "EXITO", "Alta de trabajo.");
                auditoriaDAO.registrar(connection, auditoria);

                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        }

        Cliente propietario = clienteDAO.buscarPorId(vehiculo.getClienteId()).orElse(null);
        return construirDTO(trabajo, vehiculo, propietario);
    }

    @Override
    public TrabajoDTO actualizar(TrabajoFormDTO form, Long idUsuarioEditor)
            throws ValidacionException, TrabajoNoEncontradoException, SQLException {

        TrabajoValidator.validar(form);

        TrabajoRealizado existente = trabajoDAO.buscarPorId(form.getIdTrabajo())
                .orElseThrow(() -> new TrabajoNoEncontradoException("No se encontro el trabajo solicitado (ID " + form.getIdTrabajo() + ")."));

        if (existente.getEstado() != EstadoTrabajo.EN_PROCESO) {
            throw new ValidacionException(Map.of("descripcion", "Solo se pueden modificar trabajos en proceso."));
        }

        String valoresAnteriores = "fecha_ingreso=" + existente.getFechaIngreso();

        existente.setFechaIngreso(LocalDate.parse(form.getFechaIngreso()));
        existente.setDescripcion(form.getDescripcion().trim());

        try (Connection connection = DBConnectionManager.getConnection()) {
            connection.setAutoCommit(false);
            try {
                trabajoDAO.actualizar(connection, existente);

                Auditoria auditoria = new Auditoria(idUsuarioEditor, AppConstants.MODULO_TRABAJOS,
                        "MODIFICACION", "trabajos_realizados", existente.getIdTrabajo(),
                        valoresAnteriores, "fecha_ingreso=" + existente.getFechaIngreso(),
                        "EXITO", "Modificacion de trabajo.");
                auditoriaDAO.registrar(connection, auditoria);

                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        }

        Vehiculo vehiculo = vehiculoDAO.buscarPorId(existente.getVehiculoId()).orElse(null);
        Cliente propietario = vehiculo == null ? null : clienteDAO.buscarPorId(vehiculo.getClienteId()).orElse(null);
        return construirDTO(existente, vehiculo, propietario);
    }

    @Override
    public TrabajoDTO obtenerPorId(Long idTrabajo) throws TrabajoNoEncontradoException, SQLException {
        TrabajoRealizado trabajo = trabajoDAO.buscarPorId(idTrabajo)
                .orElseThrow(() -> new TrabajoNoEncontradoException("No se encontro el trabajo solicitado (ID " + idTrabajo + ")."));

        Vehiculo vehiculo = vehiculoDAO.buscarPorId(trabajo.getVehiculoId()).orElse(null);
        Cliente propietario = vehiculo == null ? null : clienteDAO.buscarPorId(vehiculo.getClienteId()).orElse(null);
        return construirDTO(trabajo, vehiculo, propietario);
    }

    @Override
    public PaginaResultado<TrabajoDTO> listar(String textoBusqueda, String estadoFiltro, Long vehiculoId,
                                               int pagina, int registrosPorPagina) throws SQLException {

        PaginaResultado<TrabajoRealizado> resultado = trabajoDAO.listar(textoBusqueda, estadoFiltro, vehiculoId,
                pagina, registrosPorPagina);

        Map<Long, Vehiculo> vehiculosCache = new HashMap<>();
        Map<Long, Cliente> clientesCache = new HashMap<>();
        Map<Long, Usuario> usuariosCache = new HashMap<>();

        List<TrabajoDTO> dtos = resultado.getRegistros().stream()
                .map(t -> {
                    Vehiculo vehiculo = vehiculosCache.computeIfAbsent(t.getVehiculoId(), this::buscarVehiculoSilencioso);
                    Cliente propietario = vehiculo == null ? null
                            : clientesCache.computeIfAbsent(vehiculo.getClienteId(), this::buscarClienteSilencioso);
                    Usuario usuario = usuariosCache.computeIfAbsent(t.getUsuarioId(), this::buscarUsuarioSilencioso);
                    return TrabajoMapper.aDTO(t,
                            vehiculo != null ? vehiculo.getPatente() : "(sin datos)",
                            propietario != null ? propietario.getNombreCompleto() : "(sin datos)",
                            usuario != null ? usuario.getNombre() : "(sin datos)");
                })
                .collect(Collectors.toList());

        return new PaginaResultado<>(dtos, resultado.getTotalRegistros(), resultado.getPaginaActual(),
                resultado.getRegistrosPorPagina());
    }

    @Override
    public void finalizar(Long idTrabajo, LocalDate fechaEgreso, Long idUsuarioEditor)
            throws TrabajoNoEncontradoException, ValidacionException, SQLException {

        TrabajoRealizado trabajo = trabajoDAO.buscarPorId(idTrabajo)
                .orElseThrow(() -> new TrabajoNoEncontradoException("No se encontro el trabajo solicitado (ID " + idTrabajo + ")."));

        if (trabajo.getEstado() != EstadoTrabajo.EN_PROCESO) {
            throw new ValidacionException(Map.of("fechaEgreso", "Solo se pueden finalizar trabajos en proceso."));
        }
        if (fechaEgreso == null) {
            throw new ValidacionException(Map.of("fechaEgreso", "Debe indicar la fecha de egreso."));
        }
        if (fechaEgreso.isBefore(trabajo.getFechaIngreso())) {
            throw new ValidacionException(Map.of("fechaEgreso", "La fecha de egreso no puede ser anterior al ingreso."));
        }

        try (Connection connection = DBConnectionManager.getConnection()) {
            connection.setAutoCommit(false);
            try {
                trabajoDAO.finalizar(connection, idTrabajo, fechaEgreso);

                Auditoria auditoria = new Auditoria(idUsuarioEditor, AppConstants.MODULO_TRABAJOS,
                        "FINALIZACION", "trabajos_realizados", idTrabajo, "estado=EN_PROCESO",
                        "estado=FINALIZADO, fecha_egreso=" + fechaEgreso, "EXITO", "Cierre de trabajo.");
                auditoriaDAO.registrar(connection, auditoria);

                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        }
    }

    @Override
    public void reabrir(Long idTrabajo, Long idUsuarioEditor, RolUsuario rolEditor)
            throws TrabajoNoEncontradoException, ValidacionException, UsuarioSinPermisosException, SQLException {

        if (rolEditor != RolUsuario.ADMINISTRADOR) {
            throw new UsuarioSinPermisosException("Solo un administrador puede reabrir un trabajo finalizado.");
        }

        TrabajoRealizado trabajo = trabajoDAO.buscarPorId(idTrabajo)
                .orElseThrow(() -> new TrabajoNoEncontradoException("No se encontro el trabajo solicitado (ID " + idTrabajo + ")."));

        if (trabajo.getEstado() != EstadoTrabajo.FINALIZADO) {
            throw new ValidacionException(Map.of("descripcion", "Solo se pueden reabrir trabajos finalizados."));
        }

        try (Connection connection = DBConnectionManager.getConnection()) {
            connection.setAutoCommit(false);
            try {
                trabajoDAO.reabrir(connection, idTrabajo);

                Auditoria auditoria = new Auditoria(idUsuarioEditor, AppConstants.MODULO_TRABAJOS,
                        "REAPERTURA", "trabajos_realizados", idTrabajo, "estado=FINALIZADO",
                        "estado=EN_PROCESO", "EXITO", "Reapertura administrativa de trabajo.");
                auditoriaDAO.registrar(connection, auditoria);

                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        }
    }

    @Override
    public void darDeBaja(Long idTrabajo, Long idUsuarioEditor, RolUsuario rolEditor)
            throws TrabajoNoEncontradoException, UsuarioSinPermisosException, SQLException {

        if (rolEditor != RolUsuario.ADMINISTRADOR) {
            throw new UsuarioSinPermisosException("Solo un administrador puede dar de baja un trabajo.");
        }

        trabajoDAO.buscarPorId(idTrabajo)
                .orElseThrow(() -> new TrabajoNoEncontradoException("No se encontro el trabajo solicitado (ID " + idTrabajo + ")."));

        try (Connection connection = DBConnectionManager.getConnection()) {
            connection.setAutoCommit(false);
            try {
                trabajoDAO.cambiarActivo(connection, idTrabajo, false);

                Auditoria auditoria = new Auditoria(idUsuarioEditor, AppConstants.MODULO_TRABAJOS,
                        "BAJA_LOGICA", "trabajos_realizados", idTrabajo, "activo=true",
                        "activo=false", "EXITO", "Baja administrativa de trabajo.");
                auditoriaDAO.registrar(connection, auditoria);

                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        }
    }

    private TrabajoDTO construirDTO(TrabajoRealizado trabajo, Vehiculo vehiculo, Cliente propietario) throws SQLException {
        Usuario usuario = usuarioDAO.buscarPorId(trabajo.getUsuarioId()).orElse(null);
        return TrabajoMapper.aDTO(trabajo,
                vehiculo != null ? vehiculo.getPatente() : "(sin datos)",
                propietario != null ? propietario.getNombreCompleto() : "(sin datos)",
                usuario != null ? usuario.getNombre() : "(sin datos)");
    }

    private Vehiculo buscarVehiculoSilencioso(Long id) {
        try {
            return vehiculoDAO.buscarPorId(id).orElse(null);
        } catch (SQLException e) {
            return null;
        }
    }

    private Cliente buscarClienteSilencioso(Long id) {
        try {
            return clienteDAO.buscarPorId(id).orElse(null);
        } catch (SQLException e) {
            return null;
        }
    }

    private Usuario buscarUsuarioSilencioso(Long id) {
        try {
            return usuarioDAO.buscarPorId(id).orElse(null);
        } catch (SQLException e) {
            return null;
        }
    }
}
