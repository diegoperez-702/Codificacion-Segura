package com.example.infrastructure.security;

import com.example.infrastructure.ratelimit.RateLimitingFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;

/**
 * Configuracion de seguridad de la aplicacion.
 *
 * Incluye:
 * - Autenticacion stateless basada en JWT (AuthenticationFilter antes del
 *   filtro de usuario/clave).
 * - PasswordEncoder BCrypt (mitiga exposicion de datos sensibles: nunca se
 *   guardan contrasenas en claro).
 * - Rate limiting por IP como primer filtro (mitiga fuerza bruta / abuso).
 * - Cabeceras de seguridad HTTP.
 * - Reglas de autorizacion: login publico, resto autenticado.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtTokenUtil jwtTokenUtil;
    private final UserDetailsService userDetailsService;
    private final RateLimitingFilter rateLimitingFilter;

    public SecurityConfig(JwtTokenUtil jwtTokenUtil,
                          UserDetailsService userDetailsService,
                          RateLimitingFilter rateLimitingFilter) {
        this.jwtTokenUtil = jwtTokenUtil;
        this.userDetailsService = userDetailsService;
        this.rateLimitingFilter = rateLimitingFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        AuthenticationFilter jwtFilter = new AuthenticationFilter(jwtTokenUtil, userDetailsService);

        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                    // El login debe ser accesible sin token.
                    .requestMatchers("/api/auth/login").permitAll()
                    // El resto de la API requiere JWT valido.
                    .requestMatchers("/api/**").authenticated()
                    .anyRequest().authenticated())
            // Rate limiting primero, para frenar abuso antes de gastar recursos.
            .addFilterBefore(rateLimitingFilter, UsernamePasswordAuthenticationFilter.class)
            // Autenticacion JWT antes del filtro de usuario/clave.
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
            .headers(headers -> headers
                    .contentTypeOptions(Customizer.withDefaults())
                    .frameOptions(frame -> frame.deny())
                    .referrerPolicy(ref -> ref.policy(
                            ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER))
                    .contentSecurityPolicy(csp -> csp.policyDirectives(
                            "default-src 'none'; frame-ancestors 'none'")));

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Evita que Spring Boot registre RateLimitingFilter automaticamente en la
     * cadena de filtros del servlet. Solo debe ejecutarse una vez, a traves de
     * la SecurityFilterChain donde lo insertamos explicitamente.
     */
    @Bean
    public FilterRegistrationBean<RateLimitingFilter> rateLimitingFilterRegistration(
            RateLimitingFilter filter) {
        FilterRegistrationBean<RateLimitingFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }
}
