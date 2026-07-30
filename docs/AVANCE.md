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
- [ ] Ventas Directas
- [ ] Items de Venta
- [ ] Compras
- [ ] Items de Compra
- [ ] Reportes

**Nota de orden:** se reordeno la construccion respecto de la secuencia original de la Propuesta. Ventas Directas/Items de Venta necesitan un catalogo de Productos real (con stock y proveedor obligatorio) para cumplir sus propias reglas de negocio (validacion y descuento de stock), asi que Proveedores y Productos se adelantaron. Detalle de la decision en `docs/modulos/08-proveedores.md`.

## Ultimo modulo finalizado

**Modulo:** Productos
**Estado:** Terminado y verificado end-to-end (Tomcat 10 + MySQL 8 reales)
**Archivos nuevos:** `entity/Producto.java`, `dto/ProductoDTO.java`, `dto/ProductoFormDTO.java`, `dao/ProductoDAO(.impl)`, `service/ProductoService(.impl)`, `validator/ProductoValidator.java`, `exception/ProductoNoEncontradoException.java`, `controller/ProductoServlet.java`, `utils/ProductoMapper.java`, `WEB-INF/jsp/productos/{listado,form}.jsp`, `docs/modulos/09-productos.md`
**Archivos modificados:** `db/schema.sql` (tabla `productos` + FK `fk_items_trabajo_producto` sobre `items_trabajo`, que quedaba pendiente desde el modulo de Items de Trabajo), `AppConstants.java`, `layout/header.jsp`, `dashboard/dashboard.jsp`, `validations.js`
**Decision de modelado:** `categoria` como texto libre (la Propuesta da ejemplos abiertos, no una lista cerrada); `stock_actual` solo se carga en el alta y no se edita desde el formulario de modificacion (la Propuesta no lo incluye ahi); baja logica bloqueada si el producto esta referenciado en `items_trabajo`, verificado con una consulta real.
**Tablas nuevas:** `productos`
**Dependencias:** Proveedores; es dependencia de Ventas Directas/Items de Venta y Compras/Items de Compra
**Resultado:** OK

## Modulos anteriores

- **Proveedores** — CRUD sin restriccion de rol, adelantado en el orden. Ver `docs/modulos/08-proveedores.md`.
- **Comprobantes** — generacion de PDF real, alta/anulacion, cierre del ciclo de facturacion. Ver `docs/modulos/07-comprobantes.md`.
- **Items de Trabajo** — detalle de repuestos y mano de obra por trabajo. Ver `docs/modulos/06-items-trabajo.md`.
- **Trabajos Realizados** — en proceso/finalizado/facturado, reapertura y baja solo admin. Ver `docs/modulos/05-trabajos.md`.
- **Turnos** — agenda con control de capacidad simultanea. Ver `docs/modulos/04-turnos.md`.
- **Vehiculos** — patente unica, bloqueada para OPERADOR. Ver `docs/modulos/03-vehiculos.md`.
- **Clientes** — alta/baja/consulta de clientes, DNI y telefono unicos. Ver `docs/modulos/02-clientes.md`.
- **Usuarios** — login, roles, auditoria, bloqueo de cuenta. Ver `docs/modulos/01-usuarios.md`.

Siguiente paso: esperar autorizacion del cliente para iniciar el Modulo de Ventas Directas (junto con Items de Venta, con descuento y restauracion real de stock contra `productos`).
