package com.senac.projeto.domain.model;

/**
 * Modos de pagamento aceitos em um {@link Pedido}.
 *
 * <p>Cada constante carrega uma descricao legivel para exibicao na interface.</p>
 */
public enum ModoPagamento {
    DINHEIRO("Dinheiro"),
    CARTAO_CREDITO("Cartão de Crédito"),
    CARTAO_DEBITO("Cartão de Débito"),
    PIX("Pix");

    private final String descricao;

    ModoPagamento(String descricao) {
        this.descricao = descricao;
    }

    /**
     * Retorna a descricao formatada do modo de pagamento.
     *
     * @return texto legivel para exibicao ao usuario
     */
    public String getDescricao() {
        return descricao;
    }
}
