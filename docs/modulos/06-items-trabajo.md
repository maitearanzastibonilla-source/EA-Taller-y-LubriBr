# Modulo de Items de Trabajo

Spec tecnica condensada. Fuente de verdad: Propuesta Tecnica (seccion "Modulo de Items de Trabajo").

## Ficha tecnica

| Campo | Detalle |
|---|---|
| Objetivo | Detalle de repuestos y mano de obra de cada trabajo (lineas que forman el costo del servicio) |
| Roles con acceso | ADMINISTRADOR y OPERADOR, sin restricciones (la Propuesta no reserva nada de este modulo a un rol especifico) |
| Modulos relacionados | Trabajos Realizados; a futuro, Productos (stock) y Comprobantes |
| Tablas | `items_trabajo` |
| Criticidad | Media-alta (define el costo del trabajo) |

## Decision tomada ante un problema de orden en la Propuesta Tecnica

La Propuesta ubica a Items de Trabajo antes que Productos en su lista de modulos, pero el campo `producto_id` de este modulo depende del catalogo de Productos (que todavia no existe) para el descuento de stock. Como `producto_id` es nullable en el propio diseño de la Propuesta, se implemento este modulo hoy trabajando exclusivamente por `descripcion_libre` (mano de obra u otros servicios). La columna `producto_id` ya existe en la tabla pero sin FK ni logica de stock; se completaran cuando se desarrolle el Modulo de Productos.

## Reglas de negocio implementadas

1. Alta: descripcion, cantidad (> 0) y precio unitario (>= 0) obligatorios. Permitida mientras el trabajo no este Facturado (es decir, tambien con el trabajo ya Finalizado, tal como lo dice la Propuesta: la restriccion explicita es solo "no facturado").
2. Modificacion: solo mientras el trabajo este En proceso (regla mas estricta que la de alta, tal cual la describe la Propuesta).
3. Baja: eliminacion fisica (no logica — la Propuesta habla de "eliminar", nunca de baja logica para este modulo), permitida hasta que el trabajo se facture. Se deja una entrada en auditoria con los valores del item eliminado, ya que la fila en si desaparece.
4. Cada alta, modificacion y baja de item queda en `auditoria`.

## Integracion con Trabajos Realizados

Se agrego una pantalla de detalle del trabajo (`/trabajos?accion=detalle&id=`), accesible desde el listado de Trabajos ("Ver items"), que muestra la ficha del trabajo, el listado de items con su subtotal y el total general, mas un formulario para agregar o editar un item (edicion via `?editarItem=`).

## Endpoints

| Metodo | Ruta | Accion | Rol |
|---|---|---|---|
| GET | `/trabajos?accion=detalle&id=` (`&editarItem=`) | Detalle del trabajo + sus items | Cualquier autenticado |
| POST | `/items-trabajo` (`accion=guardar`) | Alta/Modificacion de item | Cualquier autenticado |
| POST | `/items-trabajo` (`accion=eliminar`) | Baja fisica de item | Cualquier autenticado |

## Modelo de datos

`items_trabajo`: `id_item`, `trabajo_id` (FK, `ON DELETE CASCADE`), `producto_id` (sin FK todavia), `descripcion_libre`, `cantidad` (DECIMAL 10,2, `CHECK > 0`), `precio_unitario` (DECIMAL 12,2, `CHECK >= 0`), `created_at`, `updated_at`. Ver `db/schema.sql`.

## Verificacion realizada

Contra Tomcat 10 + MySQL 8 reales: alta de dos items sobre un trabajo en proceso, edicion de un item, finalizacion del trabajo, edicion bloqueada tras finalizar, alta de un item nuevo permitida sobre un trabajo finalizado, bloqueo de alta y de baja al pasar el trabajo a facturado (forzando el estado en la base para probar la regla, ya que hoy no existe un flujo que lo pase a facturado), baja de un item permitida mientras el trabajo esta finalizado (pero no facturado), y auditoria completa de las cinco operaciones.
