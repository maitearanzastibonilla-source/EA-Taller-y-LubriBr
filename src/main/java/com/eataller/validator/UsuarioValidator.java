package com.eataller.validator;

import com.eataller.dto.UsuarioFormDTO;
import com.eataller.entity.RolUsuario;
import com.eataller.exception.ValidacionException;
import com.eataller.utils.ValidationUtils;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Responsabilidad: aplicar las reglas de validacion de negocio del
 * formulario de alta/modificacion de usuarios (Modulo de Usuarios,
 * Propuesta Tecnica), independientemente de las validaciones de formato
 * que ya realiza el navegador en el Frontend.
 * Autor: EA Taller y LubriBr - Equipo de Desarrollo
 */
public final class UsuarioValidator {

    private UsuarioValidator() {
    }

    /**
     * Valida los datos de un formulario de usuario.
     *
     * @param form            datos ingresados
     * @param passwordObligatoria true en el alta (la contrasenia es obligatoria);
     *                            false en la modificacion (solo se valida si se
     *                            ingreso una nueva)
     * @throws ValidacionException si existe al menos un campo invalido
     */
    public static void validar(UsuarioFormDTO form, boolean passwordObligatoria) throws ValidacionException {
        Map<String, String> errores = new LinkedHashMap<>();

        if (ValidationUtils.esVacio(form.getNombre())) {
            errores.put("nombre", "El nombre completo es obligatorio.");
        } else if (form.getNombre().trim().length() < 3 || form.getNombre().trim().length() > 100) {
            errores.put("nombre", "El nombre debe tener entre 3 y 100 caracteres.");
        }

        if (ValidationUtils.esVacio(form.getEmail())) {
            errores.put("email", "El correo electronico es obligatorio.");
        } else if (!ValidationUtils.esEmailValido(form.getEmail())) {
            errores.put("email", "El correo electronico no tiene un formato valido.");
        }

        boolean seIngresoPassword = !ValidationUtils.esVacio(form.getPassword());
        if (passwordObligatoria || seIngresoPassword) {
            if (ValidationUtils.esVacio(form.getPassword())) {
                errores.put("password", "La contrasenia es obligatoria.");
            } else if (!ValidationUtils.cumplePoliticaPassword(form.getPassword())) {
                errores.put("password", "La contrasenia debe tener al menos 8 caracteres, con letras y numeros.");
            } else if (!form.getPassword().equals(form.getConfirmarPassword())) {
                errores.put("confirmarPassword", "Las contrasenias ingresadas no coinciden.");
            }
        }

        if (ValidationUtils.esVacio(form.getRol())) {
            errores.put("rol", "Debe seleccionar un rol.");
        } else {
            try {
                RolUsuario.desdeValorBD(form.getRol());
            } catch (IllegalArgumentException e) {
                errores.put("rol", "El rol seleccionado no es valido.");
            }
        }

        if (!errores.isEmpty()) {
            throw new ValidacionException(errores);
        }
    }
}
