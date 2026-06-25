package com.senac.projeto.infrastructure.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Filtro de autenticacao JWT executado uma unica vez por requisicao.
 *
 * <p>Extrai o token do cookie {@code jwt}, valida a assinatura e expiracao,
 * e popula o {@link SecurityContextHolder} com as credenciais do usuario
 * (email como principal e role como authority). Requisicoes sem cookie valido
 * passam pelo filtro sem autenticacao, sendo bloqueadas posteriormente pelas
 * regras de autorizacao definidas em {@link SecurityConfig}.</p>
 */
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        extrairTokenDoCookie(request).ifPresent(token -> {
            if (jwtUtil.tokenValido(token) && SecurityContextHolder.getContext().getAuthentication() == null) {
                String email = jwtUtil.extrairEmail(token);
                String role = jwtUtil.extrairRole(token);

                var auth = new UsernamePasswordAuthenticationToken(
                        email, null, List.of(new SimpleGrantedAuthority(role))
                );
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        });

        filterChain.doFilter(request, response);
    }

    /**
     * Extrai o valor do cookie {@code jwt} da requisicao.
     *
     * @param request requisicao HTTP recebida
     * @return {@link Optional} com o token JWT, ou vazio se o cookie nao existir
     */
    private Optional<String> extrairTokenDoCookie(HttpServletRequest request) {
        if (request.getCookies() == null) return Optional.empty();
        return Arrays.stream(request.getCookies())
                .filter(c -> "jwt".equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst();
    }
}
