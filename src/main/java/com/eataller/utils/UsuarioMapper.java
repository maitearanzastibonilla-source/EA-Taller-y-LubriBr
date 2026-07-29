package com.eataller.utils;

import com.eataller.dto.UsuarioDTO;
import com.eataller.entity.Usuario;

/**
 * Responsabilidad: convertir la Entity Usuario en su DTO de exposicion,
 * evitando que el hash de la contrasenia salga de la capa de Service.
 * Reutilizado por UsuarioService y AuthService para no duplicar el mapeo.
 * Autor: EA Taller y LubriBr - Equipo de Desarrollo
 */
public final class UsuarioMapper {

    private UsuarioMapper() {
    }

    public static UsuarioDTO aDTO(Usuario usuario) {
        UsuarioDTO dto = new UsuarioDTO();
        dto.setIdUsuario(usuario.getIdUsuario());
        dto.setNombre(usuario.getNombre());
        dto.setEmail(usuario.getEmail());
        dto.setRol(usuario.getRol());
        dto.setActivo(usuario.isActivo());
        dto.setBloqueado(usuario.estaBloqueado());
        dto.setCreatedAt(usuario.getCreatedAt());
        return dto;
    }
}
