# Modulo de Reportes

Spec tecnica condensada. Fuente de verdad: Propuesta Tecnica (seccion "Modulo de Reportes"). Ultimo modulo de los 14 previstos en la Propuesta.

## Ficha tecnica

| Campo | Detalle |
|---|---|
| Objetivo | Informes consolidados del negocio (operativos, stock, ingresos) exportables a PDF |
| Roles con acceso | Exclusivo ADMINISTRADOR, en todas sus operaciones (la Propuesta restringe el modulo completo, no una accion puntual) |
| Modulos relacionados | Todos: Trabajos, Turnos, Comprobantes, Ventas Directas, Compras, Productos, Clientes |
| Tablas | `reportes` |
| Criticidad | Media (solo lectura/consolidacion; no modifica datos operativos) |

## Los seis reportes previstos

1. **Trabajos realizados por periodo** — listado de trabajos con patente, cliente, ingreso/egreso y estado, filtrado por `fecha_ingreso`.
2. **Ingresos por ventas y servicios** — ventas directas en estado CONFIRMADA + comprobantes en estado COBRADO dentro del periodo, con el total combinado.
3. **Movimientos de stock** — entradas (items de compras CONFIRMADA) y salidas (items de ventas CONFIRMADA + items de trabajo con producto asociado) agrupadas por producto.
4. **Compras realizadas por proveedor** — compras CONFIRMADA agrupadas por proveedor, con cantidad y monto total.
5. **Turnos por estado y periodo** — conteo de turnos agrupados por estado, filtrado por `fecha_hora`.
6. **Ranking de clientes con mayor actividad** — clientes ordenados por la suma de turnos + trabajos en el periodo (top 20).

## Decisiones de modelado

1. **Acceso restringido a nivel de modulo completo**: a diferencia de Comprobantes/Trabajos/Compras/Ventas (donde solo la anulacion es admin-only), aca la Propuesta dice "el acceso a este modulo estara restringido a usuarios administradores" sin matices. Se implemento agregando `/reportes` a la misma lista de prefijos admin-only que ya usaba `/usuarios` en `AuthenticationFilter`, en vez de repetir el chequeo en cada metodo del servicio.
2. **`generarReporte()` y `exportarPDF()` son comportamiento, no columnas**: la Propuesta las lista dentro de "Datos a guardar en la base de datos", pero son funciones. Se implementaron como metodos de `ReporteServiceImpl` y `ReportePdfGenerator` respectivamente; la tabla `reportes` solo guarda los datos reales (tipo, fecha de generacion, usuario, formato, pdf_url).
3. **`fecha_desde`/`fecha_hasta` agregadas a la tabla**: no estan en el listado de columnas de la Propuesta, pero son necesarias para poder reabrir un reporte generado anteriormente y volver a mostrar el mismo periodo (la Propuesta pide poder "acceder a la informacion consolidada utilizada para su generacion", lo que exige conocer que periodo se uso).
4. **Solo transacciones "reales" cuentan para Ingresos/Compras por proveedor**: se filtra por estado CONFIRMADA/COBRADO (no PENDIENTE ni ANULADA), interpretando que un ingreso o compra que nunca se concreto no debe contarse como actividad real del periodo.
5. **Formato de datos generico**: en vez de crear un DTO distinto por cada uno de los 6 tipos de reporte, se diseño `ReporteDatos` (columnas + filas + resumen) como contenedor generico, reutilizado tanto por `ReportePdfGenerator` como por la vista de detalle en pantalla. Evita repetir seis variantes casi identicas de tabla HTML/PDF.
6. **PDF con paginacion real**: a diferencia del PDF de una sola pagina de Comprobantes, `ReportePdfGenerator` agrega paginas automaticamente cuando el contenido no entra en una hoja A4 (relevante para reportes con muchas filas, como Trabajos por periodo en rangos largos).

## Reglas de negocio implementadas

1. Generar reporte: exige tipo de reporte valido y un rango de fechas valido (`fechaDesde <= fechaHasta`), ambas obligatorias. Se consolidan los datos, se genera el PDF, se guarda en `${catalina.base}/reportes-pdf/` y se registra en `auditoria`, todo en una transaccion.
2. Consulta: listado de reportes ya generados, filtrable por tipo, con acceso al PDF y a la vista de datos consolidados (misma consulta que genero el PDF original, ejecutada de nuevo contra los datos actuales).
3. Todo el modulo (listado, alta, detalle, descarga) esta bloqueado para el rol OPERADOR a nivel de filtro de autenticacion.

## Endpoints

| Metodo | Ruta | Accion | Rol |
|---|---|---|---|
| GET | `/reportes` | Listado con filtro por tipo y rango de fecha de generacion | Solo ADMINISTRADOR |
| GET | `/reportes?accion=nuevo` | Formulario de generacion | Solo ADMINISTRADOR |
| GET | `/reportes?accion=detalle&id=` | Datos consolidados + link al PDF | Solo ADMINISTRADOR |
| GET | `/reportes?accion=descargar&id=` | Descarga/visualizacion del PDF | Solo ADMINISTRADOR |
| POST | `/reportes` (`accion=generar`) | Generacion de un nuevo reporte | Solo ADMINISTRADOR |

## Modelo de datos

`reportes`: `id_reporte`, `tipo_reporte` (ENUM con los 6 tipos), `fecha_generacion`, `usuario_generador_id` (FK), `formato` (siempre 'PDF'), `fecha_desde`, `fecha_hasta`, `pdf_url`. Ver `db/schema.sql`.

## Verificacion realizada

Contra Tomcat 10 + MySQL 8 reales, con datos de prueba cargados en Clientes/Vehiculos/Turnos/Trabajos/Comprobantes/Proveedores/Productos/Ventas Directas/Compras: generacion de los 6 tipos de reporte, verificando en cada caso que el PDF generado es un archivo valido y no vacio, y que los datos consolidados mostrados en pantalla coinciden con los datos reales cargados (conteos y totales verificados contra la base). Validacion de formulario (tipo/fechas obligatorios, fecha desde posterior a fecha hasta bloqueada). Descarga de PDF mediante el endpoint del servlet. Acceso de un usuario OPERADOR bloqueado con 403 al intentar `/reportes` directamente, y confirmado que el enlace no aparece ni en el sidebar ni en el dashboard para ese rol. Las seis generaciones quedaron registradas en `auditoria`.
