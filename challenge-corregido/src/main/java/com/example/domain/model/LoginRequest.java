package com.example.domain.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Credenciales de inicio de sesion. Los campos se validan para evitar
 * entradas vacias o abusivamente largas antes de intentar autenticar.
 */
public record LoginRequest(
        @NotBlank
        @Size(max = 50)
        String username,

        @NotBlank
        @Size(max = 100)
        String password) {
}
