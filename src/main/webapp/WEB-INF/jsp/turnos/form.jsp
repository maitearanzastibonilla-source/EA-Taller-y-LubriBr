<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="esAlta" value="${empty form.idTurno}"/>
<c:set var="tituloPagina" value="${esAlta ? 'Nuevo turno' : 'Editar turno'}" scope="request"/>
<c:set var="subtituloPagina" value="${esAlta ? 'Programe un servicio para un cliente y su vehiculo' : 'Reprograme, cambie el estado o actualice observaciones'}" scope="request"/>
<c:set var="activeMenu" value="turnos" scope="request"/>

<%@ include file="/WEB-INF/jsp/layout/header.jsp" %>

<div class="page-header">
    <div>
        <h1 class="page-header__title"><c:out value="${tituloPagina}"/></h1>
        <p class="page-header__subtitle"><c:out value="${subtituloPagina}"/></p>
    </div>
    <div class="page-header__actions">
        <a class="btn btn--secondary" href="${pageContext.request.contextPath}/turnos">Volver al listado</a>
    </div>
</div>

<c:if test="${not empty mensajeError}">
    <div class="alert alert--error" role="alert">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><circle cx="12" cy="12" r="9"/><path d="M12 8v5M12 16h.01"/></svg>
        <span><c:out value="${mensajeError}"/></span>
    </div>
</c:if>

