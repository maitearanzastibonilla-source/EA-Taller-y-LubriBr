<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="tituloPagina" value="Panel principal" scope="request"/>
<c:set var="subtituloPagina" value="Bienvenido de nuevo" scope="request"/>
<c:set var="activeMenu" value="dashboard" scope="request"/>
<%@ include file="/WEB-INF/jsp/layout/header.jsp" %>

<div class="dashboard-welcome slide-in-up">
    <h1>Hola, <c:out value="${usuario.nombre}"/></h1>
    <p>Este es el panel principal del Sistema de Gestion Integral de EA Taller y LubriBr.
        A medida que se habiliten nuevos modulos (Clientes, Vehiculos, Turnos, Trabajos,
        Stock, Comprobantes), sus indicadores apareceran aqui.</p>
</div>

<h2 class="dashboard-section-title">Modulos disponibles</h2>
<div class="dashboard-module-grid">
    <c:if test="${sessionScope.usuarioLogueado.rol.name() == 'ADMINISTRADOR'}">
        <a class="dashboard-module-card" href="${pageContext.request.contextPath}/usuarios" style="text-decoration:none; color:inherit;">
            <div class="dashboard-module-card__icon">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><circle cx="9" cy="8" r="3.2"/><path d="M3.5 19c0-3.3 2.5-5.5 5.5-5.5s5.5 2.2 5.5 5.5"/><circle cx="17.5" cy="8.5" r="2.4"/><path d="M15 13.6c2.4.3 4.5 2.2 4.5 5.4"/></svg>
            </div>
            <h3>Usuarios</h3>
            <p>Administracion de accesos, roles y permisos del sistema.</p>
        </a>
        <a class="dashboard-module-card" href="${pageContext.request.contextPath}/reportes" style="text-decoration:none; color:inherit;">
            <div class="dashboard-module-card__icon">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M3 3v18h18"/><path d="M7 14l4-4 3 3 5-6"/></svg>
            </div>
            <h3>Reportes</h3>
            <p>Informes consolidados del negocio, exportables a PDF.</p>
        </a>
    </c:if>

    <a class="dashboard-module-card" href="${pageContext.request.contextPath}/clientes" style="text-decoration:none; color:inherit;">
        <div class="dashboard-module-card__icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="7" width="18" height="13" rx="2"/><path d="M8 7V5a2 2 0 012-2h4a2 2 0 012 2v2"/></svg>
        </div>
        <h3>Clientes</h3>
        <p>Alta, busqueda y ficha de clientes del taller.</p>
    </a>

    <a class="dashboard-module-card" href="${pageContext.request.contextPath}/vehiculos" style="text-decoration:none; color:inherit;">
        <div class="dashboard-module-card__icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M5 17h14M6 17V9l2-4h8l2 4v8M9 17v-4h6v4"/><circle cx="7.5" cy="17.5" r="1.5"/><circle cx="16.5" cy="17.5" r="1.5"/></svg>
        </div>
        <h3>Vehiculos</h3>
        <p>Patente, marca, modelo y propietario.</p>
    </a>

    <a class="dashboard-module-card" href="${pageContext.request.contextPath}/turnos" style="text-decoration:none; color:inherit;">
        <div class="dashboard-module-card__icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2"/><path d="M3 10h18M8 2v4M16 2v4"/></svg>
        </div>
        <h3>Turnos</h3>
        <p>Agenda de servicios del taller.</p>
    </a>

    <a class="dashboard-module-card" href="${pageContext.request.contextPath}/trabajos" style="text-decoration:none; color:inherit;">
        <div class="dashboard-module-card__icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M14.7 6.3a1 1 0 000 1.4l1.6 1.6a1 1 0 001.4 0l3.1-3.1a4 4 0 01-5.3 5.3L6.4 20.6a2 2 0 01-2.8-2.8L12.7 8.7a4 4 0 015.3-5.3l-3.1 3.1z"/></svg>
        </div>
        <h3>Trabajos</h3>
        <p>Seguimiento de trabajos realizados por vehiculo.</p>
    </a>

    <a class="dashboard-module-card" href="${pageContext.request.contextPath}/comprobantes" style="text-decoration:none; color:inherit;">
        <div class="dashboard-module-card__icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M7 2h10a1 1 0 011 1v18l-2.5-1.5L13 21l-2.5-1.5L8 21l-2.5-1.5L3 21V6a4 4 0 014-4z"/><path d="M8 8h8M8 12h8M8 16h5"/></svg>
        </div>
        <h3>Comprobantes</h3>
        <p>Comprobantes de cobro emitidos por trabajos finalizados.</p>
    </a>

    <a class="dashboard-module-card" href="${pageContext.request.contextPath}/ventas" style="text-decoration:none; color:inherit;">
        <div class="dashboard-module-card__icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><circle cx="9" cy="21" r="1"/><circle cx="20" cy="21" r="1"/><path d="M1 1h4l2.68 13.39a2 2 0 002 1.61h9.72a2 2 0 002-1.61L23 6H6"/></svg>
        </div>
        <h3>Ventas Directas</h3>
        <p>Ventas de productos en mostrador, sin trabajo asociado.</p>
    </a>

    <a class="dashboard-module-card" href="${pageContext.request.contextPath}/proveedores" style="text-decoration:none; color:inherit;">
        <div class="dashboard-module-card__icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><rect x="1" y="3" width="15" height="13" rx="1"/><path d="M16 8h4l3 3v5h-7V8z"/><circle cx="5.5" cy="18.5" r="2.5"/><circle cx="18.5" cy="18.5" r="2.5"/></svg>
        </div>
        <h3>Proveedores</h3>
        <p>Registro de proveedores de repuestos e insumos.</p>
    </a>

    <a class="dashboard-module-card" href="${pageContext.request.contextPath}/productos" style="text-decoration:none; color:inherit;">
        <div class="dashboard-module-card__icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M21 8l-9-5-9 5v8l9 5 9-5V8z"/><path d="M3 8l9 5 9-5M12 13v8"/></svg>
        </div>
        <h3>Productos</h3>
        <p>Catalogo de repuestos e insumos, precios y stock.</p>
    </a>

    <a class="dashboard-module-card" href="${pageContext.request.contextPath}/compras" style="text-decoration:none; color:inherit;">
        <div class="dashboard-module-card__icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M20 7h-3V6a4 4 0 00-8 0v1H6a1 1 0 00-1 1v11a2 2 0 002 2h10a2 2 0 002-2V8a1 1 0 00-1-1z"/><path d="M9 7V6a3 3 0 016 0v1"/></svg>
        </div>
        <h3>Compras</h3>
        <p>Ordenes de compra a proveedores para reponer stock.</p>
    </a>
</div>

<%@ include file="/WEB-INF/jsp/layout/footer.jsp" %>
