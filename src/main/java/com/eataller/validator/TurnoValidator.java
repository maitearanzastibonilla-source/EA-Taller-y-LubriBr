package com.eataller.validator;

import com.eataller.dto.TurnoFormDTO;
import com.eataller.entity.EstadoTurno;
import com.eataller.entity.TipoServicio;
import com.eataller.exception.ValidacionException;
import com.eataller.utils.ValidationUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.Map;

public final class TurnoValidator {

    private TurnoValidator() {
    }

    public static void validar(TurnoFormDTO form) throws ValidacionException {
        Map<String, String> errores = new LinkedHashMap<>();

        if (form.getClienteId() == null) {
            errores.put("clienteId", "Debe seleccionar un cliente.");
        }

        if (form.getVehiculoId() == null) {
            errores.put("vehiculoId", "Debe seleccionar un vehiculo del cliente.");
        }

        if (ValidationUtils.esVacio(form.getFechaHora())) {
            errores.put("fechaHora", "La fecha y hora del turno son obligatorias.");
        } else {
            try {
                LocalDateTime.parse(form.getFechaHora());
            } catch (DateTimeParseException e) {
                errores.put("fechaHora", "La fecha y hora ingresadas no son validas.");
            }
        }

        if (ValidationUtils.esVacio(form.getTipoServicio())) {
            errores.put("tipoServicio", "Debe seleccionar el tipo de servicio.");
        } else {
            try {
                TipoServicio.desdeValorBD(form.getTipoServicio());
            } catch (IllegalArgumentException e) {
                errores.put("tipoServicio", "El tipo de servicio seleccionado no es valido.");
            }
        }

        if (ValidationUtils.esVacio(form.getEstado())) {
            errores.put("estado", "Debe seleccionar el estado del turno.");
        } else {
            try {
                EstadoTurno.desdeValorBD(form.getEstado());
            } catch (IllegalArgumentException e) {
                errores.put("estado", "El estado seleccionado no es valido.");
            }
        }

        if (form.getNotas() != null && form.getNotas().length() > 500) {
            errores.put("notas", "Las notas no pueden superar los 500 caracteres.");
        }

        if (!errores.isEmpty()) {
            throw new ValidacionException(errores);
        }
    }
}
