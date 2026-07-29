<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.eataller.utils.SessionUtils" %>
<%--
    Responsabilidad: punto de entrada de la aplicacion. Redirige segun el
    estado de la sesion del usuario.
--%>
<%
    if (SessionUtils.haySesionActiva(request)) {
        response.sendRedirect(request.getContextPath() + "/dashboard");
    } else {
        response.sendRedirect(request.getContextPath() + "/login");
    }
%>
