<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%-- shell comun (sidebar + topbar) para cualquier pantalla logueada.
     espera tituloPagina, subtituloPagina (opcional) y activeMenu en el request --%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><c:out value="${tituloPagina}" default="EA Taller y LubriBr"/> · SGI</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/variables.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/layout.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/buttons.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/forms.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/tables.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/components.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/dashboard.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/animations.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/responsive.css">
</head>
<body data-flash-success="${flashExito}" data-flash-error="${flashError}">
<div class="app-shell">
    <aside class="sidebar">
        <div class="sidebar__brand">
            <div class="sidebar__brand-mark">EA</div>
            <div class="sidebar__brand-text">
                <strong>EA Taller</strong>
                <span>&amp; LubriBr SGI</span>
            </div>
        </div>
        <nav class="sidebar__nav">
            <span class="sidebar__section-label">Operacion</span>
            <a class="sidebar__link ${activeMenu == 'dashboard' ? 'is-active' : ''}"
               href="${pageContext.request.contextPath}/dashboard">
                <span class="sidebar__link-icon">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="7" height="9" rx="1.5"/><rect x="14" y="3" width="7" height="5" rx="1.5"/><rect x="14" y="12" width="7" height="9" rx="1.5"/><rect x="3" y="16" width="7" height="5" rx="1.5"/></svg>
                </span>
                <span class="sidebar__link-text">Panel principal</span>
            </a>
            <a class="sidebar__link ${activeMenu == 'clientes' ? 'is-active' : ''}"
               href="${pageContext.request.contextPath}/clientes">
                <span class="sidebar__link-icon">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="7" width="18" height="13" rx="2"/><path d="M8 7V5a2 2 0 012-2h4a2 2 0 012 2v2"/></svg>
                </span>
                <span class="sidebar__link-text">Clientes</span>
            </a>
            <a class="sidebar__link ${activeMenu == 'vehiculos' ? 'is-active' : ''}"
               href="${pageContext.request.contextPath}/vehiculos">
                <span class="sidebar__link-icon">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M5 17h14M6 17V9l2-4h8l2 4v8M9 17v-4h6v4"/><circle cx="7.5" cy="17.5" r="1.5"/><circle cx="16.5" cy="17.5" r="1.5"/></svg>
                </span>
                <span class="sidebar__link-text">Vehiculos</span>
            </a>
            <a class="sidebar__link ${activeMenu == 'turnos' ? 'is-active' : ''}"
               href="${pageContext.request.contextPath}/turnos">
                <span class="sidebar__link-icon">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2"/><path d="M3 10h18M8 2v4M16 2v4"/></svg>
                </span>
                <span class="sidebar__link-text">Turnos</span>
            </a>

            <c:if test="${sessionScope.usuarioLogueado.rol.name() == 'ADMINISTRADOR'}">
                <span class="sidebar__section-label">Administracion</span>
                <a class="sidebar__link ${activeMenu == 'usuarios' ? 'is-active' : ''}"
                   href="${pageContext.request.contextPath}/usuarios">
                    <span class="sidebar__link-icon">
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><circle cx="9" cy="8" r="3.2"/><path d="M3.5 19c0-3.3 2.5-5.5 5.5-5.5s5.5 2.2 5.5 5.5"/><circle cx="17.5" cy="8.5" r="2.4"/><path d="M15 13.6c2.4.3 4.5 2.2 4.5 5.4"/></svg>
                    </span>
                    <span class="sidebar__link-text">Usuarios</span>
                </a>
            </c:if>
        </nav>
        <button type="button" class="sidebar__toggle" data-sidebar-toggle aria-label="Colapsar menu">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M9 6l-6 6 6 6M4 12h16"/></svg>
        </button>
    </aside>

    <header class="topbar">
        <div class="topbar__breadcrumb">
            <button type="button" class="btn btn--icon btn--ghost show-mobile" data-sidebar-toggle-mobile aria-label="Abrir menu">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"><path d="M4 6h16M4 12h16M4 18h16"/></svg>
            </button>
            <div>
                <strong><c:out value="${tituloPagina}"/></strong>
                <c:if test="${not empty subtituloPagina}">
                    <div><c:out value="${subtituloPagina}"/></div>
                </c:if>
            </div>
        </div>
        <div class="topbar__actions">
            <div class="dropdown">
                <div class="topbar__user" data-dropdown-toggle style="cursor:pointer;">
                    <div class="topbar__user-avatar">
                        <c:out value="${fn:substring(sessionScope.usuarioLogueado.nombre, 0, 1)}"/>
                    </div>
                    <div class="topbar__user-info">
                        <span class="topbar__user-name"><c:out value="${sessionScope.usuarioLogueado.nombre}"/></span>
                        <span class="topbar__user-role"><c:out value="${sessionScope.usuarioLogueado.rol.etiqueta}"/></span>
                    </div>
                </div>
                <div class="dropdown__menu">
                    <a class="dropdown__item" href="${pageContext.request.contextPath}/logout">
                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M9 6V4.5A1.5 1.5 0 0110.5 3h6A1.5 1.5 0 0118 4.5v15a1.5 1.5 0 01-1.5 1.5h-6A1.5 1.5 0 019 19.5V18M3 12h11m0 0l-3.5-3.5M14 12l-3.5 3.5"/></svg>
                        Cerrar sesion
                    </a>
                </div>
            </div>
        </div>
    </header>

    <main class="main-content fade-in">
