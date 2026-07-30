# Modulo de Proveedores

Spec tecnica condensada. Fuente de verdad: Propuesta Tecnica (seccion "Modulo de Proveedores").

## Ficha tecnica

| Campo | Detalle |
|---|---|
| Objetivo | Registro centralizado de proveedores de repuestos e insumos del taller |
| Roles con acceso | ADMINISTRADOR y OPERADOR, sin restricciones (la Propuesta no reserva ninguna operacion de este modulo a un rol especifico) |
| Modulos relacionados | Productos (FK obligatoria a futuro), Compras |
| Tablas | `proveedores` |
| Criticidad | Media (habilita el resto de la cadena de abastecimiento) |

## Decision de orden de construccion

La Propuesta ubica a Proveedores despues de Ventas Directas e Items de Venta en su lista de modulos. Al analizar Items de Venta se encontro que ese modulo exige que cada item este vinculado a un producto existente y que se valide/descuente stock real — sin el "escape" de descripcion libre que si tiene Items de Trabajo. Como Productos requiere a su vez un proveedor obligatorio, no es posible construir Ventas Directas de forma honesta sin antes tener Proveedores y Productos reales. Se reordeno la construccion (con autorizacion del cliente) para resolver esta dependencia real, en lugar de simular un catalogo o descuento de stock que no existe.

## Reglas de negocio implementadas

1. Alta: nombre y telefono obligatorios (dato basico de contacto); contacto (persona) y email opcionales, email validado por formato si se completa.
2. Modificacion: sin restricciones de rol (a diferencia de Clientes, la Propuesta no marca ningun dato de Proveedores como sensible).
3. Baja: logica (Activo/Inactivo), reversible, sin restriccion de rol segun el texto de la Propuesta.
4. Consulta: busqueda por nombre, contacto, telefono o email, con filtro de estado y paginacion.
5. Alta, modificacion y cambio de estado quedan registrados en `auditoria`.

## Restriccion pendiente de conectar

La Propuesta indica "no se podran eliminar proveedores asociados a productos activos o compras registradas". Esa validacion queda pendiente de conectar cuando existan las tablas `productos` y `compras` (mismo criterio ya aplicado en Clientes con Vehiculos/Turnos/Trabajos).

## Endpoints

| Metodo | Ruta | Accion | Rol |
|---|---|---|---|
| GET | `/proveedores` | Listado con filtros (texto, estado) | Cualquier autenticado |
| GET | `/proveedores?accion=nuevo` / `accion=editar&id=` | Formulario de alta/edicion | Cualquier autenticado |
| POST | `/proveedores` (`accion=guardar`) | Alta/Modificacion | Cualquier autenticado |
| POST | `/proveedores` (`accion=cambiarEstado`) | Baja logica / reactivacion | Cualquier autenticado |

## Modelo de datos

`proveedores`: `id_proveedor`, `nombre`, `contacto` (nullable), `telefono`, `email` (nullable), `estado` (ENUM ACTIVO/INACTIVO, reutiliza el enum `Estado` compartido), `created_at`, `updated_at`. Ver `db/schema.sql`.

## Verificacion realizada

Contra Tomcat 10 + MySQL 8 reales: alta de un proveedor, alta invalida (nombre y telefono vacios, ambos errores mostrados), modificacion de datos, baja logica y reactivacion, filtro de listado por estado, y verificacion de que un usuario OPERADOR puede realizar alta/baja sin bloqueo (consistente con que la Propuesta no restringe ningun rol para este modulo). Las cuatro operaciones (alta, modificacion, baja, activacion) quedaron registradas en `auditoria`.
