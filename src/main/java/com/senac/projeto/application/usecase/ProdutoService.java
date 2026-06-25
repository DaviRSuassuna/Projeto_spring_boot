package com.senac.projeto.application.usecase;

import com.senac.projeto.domain.model.Produto;
import com.senac.projeto.domain.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Servico de aplicacao responsavel pelas operacoes de CRUD de {@link Produto}.
 *
 * <p>Atualiza automaticamente o campo {@code atualizadoEm} em toda persistencia.</p>
 */
@Service
@RequiredArgsConstructor
public class ProdutoService {

    private final ProdutoRepository produtoRepository;

    /** Retorna todos os produtos cadastrados. */
    public List<Produto> listarTodos() {
        return produtoRepository.findAll();
    }

    /**
     * Busca um produto pelo identificador.
     *
     * @param id identificador do produto
     * @return {@link java.util.Optional} contendo o produto, ou vazio se nao encontrado
     */
    public Optional<Produto> buscarPorId(Long id) {
        return produtoRepository.findById(id);
    }

    /**
     * Persiste um novo produto, definindo o instante de criacao.
     *
     * @param produto entidade a ser salva
     * @return entidade persistida com id gerado
     */
    public Produto adicionar(Produto produto) {
        produto.setAtualizadoEm(LocalDateTime.now());
        return produtoRepository.save(produto);
    }

    /**
     * Atualiza um produto existente, registrando o instante da alteracao.
     *
     * @param produto entidade com os dados atualizados
     * @return entidade persistida
     */
    public Produto atualizar(Produto produto) {
        produto.setAtualizadoEm(LocalDateTime.now());
        return produtoRepository.save(produto);
    }

    /**
     * Remove o produto pelo identificador.
     *
     * @param id identificador do produto a ser removido
     */
    public void excluir(Long id) {
        produtoRepository.deleteById(id);
    }
}
