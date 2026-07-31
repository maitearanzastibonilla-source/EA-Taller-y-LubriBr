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
- [x] Productos
- [x] Ventas Directas
- [x] Items de Venta
- [x] Compras
- [x] Items de Compra
- [ ] Reportes

**Nota de orden:** se reordeno la construccion respecto de la secuencia original de la Propuesta. Ventas Directas/Items de Venta necesitan un catalogo de Productos real (con stock y proveedor obligatorio) para cumplir sus propias reglas de negocio (validacion y descuento de stock), asi que Proveedores y Productos se adelantaron. Detalle de la decision en `docs/modulos/08-proveedores.md`.

## Ultimo modulo finalizado

**Modulo:** Compras e Items de Compra
**Estado:** Terminado y verificado end-to-end (Tomcat 10 + MySQL 8 reales)
**Archivos nuevos:** `entity/Compra.java`, `entity/EstadoCompra.java`, `entity/ItemCompra.java`, `dto/CompraDTO.java`, `dto/CompraFormDTO.java`, `dto/ItemCompraDTO.java`, `dto/ItemCompraFormDTO.java`, `dao/CompraDAO(.impl)`, `dao/ItemCompraDAO(.impl)`, `service/CompraService(.impl)`, `service/ItemCompraService(.impl)`, `validator/CompraValidator.java`, `validator/ItemCompraValidator.java`, `exception/CompraNoEncontradaException.java`, `exception/ItemCompraNoEncontradoException.java`, `controller/CompraServlet.java`, `controller/ItemCompraServlet.java`, `utils/CompraMapper.java`, `utils/ItemCompraMapper.java`, `WEB-INF/jsp/compras/{listado,form,detalle}.jsp`, `docs/modulos/11-compras.md`
**Archivos modificados:** `db/schema.sql` (tablas `compras` e `items_compra`), `AppConstants.java`, `ProductoDAO(.impl)` (tieneItemsDeCompraAsociados), `ProductoServiceImpl` (bloquea baja si el producto esta en una compra), `layout/header.jsp`, `dashboard/dashboard.jsp`, `validations.js`
**Decision de modelado:** mismo flujo en dos pasos que Ventas Directas (alta pendiente + items + confirmacion), pero aca `estado` si estaba en el campo de la Propuesta; se resolvio a favor de la version mas estricta ante la contradiccion entre "confirmadas no podran modificarse sin autorizacion administrativa" (alta) y "una compra confirmada no podra modificarse" (validaciones): ninguna modificacion post-confirmacion, sin excepcion de rol; sin chequeo de producto activo en los items (a diferencia de Ventas Directas), porque reponer stock de un producto inactivo no esta prohibido y tiene sentido de negocio.
**Tablas nuevas:** `compras`, `items_compra`
**Dependencias:** Proveedores, Productos, Usuarios
**Resultado:** OK

## Modulos anteriores

- **Ventas Directas e Items de Venta** — descuento y restauracion real de stock. Ver `docs/modulos/10-ventas-directas.md`.
- **Productos** — catalogo con proveedor obligatorio, alerta de stock bajo. Ver `docs/modulos/09-productos.md`.
- **Proveedores** — CRUD sin restriccion de rol, adelantado en el orden. Ver `docs/modulos/08-proveedores.md`.
- **Comprobantes** — generacion de PDF real, alta/anulacion, cierre del ciclo de facturacion. Ver `docs/modulos/07-comprobantes.md`.
- **Items de Trabajo** — detalle de repuestos y mano de obra por trabajo. Ver `docs/modulos/06-items-trabajo.md`.
- **Trabajos Realizados** — en proceso/finalizado/facturado, reapertura y baja solo admin. Ver `docs/modulos/05-trabajos.md`.
- **Turnos** — agenda con control de capacidad simultanea. Ver `docs/modulos/04-turnos.md`.
- **Vehiculos** — patente unica, bloqueada para OPERADOR. Ver `docs/modulos/03-vehiculos.md`.
- **Clientes** — alta/baja/consulta de clientes, DNI y telefono unicos. Ver `docs/modulos/02-clientes.md`.
- **Usuarios** — login, roles, auditoria, bloqueo de cuenta. Ver `docs/modulos/01-usuarios.md`.

Siguiente paso: esperar autorizacion del cliente para iniciar el Modulo de Reportes, el ultimo pendiente de la Propuesta.
