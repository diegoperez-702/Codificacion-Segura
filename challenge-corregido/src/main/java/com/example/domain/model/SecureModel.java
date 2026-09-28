package com.example.domain.model;

import com.example.infrastructure.validation.ValidInput;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Modelo de entrada seguro.
 *
 * El campo {@code input} se valida en capas:
 * - {@link NotBlank}: no puede ser nulo ni vacio.
 * - {@link Size}: longitud acotada entre 3 y 10.
 * - {@link ValidInput}: whitelist alfanumerica + rechazo de patrones
 *   peligrosos (SQLi, XSS, RCE).
 */
public record SecureModel(
        @NotBlank
        @Size(min = 3, max = 10)
        @ValidInput
        String input) {
}
