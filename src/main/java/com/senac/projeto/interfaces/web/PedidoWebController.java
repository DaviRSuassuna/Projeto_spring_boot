package com.senac.projeto.interfaces.web;

import com.senac.projeto.application.usecase.PedidoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/pedidos")
@RequiredArgsConstructor
public class PedidoWebController {

    private final PedidoService pedidoService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("pedidos", pedidoService.listarTodos());
        return "admin/pedidos";
    }

    @PostMapping("/excluir")
    public String excluir(@RequestParam Long id, RedirectAttributes ra) {
        pedidoService.excluir(id);
        ra.addFlashAttribute("sucesso", "Pedido excluído com sucesso.");
        return "redirect:/admin/pedidos";
    }
}
