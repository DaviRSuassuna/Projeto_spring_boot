package com.senac.projeto;

import com.senac.projeto.application.usecase.UsuarioService;
import com.senac.projeto.domain.model.Usuario;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class ProjetoApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProjetoApplication.class, args);
	}

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
