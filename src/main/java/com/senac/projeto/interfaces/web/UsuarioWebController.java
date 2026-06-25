package com.senac.projeto.interfaces.web;

import com.senac.projeto.application.usecase.UsuarioService;
import com.senac.projeto.domain.model.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controlador web para gerenciamento de usuarios na area administrativa.
 *
 * <p>Mapeado em {@code /admin/usuarios}. Todas as rotas exigem a authority
 * {@code ROLE_ADMIN}, conforme definido em {@link SecurityConfig}. Operacoes
 * destrutivas sobre o administrador principal sao bloqueadas pelo {@link UsuarioService}.</p>
 */
@Controller
@RequestMapping("/admin/usuarios")
@RequiredArgsConstructor
public class UsuarioWebController {

    private final UsuarioService usuarioService;

    /**
     * Lista todos os usuarios cadastrados.
     *
     * @param model modelo Thymeleaf
     * @return view {@code admin/usuarios}
     */
    @GetMapping
    public String listar(Model model) {
        model.addAttribute("usuarios", usuarioService.listarTodos());
        return "admin/usuarios";
    }

    /**
     * Adiciona um novo usuario ao sistema.
     *
     * <p>Valida tamanho minimo da senha e unicidade do e-mail antes de criar o registro.</p>
     *
     * @param nome  nome completo do usuario
     * @param email e-mail unico do usuario
     * @param senha senha em texto plano (minimo 8 caracteres)
     * @param admin {@code true} para criar como administrador
     * @param ra    atributos flash para mensagem de resultado apos redirecionamento
     * @return redirecionamento para {@code /admin/usuarios}
     */
    @PostMapping("/adicionar")
    public String adicionar(
            @RequestParam String nome,
            @RequestParam String email,
            @RequestParam String senha,
            @RequestParam(defaultValue = "false") boolean admin,
            RedirectAttributes ra) {

        if (senha.length() < 8) {
            ra.addFlashAttribute("erro", "A senha deve ter pelo menos 8 caracteres.");
            return "redirect:/admin/usuarios";
        }

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

    /**
     * Atualiza os dados de um usuario existente.
     *
     * <p>A senha so e alterada se {@code senha} for informada e nao-vazia.
     * Caso informada, deve ter ao menos 8 caracteres.</p>
     *
     * @param id    identificador do usuario
     * @param nome  novo nome completo
     * @param email novo e-mail
     * @param senha nova senha em texto plano, ou {@code null} para manter a atual
     * @param admin novo valor da flag de administrador
     * @param ra    atributos flash para mensagem de resultado apos redirecionamento
     * @return redirecionamento para {@code /admin/usuarios}
     */
    @PostMapping("/atualizar")
    public String atualizar(
            @RequestParam Long id,
            @RequestParam String nome,
            @RequestParam String email,
            @RequestParam(required = false) String senha,
            @RequestParam(defaultValue = "false") boolean admin,
            RedirectAttributes ra) {

        if (senha != null && !senha.isBlank() && senha.length() < 8) {
            ra.addFlashAttribute("erro", "A nova senha deve ter pelo menos 8 caracteres.");
            return "redirect:/admin/usuarios";
        }

        usuarioService.buscarPorId(id).ifPresent(u -> {
            u.setNome(nome);
            u.setEmail(email);
            u.setAdmin(admin);
            usuarioService.atualizar(u, senha);
        });
        ra.addFlashAttribute("sucesso", "Usuário atualizado com sucesso.");
        return "redirect:/admin/usuarios";
    }

    /**
     * Desativa a conta de um usuario sem remove-la do banco.
     *
     * @param id identificador do usuario
     * @param ra atributos flash para mensagem de confirmacao apos redirecionamento
     * @return redirecionamento para {@code /admin/usuarios}
     */
    @PostMapping("/desativar")
    public String desativar(@RequestParam Long id, RedirectAttributes ra) {
        usuarioService.desativar(id);
        ra.addFlashAttribute("sucesso", "Conta do usuário desativada.");
        return "redirect:/admin/usuarios";
    }

    /**
     * Reativa a conta de um usuario previamente desativado.
     *
     * @param id identificador do usuario
     * @param ra atributos flash para mensagem de confirmacao apos redirecionamento
     * @return redirecionamento para {@code /admin/usuarios}
     */
    @PostMapping("/ativar")
    public String ativar(@RequestParam Long id, RedirectAttributes ra) {
        usuarioService.ativar(id);
        ra.addFlashAttribute("sucesso", "Conta do usuário reativada.");
        return "redirect:/admin/usuarios";
    }

    /**
     * Remove permanentemente um usuario do banco de dados.
     *
     * <p>O administrador principal nao pode ser excluido; nesse caso o servico
     * retorna {@code false} e uma mensagem de erro e exibida.</p>
     *
     * @param id identificador do usuario
     * @param ra atributos flash para mensagem de resultado apos redirecionamento
     * @return redirecionamento para {@code /admin/usuarios}
     */
    @PostMapping("/excluir")
    public String excluir(@RequestParam Long id, RedirectAttributes ra) {
        boolean excluido = usuarioService.excluir(id);
        if (excluido) {
            ra.addFlashAttribute("sucesso", "Usuário excluído permanentemente.");
        } else {
            ra.addFlashAttribute("erro", "Este usuário não pode ser excluído.");
        }
        return "redirect:/admin/usuarios";
    }
}
