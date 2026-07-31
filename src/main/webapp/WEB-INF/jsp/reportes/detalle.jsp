<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="ea" uri="https://eataller.com/tags/functions" %>

<c:set var="tituloPagina" value="Detalle del reporte" scope="request"/>
<c:set var="subtituloPagina" value="${reporte.tipoReporte.etiqueta}" scope="request"/>
<c:set var="activeMenu" value="reportes" scope="request"/>

<%@ include file="/WEB-INF/jsp/layout/header.jsp" %>

<div class="page-header">
    <div>
        <h1 class="page-header__title"><c:out value="${reporte.tipoReporte.etiqueta}"/></h1>
        <p class="page-header__subtitle">
            Periodo: ${ea:fechaDia(reporte.fechaDesde)} al ${ea:fechaDia(reporte.fechaHasta)}
            &middot; Generado por <c:out value="${reporte.usuarioGeneradorNombre}"/> el ${ea:fechaHora(reporte.fechaGeneracion)}
        </p>
    </div>
    <div class="page-header__actions">
        <a class="btn btn--secondary" href="${pageContext.request.contextPath}/reportes">Volver al listado</a>
        <a class="btn btn--primary" target="_blank"
           href="${pageContext.request.contextPath}/reportes?accion=descargar&id=${reporte.idReporte}">Ver PDF</a>
    </div>
</div>

<div class="surface">
    <c:choose>
        <c:when test="${empty datos.filas}">
            <div class="empty-state">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6"><rect x="3" y="4" width="18" height="16" rx="2"/><path d="M8 2v4M16 2v4M3 10h18"/></svg>
                <h3>No se encontraron datos para el periodo seleccionado</h3>
            </div>
        </c:when>
        <c:otherwise>
            <div class="data-table-wrapper">
                <table class="data-table">
                    <thead>
                    <tr>
                        <c:forEach var="columna" items="${datos.columnas}">
                            <th><c:out value="${columna}"/></th>
                        </c:forEach>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach var="fila" items="${datos.filas}">
                        <tr>
                            <c:forEach var="valor" items="${fila}" varStatus="col">
                                <td data-label="${datos.columnas[col.index]}" class="${col.index == 0 ? 'cell-primary' : ''}">
                                    <c:out value="${valor}"/>
                                </td>
                            </c:forEach>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>
        </c:otherwise>
    </c:choose>

    <c:if test="${not empty datos.resumen}">
        <div class="surface--padded" style="border-top: 1px solid var(--color-border); margin-top: var(--space-4);">
            <c:forEach var="linea" items="${datos.resumen}">
                <p style="font-weight:600; margin: var(--space-2) 0;"><c:out value="${linea}"/></p>
            </c:forEach>
        </div>
    </c:if>
</div>

<%@ include file="/WEB-INF/jsp/layout/footer.jsp" %>
