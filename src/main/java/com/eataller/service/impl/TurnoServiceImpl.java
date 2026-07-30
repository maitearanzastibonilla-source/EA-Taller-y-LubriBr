package com.eataller.service.impl;

import com.eataller.config.DBConnectionManager;
import com.eataller.constants.AppConstants;
import com.eataller.dao.AuditoriaDAO;
import com.eataller.dao.ClienteDAO;
import com.eataller.dao.TurnoDAO;
import com.eataller.dao.UsuarioDAO;
import com.eataller.dao.VehiculoDAO;
import com.eataller.dao.impl.AuditoriaDAOImpl;
import com.eataller.dao.impl.ClienteDAOImpl;
import com.eataller.dao.impl.TurnoDAOImpl;
import com.eataller.dao.impl.UsuarioDAOImpl;
import com.eataller.dao.impl.VehiculoDAOImpl;
import com.eataller.dto.PaginaResultado;
import com.eataller.dto.TurnoDTO;
import com.eataller.dto.TurnoFormDTO;
import com.eataller.entity.Auditoria;
import com.eataller.entity.Cliente;
import com.eataller.entity.EstadoTurno;
import com.eataller.entity.TipoServicio;
import com.eataller.entity.Turno;
import com.eataller.entity.Usuario;
import com.eataller.entity.Vehiculo;
import com.eataller.exception.ClienteNoEncontradoException;
import com.eataller.exception.TurnoNoEncontradoException;
import com.eataller.exception.TurnoOcupadoException;
import com.eataller.exception.ValidacionException;
import com.eataller.exception.VehiculoNoEncontradoException;
import com.eataller.service.TurnoService;
import com.eataller.utils.TurnoMapper;
import com.eataller.validator.TurnoValidator;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TurnoServiceImpl implements TurnoService {

    private final TurnoDAO turnoDAO;
    private final ClienteDAO clienteDAO;
    private final VehiculoDAO vehiculoDAO;
    private final UsuarioDAO usuarioDAO;
    private final AuditoriaDAO auditoriaDAO;

    public TurnoServiceImpl() {
        this.turnoDAO = new TurnoDAOImpl();
        this.clienteDAO = new ClienteDAOImpl();
        this.vehiculoDAO = new VehiculoDAOImpl();
        this.usuarioDAO = new UsuarioDAOImpl();
        this.auditoriaDAO = new AuditoriaDAOImpl();
    }

    @Override
    public TurnoDTO crear(TurnoFormDTO form, Long idUsuarioCreador)
            throws ValidacionException, ClienteNoEncontradoException, VehiculoNoEncontradoException,
            TurnoOcupadoException, SQLException {

        TurnoValidator.validar(form);

        EstadoTurno estadoInicial = EstadoTurno.desdeValorBD(form.getEstado());
        if (estadoInicial != EstadoTurno.PENDIENTE && estadoInicial != EstadoTurno.CONFIRMADO) {
            throw new ValidacionException(Map.of("estado", "Un turno nuevo solo puede iniciar como Pendiente o Confirmado."));
        }

        Cliente cliente = clienteDAO.buscarPorId(form.getClienteId())
                .orElseThrow(() -> new ClienteNoEncontradoException("El cliente seleccionado no existe."));

        Vehiculo vehiculo = validarVehiculoDelCliente(form.getVehiculoId(), form.getClienteId());

        LocalDateTime fechaHora = LocalDateTime.parse(form.getFechaHora());
        verificarCapacidad(fechaHora, null);

        Turno turno = new Turno();
        turno.setClienteId(cliente.getIdCliente());
        turno.setVehiculoId(vehiculo.getIdVehiculo());
        turno.setUsuarioId(idUsuarioCreador);
        turno.setFechaHora(fechaHora);
        turno.setTipoServicio(TipoServicio.desdeValorBD(form.getTipoServicio()));
        turno.setEstado(estadoInicial);
        turno.setNotas(vacioComoNulo(form.getNotas()));

        try (Connection connection = DBConnectionManager.getConnection()) {
            connection.setAutoCommit(false);
            try {
                turnoDAO.crear(connection, turno);

                Auditoria auditoria = new Auditoria(idUsuarioCreador, AppConstants.MODULO_TURNOS,
                        "ALTA", "turnos", turno.getIdTurno(), null,
                        "fecha_hora=" + turno.getFechaHora() + ", vehiculo_id=" + turno.getVehiculoId(),
                        "EXITO", "Alta de turno.");
                auditoriaDAO.registrar(connection, auditoria);

                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        }

        return construirDTO(turno, cliente, vehiculo);
    }

    @Override
    public TurnoDTO actualizar(TurnoFormDTO form, Long idUsuarioEditor)
            throws ValidacionException, TurnoNoEncontradoException, TurnoOcupadoException, SQLException {

        TurnoValidator.validar(form);

        Turno existente = turnoDAO.buscarPorId(form.getIdTurno())
                .orElseThrow(() -> new TurnoNoEncontradoException("No se encontro el turno solicitado (ID " + form.getIdTurno() + ")."));

        if (existente.getEstado().esTerminal()) {
            throw new ValidacionException(Map.of("estado", "No se puede modificar un turno finalizado o cancelado."));
        }

        EstadoTurno nuevoEstado = EstadoTurno.desdeValorBD(form.getEstado());
        if (nuevoEstado == EstadoTurno.CANCELADO) {
            throw new ValidacionException(Map.of("estado", "Para cancelar un turno utilice la accion Cancelar."));
        }

        LocalDateTime fechaHora = LocalDateTime.parse(form.getFechaHora());
        if (!fechaHora.equals(existente.getFechaHora())) {
            verificarCapacidad(fechaHora, existente.getIdTurno());
        }

        existente.setFechaHora(fechaHora);
        existente.setTipoServicio(TipoServicio.desdeValorBD(form.getTipoServicio()));
        existente.setEstado(nuevoEstado);
        existente.setNotas(vacioComoNulo(form.getNotas()));

        try (Connection connection = DBConnectionManager.getConnection()) {
            connection.setAutoCommit(false);
            try {
                turnoDAO.actualizar(connection, existente);

                Auditoria auditoria = new Auditoria(idUsuarioEditor, AppConstants.MODULO_TURNOS,
                        "MODIFICACION", "turnos", existente.getIdTurno(), null,
                        "fecha_hora=" + existente.getFechaHora() + ", estado=" + existente.getEstado(),
                        "EXITO", "Modificacion de turno.");
                auditoriaDAO.registrar(connection, auditoria);

                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        }

        Cliente cliente = clienteDAO.buscarPorId(existente.getClienteId()).orElse(null);
        Vehiculo vehiculo = vehiculoDAO.buscarPorId(existente.getVehiculoId()).orElse(null);
        return construirDTO(existente, cliente, vehiculo);
    }

    @Override
    public TurnoDTO obtenerPorId(Long idTurno) throws TurnoNoEncontradoException, SQLException {
        Turno turno = turnoDAO.buscarPorId(idTurno)
                .orElseThrow(() -> new TurnoNoEncontradoException("No se encontro el turno solicitado (ID " + idTurno + ")."));

        Cliente cliente = clienteDAO.buscarPorId(turno.getClienteId()).orElse(null);
        Vehiculo vehiculo = vehiculoDAO.buscarPorId(turno.getVehiculoId()).orElse(null);
        return construirDTO(turno, cliente, vehiculo);
    }

    @Override
    public PaginaResultado<TurnoDTO> listar(String textoBusqueda, String estadoFiltro, String tipoServicioFiltro,
                                             LocalDate fechaDesde, LocalDate fechaHasta,
                                             int pagina, int registrosPorPagina) throws SQLException {

        PaginaResultado<Turno> resultado = turnoDAO.listar(textoBusqueda, estadoFiltro, tipoServicioFiltro,
                fechaDesde, fechaHasta, pagina, registrosPorPagina);

        Map<Long, Cliente> clientesCache = new HashMap<>();
        Map<Long, Vehiculo> vehiculosCache = new HashMap<>();
        Map<Long, Usuario> usuariosCache = new HashMap<>();

        List<TurnoDTO> dtos = resultado.getRegistros().stream()
                .map(t -> {
                    Cliente cliente = clientesCache.computeIfAbsent(t.getClienteId(), this::buscarClienteSilencioso);
                    Vehiculo vehiculo = vehiculosCache.computeIfAbsent(t.getVehiculoId(), this::buscarVehiculoSilencioso);
                    Usuario usuario = usuariosCache.computeIfAbsent(t.getUsuarioId(), this::buscarUsuarioSilencioso);
                    return TurnoMapper.aDTO(t,
                            cliente != null ? cliente.getNombreCompleto() : "(sin datos)",
                            vehiculo != null ? vehiculo.getPatente() : "(sin datos)",
                            usuario != null ? usuario.getNombre() : "(sin datos)");
                })
                .collect(Collectors.toList());

        return new PaginaResultado<>(dtos, resultado.getTotalRegistros(), resultado.getPaginaActual(),
                resultado.getRegistrosPorPagina());
    }

    @Override
    public void cancelar(Long idTurno, Long idUsuarioEditor) throws TurnoNoEncontradoException, ValidacionException, SQLException {
        Turno turno = turnoDAO.buscarPorId(idTurno)
                .orElseThrow(() -> new TurnoNoEncontradoException("No se encontro el turno solicitado (ID " + idTurno + ")."));

        if (turno.getEstado().esTerminal()) {
            throw new ValidacionException(Map.of("estado", "El turno ya esta finalizado o cancelado."));
        }

        try (Connection connection = DBConnectionManager.getConnection()) {
            connection.setAutoCommit(false);
            try {
                turnoDAO.cancelar(connection, idTurno);

                Auditoria auditoria = new Auditoria(idUsuarioEditor, AppConstants.MODULO_TURNOS,
                        "CANCELACION", "turnos", idTurno, "estado=" + turno.getEstado(),
                        "estado=CANCELADO", "EXITO", "Cancelacion de turno.");
                auditoriaDAO.registrar(connection, auditoria);

                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        }
    }

    private Vehiculo validarVehiculoDelCliente(Long vehiculoId, Long clienteId) throws VehiculoNoEncontradoException, ValidacionException, SQLException {
        Vehiculo vehiculo = vehiculoDAO.buscarPorId(vehiculoId)
                .orElseThrow(() -> new VehiculoNoEncontradoException("El vehiculo seleccionado no existe."));
        if (!vehiculo.getClienteId().equals(clienteId)) {
            throw new ValidacionException(Map.of("vehiculoId", "El vehiculo seleccionado no pertenece al cliente indicado."));
        }
        return vehiculo;
    }

    private void verificarCapacidad(LocalDateTime fechaHora, Long idTurnoExcluido) throws TurnoOcupadoException, SQLException {
        int activos = turnoDAO.contarActivosEnHorario(fechaHora, idTurnoExcluido);
        if (activos >= AppConstants.CAPACIDAD_TALLER_SIMULTANEA) {
            throw new TurnoOcupadoException("El taller ya tiene " + activos + " turno(s) asignados en ese horario. Elija otro horario.");
        }
    }

    private TurnoDTO construirDTO(Turno turno, Cliente cliente, Vehiculo vehiculo) throws SQLException {
        Usuario usuario = usuarioDAO.buscarPorId(turno.getUsuarioId()).orElse(null);
        return TurnoMapper.aDTO(turno,
                cliente != null ? cliente.getNombreCompleto() : "(sin datos)",
                vehiculo != null ? vehiculo.getPatente() : "(sin datos)",
                usuario != null ? usuario.getNombre() : "(sin datos)");
    }

    private Cliente buscarClienteSilencioso(Long id) {
        try {
            return clienteDAO.buscarPorId(id).orElse(null);
        } catch (SQLException e) {
            return null;
        }
    }

    private Vehiculo buscarVehiculoSilencioso(Long id) {
        try {
            return vehiculoDAO.buscarPorId(id).orElse(null);
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

    private String vacioComoNulo(String valor) {
        return (valor == null || valor.isBlank()) ? null : valor.trim();
    }
}
