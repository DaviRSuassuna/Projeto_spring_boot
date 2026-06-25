package com.senac.projeto.domain.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * Entidade de dominio que representa uma categoria de produtos.
 *
 * <p>Mapeada para a tabela {@code categoria}. O campo {@code atualizadoEm} e gerenciado
 * pela camada de servico e registra o instante da ultima modificacao.</p>
 */
@Entity
@Table(name = "categoria")
@Data
@NoArgsConstructor
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(name = "atualizado_em", nullable = false)
    private LocalDateTime atualizadoEm = LocalDateTime.now();

    /**
     * Construtor de conveniencia para criacao rapida de uma categoria pelo nome.
     *
     * @param nome nome da categoria
     */
    public Categoria(String nome) {
        this.nome = nome;
        this.atualizadoEm = LocalDateTime.now();
    }
}
