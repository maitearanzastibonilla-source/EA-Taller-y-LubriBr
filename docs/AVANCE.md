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
- [ ] Compras
- [ ] Items de Compra
- [ ] Reportes

**Nota de orden:** se reordeno la construccion respecto de la secuencia original de la Propuesta. Ventas Directas/Items de Venta necesitan un catalogo de Productos real (con stock y proveedor obligatorio) para cumplir sus propias reglas de negocio (validacion y descuento de stock), asi que Proveedores y Productos se adelantaron. Detalle de la decision en `docs/modulos/08-proveedores.md`.

## Ultimo modulo finalizado

**Modulo:** Ventas Directas e Items de Venta
**Estado:** Terminado y verificado end-to-end (Tomcat 10 + MySQL 8 reales)
**Archivos nuevos:** `entity/VentaDirecta.java`, `entity/EstadoVenta.java`, `entity/ItemVenta.java`, `dto/VentaDTO.java`, `dto/VentaFormDTO.java`, `dto/ItemVentaDTO.java`, `dto/ItemVentaFormDTO.java`, `dao/VentaDAO(.impl)`, `dao/ItemVentaDAO(.impl)`, `service/VentaService(.impl)`, `service/ItemVentaService(.impl)`, `validator/VentaValidator.java`, `validator/ItemVentaValidator.java`, `exception/VentaNoEncontradaException.java`, `exception/ItemVentaNoEncontradoException.java`, `controller/VentaServlet.java`, `controller/ItemVentaServlet.java`, `utils/VentaMapper.java`, `utils/ItemVentaMapper.java`, `WEB-INF/jsp/ventas/{listado,form,detalle}.jsp`, `docs/modulos/10-ventas-directas.md`
**Archivos modificados:** `db/schema.sql` (tablas `ventas_directas` e `items_venta`), `AppConstants.java`, `ProductoDAO(.impl)` (ajustarStock, tieneItemsDeVentaAsociados), `ProductoServiceImpl` (bloquea baja si el producto esta en una venta), `layout/header.jsp`, `dashboard/dashboard.jsp`, `validations.js`
**Decision de modelado:** flujo en dos pasos (alta de venta pendiente + carga de items, luego confirmacion) igual que Trabajos/Items de Trabajo; `estado` agregado a `ventas_directas` pese a no estar en la lista de campos de la Propuesta, porque el texto exige diferenciar pendiente/confirmada/anulada; stock validado al cargar cada item y revalidado/descontado recien al confirmar; anulacion restringida a administrador, mismo criterio que Comprobantes y la baja/reapertura de Trabajos.
**Tablas nuevas:** `ventas_directas`, `items_venta`
**Dependencias:** Productos, Usuarios
**Resultado:** OK

## Modulos anteriores

- **Productos** — catalogo con proveedor obligatorio, alerta de stock bajo. Ver `docs/modulos/09-productos.md`.
- **Proveedores** — CRUD sin restriccion de rol, adelantado en el orden. Ver `docs/modulos/08-proveedores.md`.
- **Comprobantes** — generacion de PDF real, alta/anulacion, cierre del ciclo de facturacion. Ver `docs/modulos/07-comprobantes.md`.
- **Items de Trabajo** — detalle de repuestos y mano de obra por trabajo. Ver `docs/modulos/06-items-trabajo.md`.
- **Trabajos Realizados** — en proceso/finalizado/facturado, reapertura y baja solo admin. Ver `docs/modulos/05-trabajos.md`.
- **Turnos** — agenda con control de capacidad simultanea. Ver `docs/modulos/04-turnos.md`.
- **Vehiculos** — patente unica, bloqueada para OPERADOR. Ver `docs/modulos/03-vehiculos.md`.
- **Clientes** — alta/baja/consulta de clientes, DNI y telefono unicos. Ver `docs/modulos/02-clientes.md`.
- **Usuarios** — login, roles, auditoria, bloqueo de cuenta. Ver `docs/modulos/01-usuarios.md`.

Siguiente paso: esperar autorizacion del cliente para iniciar el Modulo de Compras (junto con Items de Compra).
