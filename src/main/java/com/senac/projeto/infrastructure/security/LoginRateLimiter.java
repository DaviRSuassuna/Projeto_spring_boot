package com.senac.projeto.infrastructure.security;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LoginRateLimiter {

    private static final int MAX_TENTATIVAS = 5;
    private static final long JANELA_MS = 10 * 60 * 1000L; // 10 minutos

    private record Tentativas(int count, Instant primeiraEm) {}

    private final Map<String, Tentativas> registros = new ConcurrentHashMap<>();

    public boolean estaBloqueado(String ip) {
        Tentativas t = registros.get(ip);
        if (t == null) return false;
        if (janelaExpirou(t)) {
            registros.remove(ip);
            return false;
        }
        return t.count() >= MAX_TENTATIVAS;
    }

    public void registrarFalha(String ip) {
        registros.compute(ip, (key, atual) -> {
            if (atual == null || janelaExpirou(atual)) {
                return new Tentativas(1, Instant.now());
            }
            return new Tentativas(atual.count() + 1, atual.primeiraEm());
        });
    }

    public void resetar(String ip) {
        registros.remove(ip);
    }

    private boolean janelaExpirou(Tentativas t) {
        return Instant.now().toEpochMilli() - t.primeiraEm().toEpochMilli() > JANELA_MS;
    }
}
