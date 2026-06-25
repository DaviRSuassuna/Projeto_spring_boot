package com.senac.projeto.domain.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidade de dominio que representa um item dentro de um {@link Pedido}.
 *
 * <p>Mapeada para a tabela {@code item_pedido}. O campo {@code preco} armazena o valor
 * total da linha (preco unitario x quantidade) no momento da compra, preservando o
 * historico mesmo que o preco do produto seja alterado posteriormente.</p>
 */
@Entity
@Table(name = "item_pedido")
@Data
@NoArgsConstructor
public class ItemPedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id", nullable = false)
    private Pedido pedido;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    @Column(nullable = false)
    private int quantidade;

    /** Valor total da linha: preco unitario multiplicado pela quantidade. */
    @Column(nullable = false)
    private double preco;
}
