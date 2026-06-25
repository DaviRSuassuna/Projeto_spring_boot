package com.senac.projeto.interfaces.web;

import com.senac.projeto.application.usecase.CategoriaService;
import com.senac.projeto.application.usecase.ProdutoService;
import com.senac.projeto.domain.model.Produto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controlador web para gerenciamento de produtos na area administrativa.
 *
 * <p>Mapeado em {@code /admin/produtos}. Todas as rotas exigem a authority
 * {@code ROLE_ADMIN}, conforme definido em {@link SecurityConfig}.</p>
 */
@Controller
@RequestMapping("/admin/produtos")
@RequiredArgsConstructor
public class ProdutoWebController {

    private final ProdutoService produtoService;
    private final CategoriaService categoriaService;

    /**
     * Lista todos os produtos e categorias disponíveis para o formulario de cadastro.
     *
     * @param model modelo Thymeleaf
     * @return view {@code admin/produtos}
     */
    @GetMapping
    public String listar(Model model) {
        model.addAttribute("produtos", produtoService.listarTodos());
        model.addAttribute("categorias", categoriaService.listarTodas());
        model.addAttribute("produto", new Produto());
        return "admin/produtos";
    }

    /**
     * Adiciona um novo produto ao catalogo.
     *
     * @param nome        nome do produto
     * @param preco       preco unitario
     * @param quantidade  quantidade inicial em estoque
     * @param categoriaId identificador da categoria (opcional)
     * @param ra          atributos flash para mensagem de confirmacao apos redirecionamento
     * @return redirecionamento para {@code /admin/produtos}
     */
    @PostMapping("/adicionar")
    public String adicionar(
            @RequestParam String nome,
            @RequestParam double preco,
            @RequestParam int quantidade,
            @RequestParam(required = false) Long categoriaId,
            RedirectAttributes ra) {

        Produto produto = new Produto();
        produto.setNome(nome);
        produto.setPreco(preco);
        produto.setQuantidade(quantidade);
        if (categoriaId != null) {
            categoriaService.buscarPorId(categoriaId).ifPresent(produto::setCategoria);
        }
        produtoService.adicionar(produto);
        ra.addFlashAttribute("sucesso", "Produto adicionado com sucesso.");
        return "redirect:/admin/produtos";
    }

    /**
     * Atualiza os dados de um produto existente.
     *
     * <p>A categoria e sempre sobrescrita: se {@code categoriaId} for nulo,
     * a categoria do produto e removida.</p>
     *
     * @param id          identificador do produto
     * @param nome        novo nome
     * @param preco       novo preco unitario
     * @param quantidade  nova quantidade em estoque
     * @param categoriaId novo identificador de categoria (opcional)
     * @param ra          atributos flash para mensagem de confirmacao apos redirecionamento
     * @return redirecionamento para {@code /admin/produtos}
     */
    @PostMapping("/atualizar")
    public String atualizar(
            @RequestParam Long id,
            @RequestParam String nome,
            @RequestParam double preco,
            @RequestParam int quantidade,
            @RequestParam(required = false) Long categoriaId,
            RedirectAttributes ra) {

        produtoService.buscarPorId(id).ifPresent(p -> {
            p.setNome(nome);
            p.setPreco(preco);
            p.setQuantidade(quantidade);
            p.setCategoria(null);
            if (categoriaId != null) {
                categoriaService.buscarPorId(categoriaId).ifPresent(p::setCategoria);
            }
            produtoService.atualizar(p);
        });
        ra.addFlashAttribute("sucesso", "Produto atualizado com sucesso.");
        return "redirect:/admin/produtos";
    }

    /**
     * Remove um produto pelo identificador.
     *
     * @param id identificador do produto a ser removido
     * @param ra atributos flash para mensagem de confirmacao apos redirecionamento
     * @return redirecionamento para {@code /admin/produtos}
     */
    @PostMapping("/excluir")
    public String excluir(@RequestParam Long id, RedirectAttributes ra) {
        produtoService.excluir(id);
        ra.addFlashAttribute("sucesso", "Produto excluído com sucesso.");
        return "redirect:/admin/produtos";
    }
}
