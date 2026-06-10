# Projeto Spring Boot

API REST desenvolvida com Spring Boot, organizada seguindo os princípios da **Arquitetura Limpa (Clean Architecture)** e **Arquitetura de Cebola (Onion Architecture)**.

## Arquitetura

O projeto adota uma estrutura em camadas concêntricas, onde **as dependências sempre apontam para dentro** — camadas externas conhecem as internas, mas nunca o contrário. Isso garante que as regras de negócio sejam independentes de frameworks, banco de dados ou qualquer detalhe de infraestrutura.

```
┌─────────────────────────────────────┐
│         interfaces (REST)           │  ← camada mais externa
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

## Estrutura de Pacotes

```
src/main/java/com/senac/projeto/
├── domain/
│   ├── model/           # Entidades da aplicação (Java puro, sem anotações de framework)
│   └── repository/      # Interfaces dos repositórios (contratos, sem implementação)
│
├── application/
│   └── usecase/         # Casos de uso — regras e fluxos da aplicação
│
├── infrastructure/
│   ├── persistence/     # Implementações dos repositórios (JPA/banco de dados)
│   └── config/          # Configurações e beans do Spring
│
└── interfaces/
    └── rest/            # Controllers HTTP — entrada e saída da API
```

## Responsabilidade de cada camada

### `domain`
Núcleo da aplicação. Contém as entidades e as interfaces dos repositórios. **Não depende de nada** — nem do Spring, nem do banco de dados. É a camada mais estável e protegida.

### `application`
Orquestra os casos de uso utilizando as entidades e repositórios do domínio. Contém a lógica de negócio da aplicação (ex: criar um usuário, processar um pedido). Depende apenas do `domain`.

### `infrastructure`
Implementa os detalhes técnicos: acesso ao banco de dados (JPA), integrações externas e configurações do Spring. Depende do `domain` e do `application`.

### `interfaces`
Ponto de entrada da aplicação. Os controllers recebem as requisições HTTP, delegam para os casos de uso e retornam as respostas. Depende do `application`.

## Fluxo de uma funcionalidade

```
Request HTTP
    └─► interfaces/rest       (Controller recebe a requisição)
            └─► application/usecase   (Caso de uso executa a lógica)
                    └─► domain/repository     (Interface do repositório)
                                └─► infrastructure/persistence  (Implementação acessa o banco)
```

## Tecnologias

- Java
- Spring Boot
- Maven
