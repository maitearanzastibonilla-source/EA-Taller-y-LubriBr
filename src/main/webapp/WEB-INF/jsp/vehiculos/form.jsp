<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="esAlta" value="${empty form.idVehiculo}"/>
<c:set var="puedeEditarPatente" value="${esAlta or sessionScope.usuarioLogueado.rol.name() == 'ADMINISTRADOR'}"/>
<c:set var="tituloPagina" value="${esAlta ? 'Nuevo vehiculo' : 'Editar vehiculo'}" scope="request"/>
<c:set var="subtituloPagina" value="${esAlta ? 'Asocie un vehiculo a un cliente existente' : 'Actualice los datos del vehiculo'}" scope="request"/>
<c:set var="activeMenu" value="vehiculos" scope="request"/>

<%@ include file="/WEB-INF/jsp/layout/header.jsp" %>

<div class="page-header">
    <div>
        <h1 class="page-header__title"><c:out value="${tituloPagina}"/></h1>
        <p class="page-header__subtitle"><c:out value="${subtituloPagina}"/></p>
    </div>
    <div class="page-header__actions">
        <a class="btn btn--secondary" href="${pageContext.request.contextPath}/vehiculos">Volver al listado</a>
    </div>
</div>

<c:if test="${not empty mensajeError}">
    <div class="alert alert--error" role="alert">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><circle cx="12" cy="12" r="9"/><path d="M12 8v5M12 16h.01"/></svg>
        <span><c:out value="${mensajeError}"/></span>
    </div>
</c:if>

<div class="surface surface--padded" style="max-width: 720px;">
    <form method="post" action="${pageContext.request.contextPath}/vehiculos" data-form="vehiculo" novalidate>
        <input type="hidden" name="csrfToken" value="${csrfToken}">
        <input type="hidden" name="accion" value="guardar">
        <c:if test="${not esAlta}">
            <input type="hidden" name="idVehiculo" value="${form.idVehiculo}">
        </c:if>

        <div class="form-grid">
            <div class="form-field form-field--full ${not empty errores.clienteId ? 'has-error' : ''}">
                <label class="form-label" for="clienteId">Propietario <span class="form-label__required">*</span></label>
                <select class="form-control" id="clienteId" name="clienteId" required>
                    <option value="">Seleccione un cliente...</option>
                    <c:forEach var="cli" items="${clientes.registros}">
                        <option value="${cli.idCliente}" ${form.clienteId == cli.idCliente ? 'selected' : ''}>
                            <c:out value="${cli.apellido}"/>, <c:out value="${cli.nombre}"/> — DNI <c:out value="${cli.dni}"/>
                        </option>
                    </c:forEach>
                </select>
                <span class="form-error"><c:out value="${errores.clienteId}"/></span>
                <span class="form-hint">Si el cliente no aparece en la lista, primero debe estar activo en el modulo de Clientes.</span>
            </div>

            <div class="form-field ${not empty errores.patente ? 'has-error' : ''}">
                <label class="form-label" for="patente">Patente <span class="form-label__required">*</span></label>
                <input type="text" class="form-control" id="patente" name="patente" maxlength="10" style="text-transform:uppercase;"
                       placeholder="Ej: AB123CD" value="${form.patente}" ${puedeEditarPatente ? '' : 'readonly'} required>
                <c:if test="${not puedeEditarPatente}">
                    <span class="form-hint">Solo un administrador puede modificar la patente.</span>
                </c:if>
                <span class="form-error"><c:out value="${errores.patente}"/></span>
            </div>

            <div class="form-field ${not empty errores.anio ? 'has-error' : ''}">
                <label class="form-label" for="anio">Anio <span class="form-label__required">*</span></label>
                <input type="number" class="form-control" id="anio" name="anio" min="1950" max="2100"
                       placeholder="Ej: 2018" value="${form.anio}" required>
                <span class="form-error"><c:out value="${errores.anio}"/></span>
            </div>

            <div class="form-field ${not empty errores.marca ? 'has-error' : ''}">
                <label class="form-label" for="marca">Marca <span class="form-label__required">*</span></label>
                <input type="text" class="form-control" id="marca" name="marca" maxlength="60"
                       placeholder="Ej: Volkswagen" value="${form.marca}" required>
                <span class="form-error"><c:out value="${errores.marca}"/></span>
            </div>

            <div class="form-field ${not empty errores.modelo ? 'has-error' : ''}">
                <label class="form-label" for="modelo">Modelo <span class="form-label__required">*</span></label>
                <input type="text" class="form-control" id="modelo" name="modelo" maxlength="60"
                       placeholder="Ej: Gol Trend" value="${form.modelo}" required>
                <span class="form-error"><c:out value="${errores.modelo}"/></span>
            </div>
        </div>

        <div class="form-actions">
            <a class="btn btn--secondary" href="${pageContext.request.contextPath}/vehiculos">Cancelar</a>
            <button type="submit" class="btn btn--primary">
                ${esAlta ? 'Crear vehiculo' : 'Guardar cambios'}
            </button>
        </div>
    </form>
</div>

<%@ include file="/WEB-INF/jsp/layout/footer.jsp" %>
