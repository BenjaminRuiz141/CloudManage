package com.duoc.CloudManage.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.client.RestTemplate;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // URI de claves públicas para la validación de firmas JWT de Azure B2C
    // Nota: El tenant ID y el policy name deben coincidir con la configuración de
    // application.properties
    private final String jwkSetUri = "https://duocazure4cn.b2clogin.com/060df69f-b3b0-46a5-b747-3f284e2a7b53/discovery/v2.0/keys?p=b2c_1_cloud_v1_login";

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Deshabilitamos CSRF ya que es una API REST stateless con JWT
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth

                        .requestMatchers("/h2-console/**").permitAll()

                        // 1. Rol restringido SOLO al endpoint de descarga
                        // Permitimos que tanto el rol de consulta como el de admin puedan descargar
                        .requestMatchers(HttpMethod.GET, "/guias/descargar/*").hasAnyRole("CONSULTA", "ADMIN")

                        // 2. Rol para el resto de las operaciones (Crear, Subir, Modificar, Eliminar,
                        // Consultar historial)
                        .requestMatchers("/guias/**").hasRole("ADMIN")



                        // Cualquier otra petición debe estar autenticada
                        .anyRequest().authenticated())
                // Configuramos la aplicación como Resource Server que valida JWT
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))

                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.decoder(jwtDecoder())
                                .jwtAuthenticationConverter(jwtAuthenticationConverter())));

        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        // Configuramos el decoder con timeout para evitar bloqueos en la validación JWT
        return NimbusJwtDecoder.withJwkSetUri(jwkSetUri)
                .restOperations(restTemplate())
                .build();
    }

    @Bean
    public RestTemplate restTemplate() {
        // Configuramos RestTemplate con timeouts para evitar bloqueos en JWKS
        org.springframework.http.client.SimpleClientHttpRequestFactory factory = new org.springframework.http.client.SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000); // 5 segundos
        factory.setReadTimeout(5000); // 5 segundos
        return new RestTemplate(factory);
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();

        // Implementamos la conversión personalizada de Custom Claims
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            // Extraemos el valor del Custom Claim 'extension_Role'
            Object roleClaim = jwt.getClaim("extension_Role");

            // Log para depuración - ver qué claim se recibe exactamente
            System.out.println("DEBUG - extension_Role claim: '" + roleClaim + "'");
            System.out.println("DEBUG - claim class: " + (roleClaim != null ? roleClaim.getClass().getName() : "null"));

            if (roleClaim == null) {
                return Collections.emptyList();
            }

            // Convertimos el valor del claim en una lista de GrantedAuthority con prefijo
            // 'ROLE_'
            // Usamos trim() para eliminar espacios en blanco
            if (roleClaim instanceof String role) {
                String trimmedRole = role.trim();
                System.out.println("DEBUG - Role after trim: '" + trimmedRole + "'");
                return List.of(new SimpleGrantedAuthority("ROLE_" + trimmedRole.toUpperCase()));
            } else if (roleClaim instanceof Collection<?> roles) {
                return roles.stream()
                        .filter(r -> r instanceof String)
                        .map(r -> new SimpleGrantedAuthority("ROLE_" + ((String) r).trim().toUpperCase()))
                        .collect(Collectors.toList());
            }

            return Collections.emptyList();
        });

        return converter;
    }
}
