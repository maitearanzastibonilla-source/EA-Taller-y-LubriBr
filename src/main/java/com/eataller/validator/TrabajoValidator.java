package com.eataller.validator;

import com.eataller.dto.TrabajoFormDTO;
import com.eataller.exception.ValidacionException;
import com.eataller.utils.ValidationUtils;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.Map;

public final class TrabajoValidator {

    private TrabajoValidator() {
    }

    public static void validar(TrabajoFormDTO form) throws ValidacionException {
        Map<String, String> errores = new LinkedHashMap<>();

        if (form.getVehiculoId() == null) {
            errores.put("vehiculoId", "Debe seleccionar el vehiculo ingresado al taller.");
        }

        if (ValidationUtils.esVacio(form.getFechaIngreso())) {
            errores.put("fechaIngreso", "La fecha de ingreso es obligatoria.");
        } else {
            try {
                LocalDate.parse(form.getFechaIngreso());
            } catch (DateTimeParseException e) {
                errores.put("fechaIngreso", "La fecha de ingreso no es valida.");
            }
        }

        if (ValidationUtils.esVacio(form.getDescripcion())) {
            errores.put("descripcion", "La descripcion del trabajo es obligatoria.");
        } else if (form.getDescripcion().trim().length() < 5) {
            errores.put("descripcion", "La descripcion debe tener al menos 5 caracteres.");
        } else if (form.getDescripcion().length() > 2000) {
            errores.put("descripcion", "La descripcion no puede superar los 2000 caracteres.");
        }

        if (!errores.isEmpty()) {
            throw new ValidacionException(errores);
        }
    }
}
