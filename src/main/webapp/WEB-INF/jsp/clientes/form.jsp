<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="esAlta" value="${empty form.idCliente}"/>
<c:set var="puedeEditarDni" value="${esAlta or sessionScope.usuarioLogueado.rol.name() == 'ADMINISTRADOR'}"/>
<c:set var="tituloPagina" value="${esAlta ? 'Nuevo cliente' : 'Editar cliente'}" scope="request"/>
<c:set var="subtituloPagina" value="${esAlta ? 'Cargue los datos del nuevo cliente' : 'Actualice los datos del cliente seleccionado'}" scope="request"/>
<c:set var="activeMenu" value="clientes" scope="request"/>

<%@ include file="/WEB-INF/jsp/layout/header.jsp" %>

<div class="page-header">
    <div>
        <h1 class="page-header__title"><c:out value="${tituloPagina}"/></h1>
        <p class="page-header__subtitle"><c:out value="${subtituloPagina}"/></p>
    </div>
    <div class="page-header__actions">
        <a class="btn btn--secondary" href="${pageContext.request.contextPath}/clientes">Volver al listado</a>
    </div>
</div>

<c:if test="${not empty mensajeError}">
    <div class="alert alert--error" role="alert">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><circle cx="12" cy="12" r="9"/><path d="M12 8v5M12 16h.01"/></svg>
        <span><c:out value="${mensajeError}"/></span>
    </div>
</c:if>

<div class="surface surface--padded" style="max-width: 720px;">
    <form method="post" action="${pageContext.request.contextPath}/clientes" data-form="cliente" novalidate>
        <input type="hidden" name="csrfToken" value="${csrfToken}">
        <input type="hidden" name="accion" value="guardar">
        <c:if test="${not esAlta}">
            <input type="hidden" name="idCliente" value="${form.idCliente}">
        </c:if>

        <div class="form-grid">
            <div class="form-field ${not empty errores.nombre ? 'has-error' : ''}">
                <label class="form-label" for="nombre">Nombre <span class="form-label__required">*</span></label>
                <input type="text" class="form-control" id="nombre" name="nombre" maxlength="60"
                       placeholder="Ej: Maria" value="${form.nombre}" required>
                <span class="form-error"><c:out value="${errores.nombre}"/></span>
            </div>

            <div class="form-field ${not empty errores.apellido ? 'has-error' : ''}">
                <label class="form-label" for="apellido">Apellido <span class="form-label__required">*</span></label>
                <input type="text" class="form-control" id="apellido" name="apellido" maxlength="60"
                       placeholder="Ej: Gonzalez" value="${form.apellido}" required>
                <span class="form-error"><c:out value="${errores.apellido}"/></span>
            </div>

            <div class="form-field ${not empty errores.dni ? 'has-error' : ''}">
                <label class="form-label" for="dni">DNI <span class="form-label__required">*</span></label>
                <input type="text" class="form-control" id="dni" name="dni" maxlength="8" inputmode="numeric"
                       placeholder="Ej: 30123456" value="${form.dni}" ${puedeEditarDni ? '' : 'readonly'} required>
                <c:if test="${not puedeEditarDni}">
                    <span class="form-hint">Solo un administrador puede modificar el DNI.</span>
                </c:if>
                <span class="form-error"><c:out value="${errores.dni}"/></span>
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

            <div class="form-field form-field--full ${not empty errores.direccion ? 'has-error' : ''}">
                <label class="form-label" for="direccion">Direccion</label>
                <input type="text" class="form-control" id="direccion" name="direccion" maxlength="200"
                       placeholder="opcional" value="${form.direccion}">
                <span class="form-error"><c:out value="${errores.direccion}"/></span>
            </div>
        </div>

        <div class="form-actions">
            <a class="btn btn--secondary" href="${pageContext.request.contextPath}/clientes">Cancelar</a>
            <button type="submit" class="btn btn--primary">
                ${esAlta ? 'Crear cliente' : 'Guardar cambios'}
            </button>
        </div>
    </form>
</div>

<%@ include file="/WEB-INF/jsp/layout/footer.jsp" %>
