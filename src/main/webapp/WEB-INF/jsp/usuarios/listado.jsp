<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="ea" uri="https://eataller.com/tags/functions" %>

<c:set var="tituloPagina" value="Usuarios" scope="request"/>
<c:set var="subtituloPagina" value="Administracion de accesos al sistema" scope="request"/>
<c:set var="activeMenu" value="usuarios" scope="request"/>

<c:choose>
    <c:when test="${param.exito == 'creado'}"><c:set var="flashExito" value="Usuario creado correctamente." scope="request"/></c:when>
    <c:when test="${param.exito == 'modificado'}"><c:set var="flashExito" value="Usuario modificado correctamente." scope="request"/></c:when>
    <c:when test="${param.exito == 'activado'}"><c:set var="flashExito" value="Usuario activado correctamente." scope="request"/></c:when>
    <c:when test="${param.exito == 'desactivado'}"><c:set var="flashExito" value="Usuario desactivado correctamente." scope="request"/></c:when>
</c:choose>
<c:choose>
    <c:when test="${param.error == 'no_encontrado'}"><c:set var="flashError" value="El usuario solicitado no existe." scope="request"/></c:when>
    <c:when test="${param.error == 'sin_permisos'}"><c:set var="flashError" value="No tiene permisos para realizar esa accion." scope="request"/></c:when>
    <c:when test="${param.error == 'csrf'}"><c:set var="flashError" value="La sesion del formulario expiro. Intente nuevamente." scope="request"/></c:when>
</c:choose>

<%@ include file="/WEB-INF/jsp/layout/header.jsp" %>

<div class="page-header">
    <div>
        <h1 class="page-header__title">Usuarios</h1>
        <p class="page-header__subtitle">Gestione los perfiles de acceso al sistema y sus roles.</p>
    </div>
    <div class="page-header__actions">
        <a class="btn btn--primary" href="${pageContext.request.contextPath}/usuarios?accion=nuevo">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><path d="M12 5v14M5 12h14"/></svg>
            Nuevo usuario
        </a>
    </div>
</div>

<div class="surface">
    <form method="get" action="${pageContext.request.contextPath}/usuarios" class="filters-bar">
        <div class="search-input">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><circle cx="11" cy="11" r="7"/><path d="M21 21l-4.3-4.3"/></svg>
            <input type="text" class="form-control" name="q" placeholder="Buscar por nombre o email..." value="${q}">
        </div>
        <select class="form-control" name="rol">
            <option value="">Todos los roles</option>
            <c:forEach var="r" items="${roles}">
                <option value="${r}" ${rol == r.name() ? 'selected' : ''}><c:out value="${r.etiqueta}"/></option>
            </c:forEach>
        </select>
        <select class="form-control" name="activo">
            <option value="">Todos los estados</option>
            <option value="true" ${activo == 'true' ? 'selected' : ''}>Activos</option>
            <option value="false" ${activo == 'false' ? 'selected' : ''}>Inactivos</option>
        </select>
        <button type="submit" class="btn btn--secondary btn--sm">Filtrar</button>
        <button type="button" class="btn btn--ghost btn--sm"
                data-limpiar-filtros="${pageContext.request.contextPath}/usuarios">Limpiar</button>
    </form>

    <c:choose>
        <c:when test="${empty resultado.registros}">
            <div class="empty-state">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6"><circle cx="9" cy="8" r="3.2"/><path d="M3.5 19c0-3.3 2.5-5.5 5.5-5.5s5.5 2.2 5.5 5.5"/></svg>
                <h3>No se encontraron usuarios</h3>
                <p>Ajuste los filtros de busqueda o cree un nuevo usuario para comenzar.</p>
                <a class="btn btn--primary btn--sm" href="${pageContext.request.contextPath}/usuarios?accion=nuevo">Nuevo usuario</a>
            </div>
        </c:when>
        <c:otherwise>
            <div class="data-table-wrapper">
                <table class="data-table">
                    <thead>
                    <tr>
                        <th>Nombre</th>
                        <th>Email</th>
                        <th>Rol</th>
                        <th>Estado</th>
                        <th>Alta</th>
                        <th class="col-actions">Acciones</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach var="u" items="${resultado.registros}">
                        <tr>
                            <td data-label="Nombre" class="cell-primary"><c:out value="${u.nombre}"/></td>
                            <td data-label="Email"><c:out value="${u.email}"/></td>
                            <td data-label="Rol">
                                <span class="badge ${u.rol.name() == 'ADMINISTRADOR' ? 'badge--admin' : 'badge--operador'}">
                                    <c:out value="${u.rol.etiqueta}"/>
                                </span>
                            </td>
                            <td data-label="Estado">
                                <c:choose>
                                    <c:when test="${u.bloqueado}"><span class="badge badge--bloqueado">Bloqueado</span></c:when>
                                    <c:when test="${u.activo}"><span class="badge badge--activo">Activo</span></c:when>
                                    <c:otherwise><span class="badge badge--inactivo">Inactivo</span></c:otherwise>
                                </c:choose>
                            </td>
                            <td data-label="Alta" class="cell-secondary">${ea:fecha(u.createdAt)}</td>
                            <td data-label="Acciones" class="col-actions">
                                <div class="row-actions">
                                    <a class="btn btn--icon btn--ghost btn--sm" data-tooltip="Editar"
                                       href="${pageContext.request.contextPath}/usuarios?accion=editar&id=${u.idUsuario}">
                                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M12 20h9"/><path d="M16.5 3.5a2.1 2.1 0 013 3L7 19l-4 1 1-4L16.5 3.5z"/></svg>
                                    </a>
                                    <c:if test="${u.idUsuario != sessionScope.usuarioLogueado.idUsuario}">
                                        <form method="post" action="${pageContext.request.contextPath}/usuarios"
                                              data-confirm="${u.activo ? '¿Desactivar' : '¿Activar'} a ${u.nombre}?">
                                            <input type="hidden" name="csrfToken" value="${csrfToken}">
                                            <input type="hidden" name="accion" value="cambiarEstado">
                                            <input type="hidden" name="id" value="${u.idUsuario}">
                                            <input type="hidden" name="activo" value="${!u.activo}">
                                            <button type="submit" class="btn btn--icon btn--ghost btn--sm"
                                                    data-tooltip="${u.activo ? 'Desactivar' : 'Activar'}">
                                                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M12 2v10"/><path d="M6.3 6.3a9 9 0 1011.4 0"/></svg>
                                            </button>
                                        </form>
                                    </c:if>
                                </div>
                            </td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>

            <div class="pagination-bar">
                <span class="pagination-bar__info">
                    Pagina ${resultado.paginaActual} de ${resultado.totalPaginas} &middot; ${resultado.totalRegistros} usuarios
                </span>
                <div class="pagination-bar__controls">
                    <c:if test="${resultado.tieneAnterior()}">
                        <a class="btn btn--secondary btn--sm"
                           href="${pageContext.request.contextPath}/usuarios?pagina=${resultado.paginaActual - 1}&q=${q}&rol=${rol}&activo=${activo}">Anterior</a>
                    </c:if>
                    <c:if test="${resultado.tieneSiguiente()}">
                        <a class="btn btn--secondary btn--sm"
                           href="${pageContext.request.contextPath}/usuarios?pagina=${resultado.paginaActual + 1}&q=${q}&rol=${rol}&activo=${activo}">Siguiente</a>
                    </c:if>
                </div>
            </div>
        </c:otherwise>
    </c:choose>
</div>

<%@ include file="/WEB-INF/jsp/layout/footer.jsp" %>
