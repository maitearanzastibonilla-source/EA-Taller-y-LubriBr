<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="esAlta" value="${empty form.idUsuario}"/>
<c:set var="tituloPagina" value="${esAlta ? 'Nuevo usuario' : 'Editar usuario'}" scope="request"/>
<c:set var="subtituloPagina" value="${esAlta ? 'Complete los datos para dar de alta un nuevo acceso' : 'Actualice los datos del usuario seleccionado'}" scope="request"/>
<c:set var="activeMenu" value="usuarios" scope="request"/>

<%@ include file="/WEB-INF/jsp/layout/header.jsp" %>

<div class="page-header">
    <div>
        <h1 class="page-header__title"><c:out value="${tituloPagina}"/></h1>
        <p class="page-header__subtitle"><c:out value="${subtituloPagina}"/></p>
    </div>
    <div class="page-header__actions">
        <a class="btn btn--secondary" href="${pageContext.request.contextPath}/usuarios">Volver al listado</a>
    </div>
</div>

<c:if test="${not empty mensajeError}">
    <div class="alert alert--error" role="alert">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><circle cx="12" cy="12" r="9"/><path d="M12 8v5M12 16h.01"/></svg>
        <span><c:out value="${mensajeError}"/></span>
    </div>
</c:if>

<div class="surface surface--padded" style="max-width: 720px;">
    <form method="post" action="${pageContext.request.contextPath}/usuarios"
          data-form="usuario" data-es-alta="${esAlta}" novalidate>
        <input type="hidden" name="csrfToken" value="${csrfToken}">
        <input type="hidden" name="accion" value="guardar">
        <c:if test="${not esAlta}">
            <input type="hidden" name="idUsuario" value="${form.idUsuario}">
        </c:if>

        <div class="form-grid">
            <div class="form-field form-field--full ${not empty errores.nombre ? 'has-error' : ''}">
                <label class="form-label" for="nombre">Nombre completo <span class="form-label__required">*</span></label>
                <input type="text" class="form-control" id="nombre" name="nombre" maxlength="100"
                       placeholder="Ej: Juan Perez" value="${form.nombre}" required>
                <span class="form-error"><c:out value="${errores.nombre}"/></span>
            </div>

            <div class="form-field form-field--full ${not empty errores.email ? 'has-error' : ''}">
                <label class="form-label" for="email">Correo electronico <span class="form-label__required">*</span></label>
                <input type="email" class="form-control" id="email" name="email" maxlength="150"
                       placeholder="nombre@eatallerylubribr.com" value="${form.email}" autocomplete="off" required>
                <span class="form-error"><c:out value="${errores.email}"/></span>
                <span class="form-hint">Se utiliza como usuario de acceso al sistema.</span>
            </div>

            <div class="form-field ${not empty errores.password ? 'has-error' : ''}">
                <label class="form-label" for="password">
                    Contrasenia <c:if test="${esAlta}"><span class="form-label__required">*</span></c:if>
                </label>
                <div style="position:relative;">
                    <input type="password" class="form-control" id="password" name="password" autocomplete="new-password"
                           placeholder="${esAlta ? 'Minimo 8 caracteres' : 'Dejar en blanco para no modificarla'}">
                    <button type="button" class="btn btn--icon btn--ghost btn--sm" data-toggle-password="password"
                            style="position:absolute; right:2px; top:2px;" aria-label="Mostrar contrasenia">
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><path d="M1 12s4-7 11-7 11 7 11 7-4 7-11 7-11-7-11-7z"/><circle cx="12" cy="12" r="3"/></svg>
                    </button>
                </div>
                <span class="form-error"><c:out value="${errores.password}"/></span>
            </div>

            <div class="form-field ${not empty errores.confirmarPassword ? 'has-error' : ''}">
                <label class="form-label" for="confirmarPassword">
                    Confirmar contrasenia <c:if test="${esAlta}"><span class="form-label__required">*</span></c:if>
                </label>
                <input type="password" class="form-control" id="confirmarPassword" name="confirmarPassword"
                       autocomplete="new-password" placeholder="Repita la contrasenia">
                <span class="form-error"><c:out value="${errores.confirmarPassword}"/></span>
            </div>

            <div class="form-field form-field--full ${not empty errores.rol ? 'has-error' : ''}">
                <label class="form-label" for="rol">Rol <span class="form-label__required">*</span></label>
                <select class="form-control" id="rol" name="rol" required
                        ${not esAlta and form.idUsuario == sessionScope.usuarioLogueado.idUsuario ? 'disabled' : ''}>
                    <option value="">Seleccione un rol...</option>
                    <c:forEach var="r" items="${roles}">
                        <option value="${r}" ${form.rol == r.name() ? 'selected' : ''}><c:out value="${r.etiqueta}"/></option>
                    </c:forEach>
                </select>
                <c:if test="${not esAlta and form.idUsuario == sessionScope.usuarioLogueado.idUsuario}">
                    <input type="hidden" name="rol" value="${form.rol}">
                    <span class="form-hint">No puede modificar su propio rol.</span>
                </c:if>
                <span class="form-error"><c:out value="${errores.rol}"/></span>
            </div>
        </div>

        <div class="form-actions">
            <a class="btn btn--secondary" href="${pageContext.request.contextPath}/usuarios">Cancelar</a>
            <button type="submit" class="btn btn--primary">
                ${esAlta ? 'Crear usuario' : 'Guardar cambios'}
            </button>
        </div>
    </form>
</div>

<%@ include file="/WEB-INF/jsp/layout/footer.jsp" %>
