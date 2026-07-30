# Modulo de Comprobantes

Spec tecnica condensada. Fuente de verdad: Propuesta Tecnica (seccion "Modulo de Comprobantes").

## Ficha tecnica

| Campo | Detalle |
|---|---|
| Objetivo | Emision del comprobante de cobro de un trabajo finalizado, con generacion real de PDF |
| Roles con acceso | ADMINISTRADOR y OPERADOR pueden generar y consultar; solo ADMINISTRADOR puede anular |
| Modulos relacionados | Trabajos Realizados (cierra el ciclo En proceso -> Finalizado -> Facturado) |
| Tablas | `comprobantes` |
| Criticidad | Alta (formaliza el cobro y deja el respaldo documental) |

## Decisiones tomadas

1. **Generacion de PDF real**: se eligio Apache PDFBox (licencia Apache 2.0) en lugar de iText, que la Propuesta menciona como alternativa pero que exige licencia comercial o AGPL para uso no restringido. PDFBox no tiene esa limitacion.
2. **Ubicacion del archivo**: el PDF se guarda en `${catalina.base}/comprobantes-pdf/`, fuera del directorio donde se despliega el WAR, para que sobreviva un redeploy. La ruta absoluta queda en `comprobantes.pdf_url`.
3. **Cierre del estado Facturado**: el modulo de Trabajos Realizados dejo pendiente quien fija el estado `FACTURADO`. Ese punto se cierra aca: `generar()` marca el trabajo como facturado y `anular()` lo revierte a `FINALIZADO`, usando los metodos `marcarFacturado`/`desmarcarFacturado` agregados a `TrabajoDAO`.
4. **Estado inicial del comprobante**: la Propuesta describe los estados Pendiente/Senado/Cobrado/Anulado sin aclarar cual es el inicial. Se dejo como decision del usuario que genera el comprobante (selecciona Pendiente, Senado o Cobrado al momento de generar); `Anulado` queda reservado exclusivamente para la accion de anulacion administrativa.

## Reglas de negocio implementadas

1. Solo se puede generar un comprobante sobre un trabajo en estado `FINALIZADO`.
2. El trabajo debe tener al menos un item cargado (el total del comprobante sale de la suma de subtotales de los items).
3. No puede existir mas de un comprobante activo (no anulado) por trabajo.
4. Al generar: se calcula el total, se persiste el comprobante, se genera el PDF, se actualiza `pdf_url` y se marca el trabajo como `FACTURADO` — todo dentro de una unica transaccion (si falla la generacion del PDF, se revierte todo).
5. Anular un comprobante es exclusivo de ADMINISTRADOR. No se puede anular un comprobante ya anulado. Al anular, el trabajo vuelve a `FINALIZADO`, quedando habilitado para generar un nuevo comprobante.
6. Alta y anulacion quedan registradas en `auditoria`.

## Integracion con Trabajos Realizados

En `/trabajos?accion=detalle&id=` se agrego una seccion "Comprobante" que muestra, segun el caso: el formulario de generacion (metodo de pago + estado inicial), si el trabajo esta finalizado, tiene items y no tiene comprobante activo; los datos del comprobante vigente (numero, fecha, total, metodo de pago, estado, link al PDF) mas el boton de anular si corresponde; o un aviso de que todavia no se puede generar.

## Endpoints

| Metodo | Ruta | Accion | Rol |
|---|---|---|---|
| GET | `/comprobantes` | Listado con filtros (texto, estado, rango de fechas) | Cualquier autenticado |
| GET | `/comprobantes?accion=descargar&id=` | Descarga/visualizacion del PDF | Cualquier autenticado |
| POST | `/comprobantes` (`accion=generar`) | Generacion de comprobante para un trabajo | Cualquier autenticado |
| POST | `/comprobantes` (`accion=anular`) | Anulacion de comprobante | Solo ADMINISTRADOR |

## Modelo de datos

`comprobantes`: `id_comprobante`, `trabajo_id` (FK a `trabajos_realizados`, `ON DELETE RESTRICT`), `fecha` (DATE), `total` (DECIMAL 12,2, `CHECK >= 0`), `metodo_pago` (ENUM: EFECTIVO/TRANSFERENCIA/TARJETA/OTRO), `estado` (ENUM: PENDIENTE/SENADO/COBRADO/ANULADO), `pdf_url` (ruta absoluta del PDF), `created_at`, `updated_at`. Ver `db/schema.sql`.

## Verificacion realizada

Contra Tomcat 10 + MySQL 8 reales: generacion de comprobante sobre un trabajo finalizado con items (PDF verificado en disco, valido y no vacio; trabajo pasa a Facturado), intento de generar un segundo comprobante sobre el mismo trabajo (bloqueado), intento de generar sobre un trabajo en proceso (bloqueado), intento de generar sobre un trabajo finalizado sin items (bloqueado), descarga del PDF por el endpoint del servlet, listado con sus filtros, anulacion intentada por un usuario OPERADOR (bloqueada, boton ademas oculto en la vista), anulacion por ADMINISTRADOR (exitosa, trabajo vuelve a Finalizado, nuevo intento de generacion vuelve a habilitarse), intento de anular un comprobante ya anulado (bloqueado), y las dos entradas de auditoria (alta y anulacion) verificadas en la base.
