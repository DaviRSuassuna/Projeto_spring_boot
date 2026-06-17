# Lanchonete — Sistema Web

Aplicação web de lanchonete desenvolvida com **Spring Boot**, **Thymeleaf** e **Spring Security**, organizada seguindo os princípios da **Arquitetura Limpa (Clean Architecture)** e **Arquitetura de Cebola (Onion Architecture)**.

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
│   └── config/          # SecurityConfig (autenticação e autorização)
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
