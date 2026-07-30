<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="ea" uri="https://eataller.com/tags/functions" %>

<c:set var="tituloPagina" value="Trabajos" scope="request"/>
<c:set var="subtituloPagina" value="Trabajos realizados sobre los vehiculos del taller" scope="request"/>
<c:set var="activeMenu" value="trabajos" scope="request"/>

<c:choose>
    <c:when test="${param.exito == 'creado'}"><c:set var="flashExito" value="Trabajo creado correctamente." scope="request"/></c:when>
    <c:when test="${param.exito == 'modificado'}"><c:set var="flashExito" value="Trabajo modificado correctamente." scope="request"/></c:when>
    <c:when test="${param.exito == 'finalizado'}"><c:set var="flashExito" value="Trabajo finalizado." scope="request"/></c:when>
    <c:when test="${param.exito == 'reabierto'}"><c:set var="flashExito" value="Trabajo reabierto." scope="request"/></c:when>
    <c:when test="${param.exito == 'baja'}"><c:set var="flashExito" value="Trabajo dado de baja." scope="request"/></c:when>
</c:choose>
<c:choose>
    <c:when test="${param.error == 'no_encontrado'}"><c:set var="flashError" value="El trabajo solicitado no existe." scope="request"/></c:when>
    <c:when test="${param.error == 'sin_permisos'}"><c:set var="flashError" value="No tiene permisos para realizar esa accion." scope="request"/></c:when>
    <c:when test="${param.error == 'fecha_egreso'}"><c:set var="flashError" value="Revise la fecha de egreso ingresada." scope="request"/></c:when>
    <c:when test="${param.error == 'no_finalizado'}"><c:set var="flashError" value="Solo se pueden reabrir trabajos finalizados." scope="request"/></c:when>
    <c:when test="${param.error == 'csrf'}"><c:set var="flashError" value="La sesion del formulario expiro. Intente nuevamente." scope="request"/></c:when>
</c:choose>

<%@ include file="/WEB-INF/jsp/layout/header.jsp" %>

<div class="page-header">
    <div>
        <h1 class="page-header__title">Trabajos</h1>
        <p class="page-header__subtitle">Seguimiento de los trabajos realizados por vehiculo.</p>
    </div>
    <div class="page-header__actions">
        <a class="btn btn--primary" href="${pageContext.request.contextPath}/trabajos?accion=nuevo">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><path d="M12 5v14M5 12h14"/></svg>
            Nuevo trabajo
        </a>
    </div>
</div>

