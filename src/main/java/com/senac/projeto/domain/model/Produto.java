package com.senac.projeto.domain.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * Entidade de dominio que representa um produto disponivel no cardapio.
 *
 * <p>Mapeada para a tabela {@code produto}. O campo {@code quantidade} representa
 * o estoque disponivel e e decrementado a cada pedido criado.</p>
 */
@Entity
@Table(name = "produto")
@Data
@NoArgsConstructor
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private double preco;

    /** Quantidade disponivel em estoque. */
    @Column(nullable = false)
    private int quantidade;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    @Column(name = "atualizado_em", nullable = false)
    private LocalDateTime atualizadoEm = LocalDateTime.now();
}
