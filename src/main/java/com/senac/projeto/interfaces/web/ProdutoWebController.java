package com.senac.projeto.interfaces.web;

import com.senac.projeto.application.usecase.CategoriaService;
import com.senac.projeto.application.usecase.ProdutoService;
import com.senac.projeto.domain.model.Produto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/produtos")
@RequiredArgsConstructor
public class ProdutoWebController {

    private final ProdutoService produtoService;
    private final CategoriaService categoriaService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("produtos", produtoService.listarTodos());
        model.addAttribute("categorias", categoriaService.listarTodas());
        model.addAttribute("produto", new Produto());
        return "admin/produtos";
    }

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

    @PostMapping("/excluir")
    public String excluir(@RequestParam Long id, RedirectAttributes ra) {
        produtoService.excluir(id);
        ra.addFlashAttribute("sucesso", "Produto excluído com sucesso.");
        return "redirect:/admin/produtos";
    }
}
