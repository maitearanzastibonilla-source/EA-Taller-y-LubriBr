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
- [x] Reportes

Los 14 modulos de la Propuesta Tecnica estan terminados.

**Nota de orden:** se reordeno la construccion respecto de la secuencia original de la Propuesta. Ventas Directas/Items de Venta necesitan un catalogo de Productos real (con stock y proveedor obligatorio) para cumplir sus propias reglas de negocio (validacion y descuento de stock), asi que Proveedores y Productos se adelantaron. Detalle de la decision en `docs/modulos/08-proveedores.md`.

## Ultimo modulo finalizado

**Modulo:** Reportes (ultimo modulo de la Propuesta)
**Estado:** Terminado y verificado end-to-end (Tomcat 10 + MySQL 8 reales)
**Archivos nuevos:** `entity/Reporte.java`, `entity/TipoReporte.java`, `dto/ReporteDTO.java`, `dto/ReporteFormDTO.java`, `dto/ReporteDatos.java`, `dao/ReporteDAO(.impl)`, `service/ReporteService(.impl)`, `validator/ReporteValidator.java`, `exception/ReporteNoEncontradoException.java`, `controller/ReporteServlet.java`, `utils/ReporteMapper.java`, `utils/ReportePdfGenerator.java`, `WEB-INF/jsp/reportes/{listado,form,detalle}.jsp`, `docs/modulos/12-reportes.md`
**Archivos modificados:** `db/schema.sql` (tabla `reportes`), `AppConstants.java`, `AuthenticationFilter.java` (agrega `/reportes` a los prefijos admin-only), `layout/header.jsp`, `dashboard/dashboard.jsp`, `validations.js`
**Decision de modelado:** acceso restringido a nivel de modulo completo (todo `/reportes` admin-only via filtro, no solo una accion puntual); `generarReporte()`/`exportarPDF()` de la Propuesta implementadas como comportamiento (metodos), no columnas; `fecha_desde`/`fecha_hasta` agregadas a la tabla para poder reabrir un reporte y ver el mismo periodo; contenedor generico `ReporteDatos` (columnas + filas + resumen) reutilizado por los 6 tipos de reporte en vez de un DTO por tipo; solo transacciones CONFIRMADA/COBRADO cuentan para Ingresos y Compras por proveedor.
**Tablas nuevas:** `reportes`
**Dependencias:** Trabajos, Turnos, Comprobantes, Ventas Directas, Compras, Productos, Clientes, Usuarios
**Resultado:** OK

## Modulos anteriores

- **Compras e Items de Compra** — incremento y reversion real de stock. Ver `docs/modulos/11-compras.md`.
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

Siguiente paso: los 14 modulos de la Propuesta Tecnica estan implementados y verificados. Queda a criterio del cliente si se quiere una ronda de revision general, ajustes puntuales, o dar el proyecto por entregado.
