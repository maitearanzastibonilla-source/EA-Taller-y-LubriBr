/**
 * main.js
 * Responsabilidad: comportamiento transversal de la interfaz (colapso de
 * sidebar, apertura en mobile, dropdowns y sistema de toasts). Utilizado en
 * todas las paginas del sistema a traves de layout/footer.jsp.
 */
(function () {
    "use strict";

    function inicializarSidebar() {
        var appShell = document.querySelector(".app-shell");
        var sidebar = document.querySelector(".sidebar");
        var toggleDesktop = document.querySelector("[data-sidebar-toggle]");
        var toggleMobile = document.querySelector("[data-sidebar-toggle-mobile]");

        if (toggleDesktop && appShell) {
            toggleDesktop.addEventListener("click", function () {
                appShell.classList.toggle("sidebar-collapsed");
            });
        }

        if (toggleMobile && sidebar) {
            toggleMobile.addEventListener("click", function () {
                sidebar.classList.toggle("is-open");
            });
            document.addEventListener("click", function (event) {
                var esDentroSidebar = sidebar.contains(event.target);
                var esBotonToggle = toggleMobile.contains(event.target);
                if (!esDentroSidebar && !esBotonToggle) {
                    sidebar.classList.remove("is-open");
                }
            });
        }
    }

    function inicializarDropdowns() {
        document.querySelectorAll("[data-dropdown-toggle]").forEach(function (boton) {
            boton.addEventListener("click", function (event) {
                event.stopPropagation();
                var dropdown = boton.closest(".dropdown");
                var menu = dropdown ? dropdown.querySelector(".dropdown__menu") : null;
                if (!menu) {
                    return;
                }
                document.querySelectorAll(".dropdown__menu.is-open").forEach(function (otro) {
                    if (otro !== menu) {
                        otro.classList.remove("is-open");
                    }
                });
                menu.classList.toggle("is-open");
            });
        });

        document.addEventListener("click", function () {
            document.querySelectorAll(".dropdown__menu.is-open").forEach(function (menu) {
                menu.classList.remove("is-open");
            });
        });
    }

    function crearToast(mensaje, tipo) {
        var contenedor = document.querySelector(".toast-container");
        if (!contenedor) {
            contenedor = document.createElement("div");
            contenedor.className = "toast-container";
            document.body.appendChild(contenedor);
        }

        var toast = document.createElement("div");
        toast.className = "toast toast--" + tipo;
        toast.textContent = mensaje;
        contenedor.appendChild(toast);

        window.setTimeout(function () {
            toast.classList.add("is-leaving");
            window.setTimeout(function () {
                toast.remove();
            }, 220);
        }, 4200);
    }

    function inicializarToastsDesdeAtributos() {
        var mensajeExito = document.body.getAttribute("data-flash-success");
        var mensajeError = document.body.getAttribute("data-flash-error");
        if (mensajeExito) {
            crearToast(mensajeExito, "success");
        }
        if (mensajeError) {
            crearToast(mensajeError, "error");
        }
    }

    function inicializarConfirmaciones() {
        document.querySelectorAll("[data-confirm]").forEach(function (elemento) {
            elemento.addEventListener("submit", function (event) {
                var mensaje = elemento.getAttribute("data-confirm");
                if (!window.confirm(mensaje)) {
                    event.preventDefault();
                }
            });
        });
    }

    document.addEventListener("DOMContentLoaded", function () {
        inicializarSidebar();
        inicializarDropdowns();
        inicializarToastsDesdeAtributos();
        inicializarConfirmaciones();
    });

    window.EATaller = window.EATaller || {};
    window.EATaller.crearToast = crearToast;
})();
