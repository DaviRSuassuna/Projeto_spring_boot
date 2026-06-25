package com.senac.projeto;

import com.senac.projeto.application.usecase.UsuarioService;
import com.senac.projeto.domain.model.Usuario;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Ponto de entrada da aplicacao Spring Boot.
 *
 * <p>A auto-configuracao de {@code UserDetailsService} e excluida porque a autenticacao
 * e feita inteiramente via JWT, sem uso do mecanismo padrao de sessao do Spring Security.</p>
 */
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class ProjetoApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProjetoApplication.class, args);
	}

	/**
	 * Garante que o usuario administrador padrao exista e esteja ativo ao iniciar a aplicacao.
	 *
	 * <p>Se o registro ja existir porem estiver com {@code ativo=false} ou {@code admin=false},
	 * os campos sao corrigidos. Caso o registro nao exista, um novo e criado com senha padrao.</p>
	 *
	 * @param usuarioService servico responsavel por persistir e codificar a senha do usuario
	 * @param passwordEncoder encoder BCrypt — recebido por injecao, mas nao utilizado diretamente
	 *                        aqui; o {@link UsuarioService} o aplica internamente
	 * @return runner executado uma unica vez na inicializacao do contexto
	 */
	@Bean
	public CommandLineRunner inicializarAdmin(UsuarioService usuarioService, PasswordEncoder passwordEncoder) {
		return args -> {
			var adminOpt = usuarioService.buscarPorEmail("admin@lanchonete.com");
			if (adminOpt.isPresent()) {
				Usuario admin = adminOpt.get();
				if (!admin.isAtivo() || !admin.isAdmin()) {
					admin.setAtivo(true);
					admin.setAdmin(true);
					usuarioService.atualizar(admin, null);
				}
			} else {
				Usuario admin = new Usuario();
				admin.setNome("Administrador");
				admin.setEmail("admin@lanchonete.com");
				admin.setSenha("admin123");
				admin.setAdmin(true);
				admin.setAtivo(true);
				usuarioService.adicionar(admin);
			}
		};
	}

}
