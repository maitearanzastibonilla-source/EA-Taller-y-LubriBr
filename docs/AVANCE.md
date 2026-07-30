# Control de avance — SGI EA Taller y LubriBr

Un modulo a la vez, con autorizacion explicita antes de iniciar el siguiente (protocolo acordado con el cliente).

- [x] Base tecnica (Maven, MVC, JDBC/DataSource JNDI, Design System, seguridad transversal)
- [x] Usuarios (login, roles, auditoria, bloqueo de cuenta)
- [x] Clientes
- [x] Vehiculos
- [x] Turnos
- [x] Trabajos Realizados
- [x] Items de Trabajo
- [x] Comprobantes
- [x] Proveedores
- [ ] Productos
- [ ] Ventas Directas
- [ ] Items de Venta
- [ ] Compras
- [ ] Items de Compra
- [ ] Reportes

**Nota de orden:** se reordeno la construccion respecto de la secuencia original de la Propuesta. Ventas Directas/Items de Venta necesitan un catalogo de Productos real (con stock y proveedor obligatorio) para cumplir sus propias reglas de negocio (validacion y descuento de stock), asi que Proveedores y Productos se adelantaron. Detalle de la decision en `docs/modulos/08-proveedores.md`.

## Ultimo modulo finalizado

**Modulo:** Proveedores
**Estado:** Terminado y verificado end-to-end (Tomcat 10 + MySQL 8 reales)
**Archivos nuevos:** `entity/Proveedor.java`, `dto/ProveedorDTO.java`, `dto/ProveedorFormDTO.java`, `dao/ProveedorDAO(.impl)`, `service/ProveedorService(.impl)`, `validator/ProveedorValidator.java`, `exception/ProveedorNoEncontradoException.java`, `controller/ProveedorServlet.java`, `utils/ProveedorMapper.java`, `WEB-INF/jsp/proveedores/{listado,form}.jsp`, `docs/modulos/08-proveedores.md`
**Archivos modificados:** `db/schema.sql` (tabla `proveedores`), `AppConstants.java`, `layout/header.jsp`, `dashboard/dashboard.jsp`, `validations.js`
**Decision de modelado:** modulo adelantado en el orden de construccion (ver nota arriba) porque Productos, del que depende Ventas Directas/Items de Venta, exige un proveedor obligatorio. Sin restriccion de rol en ninguna operacion, ya que la Propuesta no la pide para este modulo (a diferencia de Clientes).
**Tablas nuevas:** `proveedores`
**Dependencias:** ninguna (modulo independiente); es dependencia de Productos
**Resultado:** OK

## Modulos anteriores

- **Comprobantes** — generacion de PDF real, alta/anulacion, cierre del ciclo de facturacion. Ver `docs/modulos/07-comprobantes.md`.
- **Items de Trabajo** — detalle de repuestos y mano de obra por trabajo. Ver `docs/modulos/06-items-trabajo.md`.
- **Trabajos Realizados** — en proceso/finalizado/facturado, reapertura y baja solo admin. Ver `docs/modulos/05-trabajos.md`.
- **Turnos** — agenda con control de capacidad simultanea. Ver `docs/modulos/04-turnos.md`.
- **Vehiculos** — patente unica, bloqueada para OPERADOR. Ver `docs/modulos/03-vehiculos.md`.
- **Clientes** — alta/baja/consulta de clientes, DNI y telefono unicos. Ver `docs/modulos/02-clientes.md`.
- **Usuarios** — login, roles, auditoria, bloqueo de cuenta. Ver `docs/modulos/01-usuarios.md`.

Siguiente paso: esperar autorizacion del cliente para iniciar el Modulo de Productos.
