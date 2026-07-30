<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="esAlta" value="${empty form.idProducto}"/>
<c:set var="tituloPagina" value="${esAlta ? 'Nuevo producto' : 'Editar producto'}" scope="request"/>
<c:set var="subtituloPagina" value="${esAlta ? 'Cargue los datos del nuevo producto' : 'Actualice los datos del producto seleccionado'}" scope="request"/>
<c:set var="activeMenu" value="productos" scope="request"/>

<%@ include file="/WEB-INF/jsp/layout/header.jsp" %>

<div class="page-header">
    <div>
        <h1 class="page-header__title"><c:out value="${tituloPagina}"/></h1>
        <p class="page-header__subtitle"><c:out value="${subtituloPagina}"/></p>
    </div>
    <div class="page-header__actions">
        <a class="btn btn--secondary" href="${pageContext.request.contextPath}/productos">Volver al listado</a>
    </div>
</div>

<c:if test="${not empty mensajeError}">
    <div class="alert alert--error" role="alert">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><circle cx="12" cy="12" r="9"/><path d="M12 8v5M12 16h.01"/></svg>
        <span><c:out value="${mensajeError}"/></span>
    </div>
</c:if>

<div class="surface surface--padded" style="max-width: 780px;">
    <form method="post" action="${pageContext.request.contextPath}/productos" data-form="producto" novalidate>
        <input type="hidden" name="csrfToken" value="${csrfToken}">
        <input type="hidden" name="accion" value="guardar">
        <c:if test="${not esAlta}">
            <input type="hidden" name="idProducto" value="${form.idProducto}">
        </c:if>

        <div class="form-grid">
            <div class="form-field form-field--full ${not empty errores.nombre ? 'has-error' : ''}">
                <label class="form-label" for="nombre">Nombre <span class="form-label__required">*</span></label>
                <input type="text" class="form-control" id="nombre" name="nombre" maxlength="150"
                       placeholder="Ej: Filtro de aceite" value="${form.nombre}" required>
                <span class="form-error"><c:out value="${errores.nombre}"/></span>
            </div>

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

            <div class="form-field ${not empty errores.categoria ? 'has-error' : ''}">
                <label class="form-label" for="categoria">Categoria</label>
                <input type="text" class="form-control" id="categoria" name="categoria" maxlength="60"
                       placeholder="Ej: Repuesto, Lubricante, Consumible" value="${form.categoria}">
                <span class="form-error"><c:out value="${errores.categoria}"/></span>
            </div>

            <div class="form-field form-field--full ${not empty errores.descripcion ? 'has-error' : ''}">
                <label class="form-label" for="descripcion">Descripcion</label>
                <input type="text" class="form-control" id="descripcion" name="descripcion" maxlength="500"
                       placeholder="opcional" value="${form.descripcion}">
                <span class="form-error"><c:out value="${errores.descripcion}"/></span>
            </div>

            <div class="form-field ${not empty errores.precioVenta ? 'has-error' : ''}">
                <label class="form-label" for="precioVenta">Precio de venta <span class="form-label__required">*</span></label>
                <input type="number" class="form-control" id="precioVenta" name="precioVenta" step="0.01" min="0.01"
                       value="${form.precioVenta}" required>
                <span class="form-error"><c:out value="${errores.precioVenta}"/></span>
            </div>

            <div class="form-field ${not empty errores.precioCosto ? 'has-error' : ''}">
                <label class="form-label" for="precioCosto">Precio de costo</label>
                <input type="number" class="form-control" id="precioCosto" name="precioCosto" step="0.01" min="0"
                       placeholder="opcional" value="${form.precioCosto}">
                <span class="form-error"><c:out value="${errores.precioCosto}"/></span>
            </div>

            <c:choose>
                <c:when test="${esAlta}">
                    <div class="form-field ${not empty errores.stockActual ? 'has-error' : ''}">
                        <label class="form-label" for="stockActual">Stock inicial <span class="form-label__required">*</span></label>
                        <input type="number" class="form-control" id="stockActual" name="stockActual" step="1" min="0"
                               value="${form.stockActual}" required>
                        <span class="form-error"><c:out value="${errores.stockActual}"/></span>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="form-field">
                        <span class="form-label">Stock actual</span>
                        <div>${productoActual.stockActual} unidades</div>
                        <span class="form-hint">El stock actual se ajusta automaticamente con ventas y compras, no desde este formulario.</span>
                    </div>
                </c:otherwise>
            </c:choose>

            <div class="form-field ${not empty errores.stockMinimo ? 'has-error' : ''}">
                <label class="form-label" for="stockMinimo">Stock minimo <span class="form-label__required">*</span></label>
                <input type="number" class="form-control" id="stockMinimo" name="stockMinimo" step="1" min="0"
                       value="${form.stockMinimo}" required>
                <span class="form-hint">Debajo de este valor el sistema marca el producto con alerta de reposicion.</span>
                <span class="form-error"><c:out value="${errores.stockMinimo}"/></span>
            </div>
        </div>

        <div class="form-actions">
            <a class="btn btn--secondary" href="${pageContext.request.contextPath}/productos">Cancelar</a>
            <button type="submit" class="btn btn--primary">
                ${esAlta ? 'Crear producto' : 'Guardar cambios'}
            </button>
        </div>
    </form>
</div>

<%@ include file="/WEB-INF/jsp/layout/footer.jsp" %>
