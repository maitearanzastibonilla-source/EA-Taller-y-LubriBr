<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="ea" uri="https://eataller.com/tags/functions" %>

<c:set var="tituloPagina" value="Detalle del trabajo" scope="request"/>
<c:set var="subtituloPagina" value="${trabajo.vehiculoPatente} — ${trabajo.propietarioNombre}" scope="request"/>
<c:set var="activeMenu" value="trabajos" scope="request"/>
<c:set var="puedeCargarItems" value="${trabajo.estado.name() != 'FACTURADO'}"/>
<c:set var="puedeEditarItems" value="${trabajo.estado.name() == 'EN_PROCESO'}"/>

<c:choose>
    <c:when test="${param.exito == 'item_guardado'}"><c:set var="flashExito" value="Item guardado correctamente." scope="request"/></c:when>
    <c:when test="${param.exito == 'item_eliminado'}"><c:set var="flashExito" value="Item eliminado." scope="request"/></c:when>
    <c:when test="${param.exito == 'comprobante_generado'}"><c:set var="flashExito" value="Comprobante generado correctamente." scope="request"/></c:when>
    <c:when test="${param.exito == 'comprobante_anulado'}"><c:set var="flashExito" value="Comprobante anulado." scope="request"/></c:when>
</c:choose>
<c:choose>
    <c:when test="${param.error == 'item_invalido'}"><c:set var="flashError" value="Revise los datos del item ingresado." scope="request"/></c:when>
    <c:when test="${param.error == 'no_encontrado'}"><c:set var="flashError" value="El item o el trabajo indicado no existe." scope="request"/></c:when>
    <c:when test="${param.error == 'csrf'}"><c:set var="flashError" value="La sesion del formulario expiro. Intente nuevamente." scope="request"/></c:when>
    <c:when test="${param.error == 'comprobante_invalido'}"><c:set var="flashError" value="No se pudo generar el comprobante. Revise los datos." scope="request"/></c:when>
    <c:when test="${param.error == 'ya_anulado'}"><c:set var="flashError" value="El comprobante ya se encuentra anulado." scope="request"/></c:when>
    <c:when test="${param.error == 'sin_permisos'}"><c:set var="flashError" value="No tiene permisos para realizar esa accion." scope="request"/></c:when>
</c:choose>

<%@ include file="/WEB-INF/jsp/layout/header.jsp" %>

<div class="page-header">
    <div>
        <h1 class="page-header__title">Trabajo — <c:out value="${trabajo.vehiculoPatente}"/></h1>
        <p class="page-header__subtitle"><c:out value="${trabajo.propietarioNombre}"/></p>
    </div>
    <div class="page-header__actions">
        <a class="btn btn--secondary" href="${pageContext.request.contextPath}/trabajos">Volver al listado</a>
    </div>
</div>

<div class="surface surface--padded" style="margin-bottom: var(--space-6);">
    <div class="form-grid">
        <div class="form-field">
            <span class="form-label">Estado</span>
            <div>
                <c:choose>
                    <c:when test="${trabajo.estado.name() == 'EN_PROCESO'}"><span class="badge badge--operador">En proceso</span></c:when>
                    <c:when test="${trabajo.estado.name() == 'FINALIZADO'}"><span class="badge badge--activo">Finalizado</span></c:when>
                    <c:otherwise><span class="badge badge--admin">Facturado</span></c:otherwise>
                </c:choose>
            </div>
        </div>
        <div class="form-field">
            <span class="form-label">Ingreso</span>
            <div>${ea:fechaDia(trabajo.fechaIngreso)}</div>
        </div>
        <div class="form-field">
            <span class="form-label">Egreso</span>
            <div><c:choose><c:when test="${not empty trabajo.fechaEgreso}">${ea:fechaDia(trabajo.fechaEgreso)}</c:when><c:otherwise>—</c:otherwise></c:choose></div>
        </div>
        <div class="form-field form-field--full">
            <span class="form-label">Descripcion</span>
            <div style="white-space: pre-wrap;"><c:out value="${trabajo.descripcion}"/></div>
        </div>
    </div>
</div>

<h2 class="dashboard-section-title">Items del trabajo</h2>

