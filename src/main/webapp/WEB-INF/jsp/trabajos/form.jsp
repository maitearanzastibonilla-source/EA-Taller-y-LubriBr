<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="esAlta" value="${empty form.idTrabajo}"/>
<c:set var="tituloPagina" value="${esAlta ? 'Nuevo trabajo' : 'Editar trabajo'}" scope="request"/>
<c:set var="subtituloPagina" value="${esAlta ? 'Registre un trabajo sobre un vehiculo ingresado al taller' : 'Actualice la informacion del trabajo en proceso'}" scope="request"/>
<c:set var="activeMenu" value="trabajos" scope="request"/>

<%@ include file="/WEB-INF/jsp/layout/header.jsp" %>

<div class="page-header">
    <div>
        <h1 class="page-header__title"><c:out value="${tituloPagina}"/></h1>
        <p class="page-header__subtitle"><c:out value="${subtituloPagina}"/></p>
    </div>
    <div class="page-header__actions">
        <a class="btn btn--secondary" href="${pageContext.request.contextPath}/trabajos">Volver al listado</a>
    </div>
</div>

<c:if test="${not empty mensajeError}">
    <div class="alert alert--error" role="alert">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><circle cx="12" cy="12" r="9"/><path d="M12 8v5M12 16h.01"/></svg>
        <span><c:out value="${mensajeError}"/></span>
    </div>
</c:if>

<div class="surface surface--padded" style="max-width: 720px;">
    <form method="post" action="${pageContext.request.contextPath}/trabajos" data-form="trabajo" novalidate>
        <input type="hidden" name="csrfToken" value="${csrfToken}">
        <input type="hidden" name="accion" value="guardar">

        <c:choose>
            <c:when test="${esAlta}">
                <c:choose>
                    <c:when test="${not empty vehiculoDesdeTurno}">
                        <input type="hidden" name="vehiculoId" value="${vehiculoDesdeTurno.idVehiculo}">
                        <input type="hidden" name="turnoId" value="${form.turnoId}">
                        <div class="form-field form-field--full" style="margin-bottom: var(--space-5);">
                            <span class="form-label">Vehiculo (desde el turno seleccionado)</span>
                            <div class="form-control" style="display:flex; align-items:center; background:var(--color-black-50);">
                                <c:out value="${vehiculoDesdeTurno.patente}"/> — <c:out value="${vehiculoDesdeTurno.marca}"/> <c:out value="${vehiculoDesdeTurno.modelo}"/>
                            </div>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="form-grid">
                            <div class="form-field form-field--full">
                                <label class="form-label" for="clienteId">Cliente <span class="form-label__required">*</span></label>
                                <select class="form-control" id="clienteId" name="clienteId"
                                        data-recargar-vehiculos="${pageContext.request.contextPath}/trabajos?accion=nuevo">
                                    <option value="">Seleccione un cliente...</option>
                                    <c:forEach var="cli" items="${clientes.registros}">
                                        <option value="${cli.idCliente}" ${clienteIdSeleccionado == cli.idCliente ? 'selected' : ''}>
                                            <c:out value="${cli.apellido}"/>, <c:out value="${cli.nombre}"/>
                                        </option>
                                    </c:forEach>
                                </select>
                            </div>

                            <div class="form-field form-field--full ${not empty errores.vehiculoId ? 'has-error' : ''}">
                                <label class="form-label" for="vehiculoId">Vehiculo <span class="form-label__required">*</span></label>
                                <select class="form-control" id="vehiculoId" name="vehiculoId" required ${empty clienteIdSeleccionado ? 'disabled' : ''}>
                                    <option value="">Seleccione un vehiculo...</option>
                                    <c:forEach var="veh" items="${vehiculos}">
                                        <option value="${veh.idVehiculo}" ${form.vehiculoId == veh.idVehiculo ? 'selected' : ''}>
                                            <c:out value="${veh.patente}"/> — <c:out value="${veh.marca}"/> <c:out value="${veh.modelo}"/>
                                        </option>
                                    </c:forEach>
                                </select>
                                <c:if test="${not empty clienteIdSeleccionado and empty vehiculos}">
                                    <span class="form-hint">Este cliente no tiene vehiculos activos cargados todavia.</span>
                                </c:if>
                                <span class="form-error"><c:out value="${errores.vehiculoId}"/></span>
                            </div>
                        </div>
                    </c:otherwise>
                </c:choose>
            </c:when>
            <c:otherwise>
                <input type="hidden" name="idTrabajo" value="${form.idTrabajo}">
                <input type="hidden" name="vehiculoId" value="${form.vehiculoId}">
                <c:if test="${not empty form.turnoId}"><input type="hidden" name="turnoId" value="${form.turnoId}"></c:if>
                <div class="form-field form-field--full" style="margin-bottom: var(--space-5);">
                    <span class="form-label">Vehiculo</span>
                    <div class="form-control" style="display:flex; align-items:center; background:var(--color-black-50);">
                        <c:out value="${trabajoActual.vehiculoPatente}"/> — <c:out value="${trabajoActual.propietarioNombre}"/>
                    </div>
                </div>
            </c:otherwise>
        </c:choose>

        <div class="form-grid">
            <div class="form-field ${not empty errores.fechaIngreso ? 'has-error' : ''}">
                <label class="form-label" for="fechaIngreso">Fecha de ingreso <span class="form-label__required">*</span></label>
                <input type="date" class="form-control" id="fechaIngreso" name="fechaIngreso" value="${form.fechaIngreso}" required>
                <span class="form-error"><c:out value="${errores.fechaIngreso}"/></span>
            </div>

            <div class="form-field form-field--full ${not empty errores.descripcion ? 'has-error' : ''}">
                <label class="form-label" for="descripcion">Descripcion del trabajo <span class="form-label__required">*</span></label>
                <textarea class="form-control" id="descripcion" name="descripcion" maxlength="2000" rows="5"
                          placeholder="Diagnostico, tareas realizadas, observaciones...">${form.descripcion}</textarea>
                <span class="form-error"><c:out value="${errores.descripcion}"/></span>
            </div>
        </div>

        <div class="form-actions">
            <a class="btn btn--secondary" href="${pageContext.request.contextPath}/trabajos">Cancelar</a>
            <button type="submit" class="btn btn--primary">
                ${esAlta ? 'Crear trabajo' : 'Guardar cambios'}
            </button>
        </div>
    </form>
</div>

<%@ include file="/WEB-INF/jsp/layout/footer.jsp" %>
