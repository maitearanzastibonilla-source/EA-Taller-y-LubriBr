/**
 * usuarios.js
 * Responsabilidad: interacciones especificas del Modulo de Usuarios
 * (limpiar filtros del listado y mostrar/ocultar contrasenia en el
 * formulario de alta/modificacion).
 */
(function () {
    "use strict";

    function inicializarLimpiarFiltros() {
        var boton = document.querySelector("[data-limpiar-filtros]");
        if (!boton) {
            return;
        }
        boton.addEventListener("click", function () {
            window.location.href = boton.getAttribute("data-limpiar-filtros");
        });
    }

    function inicializarMostrarPassword() {
        document.querySelectorAll("[data-toggle-password]").forEach(function (boton) {
            boton.addEventListener("click", function () {
                var idCampo = boton.getAttribute("data-toggle-password");
                var campo = document.getElementById(idCampo);
                if (!campo) {
                    return;
                }
                var esOculto = campo.type === "password";
                campo.type = esOculto ? "text" : "password";
                boton.setAttribute("aria-label", esOculto ? "Ocultar contrasenia" : "Mostrar contrasenia");
            });
        });
    }

    document.addEventListener("DOMContentLoaded", function () {
        inicializarLimpiarFiltros();
        inicializarMostrarPassword();
    });
})();
