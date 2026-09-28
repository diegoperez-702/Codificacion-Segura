package com.example.infrastructure.web;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Manejador global de errores de validacion.
 *
 * Objetivo de seguridad: devolver respuestas controladas (HTTP 400) ante
 * entradas invalidas SIN exponer detalles internos del sistema (stack traces,
 * nombres de clases, mensajes de framework). Esto evita fuga de informacion
 * que un atacante podria usar para afinar sus ataques.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Errores de validacion del cuerpo JSON (@Valid sobre @RequestBody).
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleBodyValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fe -> fe.getDefaultMessage() == null ? "invalid input" : fe.getDefaultMessage(),
                        (a, b) -> a));
        return build(HttpStatus.BAD_REQUEST, "Entrada invalida", fieldErrors);
    }

    /**
     * Errores de validacion de parametros de metodo (@Validated + @RequestParam).
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleParamValidation(ConstraintViolationException ex) {
        Map<String, String> violations = ex.getConstraintViolations().stream()
                .collect(Collectors.toMap(
                        v -> v.getPropertyPath().toString(),
                        v -> v.getMessage() == null ? "invalid input" : v.getMessage(),
                        (a, b) -> a));
        return build(HttpStatus.BAD_REQUEST, "Entrada invalida", violations);
    }

    /**
     * Cuerpo JSON mal formado o ausente. No se expone el detalle del parser.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleUnreadable(HttpMessageNotReadableException ex) {
        return build(HttpStatus.BAD_REQUEST, "Cuerpo de la peticion invalido o mal formado", Map.of());
    }

    /**
     * Red de seguridad: cualquier otra excepcion no controlada se traduce a un
     * 500 generico, sin filtrar el mensaje original.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrio un error procesando la solicitud", Map.of());
    }

    private ResponseEntity<Map<String, Object>> build(HttpStatus status, String message, Map<String, String> details) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", status.value());
        body.put("error", message);
        if (!details.isEmpty()) {
            body.put("details", details);
        }
        return ResponseEntity.status(status).body(body);
    }
}
