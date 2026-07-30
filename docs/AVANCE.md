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
- [ ] Ventas Directas
- [ ] Items de Venta
- [ ] Proveedores
- [ ] Productos
- [ ] Compras
- [ ] Items de Compra
- [ ] Reportes

## Ultimo modulo finalizado

**Modulo:** Comprobantes
**Estado:** Terminado y verificado end-to-end (Tomcat 10 + MySQL 8 reales)
**Archivos nuevos:** `entity/Comprobante.java`, `entity/EstadoComprobante.java`, `entity/MetodoPago.java`, `dto/ComprobanteDTO.java`, `dto/ComprobanteFormDTO.java`, `dao/ComprobanteDAO(.impl)`, `service/ComprobanteService(.impl)`, `validator/ComprobanteValidator.java`, `exception/ComprobanteNoEncontradoException.java`, `controller/ComprobanteServlet.java`, `utils/ComprobanteMapper.java`, `utils/ComprobantePdfGenerator.java`, `WEB-INF/jsp/comprobantes/listado.jsp`, `docs/modulos/07-comprobantes.md`
**Archivos modificados:** `db/schema.sql` (tabla `comprobantes`), `pom.xml` (dependencia Apache PDFBox), `AppConstants.java`, `TrabajoDAO(.impl)` (marcarFacturado/desmarcarFacturado), `TrabajoServlet.java` (carga el comprobante activo en el detalle), `trabajos/detalle.jsp` (seccion Comprobante: generar/ver/anular), `layout/header.jsp`, `dashboard/dashboard.jsp`, `validations.js`
**Decision de modelado:** generacion de PDF real con Apache PDFBox (Apache 2.0) en lugar de iText, guardado fuera del WAR (`${catalina.base}/comprobantes-pdf/`) para sobrevivir un redeploy; el estado `FACTURADO` de Trabajos Realizados (dejado pendiente en ese modulo) se fija y se revierte desde aca.
**Tablas nuevas:** `comprobantes`
**Dependencias:** Trabajos Realizados, Items de Trabajo
**Resultado:** OK

## Modulos anteriores

- **Items de Trabajo** — detalle de repuestos y mano de obra por trabajo. Ver `docs/modulos/06-items-trabajo.md`.
- **Trabajos Realizados** — en proceso/finalizado/facturado, reapertura y baja solo admin. Ver `docs/modulos/05-trabajos.md`.
- **Turnos** — agenda con control de capacidad simultanea. Ver `docs/modulos/04-turnos.md`.
- **Vehiculos** — patente unica, bloqueada para OPERADOR. Ver `docs/modulos/03-vehiculos.md`.
- **Clientes** — alta/baja/consulta de clientes, DNI y telefono unicos. Ver `docs/modulos/02-clientes.md`.
- **Usuarios** — login, roles, auditoria, bloqueo de cuenta. Ver `docs/modulos/01-usuarios.md`.

Siguiente paso: esperar autorizacion del cliente para iniciar el Modulo de Ventas Directas.
