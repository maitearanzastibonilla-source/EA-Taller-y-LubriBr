package com.eataller.validator;

import com.eataller.dto.ProductoFormDTO;
import com.eataller.exception.ValidacionException;
import com.eataller.utils.ValidationUtils;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ProductoValidator {

    private ProductoValidator() {
    }

    public static void validar(ProductoFormDTO form) throws ValidacionException {
        Map<String, String> errores = new LinkedHashMap<>();

        if (ValidationUtils.esVacio(form.getNombre())) {
            errores.put("nombre", "El nombre es obligatorio.");
        } else if (form.getNombre().trim().length() > 150) {
            errores.put("nombre", "El nombre no puede superar los 150 caracteres.");
        }

        if (form.getProveedorId() == null) {
            errores.put("proveedorId", "Debe seleccionar un proveedor.");
        }

        if (ValidationUtils.esVacio(form.getPrecioVenta())) {
            errores.put("precioVenta", "El precio de venta es obligatorio.");
        } else {
            try {
                BigDecimal precioVenta = new BigDecimal(form.getPrecioVenta().trim());
                if (precioVenta.compareTo(BigDecimal.ZERO) <= 0) {
                    errores.put("precioVenta", "El precio de venta debe ser mayor a cero.");
                }
            } catch (NumberFormatException e) {
                errores.put("precioVenta", "El precio de venta debe ser un numero valido.");
            }
        }

        if (!ValidationUtils.esVacio(form.getPrecioCosto())) {
            try {
                BigDecimal precioCosto = new BigDecimal(form.getPrecioCosto().trim());
                if (precioCosto.compareTo(BigDecimal.ZERO) < 0) {
                    errores.put("precioCosto", "El precio de costo no puede ser negativo.");
                }
            } catch (NumberFormatException e) {
                errores.put("precioCosto", "El precio de costo debe ser un numero valido.");
            }
        }

        if (ValidationUtils.esVacio(form.getStockMinimo())) {
            errores.put("stockMinimo", "El stock minimo es obligatorio.");
        } else {
            try {
                int stockMinimo = Integer.parseInt(form.getStockMinimo().trim());
                if (stockMinimo < 0) {
                    errores.put("stockMinimo", "El stock minimo no puede ser negativo.");
                }
            } catch (NumberFormatException e) {
                errores.put("stockMinimo", "El stock minimo debe ser un numero entero valido.");
            }
        }

        if (form.esAlta()) {
            if (ValidationUtils.esVacio(form.getStockActual())) {
                errores.put("stockActual", "El stock inicial es obligatorio.");
            } else {
                try {
                    int stockActual = Integer.parseInt(form.getStockActual().trim());
                    if (stockActual < 0) {
                        errores.put("stockActual", "El stock inicial no puede ser negativo.");
                    }
                } catch (NumberFormatException e) {
                    errores.put("stockActual", "El stock inicial debe ser un numero entero valido.");
                }
            }
        }

        if (form.getDescripcion() != null && form.getDescripcion().length() > 500) {
            errores.put("descripcion", "La descripcion no puede superar los 500 caracteres.");
        }

        if (form.getCategoria() != null && form.getCategoria().length() > 60) {
            errores.put("categoria", "La categoria no puede superar los 60 caracteres.");
        }

        if (!errores.isEmpty()) {
            throw new ValidacionException(errores);
        }
    }
}
