package com.eataller.utils;

import com.eataller.constants.AppConstants;
import org.mindrot.jbcrypt.BCrypt;

/**
 * Responsabilidad: encapsular el hashing y la verificacion segura de
 * contrasenias mediante BCrypt (con salt aleatorio embebido por hash),
 * evitando que cualquier otra clase del sistema manipule contrasenias en
 * texto plano mas alla de lo estrictamente necesario.
 * Autor: EA Taller y LubriBr - Equipo de Desarrollo
 */
public final class PasswordHasher {

    private PasswordHasher() {
    }

    /**
     * Genera el hash BCrypt de una contrasenia en texto plano.
     *
     * @param passwordPlano contrasenia ingresada por el usuario
     * @return hash seguro para persistir en la columna password_hash
     */
    public static String hash(String passwordPlano) {
        return BCrypt.hashpw(passwordPlano, BCrypt.gensalt(AppConstants.BCRYPT_ROUNDS));
    }

    /**
     * Verifica que una contrasenia en texto plano corresponda al hash
     * almacenado en base de datos.
     *
     * @param passwordPlano contrasenia ingresada en el login
     * @param passwordHash  hash almacenado en la tabla usuarios
     * @return true si la contrasenia es correcta
     */
    public static boolean verificar(String passwordPlano, String passwordHash) {
        if (passwordPlano == null || passwordHash == null) {
            return false;
        }
        return BCrypt.checkpw(passwordPlano, passwordHash);
    }
}
