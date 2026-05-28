package com.mingeso.travel_agency.config;

import org.hibernate.mapping.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Collection;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/api/v1/users/").permitAll()
                        .requestMatchers("/api/v1/public/**").permitAll()
                        .requestMatchers("/api/v1/**").authenticated()
                        .anyRequest().permitAll()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                );
        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter jwtConverter = new JwtAuthenticationConverter();

        jwtConverter.setJwtGrantedAuthoritiesConverter(jwt -> {
            // 1. Usamos getClaimAsMap() para forzar el tipo a Map y evitar errores en el IDE
            java.util.Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");

            // 2. Si no existe o no tiene roles, devolvemos lista vacía
            if (realmAccess == null || realmAccess.get("roles") == null) {
                return java.util.Collections.emptyList();
            }

            // 3. Extraemos la lista de roles que está dentro de "realm_access"
            @SuppressWarnings("unchecked")
            java.util.Collection<String> roles = (java.util.Collection<String>) realmAccess.get("roles");

            // 4. Los convertimos al formato "ROLE_NOMBRE" que Spring requiere
            return roles.stream()
                    .map(role -> new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + role))
                    .collect(java.util.stream.Collectors.toList());
        });

        return jwtConverter;
    }
}