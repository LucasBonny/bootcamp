package br.com.gunthercloud.bootcamp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/h2-console/**").permitAll() // Libera o H2 Console
                .requestMatchers("/**").permitAll() // Libera todos os endpoints da API
                .anyRequest().authenticated() // Mantém autenticação para outras rotas
            )
            .csrf(csrf -> csrf.disable()) // Desativa CSRF para facilitar o uso da API
            .headers(headers -> headers.disable()); // Permite iframes (H2 Console)

        return http.build();
    }
}
