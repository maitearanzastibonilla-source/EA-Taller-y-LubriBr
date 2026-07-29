<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="ea" uri="https://eataller.com/tags/functions" %>

<c:set var="tituloPagina" value="Clientes" scope="request"/>
<c:set var="subtituloPagina" value="Ficha y contacto de los clientes del taller" scope="request"/>
<c:set var="activeMenu" value="clientes" scope="request"/>

<c:choose>
    <c:when test="${param.exito == 'creado'}"><c:set var="flashExito" value="Cliente creado correctamente." scope="request"/></c:when>
    <c:when test="${param.exito == 'modificado'}"><c:set var="flashExito" value="Cliente modificado correctamente." scope="request"/></c:when>
    <c:when test="${param.exito == 'activado'}"><c:set var="flashExito" value="Cliente activado correctamente." scope="request"/></c:when>
    <c:when test="${param.exito == 'desactivado'}"><c:set var="flashExito" value="Cliente desactivado correctamente." scope="request"/></c:when>
</c:choose>
<c:choose>
    <c:when test="${param.error == 'no_encontrado'}"><c:set var="flashError" value="El cliente solicitado no existe." scope="request"/></c:when>
    <c:when test="${param.error == 'sin_permisos'}"><c:set var="flashError" value="No tiene permisos para realizar esa accion." scope="request"/></c:when>
    <c:when test="${param.error == 'csrf'}"><c:set var="flashError" value="La sesion del formulario expiro. Intente nuevamente." scope="request"/></c:when>
</c:choose>

<%@ include file="/WEB-INF/jsp/layout/header.jsp" %>

<div class="page-header">
    <div>
        <h1 class="page-header__title">Clientes</h1>
        <p class="page-header__subtitle">Base de clientes del taller y lubricentro.</p>
    </div>
    <div class="page-header__actions">
        <a class="btn btn--primary" href="${pageContext.request.contextPath}/clientes?accion=nuevo">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><path d="M12 5v14M5 12h14"/></svg>
            Nuevo cliente
        </a>
    </div>
</div>

<div class="surface">
    <form method="get" action="${pageContext.request.contextPath}/clientes" class="filters-bar">
        <div class="search-input">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><circle cx="11" cy="11" r="7"/><path d="M21 21l-4.3-4.3"/></svg>
            <input type="text" class="form-control" name="q" placeholder="Buscar por nombre, DNI, telefono o email..." value="${q}">
        </div>
        <select class="form-control" name="estado">
            <option value="">Todos los estados</option>
            <option value="ACTIVO" ${estado == 'ACTIVO' ? 'selected' : ''}>Activos</option>
            <option value="INACTIVO" ${estado == 'INACTIVO' ? 'selected' : ''}>Inactivos</option>
        </select>
        <button type="submit" class="btn btn--secondary btn--sm">Filtrar</button>
        <button type="button" class="btn btn--ghost btn--sm"
                data-limpiar-filtros="${pageContext.request.contextPath}/clientes">Limpiar</button>
    </form>

    <c:choose>
        <c:when test="${empty resultado.registros}">
            <div class="empty-state">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6"><rect x="3" y="7" width="18" height="13" rx="2"/><path d="M8 7V5a2 2 0 012-2h4a2 2 0 012 2v2"/></svg>
                <h3>No se encontraron clientes</h3>
                <p>Ajuste los filtros de busqueda o cargue un nuevo cliente.</p>
                <a class="btn btn--primary btn--sm" href="${pageContext.request.contextPath}/clientes?accion=nuevo">Nuevo cliente</a>
            </div>
        </c:when>
        <c:otherwise>
            <div class="data-table-wrapper">
                <table class="data-table">
                    <thead>
                    <tr>
                        <th>Cliente</th>
                        <th>DNI</th>
                        <th>Telefono</th>
                        <th>Email</th>
                        <th>Estado</th>
                        <th>Alta</th>
                        <th class="col-actions">Acciones</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach var="cli" items="${resultado.registros}">
                        <tr>
                            <td data-label="Cliente" class="cell-primary"><c:out value="${cli.nombreCompleto}"/></td>
                            <td data-label="DNI"><c:out value="${cli.dni}"/></td>
                            <td data-label="Telefono"><c:out value="${cli.telefono}"/></td>
                            <td data-label="Email" class="cell-secondary"><c:out value="${cli.email}"/></td>
                            <td data-label="Estado">
                                <c:choose>
                                    <c:when test="${cli.estado.name() == 'ACTIVO'}"><span class="badge badge--activo">Activo</span></c:when>
                                    <c:otherwise><span class="badge badge--inactivo">Inactivo</span></c:otherwise>
                                </c:choose>
                            </td>
                            <td data-label="Alta" class="cell-secondary">${ea:fecha(cli.createdAt)}</td>
                            <td data-label="Acciones" class="col-actions">
                                <div class="row-actions">
                                    <a class="btn btn--icon btn--ghost btn--sm" data-tooltip="Editar"
                                       href="${pageContext.request.contextPath}/clientes?accion=editar&id=${cli.idCliente}">
                                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M12 20h9"/><path d="M16.5 3.5a2.1 2.1 0 013 3L7 19l-4 1 1-4L16.5 3.5z"/></svg>
                                    </a>
                                    <c:if test="${cli.estado.name() == 'ACTIVO'}">
                                        <a class="btn btn--icon btn--ghost btn--sm" data-tooltip="Agregar vehiculo"
                                           href="${pageContext.request.contextPath}/vehiculos?accion=nuevo&clienteId=${cli.idCliente}">
                                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M5 17h14M6 17V9l2-4h8l2 4v8M9 17v-4h6v4"/><circle cx="7.5" cy="17.5" r="1.5"/><circle cx="16.5" cy="17.5" r="1.5"/></svg>
                                        </a>
                                    </c:if>
                                    <c:if test="${sessionScope.usuarioLogueado.rol.name() == 'ADMINISTRADOR'}">
                                        <form method="post" action="${pageContext.request.contextPath}/clientes"
                                              data-confirm="${cli.estado.name() == 'ACTIVO' ? '¿Desactivar' : '¿Activar'} a ${cli.nombreCompleto}?">
                                            <input type="hidden" name="csrfToken" value="${csrfToken}">
                                            <input type="hidden" name="accion" value="cambiarEstado">
                                            <input type="hidden" name="id" value="${cli.idCliente}">
                                            <input type="hidden" name="activo" value="${cli.estado.name() != 'ACTIVO'}">
                                            <button type="submit" class="btn btn--icon btn--ghost btn--sm"
                                                    data-tooltip="${cli.estado.name() == 'ACTIVO' ? 'Desactivar' : 'Activar'}">
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
                    Pagina ${resultado.paginaActual} de ${resultado.totalPaginas} &middot; ${resultado.totalRegistros} clientes
                </span>
                <div class="pagination-bar__controls">
                    <c:if test="${resultado.tieneAnterior()}">
                        <a class="btn btn--secondary btn--sm"
                           href="${pageContext.request.contextPath}/clientes?pagina=${resultado.paginaActual - 1}&q=${q}&estado=${estado}">Anterior</a>
                    </c:if>
                    <c:if test="${resultado.tieneSiguiente()}">
                        <a class="btn btn--secondary btn--sm"
                           href="${pageContext.request.contextPath}/clientes?pagina=${resultado.paginaActual + 1}&q=${q}&estado=${estado}">Siguiente</a>
                    </c:if>
                </div>
            </div>
        </c:otherwise>
    </c:choose>
</div>

<%@ include file="/WEB-INF/jsp/layout/footer.jsp" %>
