<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<c:set var="tituloPagina" value="Compra" scope="request"/>
<c:set var="subtituloPagina" value="Compra #${compra.idCompra}" scope="request"/>
<c:set var="activeMenu" value="compras" scope="request"/>
<c:set var="esPendiente" value="${compra.estado.name() == 'PENDIENTE'}"/>
<c:set var="esConfirmada" value="${compra.estado.name() == 'CONFIRMADA'}"/>

<c:choose>
    <c:when test="${param.exito == 'item_guardado'}"><c:set var="flashExito" value="Item guardado correctamente." scope="request"/></c:when>
    <c:when test="${param.exito == 'item_eliminado'}"><c:set var="flashExito" value="Item eliminado." scope="request"/></c:when>
    <c:when test="${param.exito == 'modificada'}"><c:set var="flashExito" value="Compra modificada correctamente." scope="request"/></c:when>
    <c:when test="${param.exito == 'confirmada'}"><c:set var="flashExito" value="Compra confirmada. Se incremento el stock de los productos." scope="request"/></c:when>
    <c:when test="${param.exito == 'anulada'}"><c:set var="flashExito" value="Compra anulada. Se revirtio el stock de los productos." scope="request"/></c:when>
</c:choose>
<c:choose>
    <c:when test="${param.error == 'item_invalido'}"><c:set var="flashError" value="Revise los datos del item ingresado." scope="request"/></c:when>
    <c:when test="${param.error == 'no_encontrado'}"><c:set var="flashError" value="El item o la compra indicada no existe." scope="request"/></c:when>
    <c:when test="${param.error == 'csrf'}"><c:set var="flashError" value="La sesion del formulario expiro. Intente nuevamente." scope="request"/></c:when>
    <c:when test="${param.error == 'confirmacion_invalida'}"><c:set var="flashError" value="No se pudo confirmar la compra. Revise que tenga al menos un item." scope="request"/></c:when>
    <c:when test="${param.error == 'no_confirmada'}"><c:set var="flashError" value="Solo se pueden anular compras confirmadas." scope="request"/></c:when>
    <c:when test="${param.error == 'sin_permisos'}"><c:set var="flashError" value="No tiene permisos para realizar esa accion." scope="request"/></c:when>
</c:choose>

<%@ include file="/WEB-INF/jsp/layout/header.jsp" %>

<div class="page-header">
    <div>
        <h1 class="page-header__title">Compra #${compra.idCompra}</h1>
        <p class="page-header__subtitle">Responsable: <c:out value="${compra.usuarioNombre}"/></p>
    </div>
    <div class="page-header__actions">
        <a class="btn btn--secondary" href="${pageContext.request.contextPath}/compras">Volver al listado</a>
    </div>
</div>

