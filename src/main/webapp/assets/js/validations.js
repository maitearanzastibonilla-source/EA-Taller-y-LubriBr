/**
 * validations.js
 * Responsabilidad: validaciones rapidas de Frontend (UX). Nunca reemplazan
 * las validaciones de Backend; su unico objetivo es dar feedback inmediato
 * al operador antes de enviar el formulario.
 */
(function () {
    "use strict";

    var PATRON_EMAIL = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

    function marcarError(campo, mensaje) {
        var grupo = campo.closest(".form-field");
        if (!grupo) {
            return;
        }
        grupo.classList.add("has-error");
        var error = grupo.querySelector(".form-error");
        if (error) {
            error.textContent = mensaje;
        }
    }

    function limpiarError(campo) {
        var grupo = campo.closest(".form-field");
        if (!grupo) {
            return;
        }
        grupo.classList.remove("has-error");
        var error = grupo.querySelector(".form-error");
        if (error) {
            error.textContent = "";
        }
    }

    function validarCampoRequerido(campo, mensaje) {
        if (!campo.value || !campo.value.trim()) {
            marcarError(campo, mensaje || "Este campo es obligatorio.");
            return false;
        }
        limpiarError(campo);
        return true;
    }

    function validarEmail(campo) {
        if (!validarCampoRequerido(campo, "El correo electronico es obligatorio.")) {
            return false;
        }
        if (!PATRON_EMAIL.test(campo.value.trim())) {
            marcarError(campo, "El correo electronico no tiene un formato valido.");
            return false;
        }
        limpiarError(campo);
        return true;
    }

    function validarPassword(campo, obligatorio) {
        var valor = campo.value || "";
        if (!obligatorio && valor.length === 0) {
            limpiarError(campo);
            return true;
        }
        if (valor.length < 8 || !/[A-Za-z]/.test(valor) || !/[0-9]/.test(valor)) {
            marcarError(campo, "Debe tener al menos 8 caracteres, con letras y numeros.");
            return false;
        }
        limpiarError(campo);
        return true;
    }

    function validarConfirmacionPassword(campoPassword, campoConfirmacion, obligatorio) {
        if (!obligatorio && !campoPassword.value) {
            limpiarError(campoConfirmacion);
            return true;
        }
        if (campoPassword.value !== campoConfirmacion.value) {
            marcarError(campoConfirmacion, "Las contrasenias no coinciden.");
            return false;
        }
        limpiarError(campoConfirmacion);
        return true;
    }

    function inicializarFormularioLogin() {
        var form = document.querySelector("[data-form='login']");
        if (!form) {
            return;
        }
        var email = form.querySelector("[name='email']");
        var password = form.querySelector("[name='password']");

        form.addEventListener("submit", function (event) {
            var valido = true;
            valido = validarEmail(email) && valido;
            valido = validarCampoRequerido(password, "Debe ingresar su contrasenia.") && valido;
            if (!valido) {
                event.preventDefault();
            }
        });
    }

    function inicializarFormularioUsuario() {
        var form = document.querySelector("[data-form='usuario']");
        if (!form) {
            return;
        }
        var esAlta = form.getAttribute("data-es-alta") === "true";
        var nombre = form.querySelector("[name='nombre']");
        var email = form.querySelector("[name='email']");
        var password = form.querySelector("[name='password']");
        var confirmarPassword = form.querySelector("[name='confirmarPassword']");
        var rol = form.querySelector("[name='rol']");

        form.addEventListener("submit", function (event) {
            var valido = true;
            valido = validarCampoRequerido(nombre, "El nombre completo es obligatorio.") && valido;
            valido = validarEmail(email) && valido;
            valido = validarPassword(password, esAlta) && valido;
            valido = validarConfirmacionPassword(password, confirmarPassword, esAlta) && valido;
            valido = validarCampoRequerido(rol, "Debe seleccionar un rol.") && valido;

            if (!valido) {
                event.preventDefault();
            }
        });
    }

    document.addEventListener("DOMContentLoaded", function () {
        inicializarFormularioLogin();
        inicializarFormularioUsuario();
    });
})();
