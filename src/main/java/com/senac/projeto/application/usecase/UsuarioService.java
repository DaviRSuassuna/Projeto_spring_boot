package com.senac.projeto.application.usecase;

import com.senac.projeto.domain.model.Usuario;
import com.senac.projeto.domain.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Servico de aplicacao responsavel pelo gerenciamento de {@link Usuario}.
 *
 * <p>O usuario administrador principal, identificado pela propriedade {@code app.admin.email},
 * e protegido contra desativacao e exclusao por este servico.</p>
 */
@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email}")
    private String adminEmail;

    /** Retorna todos os usuarios cadastrados. */
    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    /**
     * Busca um usuario pelo identificador.
     *
     * @param id identificador do usuario
     * @return {@link Optional} contendo o usuario, ou vazio se nao encontrado
     */
    public Optional<Usuario> buscarPorId(Long id) {
        return usuarioRepository.findById(id);
    }

    /**
     * Busca um usuario pelo e-mail.
     *
     * @param email endereco de e-mail unico do usuario
     * @return {@link Optional} contendo o usuario, ou vazio se nao encontrado
     */
    public Optional<Usuario> buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    /**
     * Persiste um novo usuario, codificando a senha com BCrypt.
     *
     * @param usuario entidade com a senha em texto plano
     * @return entidade persistida com senha codificada e id gerado
     */
    public Usuario adicionar(Usuario usuario) {
        usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
        usuario.setAtualizadoEm(LocalDateTime.now());
        return usuarioRepository.save(usuario);
    }

    /**
     * Atualiza os dados de um usuario existente.
     *
     * <p>A senha so e alterada se {@code novaSenha} for nao-nulo e nao-vazio;
     * nesse caso e codificada com BCrypt antes de persistir.</p>
     *
     * @param usuario   entidade com os dados atualizados (exceto senha)
     * @param novaSenha nova senha em texto plano, ou {@code null} para manter a atual
     * @return entidade persistida
     */
    public Usuario atualizar(Usuario usuario, String novaSenha) {
        if (novaSenha != null && !novaSenha.isBlank()) {
            usuario.setSenha(passwordEncoder.encode(novaSenha));
        }
        usuario.setAtualizadoEm(LocalDateTime.now());
        return usuarioRepository.save(usuario);
    }

    /**
     * Marca o usuario como inativo ({@code ativo=false}).
     *
     * <p>O usuario administrador principal nao pode ser desativado por este metodo.</p>
     *
     * @param id identificador do usuario
     */
    public void desativar(Long id) {
        usuarioRepository.findById(id).ifPresent(u -> {
            if (adminEmail.equals(u.getEmail())) return;
            u.setAtivo(false);
            u.setAtualizadoEm(LocalDateTime.now());
            usuarioRepository.save(u);
        });
    }

    /**
     * Marca o usuario como ativo ({@code ativo=true}).
     *
     * @param id identificador do usuario
     */
    public void ativar(Long id) {
        usuarioRepository.findById(id).ifPresent(u -> {
            u.setAtivo(true);
            u.setAtualizadoEm(LocalDateTime.now());
            usuarioRepository.save(u);
        });
    }

    /**
     * Remove o usuario permanentemente do banco de dados.
     *
     * <p>O usuario administrador principal nao pode ser excluido.</p>
     *
     * @param id identificador do usuario
     * @return {@code true} se o usuario foi excluido; {@code false} se for o admin protegido
     *         ou se o id nao existir
     */
    public boolean excluir(Long id) {
        return usuarioRepository.findById(id).map(u -> {
            if (adminEmail.equals(u.getEmail())) return false;
            usuarioRepository.delete(u);
            return true;
        }).orElse(false);
    }
}