<div class="surface">
    <form method="get" action="${pageContext.request.contextPath}/trabajos" class="filters-bar">
        <div class="search-input">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><circle cx="11" cy="11" r="7"/><path d="M21 21l-4.3-4.3"/></svg>
            <input type="text" class="form-control" name="q" placeholder="Buscar por patente, cliente o descripcion..." value="${q}">
        </div>
        <select class="form-control" name="estado">
            <option value="">Todos los estados</option>
            <option value="EN_PROCESO" ${estado == 'EN_PROCESO' ? 'selected' : ''}>En proceso</option>
            <option value="FINALIZADO" ${estado == 'FINALIZADO' ? 'selected' : ''}>Finalizado</option>
            <option value="FACTURADO" ${estado == 'FACTURADO' ? 'selected' : ''}>Facturado</option>
        </select>
        <button type="submit" class="btn btn--secondary btn--sm">Filtrar</button>
        <button type="button" class="btn btn--ghost btn--sm"
                data-limpiar-filtros="${pageContext.request.contextPath}/trabajos">Limpiar</button>
    </form>

    <c:choose>
        <c:when test="${empty resultado.registros}">
            <div class="empty-state">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6"><rect x="3" y="4" width="18" height="16" rx="2"/><path d="M8 2v4M16 2v4M3 10h18"/></svg>
                <h3>No se encontraron trabajos</h3>
                <p>Ajuste los filtros de busqueda o cargue un nuevo trabajo.</p>
                <a class="btn btn--primary btn--sm" href="${pageContext.request.contextPath}/trabajos?accion=nuevo">Nuevo trabajo</a>
            </div>
        </c:when>
        <c:otherwise>
            <div class="data-table-wrapper">
                <table class="data-table">
                    <thead>
                    <tr>
                        <th>Vehiculo</th>
                        <th>Propietario</th>
                        <th>Ingreso</th>
                        <th>Egreso</th>
                        <th>Estado</th>
                        <th class="col-actions">Acciones</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach var="tr" items="${resultado.registros}">
                        <tr>
                            <td data-label="Vehiculo" class="cell-primary"><c:out value="${tr.vehiculoPatente}"/></td>
                            <td data-label="Propietario"><c:out value="${tr.propietarioNombre}"/></td>
                            <td data-label="Ingreso" class="cell-secondary">${ea:fechaDia(tr.fechaIngreso)}</td>
                            <td data-label="Egreso" class="cell-secondary">
                                <c:choose>
                                    <c:when test="${not empty tr.fechaEgreso}">${ea:fechaDia(tr.fechaEgreso)}</c:when>
                                    <c:otherwise>—</c:otherwise>
                                </c:choose>
                            </td>
                            <td data-label="Estado">
                                <c:choose>
                                    <c:when test="${tr.estado.name() == 'EN_PROCESO'}"><span class="badge badge--operador">En proceso</span></c:when>
                                    <c:when test="${tr.estado.name() == 'FINALIZADO'}"><span class="badge badge--activo">Finalizado</span></c:when>
                                    <c:otherwise><span class="badge badge--admin">Facturado</span></c:otherwise>
                                </c:choose>
                            </td>
                            <td data-label="Acciones" class="col-actions">
                                <div class="row-actions">
                                    <a class="btn btn--icon btn--ghost btn--sm" data-tooltip="Ver items"
                                       href="${pageContext.request.contextPath}/trabajos?accion=detalle&id=${tr.idTrabajo}">
                                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M4 6h16M4 12h10M4 18h7"/></svg>
                                    </a>
                                    <c:if test="${tr.estado.name() == 'EN_PROCESO'}">
                                        <a class="btn btn--icon btn--ghost btn--sm" data-tooltip="Editar"
                                           href="${pageContext.request.contextPath}/trabajos?accion=editar&id=${tr.idTrabajo}">
                                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M12 20h9"/><path d="M16.5 3.5a2.1 2.1 0 013 3L7 19l-4 1 1-4L16.5 3.5z"/></svg>
                                        </a>
                                        <form method="post" action="${pageContext.request.contextPath}/trabajos" style="display:flex; gap:4px; align-items:center;">
                                            <input type="hidden" name="csrfToken" value="${csrfToken}">
                                            <input type="hidden" name="accion" value="finalizar">
                                            <input type="hidden" name="id" value="${tr.idTrabajo}">
                                            <input type="date" name="fechaEgreso" class="form-control" style="height:32px; width:140px;" required>
                                            <button type="submit" class="btn btn--secondary btn--sm" data-tooltip="Finalizar trabajo">Finalizar</button>
                                        </form>
                                    </c:if>
                                    <c:if test="${tr.estado.name() == 'FINALIZADO' and sessionScope.usuarioLogueado.rol.name() == 'ADMINISTRADOR'}">
                                        <form method="post" action="${pageContext.request.contextPath}/trabajos"
                                              data-confirm="¿Reabrir el trabajo de ${tr.vehiculoPatente}?">
                                            <input type="hidden" name="csrfToken" value="${csrfToken}">
                                            <input type="hidden" name="accion" value="reabrir">
                                            <input type="hidden" name="id" value="${tr.idTrabajo}">
                                            <button type="submit" class="btn btn--icon btn--ghost btn--sm" data-tooltip="Reabrir">
                                                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M3 12a9 9 0 1 0 3-6.7"/><path d="M3 4v5h5"/></svg>
                                            </button>
                                        </form>
                                    </c:if>
                                    <c:if test="${sessionScope.usuarioLogueado.rol.name() == 'ADMINISTRADOR'}">
                                        <form method="post" action="${pageContext.request.contextPath}/trabajos"
                                              data-confirm="¿Dar de baja este trabajo?">
                                            <input type="hidden" name="csrfToken" value="${csrfToken}">
                                            <input type="hidden" name="accion" value="baja">
                                            <input type="hidden" name="id" value="${tr.idTrabajo}">
                                            <button type="submit" class="btn btn--icon btn--ghost btn--sm" data-tooltip="Dar de baja">
                                                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M3 6h18M8 6V4a2 2 0 012-2h4a2 2 0 012 2v2m3 0-1 14a2 2 0 01-2 2H8a2 2 0 01-2-2L5 6"/></svg>
                                            </button>
                                        </form>
                                    </c:if>
                                </div>
                            </td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>

            <div class="pagination-bar">
                <span class="pagination-bar__info">
                    Pagina ${resultado.paginaActual} de ${resultado.totalPaginas} &middot; ${resultado.totalRegistros} trabajos
                </span>
                <div class="pagination-bar__controls">
                    <c:if test="${resultado.tieneAnterior()}">
                        <a class="btn btn--secondary btn--sm"
                           href="${pageContext.request.contextPath}/trabajos?pagina=${resultado.paginaActual - 1}&q=${q}&estado=${estado}">Anterior</a>
                    </c:if>
                    <c:if test="${resultado.tieneSiguiente()}">
                        <a class="btn btn--secondary btn--sm"
                           href="${pageContext.request.contextPath}/trabajos?pagina=${resultado.paginaActual + 1}&q=${q}&estado=${estado}">Siguiente</a>
                    </c:if>
                </div>
            </div>
        </c:otherwise>
    </c:choose>
</div>

<%@ include file="/WEB-INF/jsp/layout/footer.jsp" %>
