package com.senac.projeto.interfaces.web;

import com.senac.projeto.application.usecase.PedidoService;
import com.senac.projeto.application.usecase.ProdutoService;
import com.senac.projeto.application.usecase.UsuarioService;
import com.senac.projeto.domain.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.*;

@Controller
@RequestMapping("/cliente")
@RequiredArgsConstructor
public class ClienteController {

    private final ProdutoService produtoService;
    private final PedidoService pedidoService;
    private final UsuarioService usuarioService;

    @GetMapping
    public String index(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        List<Produto> produtos = produtoService.listarTodos();

        Map<String, List<Produto>> porCategoria = new LinkedHashMap<>();
        for (Produto p : produtos) {
            String cat = p.getCategoria() != null ? p.getCategoria().getNome() : "Sem Categoria";
            porCategoria.computeIfAbsent(cat, k -> new ArrayList<>()).add(p);
        }

        model.addAttribute("produtosPorCategoria", porCategoria);
        model.addAttribute("modosPagamento", ModoPagamento.values());
        model.addAttribute("nomeUsuario", userDetails.getUsername());
        return "cliente/index";
    }

    @PostMapping("/pedido")
    public String confirmarPedido(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam Map<String, String> params,
            @RequestParam String modoPagamento,
            RedirectAttributes ra) {

        Usuario usuario = usuarioService.buscarPorEmail(userDetails.getUsername())
                .orElseThrow();

        List<ItemPedido> itens = new ArrayList<>();
        double total = 0;

        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (!entry.getKey().startsWith("qtd_")) continue;
            int quantidade = Integer.parseInt(entry.getValue());
            if (quantidade <= 0) continue;

            Long produtoId = Long.parseLong(entry.getKey().substring(4));
            Produto produto = produtoService.buscarPorId(produtoId).orElse(null);
            if (produto == null) continue;

            double precoLinha = produto.getPreco() * quantidade;
            total += precoLinha;

            ItemPedido item = new ItemPedido();
            item.setProduto(produto);
            item.setQuantidade(quantidade);
            item.setPreco(precoLinha);
            itens.add(item);
        }

        if (itens.isEmpty()) {
            ra.addFlashAttribute("erro", "Adicione ao menos um produto ao carrinho.");
            return "redirect:/cliente";
        }

        Pedido pedido = new Pedido();
        pedido.setUsuario(usuario);
        pedido.setTotal(total);
        pedido.setModoPagamento(ModoPagamento.valueOf(modoPagamento));
        pedido.setItens(itens);
        pedido.setAtualizadoEm(LocalDateTime.now());

        try {
            pedidoService.criar(pedido);
            ra.addFlashAttribute("sucesso", "Pedido realizado com sucesso!");
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("erro", e.getMessage());
        }

        return "redirect:/cliente";
    }
}
