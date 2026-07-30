package com.eataller.validator;

import com.eataller.dto.ComprobanteFormDTO;
import com.eataller.entity.EstadoComprobante;
import com.eataller.entity.MetodoPago;
import com.eataller.exception.ValidacionException;
import com.eataller.utils.ValidationUtils;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ComprobanteValidator {

    private ComprobanteValidator() {
    }

    public static void validar(ComprobanteFormDTO form) throws ValidacionException {
        Map<String, String> errores = new LinkedHashMap<>();

        if (form.getTrabajoId() == null) {
            errores.put("trabajoId", "Debe indicar el trabajo a facturar.");
        }

        if (ValidationUtils.esVacio(form.getMetodoPago())) {
            errores.put("metodoPago", "Debe seleccionar el metodo de pago.");
        } else {
            try {
                MetodoPago.desdeValorBD(form.getMetodoPago());
            } catch (IllegalArgumentException e) {
                errores.put("metodoPago", "El metodo de pago seleccionado no es valido.");
            }
        }

        if (ValidationUtils.esVacio(form.getEstado())) {
            errores.put("estado", "Debe seleccionar el estado del comprobante.");
        } else {
            try {
                EstadoComprobante estado = EstadoComprobante.desdeValorBD(form.getEstado());
                if (estado == EstadoComprobante.ANULADO) {
                    errores.put("estado", "Un comprobante nuevo no puede iniciar como Anulado.");
                }
            } catch (IllegalArgumentException e) {
                errores.put("estado", "El estado seleccionado no es valido.");
            }
        }

        if (!errores.isEmpty()) {
            throw new ValidacionException(errores);
        }
    }
}
