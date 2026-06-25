package com.senac.projeto.interfaces.web;

import com.senac.projeto.application.usecase.UsuarioService;
import com.senac.projeto.domain.model.Usuario;
import com.senac.projeto.infrastructure.config.JwtUtil;
import com.senac.projeto.infrastructure.security.LoginRateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
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
    private final LoginRateLimiter rateLimiter;

    @org.springframework.beans.factory.annotation.Value("${app.cookie.secure}")
    private boolean cookieSecure;

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
            HttpServletRequest request,
            HttpServletResponse response,
            Model model) {

        String ip = obterIp(request);

        if (rateLimiter.estaBloqueado(ip)) {
            model.addAttribute("erro", "Muitas tentativas. Aguarde alguns minutos e tente novamente.");
            return "login";
        }

        var usuarioOpt = usuarioService.buscarPorEmail(email);

        if (usuarioOpt.isEmpty() || !passwordEncoder.matches(senha, usuarioOpt.get().getSenha())) {
            rateLimiter.registrarFalha(ip);
            model.addAttribute("erro", "Email ou senha incorretos.");
            return "login";
        }

        if (!usuarioOpt.get().isAtivo()) {
            model.addAttribute("erro", "Conta desativada. Entre em contato com o administrador.");
            return "login";
        }

        rateLimiter.resetar(ip);

        Usuario usuario = usuarioOpt.get();
        String role = usuario.isAdmin() ? "ROLE_ADMIN" : "ROLE_USER";
        String token = jwtUtil.gerarToken(usuario.getEmail(), role);

        ResponseCookie cookie = ResponseCookie.from("jwt", token)
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .maxAge(86400)
                .sameSite("Strict")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

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

        if (senha.length() < 8) {
            model.addAttribute("erro", "A senha deve ter pelo menos 8 caracteres.");
            return "cadastro";
        }

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

    private String obterIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
