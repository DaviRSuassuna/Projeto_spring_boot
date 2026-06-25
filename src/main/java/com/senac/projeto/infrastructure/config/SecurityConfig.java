package com.senac.projeto.infrastructure.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuracao de seguranca da aplicacao.
 *
 * <p>Define a cadeia de filtros do Spring Security com as seguintes caracteristicas:
 * <ul>
 *   <li>Autenticacao stateless via JWT transportado em cookie HttpOnly;</li>
 *   <li>CSRF desabilitado — a protecao e coberta pelo atributo {@code SameSite=Strict}
 *       do cookie, que impede browsers modernos de envia-lo em requisicoes cross-site;</li>
 *   <li>Rotas publicas: {@code /}, {@code /login}, {@code /cadastro} e recursos estaticos;</li>
 *   <li>Rotas de administracao ({@code /admin/**}) restritas a {@code ROLE_ADMIN};</li>
 *   <li>Rotas de cliente ({@code /cliente/**}) restritas a {@code ROLE_USER}.</li>
 * </ul>
 * </p>
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    /**
     * Registra o encoder BCrypt como bean padrao para codificacao de senhas.
     *
     * @return instancia de {@link BCryptPasswordEncoder}
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configura e constroi a cadeia de filtros de seguranca HTTP.
     *
     * @param http construtor de configuracao de seguranca HTTP
     * @return cadeia de filtros configurada
     * @throws Exception se a configuracao do Spring Security falhar
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // CSRF desabilitado: proteção já coberta pelo SameSite=Strict no cookie JWT,
            // que impede browsers modernos de enviar o cookie em requisições cross-site.
            .csrf(csrf -> csrf.disable())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/login", "/cadastro", "/css/**", "/js/**", "/images/**").permitAll()
                .requestMatchers("/admin/**").hasAuthority("ROLE_ADMIN")
                .requestMatchers("/cliente/**").hasAuthority("ROLE_USER")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form.disable())
            .logout(logout -> logout
                .logoutUrl("/sair")
                .deleteCookies("jwt")
                .logoutSuccessUrl("/login")
                .permitAll()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
