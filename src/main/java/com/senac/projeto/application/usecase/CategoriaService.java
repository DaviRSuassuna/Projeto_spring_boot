package com.senac.projeto.application.usecase;

import com.senac.projeto.domain.model.Categoria;
import com.senac.projeto.domain.repository.CategoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    public List<Categoria> listarTodas() {
        return categoriaRepository.findAll();
    }

    public Optional<Categoria> buscarPorId(Long id) {
        return categoriaRepository.findById(id);
    }

    public Categoria adicionar(Categoria categoria) {
        categoria.setAtualizadoEm(LocalDateTime.now());
        return categoriaRepository.save(categoria);
    }

    public Categoria atualizar(Categoria categoria) {
        categoria.setAtualizadoEm(LocalDateTime.now());
        return categoriaRepository.save(categoria);
    }

    public void excluir(Long id) {
        categoriaRepository.deleteById(id);
    }
}
