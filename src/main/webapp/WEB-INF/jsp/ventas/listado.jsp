<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="ea" uri="https://eataller.com/tags/functions" %>

<c:set var="tituloPagina" value="Ventas Directas" scope="request"/>
<c:set var="subtituloPagina" value="Ventas de productos en mostrador, sin trabajo asociado" scope="request"/>
<c:set var="activeMenu" value="ventas" scope="request"/>

<c:choose>
    <c:when test="${param.error == 'no_encontrada'}"><c:set var="flashError" value="La venta solicitada no existe." scope="request"/></c:when>
    <c:when test="${param.error == 'csrf'}"><c:set var="flashError" value="La sesion del formulario expiro. Intente nuevamente." scope="request"/></c:when>
</c:choose>

<%@ include file="/WEB-INF/jsp/layout/header.jsp" %>

<div class="page-header">
    <div>
        <h1 class="page-header__title">Ventas Directas</h1>
        <p class="page-header__subtitle">Ventas de productos realizadas directamente en el mostrador.</p>
    </div>
    <div class="page-header__actions">
        <a class="btn btn--primary" href="${pageContext.request.contextPath}/ventas?accion=nuevo">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><path d="M12 5v14M5 12h14"/></svg>
            Nueva venta
        </a>
    </div>
</div>

<div class="surface">
    <form method="get" action="${pageContext.request.contextPath}/ventas" class="filters-bar">
        <div class="search-input">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><circle cx="11" cy="11" r="7"/><path d="M21 21l-4.3-4.3"/></svg>
            <input type="text" class="form-control" name="q" placeholder="Buscar por numero o usuario..." value="${q}">
        </div>
        <select class="form-control" name="estado">
            <option value="">Todos los estados</option>
            <option value="PENDIENTE" ${estado == 'PENDIENTE' ? 'selected' : ''}>Pendiente</option>
            <option value="CONFIRMADA" ${estado == 'CONFIRMADA' ? 'selected' : ''}>Confirmada</option>
            <option value="ANULADA" ${estado == 'ANULADA' ? 'selected' : ''}>Anulada</option>
        </select>
        <input type="date" class="form-control" name="fechaDesde" value="${fechaDesde}" style="max-width: 160px;">
        <input type="date" class="form-control" name="fechaHasta" value="${fechaHasta}" style="max-width: 160px;">
        <button type="submit" class="btn btn--secondary btn--sm">Filtrar</button>
        <button type="button" class="btn btn--ghost btn--sm"
                data-limpiar-filtros="${pageContext.request.contextPath}/ventas">Limpiar</button>
    </form>

    <c:choose>
        <c:when test="${empty resultado.registros}">
            <div class="empty-state">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6"><rect x="3" y="4" width="18" height="16" rx="2"/><path d="M8 2v4M16 2v4M3 10h18"/></svg>
                <h3>No se encontraron ventas</h3>
                <p>Ajuste los filtros de busqueda o cargue una nueva venta directa.</p>
                <a class="btn btn--primary btn--sm" href="${pageContext.request.contextPath}/ventas?accion=nuevo">Nueva venta</a>
            </div>
        </c:when>
        <c:otherwise>
            <div class="data-table-wrapper">
                <table class="data-table">
                    <thead>
                    <tr>
                        <th>Numero</th>
                        <th>Fecha</th>
                        <th>Usuario</th>
                        <th>Total</th>
                        <th>Metodo de pago</th>
                        <th>Estado</th>
                        <th class="col-actions">Acciones</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach var="ve" items="${resultado.registros}">
                        <tr>
                            <td data-label="Numero" class="cell-primary">#${ve.idVenta}</td>
                            <td data-label="Fecha" class="cell-secondary">${ea:fechaDia(ve.fecha)}</td>
                            <td data-label="Usuario"><c:out value="${ve.usuarioNombre}"/></td>
                            <td data-label="Total"><fmt:formatNumber value="${ve.total}" type="currency" currencySymbol="$"/></td>
                            <td data-label="Metodo de pago">${ve.metodoPago.etiqueta}</td>
                            <td data-label="Estado">
                                <c:choose>
                                    <c:when test="${ve.estado.name() == 'ANULADA'}"><span class="badge badge--inactivo">Anulada</span></c:when>
                                    <c:when test="${ve.estado.name() == 'CONFIRMADA'}"><span class="badge badge--activo">Confirmada</span></c:when>
                                    <c:otherwise><span class="badge badge--operador">Pendiente</span></c:otherwise>
                                </c:choose>
                            </td>
                            <td data-label="Acciones" class="col-actions">
                                <a class="btn btn--icon btn--ghost btn--sm" data-tooltip="Ver detalle"
                                   href="${pageContext.request.contextPath}/ventas?accion=detalle&id=${ve.idVenta}">
                                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M4 6h16M4 12h10M4 18h7"/></svg>
                                </a>
                            </td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>

            <div class="pagination-bar">
                <span class="pagination-bar__info">
                    Pagina ${resultado.paginaActual} de ${resultado.totalPaginas} &middot; ${resultado.totalRegistros} ventas
                </span>
                <div class="pagination-bar__controls">
                    <c:if test="${resultado.tieneAnterior()}">
                        <a class="btn btn--secondary btn--sm"
                           href="${pageContext.request.contextPath}/ventas?pagina=${resultado.paginaActual - 1}&q=${q}&estado=${estado}&fechaDesde=${fechaDesde}&fechaHasta=${fechaHasta}">Anterior</a>
                    </c:if>
                    <c:if test="${resultado.tieneSiguiente()}">
                        <a class="btn btn--secondary btn--sm"
                           href="${pageContext.request.contextPath}/ventas?pagina=${resultado.paginaActual + 1}&q=${q}&estado=${estado}&fechaDesde=${fechaDesde}&fechaHasta=${fechaHasta}">Siguiente</a>
                    </c:if>
                </div>
            </div>
        </c:otherwise>
    </c:choose>
</div>

<%@ include file="/WEB-INF/jsp/layout/footer.jsp" %>
