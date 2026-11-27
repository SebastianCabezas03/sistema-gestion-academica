
package com.reinodeloscielos.backend.config;

import com.reinodeloscielos.backend.service.CustomUserDetailsService;
import com.reinodeloscielos.backend.service.JwtService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Locale;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            CustomUserDetailsService userDetailsService) {

        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String authorization = request.getHeader("Authorization");

        // Si no existe el encabezado Bearer, continúa con la cadena.
        if (authorization == null ||
                !authorization.regionMatches(true, 0, "Bearer ", 0, 7)) {

            filterChain.doFilter(request, response);
            return;
        }

        String token = authorization.substring(7).trim();

        if (token.isEmpty()) {
            enviarUnauthorized(response);
            return;
        }

        try {

            // Validar el token JWT.
            Claims claims = jwtService.validarToken(token);

            String username = claims.getSubject();
            String rolToken = claims.get("rol", String.class);

            if (username == null || username.isBlank() ||
                    rolToken == null || rolToken.isBlank()) {

                enviarUnauthorized(response);
                return;
            }

            // Recuperar los datos y permisos actuales del usuario.
            UserDetails userDetails =
                    userDetailsService.loadUserByUsername(
                            username.trim().toLowerCase(Locale.ROOT)
                    );

            // Comprobar que el rol del token coincida con el rol actual.
            String autoridadEsperada =
                    "ROLE_" + rolToken.trim().toUpperCase(Locale.ROOT);

            boolean rolValido = userDetails.getAuthorities()
                    .stream()
                    .anyMatch(authority ->
                            authority.getAuthority().equals(autoridadEsperada)
                    );

            if (!rolValido) {
                enviarUnauthorized(response);
                return;
            }

            // Evitar reemplazar una autenticación existente.
            Authentication autenticacionActual =
                    SecurityContextHolder.getContext().getAuthentication();

            if (autenticacionActual == null) {

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                authentication.setDetails(
                        new WebAuthenticationDetailsSource()
                                .buildDetails(request)
                );

                // Registrar autenticación en el contexto de seguridad.
                SecurityContextHolder.getContext()
                        .setAuthentication(authentication);
            }

        } catch (JwtException |
                 IllegalArgumentException |
                 UsernameNotFoundException e) {

            enviarUnauthorized(response);
            return;
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Devuelve una respuesta HTTP 401 en formato JSON.
     */
    private void enviarUnauthorized(HttpServletResponse response)
            throws IOException {

        SecurityContextHolder.clearContext();

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        response.getWriter().write(
                "{\"mensaje\":\"Token inválido, expirado o usuario inexistente\"}"
        );
    }
}
