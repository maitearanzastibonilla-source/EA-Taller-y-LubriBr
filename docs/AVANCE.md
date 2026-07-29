# Control de avance — SGI EA Taller y LubriBr

Un modulo a la vez, con autorizacion explicita antes de iniciar el siguiente (protocolo acordado con el cliente).

- [x] Base tecnica (Maven, MVC, JDBC/DataSource JNDI, Design System, seguridad transversal)
- [x] Usuarios (login, roles, auditoria, bloqueo de cuenta)
- [x] Clientes
- [x] Vehiculos
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

**Modulo:** Vehiculos
**Estado:** Terminado y verificado end-to-end (Tomcat 10 + MySQL 8 reales)
**Archivos nuevos:** `entity/Vehiculo.java`, `dto/VehiculoDTO.java`, `dto/VehiculoFormDTO.java`, `dao/VehiculoDAO(.impl)`, `service/VehiculoService(.impl)`, `validator/VehiculoValidator.java`, `exception/VehiculoNoEncontradoException.java`, `controller/VehiculoServlet.java`, `utils/VehiculoMapper.java`, JSPs de `vehiculos/`, `docs/modulos/03-vehiculos.md`
**Archivos modificados:** `db/schema.sql` (tabla `vehiculos`, FK a `clientes`), `AppConstants.java`, `ValidationUtils.java` (formato de patente), `validations.js`, `layout/header.jsp` y `dashboard.jsp` (link/card), `clientes/listado.jsp` (atajo "Agregar vehiculo")
**Refactor:** `entity/EstadoCliente.java` renombrado a `entity/Estado.java` (ACTIVO/INACTIVO), reutilizado por Clientes y Vehiculos y pensado para Proveedores/Productos mas adelante
**Tablas nuevas:** `vehiculos`
**Dependencias:** Clientes (propietario)
**Resultado:** OK

## Modulos anteriores

- **Clientes** — alta/baja/consulta de clientes, DNI y telefono unicos. Ver `docs/modulos/02-clientes.md`.
- **Usuarios** — login, roles, auditoria, bloqueo de cuenta. Ver `docs/modulos/01-usuarios.md`.

Siguiente paso: esperar autorizacion del cliente para iniciar el Modulo de Turnos.
