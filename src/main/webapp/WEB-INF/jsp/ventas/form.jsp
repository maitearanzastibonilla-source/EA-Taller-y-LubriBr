<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="tituloPagina" value="Nueva venta directa" scope="request"/>
<c:set var="subtituloPagina" value="Seleccione el metodo de pago para comenzar a cargar productos" scope="request"/>
<c:set var="activeMenu" value="ventas" scope="request"/>

<%@ include file="/WEB-INF/jsp/layout/header.jsp" %>

<div class="page-header">
    <div>
        <h1 class="page-header__title">Nueva venta directa</h1>
        <p class="page-header__subtitle">El detalle de productos se carga en el paso siguiente.</p>
    </div>
    <div class="page-header__actions">
        <a class="btn btn--secondary" href="${pageContext.request.contextPath}/ventas">Volver al listado</a>
    </div>
</div>

<div class="surface surface--padded" style="max-width: 560px;">
    <form method="post" action="${pageContext.request.contextPath}/ventas" data-form="venta" novalidate>
        <input type="hidden" name="csrfToken" value="${csrfToken}">
        <input type="hidden" name="accion" value="guardar">

        <div class="form-grid">
            <div class="form-field ${not empty errores.metodoPago ? 'has-error' : ''}">
                <label class="form-label" for="metodoPago">Metodo de pago <span class="form-label__required">*</span></label>
                <select class="form-control" id="metodoPago" name="metodoPago" required>
                    <option value="">Seleccionar...</option>
                    <option value="EFECTIVO">Efectivo</option>
                    <option value="TRANSFERENCIA">Transferencia</option>
                    <option value="TARJETA">Tarjeta</option>
                    <option value="OTRO">Otro</option>
                </select>
                <span class="form-error"><c:out value="${errores.metodoPago}"/></span>
            </div>
        </div>

        <div class="form-actions">
            <a class="btn btn--secondary" href="${pageContext.request.contextPath}/ventas">Cancelar</a>
            <button type="submit" class="btn btn--primary">Iniciar venta</button>
        </div>
    </form>
</div>

<%@ include file="/WEB-INF/jsp/layout/footer.jsp" %>
