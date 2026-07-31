<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="tituloPagina" value="Generar reporte" scope="request"/>
<c:set var="subtituloPagina" value="Seleccione el tipo de informe y el periodo a consultar" scope="request"/>
<c:set var="activeMenu" value="reportes" scope="request"/>

<%@ include file="/WEB-INF/jsp/layout/header.jsp" %>

<div class="page-header">
    <div>
        <h1 class="page-header__title">Generar reporte</h1>
        <p class="page-header__subtitle">El PDF se genera y se guarda automaticamente para consultas posteriores.</p>
    </div>
    <div class="page-header__actions">
        <a class="btn btn--secondary" href="${pageContext.request.contextPath}/reportes">Volver al listado</a>
    </div>
</div>

<div class="surface surface--padded" style="max-width: 620px;">
    <form method="post" action="${pageContext.request.contextPath}/reportes" data-form="reporte" novalidate>
        <input type="hidden" name="csrfToken" value="${csrfToken}">
        <input type="hidden" name="accion" value="generar">

        <div class="form-grid">
            <div class="form-field form-field--full ${not empty errores.tipoReporte ? 'has-error' : ''}">
                <label class="form-label" for="tipoReporte">Tipo de reporte <span class="form-label__required">*</span></label>
                <select class="form-control" id="tipoReporte" name="tipoReporte" required>
                    <option value="">Seleccionar...</option>
                    <option value="TRABAJOS_POR_PERIODO" ${form.tipoReporte == 'TRABAJOS_POR_PERIODO' ? 'selected' : ''}>Trabajos realizados por periodo</option>
                    <option value="INGRESOS" ${form.tipoReporte == 'INGRESOS' ? 'selected' : ''}>Ingresos por ventas y servicios</option>
                    <option value="MOVIMIENTOS_STOCK" ${form.tipoReporte == 'MOVIMIENTOS_STOCK' ? 'selected' : ''}>Movimientos de stock</option>
                    <option value="COMPRAS_POR_PROVEEDOR" ${form.tipoReporte == 'COMPRAS_POR_PROVEEDOR' ? 'selected' : ''}>Compras realizadas por proveedor</option>
                    <option value="TURNOS_POR_ESTADO" ${form.tipoReporte == 'TURNOS_POR_ESTADO' ? 'selected' : ''}>Turnos por estado y periodo</option>
                    <option value="RANKING_CLIENTES" ${form.tipoReporte == 'RANKING_CLIENTES' ? 'selected' : ''}>Ranking de clientes con mayor actividad</option>
                </select>
                <span class="form-error"><c:out value="${errores.tipoReporte}"/></span>
            </div>

            <div class="form-field ${not empty errores.fechaDesde ? 'has-error' : ''}">
                <label class="form-label" for="fechaDesde">Desde <span class="form-label__required">*</span></label>
                <input type="date" class="form-control" id="fechaDesde" name="fechaDesde" value="${form.fechaDesde}" required>
                <span class="form-error"><c:out value="${errores.fechaDesde}"/></span>
            </div>

            <div class="form-field ${not empty errores.fechaHasta ? 'has-error' : ''}">
                <label class="form-label" for="fechaHasta">Hasta <span class="form-label__required">*</span></label>
                <input type="date" class="form-control" id="fechaHasta" name="fechaHasta" value="${form.fechaHasta}" required>
                <span class="form-error"><c:out value="${errores.fechaHasta}"/></span>
            </div>
        </div>

        <div class="form-actions">
            <a class="btn btn--secondary" href="${pageContext.request.contextPath}/reportes">Cancelar</a>
            <button type="submit" class="btn btn--primary">Generar reporte</button>
        </div>
    </form>
</div>

<%@ include file="/WEB-INF/jsp/layout/footer.jsp" %>
