<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="ea" uri="https://eataller.com/tags/functions" %>

<c:set var="tituloPagina" value="Turnos" scope="request"/>
<c:set var="subtituloPagina" value="Agenda de turnos del taller" scope="request"/>
<c:set var="activeMenu" value="turnos" scope="request"/>

<c:choose>
    <c:when test="${param.exito == 'creado'}"><c:set var="flashExito" value="Turno creado correctamente." scope="request"/></c:when>
    <c:when test="${param.exito == 'modificado'}"><c:set var="flashExito" value="Turno modificado correctamente." scope="request"/></c:when>
    <c:when test="${param.exito == 'cancelado'}"><c:set var="flashExito" value="Turno cancelado." scope="request"/></c:when>
</c:choose>
<c:choose>
    <c:when test="${param.error == 'no_encontrado'}"><c:set var="flashError" value="El turno solicitado no existe." scope="request"/></c:when>
    <c:when test="${param.error == 'ya_finalizado'}"><c:set var="flashError" value="El turno ya estaba finalizado o cancelado." scope="request"/></c:when>
    <c:when test="${param.error == 'csrf'}"><c:set var="flashError" value="La sesion del formulario expiro. Intente nuevamente." scope="request"/></c:when>
</c:choose>

<%@ include file="/WEB-INF/jsp/layout/header.jsp" %>

<div class="page-header">
    <div>
        <h1 class="page-header__title">Turnos</h1>
        <p class="page-header__subtitle">Agenda de servicios mecanicos y de lubricentro.</p>
    </div>
    <div class="page-header__actions">
        <a class="btn btn--primary" href="${pageContext.request.contextPath}/turnos?accion=nuevo">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><path d="M12 5v14M5 12h14"/></svg>
            Nuevo turno
        </a>
    </div>
</div>

