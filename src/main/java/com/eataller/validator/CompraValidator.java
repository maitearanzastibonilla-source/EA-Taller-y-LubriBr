package com.eataller.validator;

import com.eataller.dto.CompraFormDTO;
import com.eataller.exception.ValidacionException;

import java.util.LinkedHashMap;
import java.util.Map;

public final class CompraValidator {

    private CompraValidator() {
    }

    public static void validar(CompraFormDTO form) throws ValidacionException {
        Map<String, String> errores = new LinkedHashMap<>();

        if (form.getProveedorId() == null) {
            errores.put("proveedorId", "Debe seleccionar un proveedor.");
        }

        if (!errores.isEmpty()) {
            throw new ValidacionException(errores);
        }
    }
}
