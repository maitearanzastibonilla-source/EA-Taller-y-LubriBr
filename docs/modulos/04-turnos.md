# Modulo de Turnos

Spec tecnica condensada. Fuente de verdad: Propuesta Tecnica (seccion "Modulo de Turnos").

## Ficha tecnica

| Campo | Detalle |
|---|---|
| Objetivo | Agendar y seguir los turnos de mecanica y lubricentro |
| Roles con acceso | ADMINISTRADOR y OPERADOR, sin restricciones dentro del modulo (la propuesta no reserva ninguna accion de Turnos a un rol especifico, y ademas describe explicitamente a Turnos como tarea del dia a dia del operador) |
| Modulos relacionados | Clientes, Vehiculos, Trabajos Realizados |
| Tablas | `turnos` |
| Criticidad | Alta |

## Decisiones tomadas ante ambiguedad de la Propuesta Tecnica

La Propuesta dice "solo usuarios autorizados/habilitados" para modificar o cancelar turnos, sin decir cuales. Como la misma Propuesta describe al rol OPERADOR con "acceso a las funciones del dia a dia: turnos, trabajos, ventas...", se interpreto que cualquier usuario autenticado esta "autorizado" — a diferencia de Usuarios/Clientes, donde la Propuesta si dice explicitamente "administrativo".

La capacidad simultanea del taller ("no se podran asignar dos turnos al mismo horario si la capacidad esta completa") tampoco esta cuantificada; se fijo `AppConstants.CAPACIDAD_TALLER_SIMULTANEA = 2` como valor de referencia.

## Reglas de negocio implementadas

1. Alta: cliente y uno de sus vehiculos (validado que el vehiculo pertenezca a ese cliente), fecha y hora, tipo de servicio y estado inicial (solo Pendiente o Confirmado). Se verifica la capacidad del taller en ese horario antes de guardar.
2. Modificacion: se puede reprogramar fecha/hora, tipo de servicio, estado (Pendiente/Confirmado/En proceso/Finalizado) y notas — mientras el turno no este Finalizado ni Cancelado. Si se reprograma a un horario distinto, se vuelve a chequear la capacidad. Cliente y vehiculo quedan fijos desde el alta (la Propuesta no menciona reasignarlos en la modificacion).
3. Cancelacion (baja logica): pasa el turno a Cancelado. Un turno ya Finalizado o Cancelado no puede volver a cancelarse.
4. Alta, modificacion y cancelacion quedan en `auditoria`.

## Maquina de estados

`PENDIENTE -> CONFIRMADO -> EN_PROCESO -> FINALIZADO` (terminal), con `CANCELADO` (terminal) alcanzable desde cualquier estado no terminal mediante la accion Cancelar. Finalizado y Cancelado no se pueden editar ni volver a cancelar.

## Flujo de alta (cliente -> vehiculo en cascada)

El formulario todavia no usa AJAX, asi que elegir un cliente recarga la pagina (`/turnos?accion=nuevo&clienteId=`) para traer solo los vehiculos activos de ese cliente en el segundo select.

## Endpoints

| Metodo | Ruta | Accion | Rol |
|---|---|---|---|
| GET | `/turnos` | Listado (filtros `q`, `estado`, `tipoServicio`, `fechaDesde`, `fechaHasta`, `pagina`) | Cualquier autenticado |
| GET | `/turnos?accion=nuevo` (`&clienteId=`) | Formulario de alta | Cualquier autenticado |
| GET | `/turnos?accion=editar&id=` | Formulario de edicion | Cualquier autenticado |
| POST | `/turnos` (`accion=guardar`) | Alta/Modificacion | Cualquier autenticado |
| POST | `/turnos` (`accion=cancelar`) | Cancelacion | Cualquier autenticado |

## Modelo de datos

`turnos`: `id_turno`, `cliente_id` (FK), `vehiculo_id` (FK), `usuario_id` (FK, responsable de la carga), `fecha_hora` (DATETIME), `tipo_servicio` (ENUM), `estado` (ENUM), `notas`, `created_at`, `updated_at`. Ver `db/schema.sql`.

**Nota tecnica sobre `fecha_hora`:** se persiste con `PreparedStatement.setObject(LocalDateTime)` / `ResultSet.getObject(..., LocalDateTime.class)`, no con `Timestamp`. Usar `Timestamp` ahi hacia que el driver de MySQL reinterpretara la hora segun `serverTimezone` de la URL JDBC y la guardara corrida (una carga a las 10:00 quedaba en 07:00). Si en el futuro se agrega alguna columna datetime de carga manual del usuario en otro modulo, seguir este mismo patron.

## Verificacion realizada

Contra Tomcat 10 + MySQL 8 reales: alta con seleccion de cliente y vehiculo en cascada, verificacion de que la hora guardada coincide exactamente con la ingresada, control de capacidad (2 turnos en el mismo horario permitidos, el 3ro rechazado), rechazo de un vehiculo que no pertenece al cliente elegido, transicion de estados Pendiente -> En proceso -> Finalizado, bloqueo de edicion sobre un turno finalizado, cancelacion y bloqueo de cancelar dos veces, filtro por estado, y acceso completo de un usuario OPERADOR a todas las acciones del modulo.
