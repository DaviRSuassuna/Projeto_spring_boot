# Lanchonete — Sistema Web

Aplicação web de lanchonete desenvolvida com **Spring Boot**, **Thymeleaf**, **Spring Security** e autenticação via **JWT**, organizada seguindo os princípios da **Arquitetura Limpa (Clean Architecture)** e **Arquitetura de Cebola (Onion Architecture)**.

## Tecnologias

| Tecnologia | Versão |
|---|---|
| Java | 25 |
| Spring Boot | 4.0.6 |
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

Para parar o container do banco:

```bash
docker compose down
```

Para remover também os dados persistidos:

```bash
docker compose down -v
```

---

## Autenticação

A autenticação é feita via **JWT (JSON Web Token)** armazenado em um cookie `HttpOnly`.

### Fluxo de login

1. Usuário envia e-mail e senha pelo formulário de login (`POST /login`)
2. O `AuthController` verifica as credenciais no banco de dados
3. Se válidas, um token JWT é gerado com o e-mail e a role do usuário (`ROLE_ADMIN` ou `ROLE_USER`)
4. O token é salvo em um **cookie HttpOnly** chamado `jwt` (válido por 24 horas)
5. Em cada requisição seguinte, o `JwtAuthFilter` lê o cookie, valida o token e autentica o usuário automaticamente
6. No logout (`/sair`), o cookie é deletado e o usuário é redirecionado para `/login`

### Arquivos envolvidos

| Arquivo | Responsabilidade |
|---|---|
| `infrastructure/config/JwtUtil.java` | Gera e valida tokens JWT |
| `infrastructure/config/JwtAuthFilter.java` | Intercepta requisições e autentica via cookie |
| `infrastructure/config/SecurityConfig.java` | Define regras de acesso e registra o filtro JWT |
| `interfaces/web/AuthController.java` | Processa login/cadastro e emite o cookie |

> O cookie é `HttpOnly` — não pode ser lido por JavaScript, o que protege contra ataques XSS.

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
│   └── config/          # SecurityConfig, JwtUtil, JwtAuthFilter (autenticação JWT)
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
