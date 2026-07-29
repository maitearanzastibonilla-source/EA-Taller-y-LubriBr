package com.eataller.validator;

import com.eataller.dto.ClienteFormDTO;
import com.eataller.exception.ValidacionException;
import com.eataller.utils.ValidationUtils;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ClienteValidator {

    private ClienteValidator() {
    }

    public static void validar(ClienteFormDTO form) throws ValidacionException {
        Map<String, String> errores = new LinkedHashMap<>();

        if (ValidationUtils.esVacio(form.getNombre())) {
            errores.put("nombre", "El nombre es obligatorio.");
        } else if (form.getNombre().trim().length() < 2 || form.getNombre().trim().length() > 60) {
            errores.put("nombre", "El nombre debe tener entre 2 y 60 caracteres.");
        }

        if (ValidationUtils.esVacio(form.getApellido())) {
            errores.put("apellido", "El apellido es obligatorio.");
        } else if (form.getApellido().trim().length() < 2 || form.getApellido().trim().length() > 60) {
            errores.put("apellido", "El apellido debe tener entre 2 y 60 caracteres.");
        }

        if (ValidationUtils.esVacio(form.getDni())) {
            errores.put("dni", "El DNI es obligatorio.");
        } else if (!ValidationUtils.esDniValido(form.getDni())) {
            errores.put("dni", "El DNI debe contener entre 7 y 8 numeros, sin puntos.");
        }

        if (ValidationUtils.esVacio(form.getTelefono())) {
            errores.put("telefono", "El telefono es obligatorio.");
        } else if (!ValidationUtils.esTelefonoValido(form.getTelefono())) {
            errores.put("telefono", "El telefono ingresado no es valido.");
        }

        if (!ValidationUtils.esVacio(form.getEmail()) && !ValidationUtils.esEmailValido(form.getEmail())) {
            errores.put("email", "El correo electronico no tiene un formato valido.");
        }

        if (form.getDireccion() != null && form.getDireccion().length() > 200) {
            errores.put("direccion", "La direccion no puede superar los 200 caracteres.");
        }

        if (!errores.isEmpty()) {
            throw new ValidacionException(errores);
        }
    }
}
