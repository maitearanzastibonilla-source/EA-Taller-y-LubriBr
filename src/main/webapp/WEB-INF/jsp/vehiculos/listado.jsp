<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="ea" uri="https://eataller.com/tags/functions" %>

<c:set var="tituloPagina" value="Vehiculos" scope="request"/>
<c:set var="subtituloPagina" value="Vehiculos registrados de los clientes del taller" scope="request"/>
<c:set var="activeMenu" value="vehiculos" scope="request"/>

<c:choose>
    <c:when test="${param.exito == 'creado'}"><c:set var="flashExito" value="Vehiculo creado correctamente." scope="request"/></c:when>
    <c:when test="${param.exito == 'modificado'}"><c:set var="flashExito" value="Vehiculo modificado correctamente." scope="request"/></c:when>
    <c:when test="${param.exito == 'activado'}"><c:set var="flashExito" value="Vehiculo activado correctamente." scope="request"/></c:when>
    <c:when test="${param.exito == 'desactivado'}"><c:set var="flashExito" value="Vehiculo desactivado correctamente." scope="request"/></c:when>
</c:choose>
<c:choose>
    <c:when test="${param.error == 'no_encontrado'}"><c:set var="flashError" value="El vehiculo solicitado no existe." scope="request"/></c:when>
    <c:when test="${param.error == 'csrf'}"><c:set var="flashError" value="La sesion del formulario expiro. Intente nuevamente." scope="request"/></c:when>
</c:choose>

<%@ include file="/WEB-INF/jsp/layout/header.jsp" %>

<div class="page-header">
    <div>
        <h1 class="page-header__title">Vehiculos</h1>
        <p class="page-header__subtitle">Patente, marca, modelo y propietario de cada vehiculo.</p>
    </div>
    <div class="page-header__actions">
        <a class="btn btn--primary" href="${pageContext.request.contextPath}/vehiculos?accion=nuevo">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><path d="M12 5v14M5 12h14"/></svg>
            Nuevo vehiculo
        </a>
    </div>
</div>

<div class="surface">
    <form method="get" action="${pageContext.request.contextPath}/vehiculos" class="filters-bar">
        <div class="search-input">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><circle cx="11" cy="11" r="7"/><path d="M21 21l-4.3-4.3"/></svg>
            <input type="text" class="form-control" name="q" placeholder="Buscar por patente, marca, modelo o propietario..." value="${q}">
        </div>
        <select class="form-control" name="estado">
            <option value="">Todos los estados</option>
            <option value="ACTIVO" ${estado == 'ACTIVO' ? 'selected' : ''}>Activos</option>
            <option value="INACTIVO" ${estado == 'INACTIVO' ? 'selected' : ''}>Inactivos</option>
        </select>
        <button type="submit" class="btn btn--secondary btn--sm">Filtrar</button>
        <button type="button" class="btn btn--ghost btn--sm"
                data-limpiar-filtros="${pageContext.request.contextPath}/vehiculos">Limpiar</button>
    </form>

    <c:choose>
        <c:when test="${empty resultado.registros}">
            <div class="empty-state">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6"><path d="M5 17h14M6 17V9l2-4h8l2 4v8M9 17v-4h6v4"/><circle cx="7.5" cy="17.5" r="1.5"/><circle cx="16.5" cy="17.5" r="1.5"/></svg>
                <h3>No se encontraron vehiculos</h3>
                <p>Ajuste los filtros de busqueda o cargue un nuevo vehiculo.</p>
                <a class="btn btn--primary btn--sm" href="${pageContext.request.contextPath}/vehiculos?accion=nuevo">Nuevo vehiculo</a>
            </div>
        </c:when>
        <c:otherwise>
            <div class="data-table-wrapper">
                <table class="data-table">
                    <thead>
                    <tr>
                        <th>Patente</th>
                        <th>Marca / Modelo</th>
                        <th>Anio</th>
                        <th>Propietario</th>
                        <th>Estado</th>
                        <th>Alta</th>
                        <th class="col-actions">Acciones</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach var="veh" items="${resultado.registros}">
                        <tr>
                            <td data-label="Patente" class="cell-primary"><c:out value="${veh.patente}"/></td>
                            <td data-label="Marca / Modelo"><c:out value="${veh.marca}"/> <c:out value="${veh.modelo}"/></td>
                            <td data-label="Anio">${veh.anio}</td>
                            <td data-label="Propietario"><c:out value="${veh.propietarioNombre}"/></td>
                            <td data-label="Estado">
                                <c:choose>
                                    <c:when test="${veh.estado.name() == 'ACTIVO'}"><span class="badge badge--activo">Activo</span></c:when>
                                    <c:otherwise><span class="badge badge--inactivo">Inactivo</span></c:otherwise>
                                </c:choose>
                            </td>
                            <td data-label="Alta" class="cell-secondary">${ea:fecha(veh.createdAt)}</td>
                            <td data-label="Acciones" class="col-actions">
                                <div class="row-actions">
                                    <a class="btn btn--icon btn--ghost btn--sm" data-tooltip="Editar"
                                       href="${pageContext.request.contextPath}/vehiculos?accion=editar&id=${veh.idVehiculo}">
                                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M12 20h9"/><path d="M16.5 3.5a2.1 2.1 0 013 3L7 19l-4 1 1-4L16.5 3.5z"/></svg>
                                    </a>
                                    <form method="post" action="${pageContext.request.contextPath}/vehiculos"
                                          data-confirm="${veh.estado.name() == 'ACTIVO' ? '¿Desactivar' : '¿Activar'} el vehiculo ${veh.patente}?">
                                        <input type="hidden" name="csrfToken" value="${csrfToken}">
                                        <input type="hidden" name="accion" value="cambiarEstado">
                                        <input type="hidden" name="id" value="${veh.idVehiculo}">
                                        <input type="hidden" name="activo" value="${veh.estado.name() != 'ACTIVO'}">
                                        <button type="submit" class="btn btn--icon btn--ghost btn--sm"
                                                data-tooltip="${veh.estado.name() == 'ACTIVO' ? 'Desactivar' : 'Activar'}">
                                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M12 2v10"/><path d="M6.3 6.3a9 9 0 1011.4 0"/></svg>
                                        </button>
                                    </form>
                                </div>
                            </td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>

            <div class="pagination-bar">
                <span class="pagination-bar__info">
                    Pagina ${resultado.paginaActual} de ${resultado.totalPaginas} &middot; ${resultado.totalRegistros} vehiculos
                </span>
                <div class="pagination-bar__controls">
                    <c:if test="${resultado.tieneAnterior()}">
                        <a class="btn btn--secondary btn--sm"
                           href="${pageContext.request.contextPath}/vehiculos?pagina=${resultado.paginaActual - 1}&q=${q}&estado=${estado}">Anterior</a>
                    </c:if>
                    <c:if test="${resultado.tieneSiguiente()}">
                        <a class="btn btn--secondary btn--sm"
                           href="${pageContext.request.contextPath}/vehiculos?pagina=${resultado.paginaActual + 1}&q=${q}&estado=${estado}">Siguiente</a>
                    </c:if>
                </div>
            </div>
        </c:otherwise>
    </c:choose>
</div>

<%@ include file="/WEB-INF/jsp/layout/footer.jsp" %>
