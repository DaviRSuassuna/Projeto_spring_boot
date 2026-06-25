package com.senac.projeto.interfaces.web;

import com.senac.projeto.application.usecase.PedidoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controlador web para gerenciamento de pedidos na area administrativa.
 *
 * <p>Mapeado em {@code /admin/pedidos}. Todas as rotas exigem a authority
 * {@code ROLE_ADMIN}, conforme definido em {@link SecurityConfig}.</p>
 */
@Controller
@RequestMapping("/admin/pedidos")
@RequiredArgsConstructor
public class PedidoWebController {

    private final PedidoService pedidoService;

    /**
     * Lista todos os pedidos registrados no sistema.
     *
     * @param model modelo Thymeleaf
     * @return view {@code admin/pedidos}
     */
    @GetMapping
    public String listar(Model model) {
        model.addAttribute("pedidos", pedidoService.listarTodos());
        return "admin/pedidos";
    }

    /**
     * Remove um pedido pelo identificador.
     *
     * @param id identificador do pedido a ser removido
     * @param ra atributos flash para mensagem de confirmacao apos redirecionamento
     * @return redirecionamento para {@code /admin/pedidos}
     */
    @PostMapping("/excluir")
    public String excluir(@RequestParam Long id, RedirectAttributes ra) {
        pedidoService.excluir(id);
        ra.addFlashAttribute("sucesso", "Pedido excluído com sucesso.");
        return "redirect:/admin/pedidos";
    }
}
