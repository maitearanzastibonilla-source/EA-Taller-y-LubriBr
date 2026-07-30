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

    function validarDni(campo) {
        if (!validarCampoRequerido(campo, "El DNI es obligatorio.")) {
            return false;
        }
        if (!/^\d{7,8}$/.test(campo.value.trim())) {
            marcarError(campo, "Debe tener 7 u 8 numeros, sin puntos.");
            return false;
        }
        limpiarError(campo);
        return true;
    }

    function validarEmailOpcional(campo) {
        if (!campo.value.trim()) {
            limpiarError(campo);
            return true;
        }
        if (!PATRON_EMAIL.test(campo.value.trim())) {
            marcarError(campo, "El correo electronico no tiene un formato valido.");
            return false;
        }
        limpiarError(campo);
        return true;
    }

    function validarPatente(campo) {
        if (!validarCampoRequerido(campo, "La patente es obligatoria.")) {
            return false;
        }
        if (!/^[A-Za-z]{2,3}\d{3}[A-Za-z]{0,2}$/.test(campo.value.trim())) {
            marcarError(campo, "Formato invalido (Ej: AB123CD o ABC123).");
            return false;
        }
        limpiarError(campo);
        return true;
    }

    function validarAnio(campo) {
        if (!validarCampoRequerido(campo, "El anio es obligatorio.")) {
            return false;
        }
        var anio = parseInt(campo.value, 10);
        var anioMaximo = new Date().getFullYear() + 1;
        if (isNaN(anio) || anio < 1950 || anio > anioMaximo) {
            marcarError(campo, "Debe estar entre 1950 y " + anioMaximo + ".");
            return false;
        }
        limpiarError(campo);
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

    function inicializarFormularioCliente() {
        var form = document.querySelector("[data-form='cliente']");
        if (!form) {
            return;
        }
        var nombre = form.querySelector("[name='nombre']");
        var apellido = form.querySelector("[name='apellido']");
        var dni = form.querySelector("[name='dni']");
        var telefono = form.querySelector("[name='telefono']");
        var email = form.querySelector("[name='email']");

        form.addEventListener("submit", function (event) {
            var valido = true;
            valido = validarCampoRequerido(nombre, "El nombre es obligatorio.") && valido;
            valido = validarCampoRequerido(apellido, "El apellido es obligatorio.") && valido;
            valido = validarDni(dni) && valido;
            valido = validarCampoRequerido(telefono, "El telefono es obligatorio.") && valido;
            valido = validarEmailOpcional(email) && valido;

            if (!valido) {
                event.preventDefault();
            }
        });
    }

    function inicializarFormularioVehiculo() {
        var form = document.querySelector("[data-form='vehiculo']");
        if (!form) {
            return;
        }
        var clienteId = form.querySelector("[name='clienteId']");
        var patente = form.querySelector("[name='patente']");
        var anio = form.querySelector("[name='anio']");
        var marca = form.querySelector("[name='marca']");
        var modelo = form.querySelector("[name='modelo']");

        form.addEventListener("submit", function (event) {
            var valido = true;
            valido = validarCampoRequerido(clienteId, "Debe seleccionar un propietario.") && valido;
            valido = validarPatente(patente) && valido;
            valido = validarAnio(anio) && valido;
            valido = validarCampoRequerido(marca, "La marca es obligatoria.") && valido;
            valido = validarCampoRequerido(modelo, "El modelo es obligatorio.") && valido;

            if (!valido) {
                event.preventDefault();
            }
        });
    }

    function inicializarFormularioTurno() {
        var form = document.querySelector("[data-form='turno']");
        if (!form) {
            return;
        }
        var clienteId = form.querySelector("[name='clienteId']");
        var vehiculoId = form.querySelector("[name='vehiculoId']");
        var fechaHora = form.querySelector("[name='fechaHora']");
        var tipoServicio = form.querySelector("[name='tipoServicio']");

        form.addEventListener("submit", function (event) {
            var valido = true;
            if (clienteId && clienteId.tagName === "SELECT") {
                valido = validarCampoRequerido(clienteId, "Debe seleccionar un cliente.") && valido;
            }
            if (vehiculoId && vehiculoId.tagName === "SELECT") {
                valido = validarCampoRequerido(vehiculoId, "Debe seleccionar un vehiculo.") && valido;
            }
            valido = validarCampoRequerido(fechaHora, "La fecha y hora son obligatorias.") && valido;
            valido = validarCampoRequerido(tipoServicio, "Debe seleccionar el tipo de servicio.") && valido;

            if (!valido) {
                event.preventDefault();
            }
        });
    }

    function inicializarFormularioTrabajo() {
        var form = document.querySelector("[data-form='trabajo']");
        if (!form) {
            return;
        }
        var vehiculoId = form.querySelector("[name='vehiculoId']");
        var fechaIngreso = form.querySelector("[name='fechaIngreso']");
        var descripcion = form.querySelector("[name='descripcion']");

        form.addEventListener("submit", function (event) {
            var valido = true;
            if (vehiculoId && vehiculoId.tagName === "SELECT") {
                valido = validarCampoRequerido(vehiculoId, "Debe seleccionar un vehiculo.") && valido;
            }
            valido = validarCampoRequerido(fechaIngreso, "La fecha de ingreso es obligatoria.") && valido;
            valido = validarCampoRequerido(descripcion, "La descripcion es obligatoria.") && valido;

            if (!valido) {
                event.preventDefault();
            }
        });
    }

    function inicializarFormularioItemTrabajo() {
        var form = document.querySelector("[data-form='item-trabajo']");
        if (!form) {
            return;
        }
        var descripcion = form.querySelector("[name='descripcionLibre']");
        var cantidad = form.querySelector("[name='cantidad']");
        var precioUnitario = form.querySelector("[name='precioUnitario']");

        form.addEventListener("submit", function (event) {
            var valido = true;
            valido = validarCampoRequerido(descripcion, "La descripcion es obligatoria.") && valido;
            valido = validarCampoRequerido(cantidad, "La cantidad es obligatoria.") && valido;
            if (cantidad.value && parseFloat(cantidad.value) <= 0) {
                marcarError(cantidad, "La cantidad debe ser mayor a cero.");
                valido = false;
            }
            valido = validarCampoRequerido(precioUnitario, "El precio unitario es obligatorio.") && valido;

            if (!valido) {
                event.preventDefault();
            }
        });
    }

    function inicializarFormularioComprobante() {
        var form = document.querySelector("[data-form='comprobante']");
        if (!form) {
            return;
        }
        var metodoPago = form.querySelector("[name='metodoPago']");
        var estado = form.querySelector("[name='estado']");

        form.addEventListener("submit", function (event) {
            var valido = true;
            valido = validarCampoRequerido(metodoPago, "Debe seleccionar un metodo de pago.") && valido;
            valido = validarCampoRequerido(estado, "Debe seleccionar un estado.") && valido;

            if (!valido) {
                event.preventDefault();
            }
        });
    }

    function inicializarFormularioProveedor() {
        var form = document.querySelector("[data-form='proveedor']");
        if (!form) {
            return;
        }
        var nombre = form.querySelector("[name='nombre']");
        var telefono = form.querySelector("[name='telefono']");
        var email = form.querySelector("[name='email']");

        form.addEventListener("submit", function (event) {
            var valido = true;
            valido = validarCampoRequerido(nombre, "El nombre es obligatorio.") && valido;
            valido = validarCampoRequerido(telefono, "El telefono es obligatorio.") && valido;
            if (email.value && !PATRON_EMAIL.test(email.value.trim())) {
                marcarError(email, "El correo electronico no tiene un formato valido.");
                valido = false;
            }

            if (!valido) {
                event.preventDefault();
            }
        });
    }

    function inicializarFormularioProducto() {
        var form = document.querySelector("[data-form='producto']");
        if (!form) {
            return;
        }
        var nombre = form.querySelector("[name='nombre']");
        var proveedorId = form.querySelector("[name='proveedorId']");
        var precioVenta = form.querySelector("[name='precioVenta']");
        var stockMinimo = form.querySelector("[name='stockMinimo']");
        var stockActual = form.querySelector("[name='stockActual']");

        form.addEventListener("submit", function (event) {
            var valido = true;
            valido = validarCampoRequerido(nombre, "El nombre es obligatorio.") && valido;
            valido = validarCampoRequerido(proveedorId, "Debe seleccionar un proveedor.") && valido;
            valido = validarCampoRequerido(precioVenta, "El precio de venta es obligatorio.") && valido;
            if (precioVenta.value && parseFloat(precioVenta.value) <= 0) {
                marcarError(precioVenta, "El precio de venta debe ser mayor a cero.");
                valido = false;
            }
            valido = validarCampoRequerido(stockMinimo, "El stock minimo es obligatorio.") && valido;
            if (stockActual) {
                valido = validarCampoRequerido(stockActual, "El stock inicial es obligatorio.") && valido;
            }

            if (!valido) {
                event.preventDefault();
            }
        });
    }

    document.addEventListener("DOMContentLoaded", function () {
        inicializarFormularioLogin();
        inicializarFormularioUsuario();
        inicializarFormularioCliente();
        inicializarFormularioVehiculo();
        inicializarFormularioTurno();
        inicializarFormularioTrabajo();
        inicializarFormularioItemTrabajo();
        inicializarFormularioComprobante();
        inicializarFormularioProveedor();
        inicializarFormularioProducto();
    });
})();