<div class="surface surface--padded" style="margin-bottom: var(--space-6);">
    <div class="form-grid">
        <div class="form-field">
            <span class="form-label">Estado</span>
            <div>
                <c:choose>
                    <c:when test="${compra.estado.name() == 'ANULADA'}"><span class="badge badge--inactivo">Anulada</span></c:when>
                    <c:when test="${compra.estado.name() == 'CONFIRMADA'}"><span class="badge badge--activo">Confirmada</span></c:when>
                    <c:otherwise><span class="badge badge--operador">Pendiente</span></c:otherwise>
                </c:choose>
            </div>
        </div>
        <div class="form-field">
            <span class="form-label">Total</span>
            <div><fmt:formatNumber value="${compra.total}" type="currency" currencySymbol="$"/></div>
        </div>
        <div class="form-field form-field--full">
            <span class="form-label">Proveedor</span>
            <c:choose>
                <c:when test="${esPendiente}">
                    <form method="post" action="${pageContext.request.contextPath}/compras" style="display:flex; gap:8px; align-items:center;">
                        <input type="hidden" name="csrfToken" value="${csrfToken}">
                        <input type="hidden" name="accion" value="guardar">
                        <input type="hidden" name="idCompra" value="${compra.idCompra}">
                        <select class="form-control" name="proveedorId" style="max-width:280px;">
                            <c:forEach var="pr" items="${proveedores}">
                                <option value="${pr.idProveedor}" ${compra.proveedorId == pr.idProveedor ? 'selected' : ''}>
                                    <c:out value="${pr.nombre}"/>
                                </option>
                            </c:forEach>
                        </select>
                        <button type="submit" class="btn btn--secondary btn--sm">Guardar</button>
                    </form>
                </c:when>
                <c:otherwise>
                    <div><c:out value="${compra.proveedorNombre}"/></div>
                </c:otherwise>
            </c:choose>
        </div>
    </div>

    <div class="form-actions">
        <c:if test="${esPendiente}">
            <form method="post" action="${pageContext.request.contextPath}/compras"
                  data-confirm="¿Confirmar esta compra? Se incrementara el stock de los productos cargados.">
                <input type="hidden" name="csrfToken" value="${csrfToken}">
                <input type="hidden" name="accion" value="confirmar">
                <input type="hidden" name="id" value="${compra.idCompra}">
                <button type="submit" class="btn btn--primary" ${empty items ? 'disabled' : ''}>Confirmar compra</button>
            </form>
        </c:if>
        <c:if test="${esConfirmada and sessionScope.usuarioLogueado.rol.name() == 'ADMINISTRADOR'}">
            <form method="post" action="${pageContext.request.contextPath}/compras"
                  data-confirm="¿Anular esta compra? Se revertira el stock de los productos.">
                <input type="hidden" name="csrfToken" value="${csrfToken}">
                <input type="hidden" name="accion" value="anular">
                <input type="hidden" name="id" value="${compra.idCompra}">
                <button type="submit" class="btn btn--danger">Anular compra</button>
            </form>
        </c:if>
    </div>
</div>

<h2 class="dashboard-section-title">Productos de la compra</h2>

<c:if test="${esPendiente}">
    <div class="surface surface--padded" style="margin-bottom: var(--space-5); max-width: 720px;">
        <form method="post" action="${pageContext.request.contextPath}/items-compra" data-form="item-compra" novalidate>
            <input type="hidden" name="csrfToken" value="${csrfToken}">
            <input type="hidden" name="accion" value="guardar">
            <input type="hidden" name="compraId" value="${compra.idCompra}">
            <c:if test="${not empty itemEnEdicion}">
                <input type="hidden" name="idItemCompra" value="${itemEnEdicion.idItemCompra}">
            </c:if>

            <div class="form-grid">
                <div class="form-field form-field--full">
                    <label class="form-label" for="productoId">Producto <span class="form-label__required">*</span></label>
                    <select class="form-control" id="productoId" name="productoId" required>
                        <option value="">Seleccionar...</option>
                        <c:forEach var="pd" items="${productos}">
                            <option value="${pd.idProducto}" data-precio="${pd.precioCosto}"
                                    ${itemEnEdicion.productoId == pd.idProducto ? 'selected' : ''}>
                                <c:out value="${pd.nombre}"/> (stock actual: ${pd.stockActual})
                            </option>
                        </c:forEach>
                    </select>
                </div>
                <div class="form-field">
                    <label class="form-label" for="cantidad">Cantidad <span class="form-label__required">*</span></label>
                    <input type="number" class="form-control" id="cantidad" name="cantidad" step="1" min="1"
                           value="${itemEnEdicion.cantidad}" required>
                </div>
                <div class="form-field">
                    <label class="form-label" for="precioUnitario">Precio unitario de costo <span class="form-label__required">*</span></label>
                    <input type="number" class="form-control" id="precioUnitario" name="precioUnitario" step="0.01" min="0"
                           value="${itemEnEdicion.precioUnitario}" required>
                </div>
            </div>

            <div class="form-actions">
                <c:if test="${not empty itemEnEdicion}">
                    <a class="btn btn--secondary" href="${pageContext.request.contextPath}/compras?accion=detalle&id=${compra.idCompra}">Cancelar edicion</a>
                </c:if>
                <button type="submit" class="btn btn--primary">${not empty itemEnEdicion ? 'Guardar cambios' : 'Agregar producto'}</button>
            </div>
        </form>
    </div>
