# Modulo de Usuarios

Spec tecnica condensada. Fuente de verdad: Propuesta Tecnica (seccion "Modulo de Usuarios") y Manual Maestro entregados por el cliente.

## Ficha tecnica

| Campo | Detalle |
|---|---|
| Objetivo | Gestionar los perfiles de acceso al sistema (login, roles y estado) |
| Roles con acceso | ADMINISTRADOR (CRUD completo) — OPERADOR (sin acceso a este modulo) |
| Modulos relacionados | Autenticacion (login/logout), Auditoria (transversal) |
| Tablas | `usuarios`, `auditoria` |
| Criticidad | Alta (base de seguridad de todo el sistema) |

## Reglas de negocio implementadas

1. Alta: nombre, email y password obligatorios; email unico (`UNIQUE` + verificacion previa en Service); password con politica minima (8 caracteres, letras y numeros); rol obligatorio (ADMINISTRADOR/OPERADOR).
2. Modificacion: password opcional (si se deja en blanco, se conserva el hash existente); un usuario no puede modificar su propio rol (`UsuarioServiceImpl.actualizar`, `UsuarioSinPermisosException`).
3. Baja: logica (`activo = false`), nunca fisica. Un usuario no puede desactivar su propia cuenta.
4. Autenticacion: bloqueo temporal de cuenta tras 5 intentos fallidos (15 minutos, configurable en `AppConstants`), reseteo de contador al iniciar sesion correctamente.
5. Auditoria: alta, modificacion, activacion/baja y cada intento de login (exitoso o fallido) quedan registrados en la tabla `auditoria`, dentro de la misma transaccion que la operacion de negocio.
6. Solo ADMINISTRADOR accede a `/usuarios/*` (validado en `AuthenticationFilter`, defensa en profundidad ademas del control de UI).

## Arquitectura (MVC + capas)

```
Cliente (JSP + fetch de formularios)
  -> UsuarioServlet / LoginServlet / LogoutServlet   (controller)
      -> UsuarioService / AuthService                (reglas de negocio, transacciones)
          -> UsuarioDAO / AuditoriaDAO                (JDBC, PreparedStatement)
              -> MySQL (usuarios, auditoria)
```

Paquetes: `entity`, `dto`, `dao`/`dao.impl`, `service`/`service.impl`, `controller`, `validator`, `exception`, `utils`, `filter`, `listener`, `constants`.

## Endpoints (Servlets)

| Metodo | Ruta | Accion | Rol |
|---|---|---|---|
| GET | `/login` | Formulario de login | Publico |
| POST | `/login` | Autenticar | Publico |
| GET | `/logout` | Cerrar sesion | Autenticado |
| GET | `/dashboard` | Panel principal | Autenticado |
| GET | `/usuarios` | Listado (filtros: `q`, `rol`, `activo`, `pagina`) | ADMINISTRADOR |
| GET | `/usuarios?accion=nuevo` | Formulario de alta | ADMINISTRADOR |
| GET | `/usuarios?accion=editar&id=` | Formulario de edicion | ADMINISTRADOR |
| POST | `/usuarios` (`accion=guardar`) | Alta/Modificacion | ADMINISTRADOR |
| POST | `/usuarios` (`accion=cambiarEstado`) | Activar/Desactivar | ADMINISTRADOR |

Todos los POST exigen `csrfToken` valido (`CsrfTokenUtils`).

## Modelo de datos

Ver `db/schema.sql`. Tabla `usuarios`: `id_usuario`, `nombre`, `email` (UNIQUE), `password_hash` (BCrypt), `rol` (ENUM), `activo`, `intentos_fallidos`, `bloqueado_hasta`, `created_at`, `updated_at`. Tabla `auditoria`: registro generico reutilizable por todos los modulos futuros (`usuario_id`, `modulo`, `accion`, `entidad_afectada`, `id_registro_afectado`, `valores_anteriores`, `valores_nuevos`, `resultado`, `observaciones`, `fecha_hora`).

## Seguridad

- Password: BCrypt (`org.mindrot.jbcrypt`, factor de costo 12).
- CSRF: token por sesion, validado en todo POST.
- XSS: JSTL `<c:out>` escapa todo dato dinamico en las vistas.
- SQL Injection: 100% `PreparedStatement`, sin concatenacion de SQL.
- Control de acceso: `AuthenticationFilter` (sesion + rol) antes de llegar a cualquier Servlet.
- Auditoria y control de intentos fallidos como se describe arriba.

## Pantallas

- `WEB-INF/jsp/auth/login.jsp` — login standalone (sin sidebar).
- `WEB-INF/jsp/dashboard/dashboard.jsp` — panel principal post-login.
- `WEB-INF/jsp/usuarios/listado.jsp` — tabla con filtros, badges de estado/rol, paginacion, acciones por fila.
- `WEB-INF/jsp/usuarios/form.jsp` — formulario unico para alta y edicion.
- `WEB-INF/jsp/error/{403,404,500}.jsp` — paginas de error con la misma identidad visual.

## Verificacion realizada

Probado extremo a extremo contra una instancia real (Tomcat 10 + MySQL 8) durante el desarrollo:
login correcto/incorrecto, redireccion por estado de sesion, creacion de usuario (con transaccion + auditoria), validaciones (email duplicado, password debil), cambio de estado con proteccion de auto-desactivacion, control de acceso por rol (403 para OPERADOR en `/usuarios`) y bloqueo de cuenta tras 5 intentos fallidos.

## Pendiente / fuera de alcance de esta entrega

Las 40 secciones de documentacion exhaustiva del Manual Maestro (wireframes descriptivos campo por campo, matrices completas, etc.) se resumieron en este documento por acuerdo explicito con el cliente, priorizando entregar el modulo funcionando. Puede ampliarse a pedido.