<div class="surface surface--padded" style="max-width: 720px;">
    <form method="post" action="${pageContext.request.contextPath}/turnos" data-form="turno" novalidate>
        <input type="hidden" name="csrfToken" value="${csrfToken}">
        <input type="hidden" name="accion" value="guardar">

        <c:choose>
            <c:when test="${esAlta}">
                <div class="form-grid">
                    <div class="form-field form-field--full ${not empty errores.clienteId ? 'has-error' : ''}">
                        <label class="form-label" for="clienteId">Cliente <span class="form-label__required">*</span></label>
                        <select class="form-control" id="clienteId" name="clienteId" required
                                data-recargar-vehiculos="${pageContext.request.contextPath}/turnos?accion=nuevo">
                            <option value="">Seleccione un cliente...</option>
                            <c:forEach var="cli" items="${clientes.registros}">
                                <option value="${cli.idCliente}" ${form.clienteId == cli.idCliente ? 'selected' : ''}>
                                    <c:out value="${cli.apellido}"/>, <c:out value="${cli.nombre}"/>
                                </option>
                            </c:forEach>
                        </select>
                        <span class="form-error"><c:out value="${errores.clienteId}"/></span>
                    </div>

                    <div class="form-field form-field--full ${not empty errores.vehiculoId ? 'has-error' : ''}">
                        <label class="form-label" for="vehiculoId">Vehiculo <span class="form-label__required">*</span></label>
                        <select class="form-control" id="vehiculoId" name="vehiculoId" required ${empty form.clienteId ? 'disabled' : ''}>
                            <option value="">Seleccione un vehiculo...</option>
                            <c:forEach var="veh" items="${vehiculos}">
                                <option value="${veh.idVehiculo}" ${form.vehiculoId == veh.idVehiculo ? 'selected' : ''}>
                                    <c:out value="${veh.patente}"/> — <c:out value="${veh.marca}"/> <c:out value="${veh.modelo}"/>
                                </option>
                            </c:forEach>
                        </select>
                        <c:if test="${not empty form.clienteId and empty vehiculos}">
                            <span class="form-hint">Este cliente no tiene vehiculos activos cargados todavia.</span>
                        </c:if>
                        <span class="form-error"><c:out value="${errores.vehiculoId}"/></span>
                    </div>
                </div>
            </c:when>
            <c:otherwise>
                <input type="hidden" name="idTurno" value="${form.idTurno}">
                <input type="hidden" name="clienteId" value="${form.clienteId}">
                <input type="hidden" name="vehiculoId" value="${form.vehiculoId}">
                <div class="form-grid" style="margin-bottom: var(--space-5);">
                    <div class="form-field">
                        <span class="form-label">Cliente</span>
                        <div class="form-control" style="display:flex; align-items:center; background:var(--color-black-50);">
                            <c:out value="${turnoActual.clienteNombre}"/>
                        </div>
                    </div>
                    <div class="form-field">
                        <span class="form-label">Vehiculo</span>
                        <div class="form-control" style="display:flex; align-items:center; background:var(--color-black-50);">
                            <c:out value="${turnoActual.vehiculoPatente}"/>
                        </div>
                    </div>
                </div>
            </c:otherwise>
        </c:choose>

        <div class="form-grid">
            <div class="form-field ${not empty errores.fechaHora ? 'has-error' : ''}">
                <label class="form-label" for="fechaHora">Fecha y hora <span class="form-label__required">*</span></label>
                <input type="datetime-local" class="form-control" id="fechaHora" name="fechaHora"
                       value="${form.fechaHora}" required>
                <span class="form-error"><c:out value="${errores.fechaHora}"/></span>
            </div>

            <div class="form-field ${not empty errores.tipoServicio ? 'has-error' : ''}">
                <label class="form-label" for="tipoServicio">Tipo de servicio <span class="form-label__required">*</span></label>
                <select class="form-control" id="tipoServicio" name="tipoServicio" required>
                    <option value="">Seleccione...</option>
                    <option value="MECANICA" ${form.tipoServicio == 'MECANICA' ? 'selected' : ''}>Mecanica</option>
                    <option value="LUBRICENTRO" ${form.tipoServicio == 'LUBRICENTRO' ? 'selected' : ''}>Lubricentro</option>
                    <option value="OTRO" ${form.tipoServicio == 'OTRO' ? 'selected' : ''}>Otro</option>
                </select>
                <span class="form-error"><c:out value="${errores.tipoServicio}"/></span>
            </div>

            <div class="form-field form-field--full ${not empty errores.estado ? 'has-error' : ''}">
                <label class="form-label" for="estado">Estado <span class="form-label__required">*</span></label>
                <select class="form-control" id="estado" name="estado" required>
                    <c:choose>
                        <c:when test="${esAlta}">
                            <option value="PENDIENTE" ${form.estado == 'PENDIENTE' ? 'selected' : ''}>Pendiente</option>
                            <option value="CONFIRMADO" ${form.estado == 'CONFIRMADO' ? 'selected' : ''}>Confirmado</option>
                        </c:when>
                        <c:otherwise>
                            <option value="PENDIENTE" ${form.estado == 'PENDIENTE' ? 'selected' : ''}>Pendiente</option>
                            <option value="CONFIRMADO" ${form.estado == 'CONFIRMADO' ? 'selected' : ''}>Confirmado</option>
                            <option value="EN_PROCESO" ${form.estado == 'EN_PROCESO' ? 'selected' : ''}>En proceso</option>
                            <option value="FINALIZADO" ${form.estado == 'FINALIZADO' ? 'selected' : ''}>Finalizado</option>
                        </c:otherwise>
                    </c:choose>
                </select>
                <c:if test="${not esAlta}">
                    <span class="form-hint">Para cancelar el turno, use la accion Cancelar desde el listado.</span>
                </c:if>
                <span class="form-error"><c:out value="${errores.estado}"/></span>
            </div>

            <div class="form-field form-field--full ${not empty errores.notas ? 'has-error' : ''}">
                <label class="form-label" for="notas">Notas</label>
                <textarea class="form-control" id="notas" name="notas" maxlength="500"
                          placeholder="Observaciones del turno (opcional)">${form.notas}</textarea>
                <span class="form-error"><c:out value="${errores.notas}"/></span>
            </div>
        </div>

        <div class="form-actions">
            <a class="btn btn--secondary" href="${pageContext.request.contextPath}/turnos">Cancelar</a>
            <button type="submit" class="btn btn--primary">
                ${esAlta ? 'Crear turno' : 'Guardar cambios'}
            </button>
        </div>
    </form>
</div>

<%@ include file="/WEB-INF/jsp/layout/footer.jsp" %>
