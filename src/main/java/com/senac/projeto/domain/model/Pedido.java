package com.senac.projeto.domain.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidade de dominio que representa um pedido realizado por um {@link Usuario}.
 *
 * <p>Mapeada para a tabela {@code pedido}. Os itens sao gerenciados com cascata total
 * ({@code CascadeType.ALL}) e remocao de orphaos, de modo que a exclusao do pedido
 * tambem remove automaticamente todos os {@link ItemPedido} associados.</p>
 */
@Entity
@Table(name = "pedido")
@Data
@NoArgsConstructor
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    /** Valor total do pedido, calculado pela soma dos campos {@code preco} de cada item. */
    @Column(nullable = false)
    private double total;

    @Enumerated(EnumType.STRING)
    @Column(name = "modo_pagamento")
    private ModoPagamento modoPagamento;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<ItemPedido> itens = new ArrayList<>();

    @Column(name = "atualizado_em", nullable = false)
    private LocalDateTime atualizadoEm = LocalDateTime.now();
}
