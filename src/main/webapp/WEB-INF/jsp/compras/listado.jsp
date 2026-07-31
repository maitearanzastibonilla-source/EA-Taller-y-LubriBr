<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="ea" uri="https://eataller.com/tags/functions" %>

<c:set var="tituloPagina" value="Compras" scope="request"/>
<c:set var="subtituloPagina" value="Ordenes de compra a proveedores para reponer stock" scope="request"/>
<c:set var="activeMenu" value="compras" scope="request"/>

<c:choose>
    <c:when test="${param.error == 'no_encontrada'}"><c:set var="flashError" value="La compra solicitada no existe." scope="request"/></c:when>
    <c:when test="${param.error == 'csrf'}"><c:set var="flashError" value="La sesion del formulario expiro. Intente nuevamente." scope="request"/></c:when>
</c:choose>

<%@ include file="/WEB-INF/jsp/layout/header.jsp" %>

<div class="page-header">
    <div>
        <h1 class="page-header__title">Compras</h1>
        <p class="page-header__subtitle">Ordenes de compra realizadas a proveedores.</p>
    </div>
    <div class="page-header__actions">
        <a class="btn btn--primary" href="${pageContext.request.contextPath}/compras?accion=nuevo">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><path d="M12 5v14M5 12h14"/></svg>
            Nueva compra
        </a>
    </div>
</div>

<div class="surface">
    <form method="get" action="${pageContext.request.contextPath}/compras" class="filters-bar">
        <div class="search-input">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><circle cx="11" cy="11" r="7"/><path d="M21 21l-4.3-4.3"/></svg>
            <input type="text" class="form-control" name="q" placeholder="Buscar por numero o proveedor..." value="${q}">
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
                data-limpiar-filtros="${pageContext.request.contextPath}/compras">Limpiar</button>
    </form>

    <c:choose>
        <c:when test="${empty resultado.registros}">
            <div class="empty-state">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6"><rect x="3" y="4" width="18" height="16" rx="2"/><path d="M8 2v4M16 2v4M3 10h18"/></svg>
                <h3>No se encontraron compras</h3>
                <p>Ajuste los filtros de busqueda o cargue una nueva compra.</p>
                <a class="btn btn--primary btn--sm" href="${pageContext.request.contextPath}/compras?accion=nuevo">Nueva compra</a>
            </div>
        </c:when>
        <c:otherwise>
            <div class="data-table-wrapper">
                <table class="data-table">
                    <thead>
                    <tr>
                        <th>Numero</th>
                        <th>Fecha</th>
                        <th>Proveedor</th>
                        <th>Responsable</th>
                        <th>Total</th>
                        <th>Estado</th>
                        <th class="col-actions">Acciones</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach var="co" items="${resultado.registros}">
                        <tr>
                            <td data-label="Numero" class="cell-primary">#${co.idCompra}</td>
                            <td data-label="Fecha" class="cell-secondary">${ea:fechaDia(co.fecha)}</td>
                            <td data-label="Proveedor"><c:out value="${co.proveedorNombre}"/></td>
                            <td data-label="Responsable"><c:out value="${co.usuarioNombre}"/></td>
                            <td data-label="Total"><fmt:formatNumber value="${co.total}" type="currency" currencySymbol="$"/></td>
                            <td data-label="Estado">
                                <c:choose>
                                    <c:when test="${co.estado.name() == 'ANULADA'}"><span class="badge badge--inactivo">Anulada</span></c:when>
                                    <c:when test="${co.estado.name() == 'CONFIRMADA'}"><span class="badge badge--activo">Confirmada</span></c:when>
                                    <c:otherwise><span class="badge badge--operador">Pendiente</span></c:otherwise>
                                </c:choose>
                            </td>
                            <td data-label="Acciones" class="col-actions">
                                <a class="btn btn--icon btn--ghost btn--sm" data-tooltip="Ver detalle"
                                   href="${pageContext.request.contextPath}/compras?accion=detalle&id=${co.idCompra}">
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
                    Pagina ${resultado.paginaActual} de ${resultado.totalPaginas} &middot; ${resultado.totalRegistros} compras
                </span>
                <div class="pagination-bar__controls">
                    <c:if test="${resultado.tieneAnterior()}">
                        <a class="btn btn--secondary btn--sm"
                           href="${pageContext.request.contextPath}/compras?pagina=${resultado.paginaActual - 1}&q=${q}&estado=${estado}&fechaDesde=${fechaDesde}&fechaHasta=${fechaHasta}">Anterior</a>
                    </c:if>
                    <c:if test="${resultado.tieneSiguiente()}">
                        <a class="btn btn--secondary btn--sm"
                           href="${pageContext.request.contextPath}/compras?pagina=${resultado.paginaActual + 1}&q=${q}&estado=${estado}&fechaDesde=${fechaDesde}&fechaHasta=${fechaHasta}">Siguiente</a>
                    </c:if>
                </div>
            </div>
        </c:otherwise>
    </c:choose>
</div>

<%@ include file="/WEB-INF/jsp/layout/footer.jsp" %>
