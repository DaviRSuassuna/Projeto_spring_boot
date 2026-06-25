package com.senac.projeto.interfaces.web;

import com.senac.projeto.application.usecase.UsuarioService;
import com.senac.projeto.domain.model.Usuario;
import com.senac.projeto.infrastructure.config.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UsuarioService usuarioService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/login")
    public String login(
            @RequestParam(required = false) String erro,
            @RequestParam(required = false) String contaDesativada,
            Model model) {
        if (erro != null) model.addAttribute("erro", "Email ou senha incorretos.");
        if (contaDesativada != null) model.addAttribute("sucesso", "Sua conta foi desativada com sucesso.");
        return "login";
    }

    @PostMapping("/login")
    public String processarLogin(
            @RequestParam String email,
            @RequestParam String senha,
            HttpServletResponse response,
            Model model) {

        var usuarioOpt = usuarioService.buscarPorEmail(email);

        if (usuarioOpt.isEmpty() || !passwordEncoder.matches(senha, usuarioOpt.get().getSenha())) {
            model.addAttribute("erro", "Email ou senha incorretos.");
            return "login";
        }

        if (!usuarioOpt.get().isAtivo()) {
            model.addAttribute("erro", "Conta desativada. Entre em contato com o administrador.");
            return "login";
        }

        Usuario usuario = usuarioOpt.get();
        String role = usuario.isAdmin() ? "ROLE_ADMIN" : "ROLE_USER";
        String token = jwtUtil.gerarToken(usuario.getEmail(), role);

        Cookie cookie = new Cookie("jwt", token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(86400); // 24 horas
        response.addCookie(cookie);

        return usuario.isAdmin() ? "redirect:/admin/produtos" : "redirect:/cliente";
    }

    @GetMapping("/cadastro")
    public String cadastroForm() {
        return "cadastro";
    }

    @PostMapping("/cadastro")
    public String cadastro(
            @RequestParam String nome,
            @RequestParam String email,
            @RequestParam String senha,
            @RequestParam String confirmarSenha,
            Model model) {

        if (!senha.equals(confirmarSenha)) {
            model.addAttribute("erro", "As senhas não coincidem.");
            return "cadastro";
        }

        if (usuarioService.buscarPorEmail(email).isPresent()) {
            model.addAttribute("erro", "E-mail já cadastrado.");
            return "cadastro";
        }

        Usuario usuario = new Usuario();
        usuario.setNome(nome);
        usuario.setEmail(email);
        usuario.setSenha(senha);
        usuario.setAdmin(false);
        usuarioService.adicionar(usuario);

        model.addAttribute("sucesso", "Cadastro realizado! Faça login.");
        return "cadastro";
    }
}
