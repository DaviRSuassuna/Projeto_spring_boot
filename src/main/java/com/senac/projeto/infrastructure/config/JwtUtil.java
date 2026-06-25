package com.senac.projeto.infrastructure.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * Utilitario para criacao e validacao de tokens JWT.
 *
 * <p>Utiliza HMAC-SHA com a chave configurada em {@code jwt.secret} e expiracao
 * definida por {@code jwt.expiration-ms}. O token armazena o e-mail do usuario
 * como {@code subject} e a role como claim {@code role}.</p>
 */
@Component
public class JwtUtil {

    private final SecretKey key;
    private final long expirationMs;

    public JwtUtil(@Value("${jwt.secret}") String secret,
                   @Value("${jwt.expiration-ms}") long expirationMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.expirationMs = expirationMs;
    }

    /**
     * Gera um token JWT assinado com o e-mail e a role do usuario.
     *
     * @param email e-mail do usuario (utilizado como {@code subject})
     * @param role  authority do usuario, no formato {@code ROLE_ADMIN} ou {@code ROLE_USER}
     * @return token JWT compacto e assinado
     */
    public String gerarToken(String email, String role) {
        return Jwts.builder()
                .subject(email)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(key)
                .compact();
    }

    /**
     * Extrai o e-mail ({@code subject}) de um token.
     *
     * @param token token JWT valido
     * @return e-mail do usuario
     */
    public String extrairEmail(String token) {
        return parsearClaims(token).getSubject();
    }

    /**
     * Extrai a role do claim {@code role} de um token.
     *
     * @param token token JWT valido
     * @return role do usuario
     */
    public String extrairRole(String token) {
        return parsearClaims(token).get("role", String.class);
    }

    /**
     * Verifica se o token e valido (assinatura correta e nao expirado).
     *
     * @param token token JWT a ser verificado
     * @return {@code true} se valido; {@code false} se invalido ou expirado
     */
    public boolean tokenValido(String token) {
        try {
            parsearClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Claims parsearClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
