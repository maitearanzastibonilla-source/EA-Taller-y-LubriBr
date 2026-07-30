package com.eataller.validator;

import com.eataller.dto.ItemVentaFormDTO;
import com.eataller.exception.ValidacionException;
import com.eataller.utils.ValidationUtils;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ItemVentaValidator {

    private ItemVentaValidator() {
    }

    public static void validar(ItemVentaFormDTO form) throws ValidacionException {
        Map<String, String> errores = new LinkedHashMap<>();

        if (form.getProductoId() == null) {
            errores.put("productoId", "Debe seleccionar un producto.");
        }

        if (ValidationUtils.esVacio(form.getCantidad())) {
            errores.put("cantidad", "La cantidad es obligatoria.");
        } else {
            try {
                int cantidad = Integer.parseInt(form.getCantidad().trim());
                if (cantidad <= 0) {
                    errores.put("cantidad", "La cantidad debe ser mayor a cero.");
                }
            } catch (NumberFormatException e) {
                errores.put("cantidad", "La cantidad debe ser un numero entero valido.");
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
