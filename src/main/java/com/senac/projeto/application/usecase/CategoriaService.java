package com.senac.projeto.application.usecase;

import com.senac.projeto.domain.model.Categoria;
import com.senac.projeto.domain.repository.CategoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Servico de aplicacao responsavel pelas operacoes de CRUD de {@link Categoria}.
 *
 * <p>Atualiza automaticamente o campo {@code atualizadoEm} em toda persistencia.</p>
 */
@Service
@RequiredArgsConstructor
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    /** Retorna todas as categorias cadastradas. */
    public List<Categoria> listarTodas() {
        return categoriaRepository.findAll();
    }

    /**
     * Busca uma categoria pelo identificador.
     *
     * @param id identificador da categoria
     * @return {@link Optional} contendo a categoria, ou vazio se nao encontrada
     */
    public Optional<Categoria> buscarPorId(Long id) {
        return categoriaRepository.findById(id);
    }

    /**
     * Persiste uma nova categoria, definindo o instante de criacao.
     *
     * @param categoria entidade a ser salva
     * @return entidade persistida com id gerado
     */
    public Categoria adicionar(Categoria categoria) {
        categoria.setAtualizadoEm(LocalDateTime.now());
        return categoriaRepository.save(categoria);
    }

    /**
     * Atualiza uma categoria existente, registrando o instante da alteracao.
     *
     * @param categoria entidade com os dados atualizados
     * @return entidade persistida
     */
    public Categoria atualizar(Categoria categoria) {
        categoria.setAtualizadoEm(LocalDateTime.now());
        return categoriaRepository.save(categoria);
    }

    /**
     * Remove a categoria pelo identificador.
     *
     * @param id identificador da categoria a ser removida
     */
    public void excluir(Long id) {
        categoriaRepository.deleteById(id);
    }
}
