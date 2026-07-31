package com.eataller.validator;

import com.eataller.dto.ReporteFormDTO;
import com.eataller.entity.TipoReporte;
import com.eataller.exception.ValidacionException;
import com.eataller.utils.ValidationUtils;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ReporteValidator {

    private ReporteValidator() {
    }

    public static void validar(ReporteFormDTO form) throws ValidacionException {
        Map<String, String> errores = new LinkedHashMap<>();

        if (ValidationUtils.esVacio(form.getTipoReporte())) {
            errores.put("tipoReporte", "Debe seleccionar un tipo de reporte.");
        } else {
            try {
                TipoReporte.valueOf(form.getTipoReporte().trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                errores.put("tipoReporte", "El tipo de reporte seleccionado no es valido.");
            }
        }

        LocalDate fechaDesde = null;
        LocalDate fechaHasta = null;

        if (ValidationUtils.esVacio(form.getFechaDesde())) {
            errores.put("fechaDesde", "La fecha desde es obligatoria.");
        } else {
            try {
                fechaDesde = LocalDate.parse(form.getFechaDesde().trim());
            } catch (DateTimeParseException e) {
                errores.put("fechaDesde", "La fecha desde no es valida.");
            }
        }

        if (ValidationUtils.esVacio(form.getFechaHasta())) {
            errores.put("fechaHasta", "La fecha hasta es obligatoria.");
        } else {
            try {
                fechaHasta = LocalDate.parse(form.getFechaHasta().trim());
            } catch (DateTimeParseException e) {
                errores.put("fechaHasta", "La fecha hasta no es valida.");
            }
        }

        if (fechaDesde != null && fechaHasta != null && fechaDesde.isAfter(fechaHasta)) {
            errores.put("fechaHasta", "La fecha hasta debe ser posterior o igual a la fecha desde.");
        }

        if (!errores.isEmpty()) {
            throw new ValidacionException(errores);
        }
    }
}
