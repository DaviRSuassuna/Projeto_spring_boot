package com.senac.projeto.interfaces.web;

import com.senac.projeto.application.usecase.UsuarioService;
import com.senac.projeto.domain.model.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/usuarios")
@RequiredArgsConstructor
public class UsuarioWebController {

    private final UsuarioService usuarioService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("usuarios", usuarioService.listarTodos());
        return "admin/usuarios";
    }

    @PostMapping("/adicionar")
    public String adicionar(
            @RequestParam String nome,
            @RequestParam String email,
            @RequestParam String senha,
            @RequestParam(defaultValue = "false") boolean admin,
            RedirectAttributes ra) {

        if (usuarioService.buscarPorEmail(email).isPresent()) {
            ra.addFlashAttribute("erro", "E-mail já cadastrado.");
            return "redirect:/admin/usuarios";
        }

        Usuario usuario = new Usuario();
        usuario.setNome(nome);
        usuario.setEmail(email);
        usuario.setSenha(senha);
        usuario.setAdmin(admin);
        usuarioService.adicionar(usuario);
        ra.addFlashAttribute("sucesso", "Usuário adicionado com sucesso.");
        return "redirect:/admin/usuarios";
    }

    @PostMapping("/atualizar")
    public String atualizar(
            @RequestParam Long id,
            @RequestParam String nome,
            @RequestParam String email,
            @RequestParam(required = false) String senha,
            @RequestParam(defaultValue = "false") boolean admin,
            RedirectAttributes ra) {

        usuarioService.buscarPorId(id).ifPresent(u -> {
            u.setNome(nome);
            u.setEmail(email);
            u.setAdmin(admin);
            usuarioService.atualizar(u, senha);
        });
        ra.addFlashAttribute("sucesso", "Usuário atualizado com sucesso.");
        return "redirect:/admin/usuarios";
    }

    @PostMapping("/excluir")
    public String excluir(@RequestParam Long id, RedirectAttributes ra) {
        usuarioService.excluir(id);
        ra.addFlashAttribute("sucesso", "Usuário excluído com sucesso.");
        return "redirect:/admin/usuarios";
    }
}
