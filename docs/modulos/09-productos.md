# Modulo de Productos

Spec tecnica condensada. Fuente de verdad: Propuesta Tecnica (seccion "Modulo de Productos").

## Ficha tecnica

| Campo | Detalle |
|---|---|
| Objetivo | Catalogo de repuestos e insumos del taller, con precios y control de stock |
| Roles con acceso | ADMINISTRADOR y OPERADOR, sin restricciones (la Propuesta no reserva ninguna operacion a un rol especifico) |
| Modulos relacionados | Proveedores (FK obligatoria), Items de Trabajo (FK ya conectada), a futuro Ventas Directas/Items de Venta y Compras/Items de Compra |
| Tablas | `productos` |
| Criticidad | Alta (habilita el resto de la cadena de venta/compra con stock real) |

## Decisiones de modelado

1. **Orden de construccion**: este modulo se adelanto respecto de la Propuesta (ver `docs/modulos/08-proveedores.md`) porque Ventas Directas/Items de Venta lo necesitan para cumplir su propia regla de "no vender mas stock del disponible".
2. **`categoria` como texto libre**: la Propuesta da ejemplos abiertos ("repuesto, lubricante, consumible, etc.") en vez de una lista cerrada, asi que se modelo como VARCHAR en vez de ENUM.
3. **`precio_costo` opcional**: la seccion de validaciones de la Propuesta solo exige nombre, precio de venta y proveedor; precio de costo no es obligatorio.
4. **`stock_actual` no se edita desde el formulario de modificacion**: la Propuesta describe la modificacion como "actualizar precios, descripciones y cantidades minimas", sin mencionar el stock actual. El stock inicial se carga solo en el alta; los movimientos posteriores (ventas, compras) son responsabilidad de esos modulos cuando existan.
5. **Se cierra la FK pendiente de Items de Trabajo**: `items_trabajo.producto_id` ya existia sin restriccion (ver `docs/modulos/06-items-trabajo.md`); ahora que `productos` existe, se agrego la FK (`fk_items_trabajo_producto`). No cambia el comportamiento actual: el formulario de Items de Trabajo sigue trabajando solo con `descripcion_libre`; esto es unicamente integridad referencial para el dia que se conecte la seleccion real de productos en ese formulario.

## Reglas de negocio implementadas

1. Alta: nombre, proveedor y precio de venta (> 0) obligatorios; categoria, descripcion y precio de costo opcionales; stock inicial y stock minimo obligatorios (enteros, >= 0).
2. Modificacion: permite cambiar proveedor, nombre, descripcion, categoria, precios y stock minimo. No permite tocar el stock actual.
3. Baja logica: bloqueada si el producto esta referenciado en `items_trabajo` (verificado con una consulta antes de aceptar la baja). La restriccion equivalente contra ventas y compras queda pendiente hasta que existan esas tablas.
4. Alerta de stock bajo: un producto se marca visualmente cuando `stock_actual <= stock_minimo`.
5. Alta, modificacion y cambio de estado quedan registrados en `auditoria`.

## Endpoints

| Metodo | Ruta | Accion | Rol |
|---|---|---|---|
| GET | `/productos` | Listado con filtros (texto, estado) y alerta de stock bajo | Cualquier autenticado |
| GET | `/productos?accion=nuevo` / `accion=editar&id=` | Formulario de alta/edicion | Cualquier autenticado |
| POST | `/productos` (`accion=guardar`) | Alta/Modificacion | Cualquier autenticado |
| POST | `/productos` (`accion=cambiarEstado`) | Baja logica / reactivacion | Cualquier autenticado |

## Modelo de datos

`productos`: `id_producto`, `proveedor_id` (FK a `proveedores`, `ON DELETE RESTRICT`), `nombre`, `descripcion` (nullable), `categoria` (nullable), `precio_venta` (DECIMAL 12,2, `CHECK > 0`), `precio_costo` (nullable, `CHECK >= 0`), `stock_actual` (INT, `CHECK >= 0`), `stock_minimo` (INT, `CHECK >= 0`), `estado` (ENUM ACTIVO/INACTIVO), `created_at`, `updated_at`. Ver `db/schema.sql`, que ademas agrega `fk_items_trabajo_producto` sobre la tabla ya existente `items_trabajo`.

## Verificacion realizada

Contra Tomcat 10 + MySQL 8 reales: alta de un producto con proveedor existente, alta invalida (nombre/proveedor/precio vacios, los tres errores mostrados), listado con alerta "Bajo" cuando `stock_actual <= stock_minimo` (y su desaparicion al subir el minimo), modificacion de precio y stock minimo (sin tocar stock actual), intento de baja de un producto referenciado por un item de trabajo (bloqueado por la FK/consulta), baja del mismo producto luego de quitar esa referencia (permitida), reactivacion, acceso de un usuario OPERADOR al modulo (sin bloqueo), y las cuatro operaciones auditadas.
