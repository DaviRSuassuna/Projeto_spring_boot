package com.senac.projeto.domain.repository;

import com.senac.projeto.domain.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * Repositorio Spring Data JPA para a entidade {@link Usuario}.
 *
 * <p>Herda as operacoes padrao de CRUD e paginacao de {@link JpaRepository}.</p>
 */
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /**
     * Busca um usuario pelo e-mail.
     *
     * @param email endereco de e-mail unico do usuario
     * @return {@link Optional} contendo o usuario, ou vazio se nao encontrado
     */
    Optional<Usuario> findByEmail(String email);
}
