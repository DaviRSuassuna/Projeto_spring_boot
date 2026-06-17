package com.senac.projeto.application.usecase;

import com.senac.projeto.domain.model.Produto;
import com.senac.projeto.domain.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProdutoService {

    private final ProdutoRepository produtoRepository;

    public List<Produto> listarTodos() {
        return produtoRepository.findAll();
    }

    public Optional<Produto> buscarPorId(Long id) {
        return produtoRepository.findById(id);
    }

    public Produto adicionar(Produto produto) {
        produto.setAtualizadoEm(LocalDateTime.now());
        return produtoRepository.save(produto);
    }

    public Produto atualizar(Produto produto) {
        produto.setAtualizadoEm(LocalDateTime.now());
        return produtoRepository.save(produto);
    }

    public void excluir(Long id) {
        produtoRepository.deleteById(id);
    }
}
