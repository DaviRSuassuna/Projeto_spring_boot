package com.senac.projeto.interfaces.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controlador da rota raiz da aplicacao.
 *
 * <p>Redireciona acessos a {@code /} para a pagina de login, evitando uma
 * pagina em branco ou erro 404 na raiz do contexto.</p>
 */
@Controller
public class HomeController {

    /**
     * Redireciona a raiz da aplicacao para a pagina de login.
     *
     * @return redirecionamento para {@code /login}
     */
    @GetMapping("/")
    public String home() {
        return "redirect:/login";
    }
}
