package com.senac.projeto.infrastructure.security;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Controle de taxa de tentativas de login por endereco IP.
 *
 * <p>Bloqueia um IP apos {@code MAX_TENTATIVAS} falhas consecutivas dentro de uma
 * janela de {@code JANELA_MS} milissegundos. O bloqueio e automaticamente liberado
 * quando a janela expira. A implementacao e thread-safe via {@link ConcurrentHashMap}.</p>
 *
 * <p>Este componente nao persiste estado — reinicializacoes da aplicacao zeram os
 * contadores de tentativas.</p>
 */
@Component
public class LoginRateLimiter {

    private static final int MAX_TENTATIVAS = 5;
    private static final long JANELA_MS = 10 * 60 * 1000L; // 10 minutos

    private record Tentativas(int count, Instant primeiraEm) {}

    private final Map<String, Tentativas> registros = new ConcurrentHashMap<>();

    /**
     * Verifica se o IP esta bloqueado por excesso de tentativas.
     *
     * <p>Se a janela de tempo ja expirou, o registro e removido e o IP e considerado
     * desbloqueado.</p>
     *
     * @param ip endereco IP do cliente
     * @return {@code true} se o IP atingiu o limite dentro da janela vigente
     */
    public boolean estaBloqueado(String ip) {
        Tentativas t = registros.get(ip);
        if (t == null) return false;
        if (janelaExpirou(t)) {
            registros.remove(ip);
            return false;
        }
        return t.count() >= MAX_TENTATIVAS;
    }

    /**
     * Registra uma tentativa de login malsucedida para o IP.
     *
     * <p>Se nao houver registro previo ou a janela anterior tiver expirado,
     * inicia uma nova contagem a partir de 1.</p>
     *
     * @param ip endereco IP do cliente
     */
    public void registrarFalha(String ip) {
        registros.compute(ip, (key, atual) -> {
            if (atual == null || janelaExpirou(atual)) {
                return new Tentativas(1, Instant.now());
            }
            return new Tentativas(atual.count() + 1, atual.primeiraEm());
        });
    }

    /**
     * Remove o registro de tentativas do IP, liberando-o imediatamente.
     *
     * <p>Deve ser chamado apos um login bem-sucedido.</p>
     *
     * @param ip endereco IP do cliente
     */
    public void resetar(String ip) {
        registros.remove(ip);
    }

    private boolean janelaExpirou(Tentativas t) {
        return Instant.now().toEpochMilli() - t.primeiraEm().toEpochMilli() > JANELA_MS;
    }
}
