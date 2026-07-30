# Modulo de Trabajos Realizados

Spec tecnica condensada. Fuente de verdad: Propuesta Tecnica (seccion "Modulo de Trabajos Realizados").

## Ficha tecnica

| Campo | Detalle |
|---|---|
| Objetivo | Registrar el trabajo hecho sobre un vehiculo, originado en un turno o cargado directamente |
| Roles con acceso | ADMINISTRADOR y OPERADOR para alta/modificacion/finalizar; solo ADMINISTRADOR para reabrir un trabajo finalizado y para la baja logica |
| Modulos relacionados | Vehiculos, Turnos, (a futuro) Items de Trabajo y Comprobantes |
| Tablas | `trabajos_realizados` |
| Criticidad | Alta |

## Decision tomada ante un vacio del modelo de datos

La Propuesta describe la baja del trabajo como logica y restringida a administradores, pero el enum de `estado` que ella misma define (`en_proceso / finalizado / facturado`) no tiene un valor para "dado de baja". Se agrego una columna `activo` (booleana) separada de `estado`, con el mismo criterio que ya usa Usuarios (rol y activo son cosas distintas): `estado` sigue representando la etapa del trabajo, `activo` representa si esta administrativamente suprimido. El listado solo muestra `activo = TRUE`.

## Reglas de negocio implementadas

1. Alta: vehiculo obligatorio; turno opcional (si se indica, se valida que sea un turno de ese mismo vehiculo). Fecha de ingreso y descripcion obligatorias. Siempre arranca en estado En proceso.
2. Modificacion: fecha de ingreso y descripcion, solo mientras este En proceso.
3. Finalizacion: pide fecha de egreso (no puede ser anterior al ingreso); solo permitida desde En proceso. Un trabajo Finalizado no admite mas cambios.
4. Reapertura: exclusiva de ADMINISTRADOR, solo desde Finalizado, vuelve a En proceso y borra la fecha de egreso.
5. Baja logica: exclusiva de ADMINISTRADOR (`activo = false`), independiente del `estado`.
6. Toda operacion queda en `auditoria`.

Pendiente de conectar cuando existan los modulos correspondientes: "no cerrar sin al menos un item registrado" (Items de Trabajo) y "no eliminar con comprobantes o movimientos economicos" (Comprobantes).

## Flujo de alta (dos caminos)

- **Desde un turno:** boton "Iniciar trabajo" en el listado de Turnos (`/trabajos?accion=nuevo&turnoId=`), que precarga el vehiculo del turno y deja cliente/vehiculo fijos.
- **Manual:** `/trabajos?accion=nuevo`, con la misma cascada cliente → vehiculo que ya usa el alta de Turnos.

## Endpoints

| Metodo | Ruta | Accion | Rol |
|---|---|---|---|
| GET | `/trabajos` | Listado (filtros `q`, `estado`, `pagina`) | Cualquier autenticado |
| GET | `/trabajos?accion=nuevo` (`&clienteId=` o `&turnoId=`) | Formulario de alta | Cualquier autenticado |
| GET | `/trabajos?accion=editar&id=` | Formulario de edicion | Cualquier autenticado |
| POST | `/trabajos` (`accion=guardar`) | Alta/Modificacion | Cualquier autenticado |
| POST | `/trabajos` (`accion=finalizar`) | Cierre del trabajo | Cualquier autenticado |
| POST | `/trabajos` (`accion=reabrir`) | Reapertura | ADMINISTRADOR |
| POST | `/trabajos` (`accion=baja`) | Baja logica | ADMINISTRADOR |

## Modelo de datos

`trabajos_realizados`: `id_trabajo`, `vehiculo_id` (FK), `turno_id` (FK nullable), `usuario_id` (FK, responsable de la carga), `fecha_ingreso`, `fecha_egreso`, `descripcion`, `estado` (ENUM), `activo`, `created_at`, `updated_at`. Ver `db/schema.sql`.

## Verificacion realizada

Contra Tomcat 10 + MySQL 8 reales: alta desde un turno (con precarga del vehiculo), alta manual con cascada cliente→vehiculo, finalizacion con fecha de egreso invalida (anterior al ingreso, rechazada) y valida, bloqueo de edicion sobre un trabajo finalizado, reapertura bloqueada para un usuario OPERADOR (incluso forzando el POST con un token CSRF valido de su propia sesion) y permitida para ADMINISTRADOR, baja logica y su desaparicion del listado, y auditoria completa de las cinco operaciones (alta, finalizacion, reapertura, nueva finalizacion, baja).

Tambien se detecto y corrigio en este modulo un error de tipos en la funcion EL de formato de fechas: `ea:fecha` esperaba `LocalDateTime` y las columnas de este modulo son `LocalDate`. Se agrego `ea:fechaDia` para fechas sin hora (ver `JspFunctions.java` y `ea-functions.tld`).