<c:if test="${puedeCargarItems}">
    <div class="surface surface--padded" style="margin-bottom: var(--space-5); max-width: 720px;">
        <form method="post" action="${pageContext.request.contextPath}/items-trabajo" data-form="item-trabajo" novalidate>
            <input type="hidden" name="csrfToken" value="${csrfToken}">
            <input type="hidden" name="accion" value="guardar">
            <input type="hidden" name="trabajoId" value="${trabajo.idTrabajo}">
            <c:if test="${not empty itemEnEdicion}">
                <input type="hidden" name="idItem" value="${itemEnEdicion.idItem}">
            </c:if>

            <div class="form-grid">
                <div class="form-field form-field--full">
                    <label class="form-label" for="descripcionLibre">Descripcion <span class="form-label__required">*</span></label>
                    <input type="text" class="form-control" id="descripcionLibre" name="descripcionLibre" maxlength="300"
                           placeholder="Ej: Mano de obra, filtro de aceite, pastillas de freno..."
                           value="${itemEnEdicion.descripcionLibre}" required>
                </div>
                <div class="form-field">
                    <label class="form-label" for="cantidad">Cantidad <span class="form-label__required">*</span></label>
                    <input type="number" class="form-control" id="cantidad" name="cantidad" step="0.01" min="0.01"
                           value="${itemEnEdicion.cantidad}" required>
                </div>
                <div class="form-field">
                    <label class="form-label" for="precioUnitario">Precio unitario <span class="form-label__required">*</span></label>
                    <input type="number" class="form-control" id="precioUnitario" name="precioUnitario" step="0.01" min="0"
                           value="${itemEnEdicion.precioUnitario}" required>
                </div>
            </div>

            <div class="form-actions">
                <c:if test="${not empty itemEnEdicion}">
                    <a class="btn btn--secondary" href="${pageContext.request.contextPath}/trabajos?accion=detalle&id=${trabajo.idTrabajo}">Cancelar edicion</a>
                </c:if>
                <button type="submit" class="btn btn--primary">${not empty itemEnEdicion ? 'Guardar cambios' : 'Agregar item'}</button>
            </div>
        </form>
    </div>
</c:if>

<div class="surface">
    <c:choose>
        <c:when test="${empty items}">
            <div class="empty-state">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6"><path d="M4 6h16M4 12h10M4 18h7"/></svg>
                <h3>Todavia no hay items cargados</h3>
                <p>Agregue repuestos o mano de obra utilizando el formulario de arriba.</p>
            </div>
        </c:when>
        <c:otherwise>
            <div class="data-table-wrapper">
                <table class="data-table">
                    <thead>
                    <tr>
                        <th>Descripcion</th>
                        <th>Cantidad</th>
                        <th>Precio unitario</th>
                        <th>Subtotal</th>
                        <th class="col-actions">Acciones</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach var="it" items="${items}">
                        <tr>
                            <td data-label="Descripcion" class="cell-primary"><c:out value="${it.descripcionLibre}"/></td>
                            <td data-label="Cantidad">${it.cantidad}</td>
                            <td data-label="Precio unitario"><fmt:formatNumber value="${it.precioUnitario}" type="currency" currencySymbol="$"/></td>
                            <td data-label="Subtotal" class="cell-primary"><fmt:formatNumber value="${it.subtotal}" type="currency" currencySymbol="$"/></td>
                            <td data-label="Acciones" class="col-actions">
                                <div class="row-actions">
                                    <c:if test="${puedeEditarItems}">
                                        <a class="btn btn--icon btn--ghost btn--sm" data-tooltip="Editar"
                                           href="${pageContext.request.contextPath}/trabajos?accion=detalle&id=${trabajo.idTrabajo}&editarItem=${it.idItem}">
                                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M12 20h9"/><path d="M16.5 3.5a2.1 2.1 0 013 3L7 19l-4 1 1-4L16.5 3.5z"/></svg>
                                        </a>
                                    </c:if>
                                    <c:if test="${puedeCargarItems}">
                                        <form method="post" action="${pageContext.request.contextPath}/items-trabajo"
                                              data-confirm="¿Eliminar este item?">
                                            <input type="hidden" name="csrfToken" value="${csrfToken}">
                                            <input type="hidden" name="accion" value="eliminar">
                                            <input type="hidden" name="trabajoId" value="${trabajo.idTrabajo}">
                                            <input type="hidden" name="idItem" value="${it.idItem}">
                                            <button type="submit" class="btn btn--icon btn--ghost btn--sm" data-tooltip="Eliminar">
                                                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M3 6h18M8 6V4a2 2 0 012-2h4a2 2 0 012 2v2m3 0-1 14a2 2 0 01-2 2H8a2 2 0 01-2-2L5 6"/></svg>
                                            </button>
                                        </form>
                                    </c:if>
                                </div>
                            </td>
                        </tr>
                    </c:forEach>
                    </tbody>
                    <tfoot>
                    <tr>
                        <td colspan="3" style="text-align:right; font-weight:600; padding: var(--space-4);">Total</td>
                        <td style="font-weight:700; padding: var(--space-4);"><fmt:formatNumber value="${totalItems}" type="currency" currencySymbol="$"/></td>
                        <td></td>
                    </tr>
                    </tfoot>
                </table>
            </div>
        </c:otherwise>
    </c:choose>
