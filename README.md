# Lanchonete — Sistema Web

Aplicação web de lanchonete desenvolvida com **Spring Boot**, **Thymeleaf**, **Spring Security** e autenticação via **JWT**, organizada seguindo os princípios da **Arquitetura Limpa (Clean Architecture)** e **Arquitetura de Cebola (Onion Architecture)**.

> Projeto acadêmico — configurado para rodar localmente via HTTP sem configurações extras.

## Tecnologias

| Tecnologia | Versão |
|---|---|
| Java | 25 |
| Spring Boot | 4.0.6 |
| Spring Security | (via Spring Boot) |
| Thymeleaf + thymeleaf-extras-springsecurity6 | (via Spring Boot) |
| Spring Data JPA / Hibernate | (via Spring Boot) |
| Spring Boot Validation | (via Spring Boot) |
| MySQL | 8.4 |
| Lombok | — |
| Maven | (wrapper incluso) |
| JJWT | 0.12.6 |

---

## Pré-requisitos

- **Java 25+** instalado
- **Docker** e **Docker Compose** instalados (para o banco de dados)

---

## Como iniciar

### 1. Subir o banco de dados (MySQL via Docker)

```bash
docker compose up -d
```

Isso cria um container MySQL com:
- Banco: `lanchonete_db`
- Usuário: `user` / Senha: `senhauser`
- Porta: `3306`

Aguarde o container ficar saudável antes de iniciar a aplicação:

```bash
docker compose ps
```

### 2. Iniciar a aplicação

**Linux/macOS:**
```bash
./mvnw spring-boot:run
```

**Windows:**
```cmd
mvnw.cmd spring-boot:run
```

A aplicação sobe em: **http://localhost:8080**

---

## Credenciais padrão

Na primeira execução o sistema cria automaticamente os seguintes usuários:

| Tipo | E-mail | Senha | Acesso |
|---|---|---|---|
| Administrador | `admin@lanchonete.com` | `admin123` | `/admin/**` |
| Cliente | `cliente@lanchonete.com` | `cliente123` | `/cliente/**` |

Além disso, o banco é populado com categorias e produtos de exemplo (lanches, bebidas, porções e sobremesas).

---

## Rotas principais

| Rota | Acesso | Descrição |
|---|---|---|
| `/login` | Público | Tela de login |
| `/cadastro` | Público | Cadastro de novo cliente |
| `/admin/produtos` | ADMIN | Gestão de produtos |
| `/admin/**` | ADMIN | Área administrativa |
| `/cliente` | USER | Área do cliente / cardápio |
| `/sair` | Autenticado | Logout |

---

## Parando a aplicação

```bash
docker compose down
```

Para remover também os dados persistidos:

```bash
docker compose down -v
```

---

## Autenticação

A autenticação é feita via **JWT (JSON Web Token)** armazenado em cookie seguro.

### Fluxo de login

1. Usuário envia e-mail e senha pelo formulário (`POST /login`)
2. O `LoginRateLimiter` verifica se o IP está bloqueado por excesso de tentativas (máx. 5 em 10 min)
3. O `AuthController` valida as credenciais no banco de dados
4. Se válidas, um token JWT é gerado com e-mail e role (`ROLE_ADMIN` ou `ROLE_USER`)
5. O token é salvo em um cookie `HttpOnly` + `SameSite=Strict`, válido por 24h
6. Em cada requisição o `JwtAuthFilter` lê o cookie, valida o token e autentica o usuário
7. No logout (`/sair`), o cookie é expirado e o usuário é redirecionado para `/login`

### Proteções implementadas

| Proteção | Implementação |
|---|---|
| Senhas com hash | BCrypt |
| Cookie HttpOnly | JavaScript não consegue ler o token |
| Cookie SameSite=Strict | Previne envio cross-site |
| CSRF token | `CookieCsrfTokenRepository` — Thymeleaf injeta automaticamente via `th:action` |
| Rate limiting no login | 5 tentativas por IP / 10 minutos (`LoginRateLimiter`) |
| Validação de senha | Mínimo 8 caracteres |
| Chave JWT via propriedade | Configurável em `application.properties` ou via env var `JWT_SECRET` |
| Admin protegido | Não pode ser excluído nem desativado |

### Configuração `app.cookie.secure`

O `application.properties` tem `app.cookie.secure=false`, o que permite rodar em **HTTP local** sem problemas. Se um dia o projeto for para HTTPS, basta mudar para `true` (ou definir a env var).

### Arquivos de segurança

| Arquivo | Responsabilidade |
|---|---|
| `infrastructure/config/JwtUtil.java` | Gera e valida tokens JWT |
| `infrastructure/config/JwtAuthFilter.java` | Intercepta requisições e autentica via cookie |
| `infrastructure/config/SecurityConfig.java` | Regras de acesso, CSRF, filtro JWT |
| `infrastructure/security/LoginRateLimiter.java` | Rate limiting em memória por IP |
| `interfaces/web/AuthController.java` | Login/cadastro com emissão de cookie |

---

## Arquitetura

O projeto adota uma estrutura em camadas concêntricas onde **as dependências sempre apontam para dentro** — camadas externas conhecem as internas, mas nunca o contrário.

```
┌─────────────────────────────────────┐
│         interfaces (Web/REST)       │  ← camada mais externa
│  ┌───────────────────────────────┐  │
│  │       infrastructure          │  │
│  │  ┌─────────────────────────┐  │  │
│  │  │      application        │  │  │
│  │  │  ┌───────────────────┐  │  │  │
│  │  │  │      domain       │  │  │  │  ← núcleo
│  │  │  └───────────────────┘  │  │  │
│  │  └─────────────────────────┘  │  │
│  └───────────────────────────────┘  │
└─────────────────────────────────────┘
```

### Estrutura de pacotes

```
src/main/java/com/senac/projeto/
├── domain/
│   ├── model/           # Entidades (Usuario, Produto, Pedido, Categoria, ItemPedido, ModoPagamento)
│   └── repository/      # Interfaces dos repositórios (contratos)
│
├── application/
│   └── usecase/         # Casos de uso (UsuarioService, ProdutoService, PedidoService, CategoriaService)
│
├── infrastructure/
│   ├── persistence/     # DataSeeder (carga inicial de dados)
│   ├── security/        # LoginRateLimiter (rate limiting por IP)
│   └── config/          # SecurityConfig, JwtUtil, JwtAuthFilter
│
└── interfaces/
    ├── rest/            # HomeController
    └── web/             # Controllers Thymeleaf (Auth, Cliente, Admin, Produtos, Pedidos, Categorias)
```

### Fluxo de uma requisição

```
Request HTTP
    └─► interfaces/web       (Controller recebe a requisição)
            └─► application/usecase   (Caso de uso executa a lógica)
                    └─► domain/repository     (Interface do repositório)
                                └─► infrastructure/persistence  (JPA acessa o banco)
```
