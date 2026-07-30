<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="esAlta" value="${empty form.idProveedor}"/>
<c:set var="tituloPagina" value="${esAlta ? 'Nuevo proveedor' : 'Editar proveedor'}" scope="request"/>
<c:set var="subtituloPagina" value="${esAlta ? 'Cargue los datos del nuevo proveedor' : 'Actualice los datos del proveedor seleccionado'}" scope="request"/>
<c:set var="activeMenu" value="proveedores" scope="request"/>

<%@ include file="/WEB-INF/jsp/layout/header.jsp" %>

<div class="page-header">
    <div>
        <h1 class="page-header__title"><c:out value="${tituloPagina}"/></h1>
        <p class="page-header__subtitle"><c:out value="${subtituloPagina}"/></p>
    </div>
    <div class="page-header__actions">
        <a class="btn btn--secondary" href="${pageContext.request.contextPath}/proveedores">Volver al listado</a>
    </div>
</div>

<div class="surface surface--padded" style="max-width: 720px;">
    <form method="post" action="${pageContext.request.contextPath}/proveedores" data-form="proveedor" novalidate>
        <input type="hidden" name="csrfToken" value="${csrfToken}">
        <input type="hidden" name="accion" value="guardar">
        <c:if test="${not esAlta}">
            <input type="hidden" name="idProveedor" value="${form.idProveedor}">
        </c:if>

        <div class="form-grid">
            <div class="form-field form-field--full ${not empty errores.nombre ? 'has-error' : ''}">
                <label class="form-label" for="nombre">Nombre / razon social <span class="form-label__required">*</span></label>
                <input type="text" class="form-control" id="nombre" name="nombre" maxlength="100"
                       placeholder="Ej: Repuestos del Sur S.A." value="${form.nombre}" required>
                <span class="form-error"><c:out value="${errores.nombre}"/></span>
            </div>

            <div class="form-field ${not empty errores.contacto ? 'has-error' : ''}">
                <label class="form-label" for="contacto">Persona de contacto</label>
                <input type="text" class="form-control" id="contacto" name="contacto" maxlength="100"
                       placeholder="opcional" value="${form.contacto}">
                <span class="form-error"><c:out value="${errores.contacto}"/></span>
            </div>

            <div class="form-field ${not empty errores.telefono ? 'has-error' : ''}">
                <label class="form-label" for="telefono">Telefono <span class="form-label__required">*</span></label>
                <input type="text" class="form-control" id="telefono" name="telefono" maxlength="30"
                       placeholder="Ej: 351 555-1234" value="${form.telefono}" required>
                <span class="form-error"><c:out value="${errores.telefono}"/></span>
            </div>

            <div class="form-field form-field--full ${not empty errores.email ? 'has-error' : ''}">
                <label class="form-label" for="email">Correo electronico</label>
                <input type="email" class="form-control" id="email" name="email" maxlength="150"
                       placeholder="opcional" value="${form.email}">
                <span class="form-error"><c:out value="${errores.email}"/></span>
            </div>
        </div>

        <div class="form-actions">
            <a class="btn btn--secondary" href="${pageContext.request.contextPath}/proveedores">Cancelar</a>
            <button type="submit" class="btn btn--primary">
                ${esAlta ? 'Crear proveedor' : 'Guardar cambios'}
            </button>
        </div>
    </form>
</div>

<%@ include file="/WEB-INF/jsp/layout/footer.jsp" %>
