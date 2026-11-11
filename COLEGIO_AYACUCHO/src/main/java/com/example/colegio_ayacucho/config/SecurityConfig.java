package com.example.colegio_ayacucho.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuración de seguridad:
 *  - Login propio en /login con sesión y CSRF activo.
 *  - ADMIN accede a usuarios, tipos y ubicaciones; TECNICO no.
 *  - Eliminar activos está reservado al ADMIN.
 *  - Contraseñas cifradas con BCrypt.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final LoginSuccessHandler loginSuccessHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // Recursos públicos (estilos, scripts, login y páginas de error)
                .requestMatchers("/css/**", "/js/**", "/img/**", "/favicon.ico").permitAll()
                .requestMatchers("/login", "/error/**").permitAll()
                // Solo el administrador puede eliminar activos (POST con CSRF)
                .requestMatchers(HttpMethod.POST, "/activos/*/eliminar").hasRole("ADMIN")
                // Catálogos y usuarios: solo ADMIN
                .requestMatchers("/usuarios/**", "/tipos/**", "/ubicaciones/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("username")
                .passwordParameter("password")
                .successHandler(loginSuccessHandler)
                .failureUrl("/login?error")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("INVENTARIO_SESSION", "JSESSIONID")
            )
            .exceptionHandling(ex -> ex.accessDeniedPage("/error/403"))
            .sessionManagement(sm -> sm.sessionFixation(sf -> sf.changeSessionId()))
            .csrf(Customizer.withDefaults());

        return http.build();
    }
}
