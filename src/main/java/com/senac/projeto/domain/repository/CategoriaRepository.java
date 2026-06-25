package com.senac.projeto.domain.repository;

import com.senac.projeto.domain.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio Spring Data JPA para a entidade {@link Categoria}.
 *
 * <p>Herda as operacoes padrao de CRUD e paginacao de {@link JpaRepository}.</p>
 */
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
}
