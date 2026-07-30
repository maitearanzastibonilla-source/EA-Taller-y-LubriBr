package com.eataller.validator;

import com.eataller.dto.ItemTrabajoFormDTO;
import com.eataller.exception.ValidacionException;
import com.eataller.utils.ValidationUtils;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ItemTrabajoValidator {

    private ItemTrabajoValidator() {
    }

    public static void validar(ItemTrabajoFormDTO form) throws ValidacionException {
        Map<String, String> errores = new LinkedHashMap<>();

        if (ValidationUtils.esVacio(form.getDescripcionLibre())) {
            errores.put("descripcionLibre", "La descripcion del item es obligatoria.");
        } else if (form.getDescripcionLibre().trim().length() > 300) {
            errores.put("descripcionLibre", "La descripcion no puede superar los 300 caracteres.");
        }

        if (ValidationUtils.esVacio(form.getCantidad())) {
            errores.put("cantidad", "La cantidad es obligatoria.");
        } else {
            try {
                BigDecimal cantidad = new BigDecimal(form.getCantidad().trim());
                if (cantidad.compareTo(BigDecimal.ZERO) <= 0) {
                    errores.put("cantidad", "La cantidad debe ser mayor a cero.");
                }
            } catch (NumberFormatException e) {
                errores.put("cantidad", "La cantidad debe ser un numero valido.");
            }
        }

        if (ValidationUtils.esVacio(form.getPrecioUnitario())) {
            errores.put("precioUnitario", "El precio unitario es obligatorio.");
        } else {
            try {
                BigDecimal precio = new BigDecimal(form.getPrecioUnitario().trim());
                if (precio.compareTo(BigDecimal.ZERO) < 0) {
                    errores.put("precioUnitario", "El precio unitario no puede ser negativo.");
                }
            } catch (NumberFormatException e) {
                errores.put("precioUnitario", "El precio unitario debe ser un numero valido.");
            }
        }

        if (!errores.isEmpty()) {
            throw new ValidacionException(errores);
        }
    }
}
