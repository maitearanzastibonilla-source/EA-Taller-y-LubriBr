# Control de avance — SGI EA Taller y LubriBr

Un modulo a la vez, con autorizacion explicita antes de iniciar el siguiente (protocolo acordado con el cliente).

- [x] Base tecnica (Maven, MVC, JDBC/DataSource JNDI, Design System, seguridad transversal)
- [x] Usuarios (login, roles, auditoria, bloqueo de cuenta)
- [x] Clientes
- [ ] Vehiculos
- [ ] Turnos
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

**Modulo:** Clientes
**Estado:** Terminado y verificado end-to-end (Tomcat 10 + MySQL 8 reales)
**Archivos nuevos:** `entity/Cliente.java`, `entity/EstadoCliente.java`, `dto/ClienteDTO.java`, `dto/ClienteFormDTO.java`, `dao/ClienteDAO(.impl)`, `service/ClienteService(.impl)`, `validator/ClienteValidator.java`, `exception/ClienteNoEncontradoException.java`, `exception/DatoDuplicadoException.java`, `controller/ClienteServlet.java`, `utils/ClienteMapper.java`, JSPs de `clientes/`, `docs/modulos/02-clientes.md`
**Archivos modificados:** `db/schema.sql` (tabla `clientes`), `AppConstants.java` (modulo de auditoria), `ValidationUtils.java` (validacion de DNI/telefono), `validations.js` (validacion de formulario), `layout/header.jsp` y `dashboard.jsp` (link/card del modulo)
**Tablas nuevas:** `clientes`
**Dependencias:** Usuarios (auditoria, roles)
**Resultado:** OK

## Modulo anterior

**Modulo:** Usuarios — login, roles, auditoria, bloqueo de cuenta. Ver `docs/modulos/01-usuarios.md`.

Siguiente paso: esperar autorizacion del cliente para iniciar el Modulo de Vehiculos.