<div class="surface">
    <form method="get" action="${pageContext.request.contextPath}/turnos" class="filters-bar">
        <div class="search-input">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><circle cx="11" cy="11" r="7"/><path d="M21 21l-4.3-4.3"/></svg>
            <input type="text" class="form-control" name="q" placeholder="Buscar por cliente o patente..." value="${q}">
        </div>
        <input type="date" class="form-control" name="fechaDesde" value="${fechaDesde}" data-tooltip="Desde">
        <input type="date" class="form-control" name="fechaHasta" value="${fechaHasta}" data-tooltip="Hasta">
        <select class="form-control" name="estado">
            <option value="">Todos los estados</option>
            <option value="PENDIENTE" ${estado == 'PENDIENTE' ? 'selected' : ''}>Pendiente</option>
            <option value="CONFIRMADO" ${estado == 'CONFIRMADO' ? 'selected' : ''}>Confirmado</option>
            <option value="EN_PROCESO" ${estado == 'EN_PROCESO' ? 'selected' : ''}>En proceso</option>
            <option value="FINALIZADO" ${estado == 'FINALIZADO' ? 'selected' : ''}>Finalizado</option>
            <option value="CANCELADO" ${estado == 'CANCELADO' ? 'selected' : ''}>Cancelado</option>
        </select>
        <select class="form-control" name="tipoServicio">
            <option value="">Todos los servicios</option>
            <option value="MECANICA" ${tipoServicio == 'MECANICA' ? 'selected' : ''}>Mecanica</option>
            <option value="LUBRICENTRO" ${tipoServicio == 'LUBRICENTRO' ? 'selected' : ''}>Lubricentro</option>
            <option value="OTRO" ${tipoServicio == 'OTRO' ? 'selected' : ''}>Otro</option>
        </select>
        <button type="submit" class="btn btn--secondary btn--sm">Filtrar</button>
        <button type="button" class="btn btn--ghost btn--sm"
                data-limpiar-filtros="${pageContext.request.contextPath}/turnos">Limpiar</button>
    </form>

    <c:choose>
        <c:when test="${empty resultado.registros}">
            <div class="empty-state">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6"><rect x="3" y="4" width="18" height="18" rx="2"/><path d="M3 10h18M8 2v4M16 2v4"/></svg>
                <h3>No se encontraron turnos</h3>
                <p>Ajuste los filtros de busqueda o cargue un nuevo turno.</p>
                <a class="btn btn--primary btn--sm" href="${pageContext.request.contextPath}/turnos?accion=nuevo">Nuevo turno</a>
            </div>
        </c:when>
        <c:otherwise>
            <div class="data-table-wrapper">
                <table class="data-table">
                    <thead>
                    <tr>
                        <th>Fecha y hora</th>
                        <th>Cliente</th>
                        <th>Vehiculo</th>
                        <th>Servicio</th>
                        <th>Estado</th>
                        <th class="col-actions">Acciones</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach var="t" items="${resultado.registros}">
                        <tr>
                            <td data-label="Fecha y hora" class="cell-primary">${ea:fechaHora(t.fechaHora)}</td>
                            <td data-label="Cliente"><c:out value="${t.clienteNombre}"/></td>
                            <td data-label="Vehiculo"><c:out value="${t.vehiculoPatente}"/></td>
                            <td data-label="Servicio"><c:out value="${t.tipoServicio.etiqueta}"/></td>
                            <td data-label="Estado">
                                <c:choose>
                                    <c:when test="${t.estado.name() == 'PENDIENTE'}"><span class="badge badge--inactivo">Pendiente</span></c:when>
                                    <c:when test="${t.estado.name() == 'CONFIRMADO'}"><span class="badge badge--operador">Confirmado</span></c:when>
                                    <c:when test="${t.estado.name() == 'EN_PROCESO'}"><span class="badge badge--admin">En proceso</span></c:when>
                                    <c:when test="${t.estado.name() == 'FINALIZADO'}"><span class="badge badge--activo">Finalizado</span></c:when>
                                    <c:otherwise><span class="badge badge--bloqueado">Cancelado</span></c:otherwise>
                                </c:choose>
                            </td>
                            <td data-label="Acciones" class="col-actions">
                                <div class="row-actions">
                                    <c:if test="${t.estado.name() != 'FINALIZADO' and t.estado.name() != 'CANCELADO'}">
                                        <a class="btn btn--icon btn--ghost btn--sm" data-tooltip="Editar"
                                           href="${pageContext.request.contextPath}/turnos?accion=editar&id=${t.idTurno}">
                                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M12 20h9"/><path d="M16.5 3.5a2.1 2.1 0 013 3L7 19l-4 1 1-4L16.5 3.5z"/></svg>
                                        </a>
                                        <form method="post" action="${pageContext.request.contextPath}/turnos"
                                              data-confirm="¿Cancelar el turno del ${ea:fechaHora(t.fechaHora)}?">
                                            <input type="hidden" name="csrfToken" value="${csrfToken}">
                                            <input type="hidden" name="accion" value="cancelar">
                                            <input type="hidden" name="id" value="${t.idTurno}">
                                            <button type="submit" class="btn btn--icon btn--ghost btn--sm" data-tooltip="Cancelar turno">
                                                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="9"/><path d="M9 9l6 6M15 9l-6 6"/></svg>
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
                    Pagina ${resultado.paginaActual} de ${resultado.totalPaginas} &middot; ${resultado.totalRegistros} turnos
                </span>
                <div class="pagination-bar__controls">
                    <c:if test="${resultado.tieneAnterior()}">
                        <a class="btn btn--secondary btn--sm"
                           href="${pageContext.request.contextPath}/turnos?pagina=${resultado.paginaActual - 1}&q=${q}&estado=${estado}&tipoServicio=${tipoServicio}&fechaDesde=${fechaDesde}&fechaHasta=${fechaHasta}">Anterior</a>
                    </c:if>
                    <c:if test="${resultado.tieneSiguiente()}">
                        <a class="btn btn--secondary btn--sm"
                           href="${pageContext.request.contextPath}/turnos?pagina=${resultado.paginaActual + 1}&q=${q}&estado=${estado}&tipoServicio=${tipoServicio}&fechaDesde=${fechaDesde}&fechaHasta=${fechaHasta}">Siguiente</a>
                    </c:if>
                </div>
            </div>
        </c:otherwise>
    </c:choose>
</div>

<%@ include file="/WEB-INF/jsp/layout/footer.jsp" %>
