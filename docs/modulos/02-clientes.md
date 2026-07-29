# Modulo de Clientes

Spec tecnica condensada. Fuente de verdad: Propuesta Tecnica (seccion "Modulo de Clientes").

## Ficha tecnica

| Campo | Detalle |
|---|---|
| Objetivo | Registrar y administrar los clientes del taller |
| Roles con acceso | ADMINISTRADOR y OPERADOR (alta/consulta/modificacion); solo ADMINISTRADOR puede dar de baja o tocar el DNI de un cliente ya cargado |
| Modulos relacionados | Vehiculos, Turnos, Trabajos, Comprobantes (todos referencian a un cliente) |
| Tablas | `clientes` |
| Criticidad | Alta |

## Reglas de negocio implementadas

1. Alta: nombre, apellido, DNI y telefono obligatorios; email y direccion opcionales. DNI (7-8 digitos) y telefono no pueden repetirse entre clientes.
2. Modificacion: cualquier campo editable por ambos roles, salvo el DNI, que un OPERADOR no puede cambiar (el campo queda en solo lectura en el formulario y, ademas, `ClienteServiceImpl.actualizar` lo rechaza aunque llegue un DNI distinto en el POST).
3. Baja: logica (`estado = INACTIVO`), reservada a ADMINISTRADOR.
4. Cada alta, modificacion y cambio de estado queda registrado en `auditoria`.

Nota: la Propuesta Tecnica tambien pide bloquear la baja de un cliente con vehiculos activos, turnos pendientes o saldo en cuenta corriente. Esa validacion se agrega cuando existan esas tablas (Vehiculos, Turnos, Cuenta Corriente); por ahora no hay nada contra lo que verificar.

## Endpoints

| Metodo | Ruta | Accion | Rol |
|---|---|---|---|
| GET | `/clientes` | Listado (filtros `q`, `estado`, `pagina`) | Cualquier autenticado |
| GET | `/clientes?accion=nuevo` | Formulario de alta | Cualquier autenticado |
| GET | `/clientes?accion=editar&id=` | Formulario de edicion | Cualquier autenticado |
| POST | `/clientes` (`accion=guardar`) | Alta/Modificacion | Cualquier autenticado (DNI solo ADMINISTRADOR) |
| POST | `/clientes` (`accion=cambiarEstado`) | Baja/Reactivacion | ADMINISTRADOR |

## Modelo de datos

`clientes`: `id_cliente`, `nombre`, `apellido`, `dni` (UNIQUE), `email`, `telefono`, `direccion`, `estado` (ENUM ACTIVO/INACTIVO), `created_at`, `updated_at`. Ver `db/schema.sql`.

## Verificacion realizada

Contra Tomcat 10 + MySQL 8 reales: alta y listado de cliente, DNI duplicado, telefono duplicado, formato de DNI invalido, edicion como administrador (incluye cambio de DNI), intento de cambio de DNI y de baja por un usuario OPERADOR (rechazados por el Service aunque se fuerce el POST a mano), baja y reactivacion por administrador, y auditoria de las tres operaciones.
