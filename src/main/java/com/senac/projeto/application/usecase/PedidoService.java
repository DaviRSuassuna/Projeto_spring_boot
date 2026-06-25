package com.senac.projeto.application.usecase;

import com.senac.projeto.domain.model.ItemPedido;
import com.senac.projeto.domain.model.Pedido;
import com.senac.projeto.domain.model.Produto;
import com.senac.projeto.domain.repository.PedidoRepository;
import com.senac.projeto.domain.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

/**
 * Servico de aplicacao responsavel pela criacao e remocao de pedidos.
 *
 * <p>A criacao de pedidos valida a disponibilidade em estoque e debita as quantidades
 * dos produtos dentro de uma unica transacao, garantindo consistencia.</p>
 */
@Service
@RequiredArgsConstructor
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ProdutoRepository produtoRepository;

    /** Retorna todos os pedidos cadastrados. */
    public List<Pedido> listarTodos() {
        return pedidoRepository.findAll();
    }

    /**
     * Busca um pedido pelo identificador.
     *
     * @param id identificador do pedido
     * @return {@link Optional} contendo o pedido, ou vazio se nao encontrado
     */
    public Optional<Pedido> buscarPorId(Long id) {
        return pedidoRepository.findById(id);
    }

    /**
     * Cria um pedido, validando e debitando o estoque de cada item em uma transacao atomica.
     *
     * @param pedido pedido a ser criado, com a lista de itens preenchida
     * @return pedido persistido
     * @throws IllegalArgumentException se algum produto referenciado nao existir
     * @throws IllegalStateException    se o estoque de qualquer produto for insuficiente
     */
    @Transactional
    public Pedido criar(Pedido pedido) {
        for (ItemPedido item : pedido.getItens()) {
            Produto produto = produtoRepository.findById(item.getProduto().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado"));

            if (produto.getQuantidade() < item.getQuantidade()) {
                throw new IllegalStateException("Estoque insuficiente para: " + produto.getNome());
            }

            produto.setQuantidade(produto.getQuantidade() - item.getQuantidade());
            produtoRepository.save(produto);
            item.setPedido(pedido);
        }

        return pedidoRepository.save(pedido);
    }

    /**
     * Remove o pedido pelo identificador.
     *
     * @param id identificador do pedido a ser removido
     */
    public void excluir(Long id) {
        pedidoRepository.deleteById(id);
    }
}
