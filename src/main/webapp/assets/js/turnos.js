/**
 * turnos.js
 * Al elegir un cliente en el alta de turno, recargamos la pagina con
 * clienteId en la URL para traer sus vehiculos (no hay endpoint AJAX
 * todavia, asi que el recambio del select se resuelve del lado servidor).
 */
(function () {
    "use strict";

    function inicializarSelectorCliente() {
        var select = document.querySelector("[data-recargar-vehiculos]");
        if (!select) {
            return;
        }
        select.addEventListener("change", function () {
            var base = select.getAttribute("data-recargar-vehiculos");
            if (!select.value) {
                window.location.href = base;
                return;
            }
            window.location.href = base + (base.indexOf("?") === -1 ? "?" : "&") + "clienteId=" + select.value;
        });
    }

    document.addEventListener("DOMContentLoaded", inicializarSelectorCliente);
})();
