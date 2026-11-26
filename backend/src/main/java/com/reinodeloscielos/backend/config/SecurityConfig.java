
package com.reinodeloscielos.backend.config;

import com.reinodeloscielos.backend.service.CustomUserDetailsService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            DaoAuthenticationProvider authenticationProvider) throws Exception {

        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())

            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            .authenticationProvider(authenticationProvider)

            .authorizeHttpRequests(auth -> auth

                // Permitir solicitudes OPTIONS para CORS
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // Autenticación
                .requestMatchers("/api/auth/login").permitAll()

                // Gestión de cuentas
                .requestMatchers("/api/cuentas/**").hasRole("ADMIN")

                // Historial académico del alumno
                .requestMatchers(
                    HttpMethod.GET,
                    "/api/alumnos/mi-historial"
                ).hasRole("ALUMNO")

                // Gestión de alumnos
                .requestMatchers("/api/alumnos/**").hasRole("ADMIN")

                // Gestión de docentes
                .requestMatchers("/api/docentes/**").hasRole("ADMIN")

                // Gestión de matrículas
                .requestMatchers("/api/matriculas/**").hasRole("ADMIN")

                // Evaluaciones: administrador y docentes
                .requestMatchers("/api/evaluaciones/**")
                    .hasAnyRole("ADMIN", "DOCENTE")

                // Consulta de calificaciones propias del alumno
                .requestMatchers(
                    HttpMethod.GET,
                    "/api/calificaciones/mis-calificaciones"
                ).hasRole("ALUMNO")

                // Gestión de calificaciones
                .requestMatchers("/api/calificaciones/**")
                    .hasAnyRole("ADMIN", "DOCENTE")

                // Dashboard y estadísticas: solamente administrador
                .requestMatchers("/api/dashboard/**")
                    .hasRole("ADMIN")

                // Consulta de cursos
                .requestMatchers(HttpMethod.GET, "/api/cursos/**")
                    .hasAnyRole("ADMIN", "DOCENTE", "ALUMNO")

                // Administración de cursos
                .requestMatchers("/api/cursos/**").hasRole("ADMIN")

                // Cualquier otra ruta requiere autenticación
                .anyRequest().authenticated()
            )

            .addFilterBefore(
                jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }

    // Configuración CORS para Angular
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(List.of(
            "http://localhost:4200",
            "http://127.0.0.1:4200"
        ));

        configuration.setAllowedMethods(List.of(
            "GET",
            "POST",
            "PUT",
            "DELETE",
            "PATCH",
            "OPTIONS"
        ));

        configuration.setAllowedHeaders(List.of("*"));

        UrlBasedCorsConfigurationSource source =
            new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    // Codificación de contraseñas
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Proveedor de autenticación
    @Bean
    public DaoAuthenticationProvider authenticationProvider(
            CustomUserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {

        DaoAuthenticationProvider provider =
            new DaoAuthenticationProvider(userDetailsService);

        provider.setPasswordEncoder(passwordEncoder);

        return provider;
    }

    // Administrador de autenticación
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration) throws Exception {

        return configuration.getAuthenticationManager();
    }
}
