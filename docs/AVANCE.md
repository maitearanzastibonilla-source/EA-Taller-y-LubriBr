# Control de avance — SGI EA Taller y LubriBr

Un modulo a la vez, con autorizacion explicita antes de iniciar el siguiente (protocolo acordado con el cliente).

- [x] Base tecnica (Maven, MVC, JDBC/DataSource JNDI, Design System, seguridad transversal)
- [x] Usuarios (login, roles, auditoria, bloqueo de cuenta)
- [ ] Clientes
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

**Modulo:** Usuarios
**Estado:** Terminado y verificado end-to-end (Tomcat 10 + MySQL 8 reales)
**Archivos nuevos:** ver commit correspondiente (paquetes `entity`, `dto`, `dao`, `service`, `controller`, `validator`, `exception`, `utils`, `filter`, `listener`, `constants`, JSPs de `auth/`, `usuarios/`, `dashboard/`, `error/`, `layout/`, Design System completo en `assets/css` y `assets/js`, `db/schema.sql`, `db/seed.sql`)
**Tablas nuevas:** `usuarios`, `auditoria`
**Dependencias:** ninguna (modulo base, del que dependeran todos los demas para autenticacion/roles/auditoria)
**Resultado:** OK

Siguiente paso: esperar autorizacion del cliente para iniciar el Modulo de Clientes.
