package com.eataller.validator;

import com.eataller.dto.VehiculoFormDTO;
import com.eataller.exception.ValidacionException;
import com.eataller.utils.ValidationUtils;

import java.time.Year;
import java.util.LinkedHashMap;
import java.util.Map;

public final class VehiculoValidator {

    private static final int ANIO_MINIMO = 1950;

    private VehiculoValidator() {
    }

    public static void validar(VehiculoFormDTO form) throws ValidacionException {
        Map<String, String> errores = new LinkedHashMap<>();

        if (form.getClienteId() == null) {
            errores.put("clienteId", "Debe seleccionar el propietario del vehiculo.");
        }

        if (ValidationUtils.esVacio(form.getPatente())) {
            errores.put("patente", "La patente es obligatoria.");
        } else if (!ValidationUtils.esPatenteValida(form.getPatente())) {
            errores.put("patente", "La patente no tiene un formato valido (Ej: AB123CD o ABC123).");
        }

        if (ValidationUtils.esVacio(form.getMarca())) {
            errores.put("marca", "La marca es obligatoria.");
        }

        if (ValidationUtils.esVacio(form.getModelo())) {
            errores.put("modelo", "El modelo es obligatorio.");
        }

        int anioActual = Year.now().getValue();
        if (ValidationUtils.esVacio(form.getAnio())) {
            errores.put("anio", "El anio es obligatorio.");
        } else {
            try {
                int anio = Integer.parseInt(form.getAnio().trim());
                if (anio < ANIO_MINIMO || anio > anioActual + 1) {
                    errores.put("anio", "El anio debe estar entre " + ANIO_MINIMO + " y " + (anioActual + 1) + ".");
                }
            } catch (NumberFormatException e) {
                errores.put("anio", "El anio debe ser un numero.");
            }
        }

        if (!errores.isEmpty()) {
            throw new ValidacionException(errores);
        }
    }
}
