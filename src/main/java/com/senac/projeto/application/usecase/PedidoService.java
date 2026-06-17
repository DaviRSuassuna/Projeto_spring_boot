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

@Service
@RequiredArgsConstructor
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ProdutoRepository produtoRepository;

    public List<Pedido> listarTodos() {
        return pedidoRepository.findAll();
    }

    public Optional<Pedido> buscarPorId(Long id) {
        return pedidoRepository.findById(id);
    }

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

    public void excluir(Long id) {
        pedidoRepository.deleteById(id);
    }
}
