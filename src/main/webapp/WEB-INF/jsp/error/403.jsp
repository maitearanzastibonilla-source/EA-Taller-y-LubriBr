<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Acceso denegado · EA Taller y LubriBr</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/variables.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/buttons.css">
</head>
<body style="min-height:100vh; display:flex; align-items:center; justify-content:center; font-family: var(--font-family-base); background: var(--color-bg-app); margin:0;">
<div style="text-align:center; max-width:420px; padding: var(--space-8);">
    <div style="font-size:3rem; font-weight:700; color: var(--color-red-600);">403</div>
    <h1 style="font-size: var(--font-size-h2); margin: var(--space-2) 0;">Acceso denegado</h1>
    <p style="color: var(--color-text-secondary); margin-bottom: var(--space-6);">
        Su rol no tiene permisos para acceder a esta seccion del sistema.
    </p>
    <a class="btn btn--primary" href="${pageContext.request.contextPath}/dashboard">Volver al panel principal</a>
</div>
</body>
</html>
