# Lanchonete — Sistema Web

Aplicação web de lanchonete desenvolvida com **Spring Boot**, **Thymeleaf**, **Spring Security** e autenticação via **JWT**, organizada seguindo os princípios da **Arquitetura Limpa (Clean Architecture)** e **Arquitetura de Cebola (Onion Architecture)**.

> Projeto acadêmico — configurado para rodar localmente via HTTP sem configurações extras.

## Tecnologias

| Tecnologia | Versão |
|---|---|
| Java | 25 |
| Spring Boot | 4.1.0 |
| Spring Security | (via Spring Boot) |
| Thymeleaf | (via Spring Boot) |
| Spring Data JPA / Hibernate | (via Spring Boot) |
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

O projeto combina dois padrões arquiteturais complementares: **Clean Architecture** e **Onion Architecture**. A ideia central dos dois é a mesma — **as dependências sempre apontam para dentro**. As camadas externas conhecem as internas, mas nunca o contrário. Isso garante que a lógica de negócio não depende de frameworks, banco de dados ou HTTP.

```
┌─────────────────────────────────────┐
│         interfaces (Web)            │  ← camada mais externa
│  ┌───────────────────────────────┐  │
│  │       infrastructure          │  │
│  │  ┌─────────────────────────┐  │  │
│  │  │      application        │  │  │
│  │  │  ┌───────────────────┐  │  │  │
│  │  │  │      domain       │  │  │  │  ← núcleo (sem dependências externas)
│  │  │  └───────────────────┘  │  │  │
│  │  └─────────────────────────┘  │  │
│  └───────────────────────────────┘  │
└─────────────────────────────────────┘
```

### As quatro camadas

#### 1. `domain` — O núcleo da aplicação

Contém as **entidades** e os **contratos dos repositórios**. Não importa nada de Spring, JPA ou qualquer framework — é Java puro com anotações JPA apenas para mapeamento de dados.

- `model/` — As entidades do negócio: `Usuario`, `Produto`, `Pedido`, `ItemPedido`, `Categoria`, `ModoPagamento`
- `repository/` — Interfaces como `ProdutoRepository` e `UsuarioRepository` que definem *o que* pode ser feito com os dados, sem dizer *como*

> **Exemplo:** `ProdutoRepository` declara `findByCategoriaId(Long)` como contrato. Quem implementa (Spring Data JPA) fica fora dessa camada.

---

#### 2. `application` — A lógica de negócio

Contém os **casos de uso** — classes `@Service` que orquestram as regras da aplicação usando os repositórios do domínio. Só conhece a camada `domain`.

- `usecase/` — `ProdutoService`, `UsuarioService`, `PedidoService`, `CategoriaService`

> **Exemplo:** `ProdutoService.adicionar(produto)` define o timestamp `atualizadoEm` antes de persistir — essa é uma regra de negócio, não uma responsabilidade do controller nem do banco.

---

#### 3. `infrastructure` — Detalhes técnicos externos

Implementa os contratos do domínio e cuida de tudo que é "detalhe de infraestrutura": banco de dados, segurança, configurações.

- `persistence/DataSeeder` — Popula o banco no primeiro boot com usuários, categorias e produtos de exemplo (executa apenas se o banco estiver vazio)
- `security/LoginRateLimiter` — Controle de tentativas de login por IP em memória
- `config/SecurityConfig` — Configura o Spring Security: rotas públicas, restrições por role (`ROLE_ADMIN`, `ROLE_USER`), autenticação stateless
- `config/JwtUtil` — Gera e valida tokens JWT usando a chave configurada em `application.properties`
- `config/JwtAuthFilter` — Filtro que intercepta cada requisição, lê o cookie `jwt`, valida o token e registra o usuário no contexto do Spring Security

---

#### 4. `interfaces` — Ponto de entrada HTTP

Recebe as requisições HTTP, chama os casos de uso da camada `application` e devolve as respostas. Só conhece a camada `application`.

- `web/AuthController` — Login, cadastro e emissão do cookie JWT
- `web/ProdutoWebController` — CRUD de produtos (área admin)
- `web/CategoriaWebController` — CRUD de categorias (área admin)
- `web/PedidoWebController` — Criação e visualização de pedidos
- `web/UsuarioWebController` — Gestão de usuários (área admin)
- `web/ClienteController` — Cardápio e fluxo de compra do cliente
- `web/HomeController` — Redireciona `/` para a área correta conforme o role

---

### Estrutura de pacotes

```
src/main/java/com/senac/projeto/
├── domain/
│   ├── model/           # Entidades: Usuario, Produto, Pedido, ItemPedido, Categoria, ModoPagamento
│   └── repository/      # Contratos: UsuarioRepository, ProdutoRepository, PedidoRepository, CategoriaRepository
│
├── application/
│   └── usecase/         # Regras de negócio: UsuarioService, ProdutoService, PedidoService, CategoriaService
│
├── infrastructure/
│   ├── persistence/     # DataSeeder — carga inicial do banco
│   ├── security/        # LoginRateLimiter — rate limiting por IP
│   └── config/          # SecurityConfig, JwtUtil, JwtAuthFilter
│
└── interfaces/
    └── web/             # Controllers: Auth, Cliente, Admin, Produtos, Pedidos, Categorias, Home
```

### Fluxo de uma requisição

```
Request HTTP
    └─► interfaces/web (Controller)
            │  recebe parâmetros, chama o serviço
            └─► application/usecase (Service)
                    │  aplica regras de negócio, usa o repositório
                    └─► domain/repository (Interface)
                                │  contrato cumprido pelo Spring Data JPA
                                └─► infrastructure (banco MySQL via Hibernate)
```

### Por que essa arquitetura?

| Benefício | Como aparece no projeto |
|---|---|
| Testabilidade | Os serviços de `application` podem ser testados com repositórios falsos, sem subir o banco |
| Isolamento de framework | Trocar Spring Data por outra lib de persistência não afeta `domain` nem `application` |
| Clareza de responsabilidades | Regra de negócio fica no `Service`, não espalhada nos controllers ou entidades |
| Direção única de dependência | `interfaces` → `application` → `domain` ← `infrastructure` (infraestrutura implementa o contrato do domínio) |
