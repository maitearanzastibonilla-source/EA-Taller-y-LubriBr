<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="ea" uri="https://eataller.com/tags/functions" %>

<c:set var="tituloPagina" value="Comprobantes" scope="request"/>
<c:set var="subtituloPagina" value="Comprobantes de cobro emitidos por trabajos finalizados" scope="request"/>
<c:set var="activeMenu" value="comprobantes" scope="request"/>

<c:choose>
    <c:when test="${param.error == 'no_encontrado'}"><c:set var="flashError" value="El comprobante solicitado no existe." scope="request"/></c:when>
</c:choose>

<%@ include file="/WEB-INF/jsp/layout/header.jsp" %>

<div class="page-header">
    <div>
        <h1 class="page-header__title">Comprobantes</h1>
        <p class="page-header__subtitle">Consulta de comprobantes emitidos. Se generan desde el detalle de un trabajo finalizado.</p>
    </div>
</div>

<div class="surface">
    <form method="get" action="${pageContext.request.contextPath}/comprobantes" class="filters-bar">
        <div class="search-input">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><circle cx="11" cy="11" r="7"/><path d="M21 21l-4.3-4.3"/></svg>
            <input type="text" class="form-control" name="q" placeholder="Buscar por numero, patente o cliente..." value="${q}">
        </div>
        <select class="form-control" name="estado">
            <option value="">Todos los estados</option>
            <option value="PENDIENTE" ${estado == 'PENDIENTE' ? 'selected' : ''}>Pendiente</option>
            <option value="SENADO" ${estado == 'SENADO' ? 'selected' : ''}>Senado</option>
            <option value="COBRADO" ${estado == 'COBRADO' ? 'selected' : ''}>Cobrado</option>
            <option value="ANULADO" ${estado == 'ANULADO' ? 'selected' : ''}>Anulado</option>
        </select>
        <input type="date" class="form-control" name="fechaDesde" value="${fechaDesde}" style="max-width: 160px;">
        <input type="date" class="form-control" name="fechaHasta" value="${fechaHasta}" style="max-width: 160px;">
        <button type="submit" class="btn btn--secondary btn--sm">Filtrar</button>
        <button type="button" class="btn btn--ghost btn--sm"
                data-limpiar-filtros="${pageContext.request.contextPath}/comprobantes">Limpiar</button>
    </form>

    <c:choose>
        <c:when test="${empty resultado.registros}">
            <div class="empty-state">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6"><rect x="3" y="4" width="18" height="16" rx="2"/><path d="M8 2v4M16 2v4M3 10h18"/></svg>
                <h3>No se encontraron comprobantes</h3>
                <p>Ajuste los filtros de busqueda, o genere un comprobante desde el detalle de un trabajo finalizado.</p>
            </div>
        </c:when>
        <c:otherwise>
            <div class="data-table-wrapper">
                <table class="data-table">
                    <thead>
                    <tr>
                        <th>Numero</th>
                        <th>Vehiculo</th>
                        <th>Cliente</th>
                        <th>Fecha</th>
                        <th>Total</th>
                        <th>Metodo de pago</th>
                        <th>Estado</th>
                        <th class="col-actions">PDF</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach var="co" items="${resultado.registros}">
                        <tr>
                            <td data-label="Numero" class="cell-primary">#${co.idComprobante}</td>
                            <td data-label="Vehiculo"><c:out value="${co.vehiculoPatente}"/></td>
                            <td data-label="Cliente"><c:out value="${co.clienteNombre}"/></td>
                            <td data-label="Fecha" class="cell-secondary">${ea:fechaDia(co.fecha)}</td>
                            <td data-label="Total"><fmt:formatNumber value="${co.total}" type="currency" currencySymbol="$"/></td>
                            <td data-label="Metodo de pago">${co.metodoPago.etiqueta}</td>
                            <td data-label="Estado">
                                <c:choose>
                                    <c:when test="${co.estado.name() == 'ANULADO'}"><span class="badge badge--inactivo">Anulado</span></c:when>
                                    <c:when test="${co.estado.name() == 'COBRADO'}"><span class="badge badge--activo">Cobrado</span></c:when>
                                    <c:otherwise><span class="badge badge--operador">${co.estado.etiqueta}</span></c:otherwise>
                                </c:choose>
                            </td>
                            <td data-label="PDF" class="col-actions">
                                <a class="btn btn--icon btn--ghost btn--sm" data-tooltip="Ver PDF" target="_blank"
                                   href="${pageContext.request.contextPath}/comprobantes?accion=descargar&id=${co.idComprobante}">
                                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8z"/><path d="M14 2v6h6"/></svg>
                                </a>
                            </td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>

            <div class="pagination-bar">
                <span class="pagination-bar__info">
                    Pagina ${resultado.paginaActual} de ${resultado.totalPaginas} &middot; ${resultado.totalRegistros} comprobantes
                </span>
                <div class="pagination-bar__controls">
                    <c:if test="${resultado.tieneAnterior()}">
                        <a class="btn btn--secondary btn--sm"
                           href="${pageContext.request.contextPath}/comprobantes?pagina=${resultado.paginaActual - 1}&q=${q}&estado=${estado}&fechaDesde=${fechaDesde}&fechaHasta=${fechaHasta}">Anterior</a>
                    </c:if>
                    <c:if test="${resultado.tieneSiguiente()}">
                        <a class="btn btn--secondary btn--sm"
                           href="${pageContext.request.contextPath}/comprobantes?pagina=${resultado.paginaActual + 1}&q=${q}&estado=${estado}&fechaDesde=${fechaDesde}&fechaHasta=${fechaHasta}">Siguiente</a>
                    </c:if>
                </div>
            </div>
        </c:otherwise>
    </c:choose>
</div>

<%@ include file="/WEB-INF/jsp/layout/footer.jsp" %>
