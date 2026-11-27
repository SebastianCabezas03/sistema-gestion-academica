
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
            .cors(cors ->
                cors.configurationSource(corsConfigurationSource())
            )

            .csrf(csrf -> csrf.disable())

            .sessionManagement(session ->
                session.sessionCreationPolicy(
                    SessionCreationPolicy.STATELESS
                )
            )

            .authenticationProvider(authenticationProvider)

            .authorizeHttpRequests(auth -> auth

                // Solicitudes OPTIONS para CORS
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // Autenticación pública
                .requestMatchers(
                    HttpMethod.POST,
                    "/api/auth/login"
                ).permitAll()

                // Gestión de cuentas: ADMIN
                .requestMatchers("/api/cuentas/**")
                    .hasRole("ADMIN")

                // Perfil propio: ADMIN, DOCENTE y ALUMNO
                .requestMatchers(
                    HttpMethod.GET,
                    "/api/perfil/me"
                ).hasAnyRole("ADMIN", "DOCENTE", "ALUMNO")

                // Historial académico propio: ALUMNO
                .requestMatchers(
                    HttpMethod.GET,
                    "/api/alumnos/mi-historial"
                ).hasRole("ALUMNO")

                // Gestión de alumnos: ADMIN
                .requestMatchers("/api/alumnos/**")
                    .hasRole("ADMIN")

                // Gestión de docentes: ADMIN
                .requestMatchers("/api/docentes/**")
                    .hasRole("ADMIN")

                // Matrículas: ADMIN
                .requestMatchers("/api/matriculas/**")
                    .hasRole("ADMIN")

                // Evaluaciones: ADMIN y DOCENTE
                .requestMatchers("/api/evaluaciones/**")
                    .hasAnyRole("ADMIN", "DOCENTE")

                // Consulta de calificaciones propias: ALUMNO
                .requestMatchers(
                    HttpMethod.GET,
                    "/api/calificaciones/mis-calificaciones"
                ).hasRole("ALUMNO")

                // Gestión de calificaciones: ADMIN y DOCENTE
                .requestMatchers("/api/calificaciones/**")
                    .hasAnyRole("ADMIN", "DOCENTE")

                // Dashboard: ADMIN
                .requestMatchers("/api/dashboard/**")
                    .hasRole("ADMIN")

                // Reportes de asistencia: ADMIN y DOCENTE
                .requestMatchers("/api/reportes/asistencias/**")
                    .hasAnyRole("ADMIN", "DOCENTE")

                // Reportes de matrículas: ADMIN y DOCENTE
                .requestMatchers("/api/reportes/matriculas/**")
                    .hasAnyRole("ADMIN", "DOCENTE")

                // Cursos del alumno: ALUMNO
                .requestMatchers(
                    HttpMethod.GET,
                    "/api/mis-cursos"
                ).hasRole("ALUMNO")

                // Consultas y gestión de asistencia

                // Asistencia propia: ALUMNO
                .requestMatchers(
                    HttpMethod.GET,
                    "/api/asistencias/mi-asistencia"
                ).hasRole("ALUMNO")

                // Asistencia de un alumno: ADMIN
                .requestMatchers(
                    HttpMethod.GET,
                    "/api/asistencias/alumno/**"
                ).hasRole("ADMIN")

                // Todas las asistencias: ADMIN
                .requestMatchers(
                    HttpMethod.GET,
                    "/api/asistencias/admin/**"
                ).hasRole("ADMIN")

                // Asistencia por curso: ADMIN y DOCENTE
                .requestMatchers(
                    HttpMethod.GET,
                    "/api/asistencias/curso/**"
                ).hasAnyRole("ADMIN", "DOCENTE")

                // Registrar asistencia: DOCENTE
                .requestMatchers(
                    HttpMethod.POST,
                    "/api/asistencias/registrar",
                    "/api/asistencias/registrar/lote"
                ).hasRole("DOCENTE")

                // Actualizar y eliminar asistencia: ADMIN y DOCENTE
                .requestMatchers(
                    HttpMethod.PUT,
                    "/api/asistencias/*"
                ).hasAnyRole("ADMIN", "DOCENTE")

                .requestMatchers(
                    HttpMethod.DELETE,
                    "/api/asistencias/*"
                ).hasAnyRole("ADMIN", "DOCENTE")

                // Gestión de asistencia docente
                .requestMatchers("/api/docente/asistencia/**")
                    .hasRole("DOCENTE")

                // Consulta de cursos
                .requestMatchers(
                    HttpMethod.GET,
                    "/api/cursos/**"
                ).hasAnyRole("ADMIN", "DOCENTE", "ALUMNO")

                // Administración de cursos
                .requestMatchers("/api/cursos/**")
                    .hasRole("ADMIN")

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
