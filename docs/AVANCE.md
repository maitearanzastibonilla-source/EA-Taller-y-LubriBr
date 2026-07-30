# Control de avance — SGI EA Taller y LubriBr

Un modulo a la vez, con autorizacion explicita antes de iniciar el siguiente (protocolo acordado con el cliente).

- [x] Base tecnica (Maven, MVC, JDBC/DataSource JNDI, Design System, seguridad transversal)
- [x] Usuarios (login, roles, auditoria, bloqueo de cuenta)
- [x] Clientes
- [x] Vehiculos
- [x] Turnos
- [x] Trabajos Realizados
- [x] Items de Trabajo
- [ ] Comprobantes
- [ ] Ventas Directas
- [ ] Items de Venta
- [ ] Proveedores
- [ ] Productos
- [ ] Compras
- [ ] Items de Compra
- [ ] Reportes

## Ultimo modulo finalizado

**Modulo:** Items de Trabajo
**Estado:** Terminado y verificado end-to-end (Tomcat 10 + MySQL 8 reales)
**Archivos nuevos:** `entity/ItemTrabajo.java`, `dto/ItemTrabajoDTO.java`, `dto/ItemTrabajoFormDTO.java`, `dao/ItemTrabajoDAO(.impl)`, `service/ItemTrabajoService(.impl)`, `validator/ItemTrabajoValidator.java`, `exception/ItemTrabajoNoEncontradoException.java`, `controller/ItemTrabajoServlet.java`, `utils/ItemTrabajoMapper.java`, `WEB-INF/jsp/trabajos/detalle.jsp`, `docs/modulos/06-items-trabajo.md`
**Archivos modificados:** `db/schema.sql` (tabla `items_trabajo`), `AppConstants.java`, `TrabajoServlet.java` (accion `detalle`), `trabajos/listado.jsp` (link "Ver items"), `validations.js`
**Decision de modelado:** `producto_id` existe como columna pero sin FK y sin logica de stock, porque el Modulo de Productos (del que depende) todavia no existe pese a que la Propuesta ordena Items de Trabajo antes que Productos. Todos los items se cargan hoy por descripcion libre.
**Tablas nuevas:** `items_trabajo`
**Dependencias:** Trabajos Realizados
**Resultado:** OK

## Modulos anteriores

- **Trabajos Realizados** — en proceso/finalizado/facturado, reapertura y baja solo admin. Ver `docs/modulos/05-trabajos.md`.
- **Turnos** — agenda con control de capacidad simultanea. Ver `docs/modulos/04-turnos.md`.
- **Vehiculos** — patente unica, bloqueada para OPERADOR. Ver `docs/modulos/03-vehiculos.md`.
- **Clientes** — alta/baja/consulta de clientes, DNI y telefono unicos. Ver `docs/modulos/02-clientes.md`.
- **Usuarios** — login, roles, auditoria, bloqueo de cuenta. Ver `docs/modulos/01-usuarios.md`.

Siguiente paso: esperar autorizacion del cliente para iniciar el Modulo de Comprobantes.
