# Modulo de Vehiculos

Spec tecnica condensada. Fuente de verdad: Propuesta Tecnica (seccion "Modulo de Vehiculos").

## Ficha tecnica

| Campo | Detalle |
|---|---|
| Objetivo | Registrar los vehiculos de cada cliente y su historial operativo |
| Roles con acceso | ADMINISTRADOR y OPERADOR (alta/consulta/modificacion); solo ADMINISTRADOR puede tocar la patente de un vehiculo ya cargado |
| Modulos relacionados | Clientes (propietario), Turnos, Trabajos Realizados, Comprobantes |
| Tablas | `vehiculos` |
| Criticidad | Alta |

## Reglas de negocio implementadas

1. Alta: patente, marca, modelo y anio obligatorios, siempre asociados a un cliente existente. La patente es unica en el sistema (formato AAA000 o AA000AA, validado en Frontend, Backend y por el `UNIQUE` de la tabla).
2. Modificacion: cualquier campo editable por ambos roles, salvo la patente — un OPERADOR no puede cambiarla (campo en solo lectura en el formulario y rechazada igual en `VehiculoServiceImpl.actualizar` si llega modificada en el POST).
3. Baja: logica (`estado = INACTIVO`). A diferencia de Usuarios y Clientes, la Propuesta Tecnica no restringe esta accion a un rol especifico, asi que cualquier usuario autenticado puede darla de baja o reactivarla.
4. Alta, modificacion y cambio de estado quedan en `auditoria`.

Nota: igual que en Clientes, la restriccion de "no dar de baja un vehiculo con turnos activos o trabajos pendientes" queda pendiente de conectar cuando existan esos modulos.

## Flujo de alta

Desde el propio modulo (`/vehiculos?accion=nuevo`) o desde la ficha de un cliente activo en el listado de Clientes (boton "Agregar vehiculo", que precompleta el propietario). El propietario se elige de un select con todos los clientes activos.

## Endpoints

| Metodo | Ruta | Accion | Rol |
|---|---|---|---|
| GET | `/vehiculos` | Listado (filtros `q`, `estado`, `pagina`) | Cualquier autenticado |
| GET | `/vehiculos?accion=nuevo` (`&clienteId=`) | Formulario de alta | Cualquier autenticado |
| GET | `/vehiculos?accion=editar&id=` | Formulario de edicion | Cualquier autenticado |
| POST | `/vehiculos` (`accion=guardar`) | Alta/Modificacion | Cualquier autenticado (patente solo ADMINISTRADOR) |
| POST | `/vehiculos` (`accion=cambiarEstado`) | Baja/Reactivacion | Cualquier autenticado |

## Modelo de datos

`vehiculos`: `id_vehiculo`, `cliente_id` (FK a `clientes`, `ON DELETE RESTRICT`), `patente` (UNIQUE), `marca`, `modelo`, `anio`, `estado` (ENUM ACTIVO/INACTIVO), `created_at`, `updated_at`. Ver `db/schema.sql`.

## Verificacion realizada

Contra Tomcat 10 + MySQL 8 reales: alta de vehiculo con selector de cliente, listado con nombre del propietario (join contra `clientes`), patente duplicada, anio fuera de rango, edicion de patente bloqueada para OPERADOR (incluso forzando el POST), edicion de patente permitida para ADMINISTRADOR, baja y reactivacion por un usuario OPERADOR (sin bloqueo, segun lo definido), atajo "Agregar vehiculo" desde el listado de Clientes, y auditoria completa de las tres operaciones.
