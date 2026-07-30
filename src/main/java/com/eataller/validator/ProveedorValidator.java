package com.eataller.validator;

import com.eataller.dto.ProveedorFormDTO;
import com.eataller.exception.ValidacionException;
import com.eataller.utils.ValidationUtils;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ProveedorValidator {

    private ProveedorValidator() {
    }

    public static void validar(ProveedorFormDTO form) throws ValidacionException {
        Map<String, String> errores = new LinkedHashMap<>();

        if (ValidationUtils.esVacio(form.getNombre())) {
            errores.put("nombre", "El nombre es obligatorio.");
        } else if (form.getNombre().trim().length() < 2 || form.getNombre().trim().length() > 100) {
            errores.put("nombre", "El nombre debe tener entre 2 y 100 caracteres.");
        }

        if (ValidationUtils.esVacio(form.getTelefono())) {
            errores.put("telefono", "El telefono es obligatorio.");
        } else if (!ValidationUtils.esTelefonoValido(form.getTelefono())) {
            errores.put("telefono", "El telefono ingresado no es valido.");
        }

        if (!ValidationUtils.esVacio(form.getEmail()) && !ValidationUtils.esEmailValido(form.getEmail())) {
            errores.put("email", "El correo electronico no tiene un formato valido.");
        }

        if (form.getContacto() != null && form.getContacto().length() > 100) {
            errores.put("contacto", "El nombre de contacto no puede superar los 100 caracteres.");
        }

        if (!errores.isEmpty()) {
            throw new ValidacionException(errores);
        }
    }
}
