package com.senac.projeto.domain.repository;

import com.senac.projeto.domain.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 * Repositorio Spring Data JPA para a entidade {@link Pedido}.
 *
 * <p>Herda as operacoes padrao de CRUD e paginacao de {@link JpaRepository}.</p>
 */
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    /**
     * Retorna todos os pedidos associados a um usuario.
     *
     * @param usuarioId identificador do usuario
     * @return lista de pedidos do usuario, possivelmente vazia
     */
    List<Pedido> findByUsuarioId(Long usuarioId);
}
