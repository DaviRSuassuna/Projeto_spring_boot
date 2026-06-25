package com.senac.projeto.interfaces.web;

import com.senac.projeto.application.usecase.PedidoService;
import com.senac.projeto.application.usecase.ProdutoService;
import com.senac.projeto.application.usecase.UsuarioService;
import com.senac.projeto.domain.model.*;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.beans.factory.annotation.Value;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Controlador web para a area do cliente.
 *
 * <p>Mapeado em {@code /cliente}. Todas as rotas exigem a authority {@code ROLE_USER},
 * conforme definido em {@link SecurityConfig}. O principal autenticado e sempre
 * o e-mail do usuario, injetado via {@code @AuthenticationPrincipal}.</p>
 */
@Controller
@RequestMapping("/cliente")
@RequiredArgsConstructor
public class ClienteController {

    private final ProdutoService produtoService;
    private final PedidoService pedidoService;
    private final UsuarioService usuarioService;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.cookie.secure}")
    private boolean cookieSecure;

    /**
     * Exibe a pagina principal do cliente com os produtos agrupados por categoria.
     *
     * @param email e-mail do usuario autenticado (principal JWT)
     * @param model modelo Thymeleaf
     * @return view {@code cliente/index}
     */
    @GetMapping
    public String index(@AuthenticationPrincipal String email, Model model) {
        List<Produto> produtos = produtoService.listarTodos();

        Map<String, List<Produto>> porCategoria = new LinkedHashMap<>();
        for (Produto p : produtos) {
            String cat = p.getCategoria() != null ? p.getCategoria().getNome() : "Sem Categoria";
            porCategoria.computeIfAbsent(cat, k -> new ArrayList<>()).add(p);
        }

        model.addAttribute("produtosPorCategoria", porCategoria);
        model.addAttribute("modosPagamento", ModoPagamento.values());
        model.addAttribute("nomeUsuario", email);
        return "cliente/index";
    }

    /**
     * Processa a confirmacao de um pedido enviado pelo formulario do cliente.
     *
     * <p>Os itens sao identificados pelos parametros de forma {@code qtd_<produtoId>}.
     * Parametros com quantidade zero ou invalida sao ignorados. O total e calculado
     * pelo controller e o debito de estoque e realizado atomicamente pelo servico.</p>
     *
     * @param email        e-mail do usuario autenticado (principal JWT)
     * @param params       todos os parametros do formulario, incluindo os prefixados com {@code qtd_}
     * @param modoPagamento nome da constante de {@link ModoPagamento}
     * @param ra           atributos flash para mensagem de resultado apos redirecionamento
     * @return redirecionamento para {@code /cliente}
     */
    @PostMapping("/pedido")
    public String confirmarPedido(
            @AuthenticationPrincipal String email,
            @RequestParam Map<String, String> params,
            @RequestParam String modoPagamento,
            RedirectAttributes ra) {

        Usuario usuario = usuarioService.buscarPorEmail(email)
                .orElseThrow();

        List<ItemPedido> itens = new ArrayList<>();
        double total = 0;

        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (!entry.getKey().startsWith("qtd_")) continue;

            int quantidade;
            Long produtoId;
            try {
                quantidade = Integer.parseInt(entry.getValue());
                produtoId = Long.parseLong(entry.getKey().substring(4));
            } catch (NumberFormatException e) {
                continue;
            }
            if (quantidade <= 0) continue;
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

    /**
     * Desativa a conta do usuario autenticado apos confirmacao de senha.
     *
     * <p>Apos a desativacao, o cookie JWT e invalidado (maxAge=0) e o usuario e
     * redirecionado para a pagina de login com o parametro {@code contaDesativada}.</p>
     *
     * @param email    e-mail do usuario autenticado (principal JWT)
     * @param senha    senha atual para confirmacao da operacao
     * @param response resposta HTTP, usada para limpar o cookie JWT
     * @param ra       atributos flash para mensagem de erro em caso de senha incorreta
     * @return redirecionamento para {@code /login?contaDesativada} ou para {@code /cliente} com erro
     */
    @PostMapping("/desativar-conta")
    public String desativarConta(
            @AuthenticationPrincipal String email,
            @RequestParam String senha,
            HttpServletResponse response,
            RedirectAttributes ra) {

        Usuario usuario = usuarioService.buscarPorEmail(email).orElseThrow();

        if (!passwordEncoder.matches(senha, usuario.getSenha())) {
            ra.addFlashAttribute("erro", "Senha incorreta. Conta não foi desativada.");
            return "redirect:/cliente";
        }

        usuarioService.desativar(usuario.getId());

        ResponseCookie cookie = ResponseCookie.from("jwt", "")
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .maxAge(0)
                .sameSite("Strict")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return "redirect:/login?contaDesativada";
    }
}
