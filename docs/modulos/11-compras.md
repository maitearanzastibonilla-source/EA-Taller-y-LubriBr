# Modulo de Compras e Items de Compra

Spec tecnica condensada. Fuente de verdad: Propuesta Tecnica (secciones "Modulo de Compras" e "Modulo de Ítems de Compra").

## Ficha tecnica

| Campo | Detalle |
|---|---|
| Objetivo | Ordenes de compra a proveedores para reponer stock del taller |
| Roles con acceso | ADMINISTRADOR y OPERADOR pueden crear, cargar items y confirmar; solo ADMINISTRADOR puede anular una compra confirmada |
| Modulos relacionados | Proveedores, Productos (stock real), Usuarios (responsable de la compra) |
| Tablas | `compras`, `items_compra` |
| Criticidad | Alta (incrementa stock real) |

## Decisiones de modelado

1. **Estado explicito en la Propuesta**: a diferencia de Ventas Directas, aca el campo `estado` (pendiente/confirmada/anulada) si esta en la lista de columnas de la tabla `compras` que da la Propuesta, asi que no hizo falta ninguna decision de diseño para agregarlo.
2. **Flujo en dos pasos**: igual que Ventas Directas — alta de la compra (cabecera: proveedor, usuario, fecha) y carga de items por separado, con una confirmacion explicita que recien ahi actualiza el stock. La Propuesta lo describe igual ("indicando proveedor, fecha, usuario responsable e items... al confirmar la compra, el stock se actualizara automaticamente").
3. **Sin chequeo de "producto activo" en items_compra**: a diferencia de Ventas Directas, reponer stock de un producto marcado inactivo no esta prohibido por la Propuesta y tiene sentido de negocio (podria reactivarse el producto luego); no se agrego esa restriccion.
4. **Contradiccion resuelta sobre la modificacion de compras confirmadas**: el texto de alta dice "las compras confirmadas no podran modificarse sin autorizacion administrativa" (dejando una puerta abierta a modificacion admin), pero la seccion de validaciones es tajante: "una compra confirmada no podra modificarse". Se opto por la regla mas estricta y explicita (validaciones), consistente con el mismo tratamiento que ya recibio Ventas Directas: una vez confirmada, ni cabecera ni items se pueden tocar, sin excepcion de rol.
5. **Total recalculado en cada cambio de items**: mismo mecanismo que Ventas Directas (`SUM(cantidad * precio_unitario)` recalculado en cada alta/edicion/baja de item).
6. **Cierre de la restriccion pendiente en Productos**: se agrego la verificacion correspondiente a `ProductoServiceImpl.cambiarEstado` (no se puede dar de baja un producto referenciado en una compra), completando la nota que habia quedado en `10-ventas-directas.md`.

## Reglas de negocio implementadas

1. Alta de compra: solo requiere proveedor; queda en PENDIENTE, con total en 0, usuario tomado de la sesion y fecha actual.
2. Modificacion de compra: solo el proveedor, y solo mientras este PENDIENTE.
3. Alta/modificacion/baja de items: solo mientras la compra este PENDIENTE. Cada item exige un producto existente, cantidad entera mayor a cero y precio unitario no negativo (sin chequeo de stock, ya que se esta incrementando).
4. Confirmar compra: exige al menos un item; incrementa el stock de cada producto involucrado y pasa la compra a CONFIRMADA, todo en una transaccion.
5. Anular compra: solo ADMINISTRADOR, solo sobre compras CONFIRMADA; revierte (resta) el stock de cada producto involucrado y pasa la compra a ANULADA.
6. Todas las operaciones (alta/modificacion/items/confirmacion/anulacion) quedan registradas en `auditoria`.

## Endpoints

| Metodo | Ruta | Accion | Rol |
|---|---|---|---|
| GET | `/compras` | Listado con filtros (texto, estado, rango de fechas) | Cualquier autenticado |
| GET | `/compras?accion=nuevo` | Formulario de alta (proveedor) | Cualquier autenticado |
| GET | `/compras?accion=detalle&id=` (`&editarItem=`) | Detalle de la compra + items | Cualquier autenticado |
| POST | `/compras` (`accion=guardar`) | Alta/Modificacion de compra | Cualquier autenticado |
| POST | `/compras` (`accion=confirmar`) | Confirmacion (incrementa stock) | Cualquier autenticado |
| POST | `/compras` (`accion=anular`) | Anulacion (revierte stock) | Solo ADMINISTRADOR |
| POST | `/items-compra` (`accion=guardar` / `accion=eliminar`) | Alta/Modificacion/Baja de item | Cualquier autenticado |

## Modelo de datos

`compras`: `id_compra`, `proveedor_id` (FK), `usuario_id` (FK), `fecha`, `total`, `estado` (ENUM PENDIENTE/CONFIRMADA/ANULADA), `created_at`, `updated_at`.

`items_compra`: `id_item_compra`, `compra_id` (FK, `ON DELETE CASCADE`), `producto_id` (FK obligatoria a `productos`, `ON DELETE RESTRICT`), `cantidad` (INT, `CHECK > 0`), `precio_unitario` (DECIMAL 12,2, `CHECK >= 0`), `created_at`, `updated_at`. Ver `db/schema.sql`.

## Verificacion realizada

Contra Tomcat 10 + MySQL 8 reales: alta de una compra pendiente, alta de item invalida (sin producto, bloqueada), alta de item valida con recalculo de total verificado, confirmacion de la compra (stock incrementado en el producto, verificado en la base), intento de agregar items sobre una compra ya confirmada (bloqueado), intento de anular como OPERADOR (bloqueado, boton ademas oculto en la vista) y como ADMINISTRADOR (exitoso, stock revertido, verificado en la base), intento de anular dos veces (bloqueado en el segundo intento), intento de confirmar una compra sin items (bloqueado), intento de baja de un producto referenciado en `items_compra` (bloqueado), listado con filtro por estado, y las cuatro operaciones (alta, alta de item, confirmacion, anulacion) verificadas en `auditoria`.
