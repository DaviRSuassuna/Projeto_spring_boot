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

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email}")
    private String adminEmail;

    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    public Optional<Usuario> buscarPorId(Long id) {
        return usuarioRepository.findById(id);
    }

    public Optional<Usuario> buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    public Usuario adicionar(Usuario usuario) {
        usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
        usuario.setAtualizadoEm(LocalDateTime.now());
        return usuarioRepository.save(usuario);
    }

    public Usuario atualizar(Usuario usuario, String novaSenha) {
        if (novaSenha != null && !novaSenha.isBlank()) {
            usuario.setSenha(passwordEncoder.encode(novaSenha));
        }
        usuario.setAtualizadoEm(LocalDateTime.now());
        return usuarioRepository.save(usuario);
    }

    public void desativar(Long id) {
        usuarioRepository.findById(id).ifPresent(u -> {
            if (adminEmail.equals(u.getEmail())) return;
            u.setAtivo(false);
            u.setAtualizadoEm(LocalDateTime.now());
            usuarioRepository.save(u);
        });
    }

    public void ativar(Long id) {
        usuarioRepository.findById(id).ifPresent(u -> {
            u.setAtivo(true);
            u.setAtualizadoEm(LocalDateTime.now());
            usuarioRepository.save(u);
        });
    }

    public boolean excluir(Long id) {
        return usuarioRepository.findById(id).map(u -> {
            if (adminEmail.equals(u.getEmail())) return false;
            usuarioRepository.delete(u);
            return true;
        }).orElse(false);
    }
}
