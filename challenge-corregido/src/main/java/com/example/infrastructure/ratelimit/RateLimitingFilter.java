package com.example.infrastructure.ratelimit;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Filtro de rate limiting por IP basado en ventana fija.
 *
 * Mitiga la vulnerabilidad "Falta de rate limiting": limita el numero de
 * peticiones que una misma IP puede realizar dentro de una ventana temporal.
 * Esto reduce el riesgo de fuerza bruta contra el login y de abuso/DoS de los
 * endpoints.
 *
 * Implementacion sin dependencias externas (contador por ventana):
 * - Cada IP tiene un contador y una marca de inicio de ventana.
 * - Al superar la capacidad dentro de la ventana se responde HTTP 429.
 * - Al expirar la ventana, el contador se reinicia.
 *
 * Nota: es un limitador en memoria, adecuado para una sola instancia. En un
 * despliegue distribuido conviene un backend compartido (p. ej. Redis).
 */
@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private final int capacity;
    private final long windowMillis;

    private final ConcurrentHashMap<String, Window> counters = new ConcurrentHashMap<>();

    public RateLimitingFilter(
            @Value("${app.rate-limit.capacity:20}") int capacity,
            @Value("${app.rate-limit.window-seconds:60}") long windowSeconds) {
        this.capacity = capacity;
        this.windowMillis = windowSeconds * 1000L;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String clientId = resolveClientIp(request);
        long now = System.currentTimeMillis();

        Window window = counters.compute(clientId, (key, existing) -> {
            if (existing == null || now - existing.windowStart >= windowMillis) {
                // Nueva ventana.
                return new Window(now);
            }
            existing.count.incrementAndGet();
            return existing;
        });

        int used = window.count.get();
        long resetInSeconds = Math.max(0, (window.windowStart + windowMillis - now) / 1000);

        if (used > capacity) {
            response.setStatus(429); // Too Many Requests
            response.setHeader("Retry-After", String.valueOf(resetInSeconds));
            response.setContentType("application/json");
            response.getWriter().write(
                    "{\"status\":429,\"error\":\"Demasiadas peticiones. Intente mas tarde.\"}");
            return;
        }

        response.setHeader("X-RateLimit-Limit", String.valueOf(capacity));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(Math.max(0, capacity - used)));
        filterChain.doFilter(request, response);
    }

    /**
     * Obtiene la IP del cliente. Considera X-Forwarded-For (primer valor) si la
     * app corre detras de un proxy/balanceador de confianza.
     */
    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            int comma = forwarded.indexOf(',');
            return (comma > 0 ? forwarded.substring(0, comma) : forwarded).trim();
        }
        return request.getRemoteAddr();
    }

    /** Estado de la ventana de conteo para una IP. */
    private static final class Window {
        final long windowStart;
        final AtomicInteger count;

        Window(long windowStart) {
            this.windowStart = windowStart;
            this.count = new AtomicInteger(1);
        }
    }
}
