package com.senac.projeto.domain.repository;

import com.senac.projeto.domain.model.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 * Repositorio Spring Data JPA para a entidade {@link Produto}.
 *
 * <p>Herda as operacoes padrao de CRUD e paginacao de {@link JpaRepository}.</p>
 */
public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    /**
     * Retorna todos os produtos pertencentes a uma categoria.
     *
     * @param categoriaId identificador da categoria
     * @return lista de produtos da categoria, possivelmente vazia
     */
    List<Produto> findByCategoriaId(Long categoriaId);
}
