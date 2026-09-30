package com.naveena.employeemanagement.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                // Public: viewing the frontend, Swagger docs, and all GET requests
                .requestMatchers("/", "/index.html", "/*.js", "/*.css").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/employees/**").permitAll()
                // Protected: creating, updating, deleting employees
                .requestMatchers(HttpMethod.POST, "/employees/**").authenticated()
                .requestMatchers(HttpMethod.PUT, "/employees/**").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/employees/**").authenticated()
                .anyRequest().permitAll()
            )
            .httpBasic(basic -> {}); // enables simple username/password prompt

        return http.build();
    }
}