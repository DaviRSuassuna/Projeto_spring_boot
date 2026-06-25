package com.senac.projeto.interfaces.web;

import com.senac.projeto.application.usecase.UsuarioService;
import com.senac.projeto.domain.model.Usuario;
import com.senac.projeto.infrastructure.config.JwtUtil;
import com.senac.projeto.infrastructure.security.LoginRateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controlador responsavel pelos fluxos de autenticacao e cadastro de usuarios.
 *
 * <p>Apos autenticacao bem-sucedida, emite um cookie JWT HttpOnly com atributo
 * {@code SameSite=Strict} e redireciona o usuario conforme sua role:
 * administradores vao para {@code /admin/produtos} e clientes para {@code /cliente}.</p>
 */
@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UsuarioService usuarioService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final LoginRateLimiter rateLimiter;

    @Value("${app.cookie.secure}")
    private boolean cookieSecure;

    /**
     * Exibe a pagina de login.
     *
     * @param erro           presente na query string quando ha credenciais invalidas
     * @param contaDesativada presente na query string quando o usuario acabou de desativar a conta
     * @param model          modelo Thymeleaf
     * @return nome da view {@code login}
     */
    @GetMapping("/login")
    public String login(
            @RequestParam(required = false) String erro,
            @RequestParam(required = false) String contaDesativada,
            Model model) {
        if (erro != null) model.addAttribute("erro", "Email ou senha incorretos.");
        if (contaDesativada != null) model.addAttribute("sucesso", "Sua conta foi desativada com sucesso.");
        return "login";
    }

    /**
     * Processa o formulario de login.
     *
     * <p>Verifica bloqueio por rate limiting, valida credenciais, checa se a conta
     * esta ativa e, em caso de sucesso, emite o cookie JWT e redireciona o usuario.</p>
     *
     * @param email    e-mail informado no formulario
     * @param senha    senha informada no formulario
     * @param request  requisicao HTTP, usada para extrair o IP do cliente
     * @param response resposta HTTP, usada para adicionar o cookie JWT
     * @param model    modelo Thymeleaf para mensagens de erro
     * @return redirecionamento para a area do usuario ou retorno a view {@code login} com erro
     */
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

    /**
     * Exibe o formulario de cadastro de novo usuario.
     *
     * @return nome da view {@code cadastro}
     */
    @GetMapping("/cadastro")
    public String cadastroForm() {
        return "cadastro";
    }

    /**
     * Processa o formulario de cadastro de novo usuario.
     *
     * <p>Valida tamanho minimo de senha, confirmacao de senha e unicidade do e-mail
     * antes de criar o usuario. Novos cadastros sao sempre clientes ({@code admin=false}).</p>
     *
     * @param nome           nome completo do usuario
     * @param email          e-mail que sera usado como login
     * @param senha          senha desejada (minimo 8 caracteres)
     * @param confirmarSenha confirmacao da senha
     * @param model          modelo Thymeleaf para mensagens de erro ou sucesso
     * @return view {@code cadastro} com mensagem de resultado
     */
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

    /**
     * Determina o IP real do cliente, considerando possiveis proxies reversos.
     *
     * <p>Utiliza o cabecalho {@code X-Forwarded-For} quando presente, tomando apenas
     * o primeiro endereco da lista (o cliente original). Caso ausente, usa o IP direto
     * da conexao TCP.</p>
     *
     * @param request requisicao HTTP
     * @return endereco IP do cliente
     */
    private String obterIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
