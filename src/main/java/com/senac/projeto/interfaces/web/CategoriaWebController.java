package com.senac.projeto.interfaces.web;

import com.senac.projeto.application.usecase.CategoriaService;
import com.senac.projeto.domain.model.Categoria;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/categorias")
@RequiredArgsConstructor
public class CategoriaWebController {

    private final CategoriaService categoriaService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("categorias", categoriaService.listarTodas());
        return "admin/categorias";
    }

    @PostMapping("/adicionar")
    public String adicionar(@RequestParam String nome, RedirectAttributes ra) {
        Categoria categoria = new Categoria();
        categoria.setNome(nome);
        categoriaService.adicionar(categoria);
        ra.addFlashAttribute("sucesso", "Categoria adicionada com sucesso.");
        return "redirect:/admin/categorias";
    }

    @PostMapping("/atualizar")
    public String atualizar(@RequestParam Long id, @RequestParam String nome, RedirectAttributes ra) {
        categoriaService.buscarPorId(id).ifPresent(c -> {
            c.setNome(nome);
            categoriaService.atualizar(c);
        });
        ra.addFlashAttribute("sucesso", "Categoria atualizada com sucesso.");
        return "redirect:/admin/categorias";
    }

    @PostMapping("/excluir")
    public String excluir(@RequestParam Long id, RedirectAttributes ra) {
        categoriaService.excluir(id);
        ra.addFlashAttribute("sucesso", "Categoria excluída com sucesso.");
        return "redirect:/admin/categorias";
    }
}
