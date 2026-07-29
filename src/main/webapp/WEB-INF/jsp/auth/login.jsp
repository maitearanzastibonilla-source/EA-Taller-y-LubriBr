<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Iniciar sesion · EA Taller y LubriBr</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/variables.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/layout.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/buttons.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/forms.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/components.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/animations.css">
</head>
<body>
<div class="auth-shell">
    <section class="auth-shell__brand">
        <div class="auth-shell__brand-mark">EA</div>
        <h1>Sistema de Gestion Integral</h1>
        <p>Turnos, trabajos, stock y comprobantes de EA Taller y LubriBr,
            centralizados en un unico sistema.</p>
    </section>
    <section class="auth-shell__form">
        <div class="auth-card fade-in">
            <h2>Iniciar sesion</h2>
            <p class="auth-card__hint">Ingrese sus credenciales para acceder al sistema.</p>

            <c:if test="${not empty mensajeError}">
                <div class="alert alert--error" role="alert">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><circle cx="12" cy="12" r="9"/><path d="M12 8v5M12 16h.01"/></svg>
                    <span><c:out value="${mensajeError}"/></span>
                </div>
            </c:if>

            <form method="post" action="${pageContext.request.contextPath}/login" data-form="login" novalidate>
                <input type="hidden" name="csrfToken" value="${csrfToken}">

                <div class="form-field" style="margin-bottom: var(--space-4);">
                    <label class="form-label" for="email">Correo electronico <span class="form-label__required">*</span></label>
                    <input type="email" class="form-control" id="email" name="email" placeholder="nombre@eatallerylubribr.com"
                           value="${emailIngresado}" autocomplete="username" required>
                    <span class="form-error"></span>
                </div>

                <div class="form-field" style="margin-bottom: var(--space-6);">
                    <label class="form-label" for="password">Contrasenia <span class="form-label__required">*</span></label>
                    <div style="position:relative;">
                        <input type="password" class="form-control" id="password" name="password"
                               placeholder="Su contrasenia" autocomplete="current-password" required>
                        <button type="button" class="btn btn--icon btn--ghost btn--sm" data-toggle-password="password"
                                style="position:absolute; right:2px; top:2px;" aria-label="Mostrar contrasenia">
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><path d="M1 12s4-7 11-7 11 7 11 7-4 7-11 7-11-7-11-7z"/><circle cx="12" cy="12" r="3"/></svg>
                        </button>
                    </div>
                    <span class="form-error"></span>
                </div>

                <button type="submit" class="btn btn--primary btn--block">Ingresar</button>
            </form>
        </div>
    </section>
</div>
<script src="${pageContext.request.contextPath}/assets/js/main.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/validations.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/usuarios.js"></script>
</body>
</html>
