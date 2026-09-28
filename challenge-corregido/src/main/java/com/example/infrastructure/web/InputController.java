package com.example.infrastructure.web;

import com.example.domain.model.SecureModel;
import com.example.infrastructure.validation.ValidInput;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.HtmlUtils;

import java.util.Map;

/**
 * Punto de entrada HTTP del sistema. Aqui es donde llegan los datos externos,
 * por lo que TODA entrada se valida antes de procesarse.
 *
 * Controles aplicados (Fase 2 - validacion de entrada):
 * - {@code @Valid} sobre el cuerpo JSON: dispara las restricciones del record
 *   {@link SecureModel} (NotBlank, Size, ValidInput).
 * - {@code @Validated} a nivel de clase + restricciones en parametros: valida
 *   los query params directamente en el metodo.
 * - Codificacion de salida (HtmlUtils.htmlEscape) como defensa adicional
 *   contra XSS al reflejar cualquier dato de vuelta al cliente.
 */
@RestController
@RequestMapping("/api")
@Validated
public class InputController {

    /**
     * Recibe datos en el cuerpo de la peticion (JSON) y los valida via bean
     * validation. Si el input no cumple las reglas, Spring lanza
     * MethodArgumentNotValidException (manejada por el GlobalExceptionHandler).
     */
    @PostMapping("/process")
    public ResponseEntity<Map<String, String>> process(@Valid @RequestBody SecureModel model) {
        // La entrada ya esta validada. Se codifica en la salida como refuerzo anti-XSS.
        String safeOutput = HtmlUtils.htmlEscape(model.input());
        return ResponseEntity.ok(Map.of(
                "status", "accepted",
                "input", safeOutput));
    }

    /**
     * Recibe un dato via query param y lo valida con la whitelist reforzada.
     */
    @GetMapping("/echo")
    public ResponseEntity<Map<String, String>> echo(
            @RequestParam("value")
            @NotBlank
            @Size(min = 3, max = 10)
            @ValidInput String value) {

        String safeOutput = HtmlUtils.htmlEscape(value);
        return ResponseEntity.status(HttpStatus.OK).body(Map.of(
                "status", "accepted",
                "value", safeOutput));
    }
}
