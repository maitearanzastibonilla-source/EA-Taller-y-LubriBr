package com.eataller.validator;

import com.eataller.dto.VentaFormDTO;
import com.eataller.entity.MetodoPago;
import com.eataller.exception.ValidacionException;
import com.eataller.utils.ValidationUtils;

import java.util.LinkedHashMap;
import java.util.Map;

public final class VentaValidator {

    private VentaValidator() {
    }

    public static void validar(VentaFormDTO form) throws ValidacionException {
        Map<String, String> errores = new LinkedHashMap<>();

        if (ValidationUtils.esVacio(form.getMetodoPago())) {
            errores.put("metodoPago", "Debe seleccionar un metodo de pago.");
        } else {
            try {
                MetodoPago.valueOf(form.getMetodoPago().trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                errores.put("metodoPago", "El metodo de pago seleccionado no es valido.");
            }
        }

        if (!errores.isEmpty()) {
            throw new ValidacionException(errores);
        }
    }
}
