package com.eataller.utils;

import com.eataller.dto.ClienteDTO;
import com.eataller.entity.Cliente;

public final class ClienteMapper {

    private ClienteMapper() {
    }

    public static ClienteDTO aDTO(Cliente cliente) {
        ClienteDTO dto = new ClienteDTO();
        dto.setIdCliente(cliente.getIdCliente());
        dto.setNombre(cliente.getNombre());
        dto.setApellido(cliente.getApellido());
        dto.setDni(cliente.getDni());
        dto.setEmail(cliente.getEmail());
        dto.setTelefono(cliente.getTelefono());
        dto.setDireccion(cliente.getDireccion());
        dto.setEstado(cliente.getEstado());
        dto.setCreatedAt(cliente.getCreatedAt());
        return dto;
    }
}
