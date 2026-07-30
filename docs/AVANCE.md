# Control de avance — SGI EA Taller y LubriBr

Un modulo a la vez, con autorizacion explicita antes de iniciar el siguiente (protocolo acordado con el cliente).

- [x] Base tecnica (Maven, MVC, JDBC/DataSource JNDI, Design System, seguridad transversal)
- [x] Usuarios (login, roles, auditoria, bloqueo de cuenta)
- [x] Clientes
- [x] Vehiculos
- [x] Turnos
- [ ] Trabajos Realizados
- [ ] Items de Trabajo
- [ ] Comprobantes
- [ ] Ventas Directas
- [ ] Items de Venta
- [ ] Proveedores
- [ ] Productos
- [ ] Compras
- [ ] Items de Compra
- [ ] Reportes

## Ultimo modulo finalizado

**Modulo:** Turnos
**Estado:** Terminado y verificado end-to-end (Tomcat 10 + MySQL 8 reales)
**Archivos nuevos:** `entity/Turno.java`, `entity/EstadoTurno.java`, `entity/TipoServicio.java`, `dto/TurnoDTO.java`, `dto/TurnoFormDTO.java`, `dao/TurnoDAO(.impl)`, `service/TurnoService(.impl)`, `validator/TurnoValidator.java`, `exception/TurnoNoEncontradoException.java`, `exception/TurnoOcupadoException.java`, `controller/TurnoServlet.java`, `utils/TurnoMapper.java`, `assets/js/turnos.js`, JSPs de `turnos/`, `docs/modulos/04-turnos.md`
**Archivos modificados:** `db/schema.sql` (tabla `turnos`, FKs a `clientes`/`vehiculos`/`usuarios`), `AppConstants.java` (capacidad simultanea), `validations.js`, `layout/footer.jsp`, `layout/header.jsp` y `dashboard.jsp` (link/card)
**Bug encontrado y corregido durante la verificacion:** la fecha/hora del turno se guardaba corrida (una carga a las 10:00 quedaba en 07:00) por usar `Timestamp` junto con `serverTimezone` en la URL JDBC. Se soluciono usando `setObject`/`getObject` con `LocalDateTime` para esa columna, que no aplica conversion de zona horaria.
**Tablas nuevas:** `turnos`
**Dependencias:** Clientes, Vehiculos, Usuarios
**Resultado:** OK

## Modulos anteriores

- **Vehiculos** — patente unica, bloqueada para OPERADOR. Ver `docs/modulos/03-vehiculos.md`.
- **Clientes** — alta/baja/consulta de clientes, DNI y telefono unicos. Ver `docs/modulos/02-clientes.md`.
- **Usuarios** — login, roles, auditoria, bloqueo de cuenta. Ver `docs/modulos/01-usuarios.md`.

Siguiente paso: esperar autorizacion del cliente para iniciar el Modulo de Trabajos Realizados.
