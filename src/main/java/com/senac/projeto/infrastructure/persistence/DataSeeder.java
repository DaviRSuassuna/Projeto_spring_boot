package com.senac.projeto.infrastructure.persistence;

import com.senac.projeto.domain.model.Categoria;
import com.senac.projeto.domain.model.Produto;
import com.senac.projeto.domain.model.Usuario;
import com.senac.projeto.domain.repository.CategoriaRepository;
import com.senac.projeto.domain.repository.ProdutoRepository;
import com.senac.projeto.domain.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final CategoriaRepository categoriaRepository;
    private final ProdutoRepository produtoRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.senha}")
    private String adminSenha;

    @Override
    public void run(String... args) {
        if (usuarioRepository.count() > 0) return;

        Usuario admin = new Usuario();
        admin.setNome("Administrador");
        admin.setEmail(adminEmail);
        admin.setSenha(passwordEncoder.encode(adminSenha));
        admin.setAdmin(true);
        admin.setAtualizadoEm(LocalDateTime.now());
        usuarioRepository.save(admin);

        Usuario cliente = new Usuario();
        cliente.setNome("Cliente Teste");
        cliente.setEmail("cliente@lanchonete.com");
        cliente.setSenha(passwordEncoder.encode("cliente123"));
        cliente.setAdmin(false);
        cliente.setAtualizadoEm(LocalDateTime.now());
        usuarioRepository.save(cliente);

        Categoria lanches = criarCategoria("Lanches");
        Categoria bebidas = criarCategoria("Bebidas");
        Categoria porcoes = criarCategoria("Porções");
        Categoria sobremesas = criarCategoria("Sobremesas");

        List.of(
            criarProduto("X-Burguer", 18.90, 50, lanches),
            criarProduto("X-Bacon", 22.50, 40, lanches),
            criarProduto("X-Salada", 16.00, 45, lanches),
            criarProduto("X-Frango", 19.90, 35, lanches),
            criarProduto("Hot Dog", 12.00, 60, lanches),
            criarProduto("Suco de Laranja", 8.00, 80, bebidas),
            criarProduto("Suco de Maracujá", 8.50, 70, bebidas),
            criarProduto("Refrigerante", 6.00, 100, bebidas),
            criarProduto("Água Mineral", 3.50, 120, bebidas),
            criarProduto("Milk Shake", 14.00, 30, bebidas),
            criarProduto("Batata Frita", 14.00, 55, porcoes),
            criarProduto("Frango Frito", 22.00, 30, porcoes),
            criarProduto("Onion Rings", 16.00, 40, porcoes),
            criarProduto("Nuggets", 18.00, 35, porcoes),
            criarProduto("Pudim", 9.00, 25, sobremesas),
            criarProduto("Sorvete", 10.00, 40, sobremesas),
            criarProduto("Brownie", 12.00, 20, sobremesas)
        ).forEach(produtoRepository::save);
    }

    private Categoria criarCategoria(String nome) {
        Categoria c = new Categoria();
        c.setNome(nome);
        c.setAtualizadoEm(LocalDateTime.now());
        return categoriaRepository.save(c);
    }

    private Produto criarProduto(String nome, double preco, int qtd, Categoria categoria) {
        Produto p = new Produto();
        p.setNome(nome);
        p.setPreco(preco);
        p.setQuantidade(qtd);
        p.setCategoria(categoria);
        p.setAtualizadoEm(LocalDateTime.now());
        return p;
    }
}
