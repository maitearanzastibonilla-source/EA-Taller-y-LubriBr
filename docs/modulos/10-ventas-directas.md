# Modulo de Ventas Directas e Items de Venta

Spec tecnica condensada. Fuente de verdad: Propuesta Tecnica (secciones "Modulo de Ventas Directas" e "Modulo de Items de Venta").

## Ficha tecnica

| Campo | Detalle |
|---|---|
| Objetivo | Ventas de productos por mostrador, sin trabajo mecanico asociado, con descuento y restauracion real de stock |
| Roles con acceso | ADMINISTRADOR y OPERADOR pueden crear, cargar items y confirmar; solo ADMINISTRADOR puede anular una venta confirmada |
| Modulos relacionados | Productos (stock real), Usuarios (responsable de la venta) |
| Tablas | `ventas_directas`, `items_venta` |
| Criticidad | Alta (mueve stock real y dinero fuera del circuito de Trabajos/Comprobantes) |

## Decision de orden de construccion

Este modulo es la razon por la que se reordeno la construccion (ver `docs/modulos/08-proveedores.md` y `09-productos.md`): a diferencia de Items de Trabajo, la Propuesta no deja una descripcion libre como alternativa para Items de Venta — exige que cada item este vinculado a un producto existente y valide/descuente stock real. Por eso Proveedores y Productos se construyeron antes.

## Decisiones de modelado

1. **Estado de la venta**: el campo `estado` no esta en la lista de columnas de `ventas_directas` en la Propuesta, pero el texto describe tres situaciones (pendiente/confirmada/anulada). Se agrego el enum `EstadoVenta` (PENDIENTE/CONFIRMADA/ANULADA), con el mismo criterio ya usado para `activo` en Trabajos Realizados.
2. **Flujo de dos pasos**: alta de la venta (cabecera: usuario, fecha, metodo de pago) y carga de items quedan separados, igual que Trabajos + Items de Trabajo. La "confirmacion" es una accion propia que valida stock y lo descuenta recien en ese momento — la Propuesta describe ambas cosas ("agregar productos... y confirmar la operacion" en el alta, "descontara stock en tiempo real al confirmar la venta" en items) y este flujo las respeta sin mezclarlas en una sola transaccion HTTP.
3. **Validacion de stock en dos momentos**: al cargar/editar un item se valida contra el stock actual del producto (para dar feedback inmediato); al confirmar se vuelve a validar cada item contra el stock vigente (por si cambio entre la carga y la confirmacion) antes de descontarlo, todo dentro de una unica transaccion.
4. **Total recalculado en cada cambio de items**: `ventas_directas.total` se recalcula (`SUM(cantidad * precio_unitario)`) cada vez que se agrega, modifica o elimina un item, para que el listado y el detalle siempre muestren el total real sin necesitar un join adicional.
5. **Anulacion restringida a administrador**: la Propuesta usa la misma frase ambigua ("solo usuarios habilitados") que ya aparecia en Trabajos y Comprobantes para sus operaciones de reversion; se mantiene el mismo criterio ya aplicado ahi (reabrir/dar de baja un Trabajo, anular un Comprobante): reversion de una operacion confirmada = solo administrador.
6. **Cierre de la restriccion pendiente en Productos**: ahora que `items_venta` existe, se agrego la verificacion correspondiente a `ProductoServiceImpl.cambiarEstado` (no se puede dar de baja un producto referenciado en una venta), completando lo que `09-productos.md` había dejado pendiente.

## Reglas de negocio implementadas

1. Alta de venta: solo requiere metodo de pago; queda en PENDIENTE, con total en 0 y usuario tomado de la sesion.
2. Modificacion de venta: solo el metodo de pago, y solo mientras este PENDIENTE.
3. Alta/modificacion/baja de items: solo mientras la venta este PENDIENTE. Cada item exige un producto activo existente, cantidad entera mayor a cero, y que la cantidad no supere el stock actual del producto.
4. Confirmar venta: exige al menos un item y revalida stock disponible de cada uno; si todo es correcto, descuenta el stock de cada producto y pasa la venta a CONFIRMADA, todo en una transaccion.
5. Anular venta: solo ADMINISTRADOR, solo sobre ventas CONFIRMADA; restaura el stock de cada producto involucrado y pasa la venta a ANULADA.
6. Todas las operaciones (alta/modificacion/items/confirmacion/anulacion) quedan registradas en `auditoria`.

## Endpoints

| Metodo | Ruta | Accion | Rol |
|---|---|---|---|
| GET | `/ventas` | Listado con filtros (texto, estado, rango de fechas) | Cualquier autenticado |
| GET | `/ventas?accion=nuevo` | Formulario de alta (metodo de pago) | Cualquier autenticado |
| GET | `/ventas?accion=detalle&id=` (`&editarItem=`) | Detalle de la venta + items | Cualquier autenticado |
| POST | `/ventas` (`accion=guardar`) | Alta/Modificacion de venta | Cualquier autenticado |
| POST | `/ventas` (`accion=confirmar`) | Confirmacion (descuenta stock) | Cualquier autenticado |
| POST | `/ventas` (`accion=anular`) | Anulacion (restaura stock) | Solo ADMINISTRADOR |
| POST | `/items-venta` (`accion=guardar` / `accion=eliminar`) | Alta/Modificacion/Baja de item | Cualquier autenticado |

## Modelo de datos

`ventas_directas`: `id_venta`, `usuario_id` (FK), `fecha`, `total`, `metodo_pago` (ENUM, reutiliza `MetodoPago` de Comprobantes), `estado` (ENUM PENDIENTE/CONFIRMADA/ANULADA), `created_at`, `updated_at`.

`items_venta`: `id_item_venta`, `venta_id` (FK, `ON DELETE CASCADE`), `producto_id` (FK obligatoria a `productos`, `ON DELETE RESTRICT`), `cantidad` (INT, `CHECK > 0`), `precio_unitario` (DECIMAL 12,2, `CHECK >= 0`), `created_at`, `updated_at`. Ver `db/schema.sql`.

## Verificacion realizada

Contra Tomcat 10 + MySQL 8 reales: alta de una venta pendiente, alta de item invalida (sin producto, bloqueada) y con cantidad mayor al stock disponible (bloqueada), alta de dos items validos con recalculo de total verificado en cada paso, confirmacion de la venta (stock descontado en ambos productos, verificado en la base), intento de agregar/eliminar items sobre una venta ya confirmada (bloqueado), intento de anular como OPERADOR (bloqueado, boton ademas oculto en la vista) y como ADMINISTRADOR (exitoso, stock restaurado en ambos productos, verificado en la base), intento de anular dos veces (bloqueado en el segundo intento), intento de confirmar una venta sin items (bloqueado), intento de baja de un producto referenciado en `items_venta` (bloqueado), listado con filtro por estado, y las cinco operaciones (alta de venta, altas de item, confirmacion, anulacion) verificadas en `auditoria`.
