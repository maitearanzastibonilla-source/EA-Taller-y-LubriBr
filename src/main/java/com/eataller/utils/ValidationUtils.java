package com.eataller.utils;

import java.util.regex.Pattern;

/**
 * Responsabilidad: centralizar expresiones regulares y reglas de validacion
 * de formato reutilizables por los distintos validadores del sistema
 * (evita duplicar patrones de email, longitud minima de contrasenia, etc.).
 * Autor: EA Taller y LubriBr - Equipo de Desarrollo
 */
public final class ValidationUtils {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    public static final int PASSWORD_LONGITUD_MINIMA = 8;

    private ValidationUtils() {
    }

    public static boolean esEmailValido(String email) {
        return email != null && !email.isBlank() && EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public static boolean esVacio(String valor) {
        return valor == null || valor.isBlank();
    }

    /**
     * Verifica la politica minima de contrasenias: al menos 8 caracteres,
     * una letra y un numero.
     */
    public static boolean cumplePoliticaPassword(String password) {
        if (password == null || password.length() < PASSWORD_LONGITUD_MINIMA) {
            return false;
        }
        boolean tieneLetra = password.chars().anyMatch(Character::isLetter);
        boolean tieneNumero = password.chars().anyMatch(Character::isDigit);
        return tieneLetra && tieneNumero;
    }
}
