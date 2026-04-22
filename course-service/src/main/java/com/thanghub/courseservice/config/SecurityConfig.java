package com.thanghub.courseservice.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import org.springframework.security.web.SecurityFilterChain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth -> auth

                        // public APIs
                        .requestMatchers(
                                "/actuator/health",
                                "/swagger-ui/**",
                                "/v3/api-docs/**"
                        ).permitAll()

                        .requestMatchers(HttpMethod.GET, "/courses/public/**").permitAll()

                        // instructor APIs
                        .requestMatchers(HttpMethod.POST, "/courses/**")
                        .hasAnyRole("INSTRUCTOR", "ADMIN")

                        .requestMatchers(HttpMethod.PUT, "/courses/**")
                        .hasAnyRole("INSTRUCTOR", "ADMIN")

                        .requestMatchers(HttpMethod.DELETE, "/courses/**")
                        .hasAnyRole("INSTRUCTOR", "ADMIN")

                        // admin APIs
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // all others need login
                        .anyRequest().authenticated()
                )


                .httpBasic(Customizer.withDefaults());

        return http.build();
    }

//    @Bean
//    public JwtAuthenticationConverter jwtAuthenticationConverter() {
//
//        JwtAuthenticationConverter converter =
//                new JwtAuthenticationConverter();
//
//        converter.setJwtGrantedAuthoritiesConverter(this::extractAuthorities);
//
//        return converter;
//    }
//
//    private Collection<SimpleGrantedAuthority> extractAuthorities(Jwt jwt) {
//
//        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
//
//        // roles claim
//        List<String> roles = jwt.getClaimAsStringList("roles");
//
//        if (roles != null) {
//            roles.forEach(role ->
//                    authorities.add(
//                            new SimpleGrantedAuthority("ROLE_" + role)
//                    )
//            );
//        }
//
//        // permissions claim
//        List<String> permissions =
//                jwt.getClaimAsStringList("permissions");
//
//        if (permissions != null) {
//            permissions.forEach(permission ->
//                    authorities.add(
//                            new SimpleGrantedAuthority(permission)
//                    )
//            );
//        }
//
//        return authorities;
//    }
}