</div>

<h2 class="dashboard-section-title">Comprobante</h2>

<c:choose>
    <c:when test="${not empty comprobante}">
        <div class="surface surface--padded" style="max-width: 720px;">
            <div class="form-grid">
                <div class="form-field">
                    <span class="form-label">Numero</span>
                    <div>#${comprobante.idComprobante}</div>
                </div>
                <div class="form-field">
                    <span class="form-label">Fecha</span>
                    <div>${ea:fechaDia(comprobante.fecha)}</div>
                </div>
                <div class="form-field">
                    <span class="form-label">Total</span>
                    <div><fmt:formatNumber value="${comprobante.total}" type="currency" currencySymbol="$"/></div>
                </div>
                <div class="form-field">
                    <span class="form-label">Metodo de pago</span>
                    <div>${comprobante.metodoPago.etiqueta}</div>
                </div>
                <div class="form-field">
                    <span class="form-label">Estado</span>
                    <div>
                        <c:choose>
                            <c:when test="${comprobante.estado.name() == 'ANULADO'}"><span class="badge badge--inactivo">Anulado</span></c:when>
                            <c:when test="${comprobante.estado.name() == 'COBRADO'}"><span class="badge badge--activo">Cobrado</span></c:when>
                            <c:otherwise><span class="badge badge--operador">${comprobante.estado.etiqueta}</span></c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </div>
            <div class="form-actions">
                <a class="btn btn--secondary" target="_blank"
                   href="${pageContext.request.contextPath}/comprobantes?accion=descargar&id=${comprobante.idComprobante}">Ver PDF</a>
                <c:if test="${comprobante.estado.name() != 'ANULADO' and sessionScope.usuarioLogueado.rol.name() == 'ADMINISTRADOR'}">
                    <form method="post" action="${pageContext.request.contextPath}/comprobantes"
                          data-confirm="¿Anular este comprobante? El trabajo volvera a quedar finalizado sin facturar.">
                        <input type="hidden" name="csrfToken" value="${csrfToken}">
                        <input type="hidden" name="accion" value="anular">
                        <input type="hidden" name="trabajoId" value="${trabajo.idTrabajo}">
                        <input type="hidden" name="idComprobante" value="${comprobante.idComprobante}">
                        <button type="submit" class="btn btn--danger">Anular comprobante</button>
                    </form>
                </c:if>
            </div>
        </div>
    </c:when>
    <c:when test="${trabajo.estado.name() == 'FINALIZADO' and not empty items}">
        <div class="surface surface--padded" style="max-width: 720px;">
            <form method="post" action="${pageContext.request.contextPath}/comprobantes" data-form="comprobante" novalidate>
                <input type="hidden" name="csrfToken" value="${csrfToken}">
                <input type="hidden" name="accion" value="generar">
                <input type="hidden" name="trabajoId" value="${trabajo.idTrabajo}">

                <div class="form-grid">
                    <div class="form-field">
                        <label class="form-label" for="metodoPago">Metodo de pago <span class="form-label__required">*</span></label>
                        <select class="form-control" id="metodoPago" name="metodoPago" required>
                            <option value="">Seleccionar...</option>
                            <option value="EFECTIVO">Efectivo</option>
                            <option value="TRANSFERENCIA">Transferencia</option>
                            <option value="TARJETA">Tarjeta</option>
                            <option value="OTRO">Otro</option>
                        </select>
                    </div>
                    <div class="form-field">
                        <label class="form-label" for="estadoComprobante">Estado <span class="form-label__required">*</span></label>
                        <select class="form-control" id="estadoComprobante" name="estado" required>
                            <option value="">Seleccionar...</option>
                            <option value="PENDIENTE">Pendiente</option>
                            <option value="SENADO">Senado</option>
                            <option value="COBRADO">Cobrado</option>
                        </select>
                    </div>
                </div>

                <div class="form-actions">
                    <button type="submit" class="btn btn--primary">Generar comprobante</button>
                </div>
            </form>
        </div>
    </c:when>
    <c:otherwise>
        <div class="empty-state">
            <p>El comprobante se puede generar una vez que el trabajo este finalizado y tenga items cargados.</p>
        </div>
    </c:otherwise>
</c:choose>

<%@ include file="/WEB-INF/jsp/layout/footer.jsp" %>
