<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="ea" uri="https://eataller.com/tags/functions" %>

<c:set var="tituloPagina" value="Reportes" scope="request"/>
<c:set var="subtituloPagina" value="Informes consolidados del funcionamiento del taller" scope="request"/>
<c:set var="activeMenu" value="reportes" scope="request"/>

<c:choose>
    <c:when test="${param.error == 'no_encontrado'}"><c:set var="flashError" value="El reporte solicitado no existe." scope="request"/></c:when>
    <c:when test="${param.error == 'csrf'}"><c:set var="flashError" value="La sesion del formulario expiro. Intente nuevamente." scope="request"/></c:when>
</c:choose>

<%@ include file="/WEB-INF/jsp/layout/header.jsp" %>

<div class="page-header">
    <div>
        <h1 class="page-header__title">Reportes</h1>
        <p class="page-header__subtitle">Reportes generados anteriormente, con acceso al PDF y a los datos consolidados.</p>
    </div>
    <div class="page-header__actions">
        <a class="btn btn--primary" href="${pageContext.request.contextPath}/reportes?accion=nuevo">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><path d="M12 5v14M5 12h14"/></svg>
            Generar reporte
        </a>
    </div>
</div>

<div class="surface">
    <form method="get" action="${pageContext.request.contextPath}/reportes" class="filters-bar">
        <select class="form-control" name="tipo">
            <option value="">Todos los tipos</option>
            <option value="TRABAJOS_POR_PERIODO" ${tipo == 'TRABAJOS_POR_PERIODO' ? 'selected' : ''}>Trabajos por periodo</option>
            <option value="INGRESOS" ${tipo == 'INGRESOS' ? 'selected' : ''}>Ingresos</option>
            <option value="MOVIMIENTOS_STOCK" ${tipo == 'MOVIMIENTOS_STOCK' ? 'selected' : ''}>Movimientos de stock</option>
            <option value="COMPRAS_POR_PROVEEDOR" ${tipo == 'COMPRAS_POR_PROVEEDOR' ? 'selected' : ''}>Compras por proveedor</option>
            <option value="TURNOS_POR_ESTADO" ${tipo == 'TURNOS_POR_ESTADO' ? 'selected' : ''}>Turnos por estado</option>
            <option value="RANKING_CLIENTES" ${tipo == 'RANKING_CLIENTES' ? 'selected' : ''}>Ranking de clientes</option>
        </select>
        <input type="date" class="form-control" name="fechaDesde" value="${fechaDesde}" style="max-width: 160px;">
        <input type="date" class="form-control" name="fechaHasta" value="${fechaHasta}" style="max-width: 160px;">
        <button type="submit" class="btn btn--secondary btn--sm">Filtrar</button>
        <button type="button" class="btn btn--ghost btn--sm"
                data-limpiar-filtros="${pageContext.request.contextPath}/reportes">Limpiar</button>
    </form>

    <c:choose>
        <c:when test="${empty resultado.registros}">
            <div class="empty-state">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6"><rect x="3" y="4" width="18" height="16" rx="2"/><path d="M8 2v4M16 2v4M3 10h18"/></svg>
                <h3>No se encontraron reportes</h3>
                <p>Ajuste los filtros de busqueda o genere un nuevo reporte.</p>
                <a class="btn btn--primary btn--sm" href="${pageContext.request.contextPath}/reportes?accion=nuevo">Generar reporte</a>
            </div>
        </c:when>
        <c:otherwise>
            <div class="data-table-wrapper">
                <table class="data-table">
                    <thead>
                    <tr>
                        <th>Tipo</th>
                        <th>Periodo</th>
                        <th>Generado por</th>
                        <th>Fecha de generacion</th>
                        <th class="col-actions">Acciones</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach var="re" items="${resultado.registros}">
                        <tr>
                            <td data-label="Tipo" class="cell-primary">${re.tipoReporte.etiqueta}</td>
                            <td data-label="Periodo" class="cell-secondary">${ea:fechaDia(re.fechaDesde)} - ${ea:fechaDia(re.fechaHasta)}</td>
                            <td data-label="Generado por"><c:out value="${re.usuarioGeneradorNombre}"/></td>
                            <td data-label="Fecha de generacion" class="cell-secondary">${ea:fechaHora(re.fechaGeneracion)}</td>
                            <td data-label="Acciones" class="col-actions">
                                <div class="row-actions">
                                    <a class="btn btn--icon btn--ghost btn--sm" data-tooltip="Ver detalle"
                                       href="${pageContext.request.contextPath}/reportes?accion=detalle&id=${re.idReporte}">
                                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M4 6h16M4 12h10M4 18h7"/></svg>
                                    </a>
                                    <a class="btn btn--icon btn--ghost btn--sm" data-tooltip="Descargar PDF" target="_blank"
                                       href="${pageContext.request.contextPath}/reportes?accion=descargar&id=${re.idReporte}">
                                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8z"/><path d="M14 2v6h6"/></svg>
                                    </a>
                                </div>
                            </td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>

            <div class="pagination-bar">
                <span class="pagination-bar__info">
                    Pagina ${resultado.paginaActual} de ${resultado.totalPaginas} &middot; ${resultado.totalRegistros} reportes
                </span>
                <div class="pagination-bar__controls">
                    <c:if test="${resultado.tieneAnterior()}">
                        <a class="btn btn--secondary btn--sm"
                           href="${pageContext.request.contextPath}/reportes?pagina=${resultado.paginaActual - 1}&tipo=${tipo}&fechaDesde=${fechaDesde}&fechaHasta=${fechaHasta}">Anterior</a>
                    </c:if>
                    <c:if test="${resultado.tieneSiguiente()}">
                        <a class="btn btn--secondary btn--sm"
                           href="${pageContext.request.contextPath}/reportes?pagina=${resultado.paginaActual + 1}&tipo=${tipo}&fechaDesde=${fechaDesde}&fechaHasta=${fechaHasta}">Siguiente</a>
                    </c:if>
                </div>
            </div>
        </c:otherwise>
    </c:choose>
</div>

<%@ include file="/WEB-INF/jsp/layout/footer.jsp" %>
