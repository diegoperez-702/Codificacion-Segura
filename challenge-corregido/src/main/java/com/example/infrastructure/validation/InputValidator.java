package com.example.infrastructure.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

/**
 * Validador de entrada basado principalmente en lista blanca (whitelist).
 *
 * Estrategia de defensa en profundidad:
 * 1. Whitelist estricta: solo se permiten caracteres alfanumericos con una
 *    longitud acotada. Esto, por si solo, ya neutraliza la mayoria de los
 *    vectores de SQL Injection, XSS y RCE, porque los metacaracteres que
 *    esos ataques necesitan (comillas, angulos, punto y coma, etc.) quedan
 *    fuera del conjunto permitido.
 * 2. Blacklist complementaria: ante entradas que pudieran relajarse en el
 *    futuro, se rechazan explicitamente patrones conocidos de ataque. Nunca
 *    debe ser la unica defensa, solo un refuerzo.
 */
public class InputValidator implements ConstraintValidator<ValidInput, String> {

    /** Lista blanca: solo alfanumerico, entre 3 y 10 caracteres. */
    private static final Pattern WHITELIST = Pattern.compile("^[a-zA-Z0-9]{3,10}$");

    /**
     * Patrones peligrosos (blacklist de refuerzo). Se evaluan sin distinguir
     * mayusculas/minusculas para cubrir SQL Injection, XSS y ejecucion de
     * comandos / RCE.
     */
    private static final Pattern DANGEROUS = Pattern.compile(
            "(')|(--)|(;)|(/\\*)|(\\*/)"                       // SQLi: comillas, comentarios, separadores
            + "|(\\b(select|insert|update|delete|drop|union|exec|execute|alter|create|truncate)\\b)" // SQLi keywords
            + "|(<|>|script|onerror|onload|javascript:|<img|<svg)" // XSS
            + "|(\\||&|`|\\$\\(|\\.\\./|%00)",                  // RCE / command injection / path traversal
            Pattern.CASE_INSENSITIVE);

    private static final int MAX_LENGTH = 10;

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // Un valor nulo se delega a @NotNull/@NotBlank; aqui no se considera invalido.
        if (value == null) {
            return true;
        }

        // Corte defensivo por longitud antes de aplicar regex (evita ReDoS y DoS).
        if (value.length() > MAX_LENGTH) {
            return false;
        }

        // 1) Debe cumplir la lista blanca.
        if (!WHITELIST.matcher(value).matches()) {
            return false;
        }

        // 2) Refuerzo: rechazar cualquier patron peligroso conocido.
        if (DANGEROUS.matcher(value).find()) {
            return false;
        }

        return true;
    }
}
