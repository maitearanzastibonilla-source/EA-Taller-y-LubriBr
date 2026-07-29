package com.eataller.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.Filter;

import java.io.IOException;

/**
 * Responsabilidad: garantizar codificacion UTF-8 consistente en todos los
 * requests y responses de la aplicacion (evita problemas con acentos y
 * caracteres especiales del espanol en formularios y vistas).
 * Registrado explicitamente en web.xml (antes que AuthenticationFilter) para
 * garantizar un orden de ejecucion deterministico.
 * Autor: EA Taller y LubriBr - Equipo de Desarrollo
 */
public class EncodingFilter implements Filter {

    private static final String ENCODING = "UTF-8";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        request.setCharacterEncoding(ENCODING);
        response.setCharacterEncoding(ENCODING);
        chain.doFilter(request, response);
    }
}