</c:if>

<div class="surface">
    <c:choose>
        <c:when test="${empty items}">
            <div class="empty-state">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6"><path d="M4 6h16M4 12h10M4 18h7"/></svg>
                <h3>Todavia no hay productos cargados</h3>
                <p>Agregue productos del catalogo utilizando el formulario de arriba.</p>
            </div>
        </c:when>
        <c:otherwise>
            <div class="data-table-wrapper">
                <table class="data-table">
                    <thead>
                    <tr>
                        <th>Producto</th>
                        <th>Cantidad</th>
                        <th>Precio unitario</th>
                        <th>Subtotal</th>
                        <th class="col-actions">Acciones</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach var="it" items="${items}">
                        <tr>
                            <td data-label="Producto" class="cell-primary"><c:out value="${it.productoNombre}"/></td>
                            <td data-label="Cantidad">${it.cantidad}</td>
                            <td data-label="Precio unitario"><fmt:formatNumber value="${it.precioUnitario}" type="currency" currencySymbol="$"/></td>
                            <td data-label="Subtotal" class="cell-primary"><fmt:formatNumber value="${it.subtotal}" type="currency" currencySymbol="$"/></td>
                            <td data-label="Acciones" class="col-actions">
                                <c:if test="${esPendiente}">
                                    <div class="row-actions">
                                        <a class="btn btn--icon btn--ghost btn--sm" data-tooltip="Editar"
                                           href="${pageContext.request.contextPath}/compras?accion=detalle&id=${compra.idCompra}&editarItem=${it.idItemCompra}">
                                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M12 20h9"/><path d="M16.5 3.5a2.1 2.1 0 013 3L7 19l-4 1 1-4L16.5 3.5z"/></svg>
                                        </a>
                                        <form method="post" action="${pageContext.request.contextPath}/items-compra"
                                              data-confirm="¿Eliminar este producto de la compra?">
                                            <input type="hidden" name="csrfToken" value="${csrfToken}">
                                            <input type="hidden" name="accion" value="eliminar">
                                            <input type="hidden" name="compraId" value="${compra.idCompra}">
                                            <input type="hidden" name="idItemCompra" value="${it.idItemCompra}">
                                            <button type="submit" class="btn btn--icon btn--ghost btn--sm" data-tooltip="Eliminar">
                                                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M3 6h18M8 6V4a2 2 0 012-2h4a2 2 0 012 2v2m3 0-1 14a2 2 0 01-2 2H8a2 2 0 01-2-2L5 6"/></svg>
                                            </button>
                                        </form>
                                    </div>
                                </c:if>
                            </td>
                        </tr>
                    </c:forEach>
                    </tbody>
                    <tfoot>
                    <tr>
                        <td colspan="3" style="text-align:right; font-weight:600; padding: var(--space-4);">Total</td>
                        <td style="font-weight:700; padding: var(--space-4);"><fmt:formatNumber value="${compra.total}" type="currency" currencySymbol="$"/></td>
                        <td></td>
                    </tr>
                    </tfoot>
                </table>
            </div>
        </c:otherwise>
    </c:choose>
</div>

<script>
    (function () {
        var select = document.getElementById("productoId");
        var precio = document.getElementById("precioUnitario");
        if (!select || !precio) {
            return;
        }
        select.addEventListener("change", function () {
            var opcion = select.options[select.selectedIndex];
            var precioProducto = opcion ? opcion.getAttribute("data-precio") : null;
            if (precioProducto && !precio.value) {
                precio.value = precioProducto;
            }
        });
    })();
</script>

<%@ include file="/WEB-INF/jsp/layout/footer.jsp" %>
