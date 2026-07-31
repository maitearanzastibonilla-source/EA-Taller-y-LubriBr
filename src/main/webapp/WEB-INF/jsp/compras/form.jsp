<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="tituloPagina" value="Nueva compra" scope="request"/>
<c:set var="subtituloPagina" value="Seleccione el proveedor para comenzar a cargar productos" scope="request"/>
<c:set var="activeMenu" value="compras" scope="request"/>

<%@ include file="/WEB-INF/jsp/layout/header.jsp" %>

<div class="page-header">
    <div>
        <h1 class="page-header__title">Nueva compra</h1>
        <p class="page-header__subtitle">El detalle de productos se carga en el paso siguiente.</p>
    </div>
    <div class="page-header__actions">
        <a class="btn btn--secondary" href="${pageContext.request.contextPath}/compras">Volver al listado</a>
    </div>
</div>

<c:if test="${not empty mensajeError}">
    <div class="alert alert--error" role="alert">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><circle cx="12" cy="12" r="9"/><path d="M12 8v5M12 16h.01"/></svg>
        <span><c:out value="${mensajeError}"/></span>
    </div>
</c:if>

<div class="surface surface--padded" style="max-width: 560px;">
    <form method="post" action="${pageContext.request.contextPath}/compras" data-form="compra" novalidate>
        <input type="hidden" name="csrfToken" value="${csrfToken}">
        <input type="hidden" name="accion" value="guardar">

        <div class="form-grid">
            <div class="form-field ${not empty errores.proveedorId ? 'has-error' : ''}">
                <label class="form-label" for="proveedorId">Proveedor <span class="form-label__required">*</span></label>
                <select class="form-control" id="proveedorId" name="proveedorId" required>
                    <option value="">Seleccionar...</option>
                    <c:forEach var="pr" items="${proveedores.registros}">
                        <option value="${pr.idProveedor}" ${form.proveedorId == pr.idProveedor ? 'selected' : ''}>
                            <c:out value="${pr.nombre}"/>
                        </option>
                    </c:forEach>
                </select>
                <span class="form-error"><c:out value="${errores.proveedorId}"/></span>
            </div>
        </div>

        <div class="form-actions">
            <a class="btn btn--secondary" href="${pageContext.request.contextPath}/compras">Cancelar</a>
            <button type="submit" class="btn btn--primary">Iniciar compra</button>
        </div>
    </form>
</div>

<%@ include file="/WEB-INF/jsp/layout/footer.jsp" %>
