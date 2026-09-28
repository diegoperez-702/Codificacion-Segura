package com.example.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro que autentica cada peticion a partir del JWT en la cabecera
 * Authorization: Bearer &lt;token&gt;.
 *
 * Seguridad:
 * - Si no hay cabecera o no es Bearer, se deja pasar la peticion sin
 *   autenticar (los endpoints protegidos la rechazaran despues).
 * - Si el token es invalido o expiro, NO se establece autenticacion; se deja
 *   que la cadena de seguridad devuelva 401/403 de forma uniforme.
 * - No se registran tokens ni datos sensibles en logs.
 *
 * No lleva @Component para evitar que Spring lo registre dos veces; se instancia
 * explicitamente en SecurityConfig y se ubica antes del filtro de usuario/clave.
 */
public class AuthenticationFilter extends OncePerRequestFilter {

    private static final String HEADER = "Authorization";
    private static final String PREFIX = "Bearer ";

    private final JwtTokenUtil jwtTokenUtil;
    private final UserDetailsService userDetailsService;

    public AuthenticationFilter(JwtTokenUtil jwtTokenUtil, UserDetailsService userDetailsService) {
        this.jwtTokenUtil = jwtTokenUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader(HEADER);

        if (authHeader != null && authHeader.startsWith(PREFIX)) {
            String token = authHeader.substring(PREFIX.length());

            if (jwtTokenUtil.isValid(token)
                    && SecurityContextHolder.getContext().getAuthentication() == null) {
                String username = jwtTokenUtil.getSubject(token);
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }
}
