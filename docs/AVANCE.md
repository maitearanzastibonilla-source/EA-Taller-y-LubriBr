# Control de avance — SGI EA Taller y LubriBr

Un modulo a la vez, con autorizacion explicita antes de iniciar el siguiente (protocolo acordado con el cliente).

- [x] Base tecnica (Maven, MVC, JDBC/DataSource JNDI, Design System, seguridad transversal)
- [x] Usuarios (login, roles, auditoria, bloqueo de cuenta)
- [x] Clientes
- [x] Vehiculos
- [x] Turnos
- [x] Trabajos Realizados
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

**Modulo:** Trabajos Realizados
**Estado:** Terminado y verificado end-to-end (Tomcat 10 + MySQL 8 reales)
**Archivos nuevos:** `entity/TrabajoRealizado.java`, `entity/EstadoTrabajo.java`, `dto/TrabajoDTO.java`, `dto/TrabajoFormDTO.java`, `dao/TrabajoDAO(.impl)`, `service/TrabajoService(.impl)`, `validator/TrabajoValidator.java`, `exception/TrabajoNoEncontradoException.java`, `controller/TrabajoServlet.java`, `utils/TrabajoMapper.java`, JSPs de `trabajos/`, `docs/modulos/05-trabajos.md`
**Archivos modificados:** `db/schema.sql` (tabla `trabajos_realizados`, FKs a `vehiculos`/`turnos`/`usuarios`), `AppConstants.java`, `JspFunctions.java` y `ea-functions.tld` (nueva funcion `fechaDia` para columnas `LocalDate`), `validations.js`, `layout/header.jsp` y `dashboard.jsp` (link/card), `turnos/listado.jsp` (atajo "Iniciar trabajo")
**Decision de modelado:** se agrego la columna `activo` (aparte de `estado`) para poder implementar la baja logica que pide la Propuesta, ya que su propio enum de estado (en_proceso/finalizado/facturado) no tiene un valor para "dado de baja".
**Bug encontrado y corregido durante la verificacion:** la funcion EL `ea:fecha` esperaba `LocalDateTime` y las fechas de este modulo son `LocalDate`, lo que rompia el listado con un `ELException`. Se agrego `ea:fechaDia` para columnas de solo fecha.
**Tablas nuevas:** `trabajos_realizados`
**Dependencias:** Vehiculos, Turnos, Usuarios
**Resultado:** OK

## Modulos anteriores

- **Turnos** — agenda con control de capacidad simultanea. Ver `docs/modulos/04-turnos.md`.
- **Vehiculos** — patente unica, bloqueada para OPERADOR. Ver `docs/modulos/03-vehiculos.md`.
- **Clientes** — alta/baja/consulta de clientes, DNI y telefono unicos. Ver `docs/modulos/02-clientes.md`.
- **Usuarios** — login, roles, auditoria, bloqueo de cuenta. Ver `docs/modulos/01-usuarios.md`.

Siguiente paso: esperar autorizacion del cliente para iniciar el Modulo de Items de Trabajo.
