# Fiado Fácil (SafeBook) — Contexto do Projeto

> Documento de contexto técnico para dar continuidade ao desenvolvimento com apoio de IA.
> TCC de Análise e Desenvolvimento de Sistemas (IFRS - Campus Rolante).

## 1. O que é o sistema

Aplicação web para digitalizar o controle de **vendas a crédito ("fiado")** em pequenos
comércios (mercearias, açougues, padarias). Substitui o caderno físico de anotações por
uma plataforma segura onde o comerciante cadastra clientes, registra vendas, controla
pagamentos parcelados com juros, e acompanha em tempo real quem está devendo.

**Nome do sistema:** SafeBook / Fiado Fácil (usados como sinônimos)
**Nome do repositório/artifactId:** `fiadoFacil`

## 2. Stack tecnológica

| Camada | Tecnologia |
|---|---|
| Back-end | Spring Boot **4.1.0**, Java **17** |
| Front-end | Angular (CLI recente — standalone components, sem NgModule) |
| Banco de dados | PostgreSQL |
| Autenticação | JWT assinado com par de chaves **RSA** (assimétrico), via Spring Security OAuth2 Resource Server |
| Documentação da API | springdoc-openapi 3.1.0 (Swagger UI em `/swagger-ui/index.html`) |
| ORM | Hibernate / Spring Data JPA |

**groupId Maven:** `br.com` — pacote raiz do back-end: `br.com.fiadoFacil`

## 3. Regras de colaboração (aplicam-se a qualquer IA trabalhando neste projeto)

1. **Não alterar a arquitetura/estrutura de pastas** sem perguntar antes.
2. **Não introduzir vulnerabilidades**: sem senha em texto puro, sem SQL injection, sem
   endpoint sem validação, sem exposição de dado sensível em resposta de API.
3. **Perguntar antes de assumir** qualquer decisão de design não especificada (nome de
   campo, regra de negócio, estratégia de validação) — nunca "chutar" silenciosamente.
4. **Nada de gambiarra**: seguir convenções padrão de Java/Spring/Angular.
5. Sempre que uma decisão de negócio for ambígua (ex: fórmula de juros, regra de
   exclusão), parar e perguntar em vez de presumir.

## 4. Modelo de dados (PostgreSQL)

Entidades e relacionamentos (script completo de criação disponível no projeto,
`schema.sql`):

```
usuario (comerciante/estabelecimento)
  id, nome_empresa, cnpj (UNIQUE), email (UNIQUE), senha (hash BCrypt), data_criacao

cliente (devedor, N:1 com usuario)
  id, fk_usuario, nome, telefone, cpf, endereco, data_criacao
  UNIQUE (fk_usuario, cpf) e UNIQUE (fk_usuario, telefone) — cliente não pode
  duplicar CPF/telefone dentro do mesmo usuário (NULL é permitido múltiplas vezes)

venda (N:1 com cliente)
  id, fk_cliente, status (EM_ABERTO | PAGO), data_criacao

item_venda (N:1 com venda)
  id, fk_venda, nome (texto livre, sem catálogo fixo), quantidade,
  valor_unitario, valor_total (sempre calculado no back-end)

pagamento (1:1 com venda, UNIQUE fk_venda)
  id, fk_venda, forma_pagamento (A_VISTA | CREDITO), quantidade_parcelas,
  juros_mes, valor_entrada

parcela (N:1 com pagamento)
  id, fk_pagamento, numero, valor, data_pagamento, status (EM_ABERTO | PAGO)
```

**ON DELETE:** `usuario→cliente` e `cliente→venda` são `RESTRICT` (protege histórico
financeiro — não dá pra excluir cliente/usuário com vendas). `venda→item_venda`,
`venda→pagamento`, `pagamento→parcela` são `CASCADE`.

**Não existe tabela de produto/catálogo** — o comerciante digita o item livremente a
cada venda (pode itemizar produto a produto ou lançar só o valor total numa linha só).

## 5. Regras de negócio importantes

- **Produto não tem catálogo**: campo `nome` do item é texto livre a cada venda.
- **Pagamento à vista**: `quantidadeParcelas=1`, `jurosMes=0`, `valorEntrada` = total da
  compra, venda já nasce com `status=PAGO`, nenhuma parcela é gerada.
- **Pagamento parcelado (CREDITO)**: usa **juros simples** — `jurosTotal = valorFinanciado
  × (jurosMes/100) × quantidadeParcelas`, onde `valorFinanciado = total dos itens −
  valorEntrada`. O valor final é dividido em parcelas iguais; a **última parcela absorve
  a diferença de arredondamento** (pra soma bater exatamente).
- **Editar/excluir venda**: só é permitido se a venda **não** está `PAGO` **e** nenhuma
  parcela dela já foi paga. Uma vez que há qualquer pagamento (à vista ou uma parcela
  paga), a venda fica travada pra sempre — nunca mais editável/excluível.
- **Editar venda** reabre tudo: itens novos + forma de pagamento nova (usuário escolhe de
  novo), o pagamento/parcelas antigos são apagados e recalculados do zero.
- **Excluir venda** é exclusão real (remove do banco), não é "cancelamento" lógico.
- **Marcar parcela como paga**: seta `dataPagamento = hoje`. Se essa era a última parcela
  em aberto do pagamento, a `venda` muda para `status=PAGO` automaticamente.
- **Painel — "Recebido este mês"** soma DUAS fontes: (a) `valorEntrada` de pagamentos
  cuja venda foi criada no mês (cobre à vista + entrada de parcelado), e (b) valor de
  parcelas com `status=PAGO` e `dataPagamento` no mês. Não dá pra usar só uma das duas,
  senão vendas à vista apareceriam como "R$ 0 recebido".
- **Ownership em tudo**: toda consulta/edição/exclusão confere que o registro pertence
  ao usuário logado (nunca confia em id vindo do front). Quando não encontra ou não
  pertence, retorna sempre **404** (nunca 403), pra não vazar a existência de dados de
  outro usuário.

## 6. Segurança / Autenticação

- Senha armazenada com hash **BCrypt** (nunca texto puro).
- Login gera um **JWT assinado com RSA** (chave privada `authz.pem`, pública `authz.pub`,
  em `src/main/resources/`, referenciadas no `application.yml` via
  `jwt.private.key=classpath:authz.pem` / `jwt.public.key=classpath:authz.pub`).
  A leitura do PEM para `RSAPublicKey`/`RSAPrivateKey` via `@Value` funciona automaticamente
  graças ao `RsaKeyConversionServicePostProcessor` que o `@EnableWebSecurity` registra —
  não precisa de conversor manual.
- **`authz.pem` (chave privada) NUNCA pode ir para o Git** — está no `.gitignore`.
- Token enviado pelo Angular via header `Authorization: Bearer <token>`, **não** por
  cookie (decisão explícita, então CORS não usa `allowCredentials`).
- Endpoints públicos (`permitAll`): `POST /api/usuarios` (cadastro), `POST /api/auth/login`,
  e as rotas do Swagger (`/v3/api-docs/**`, `/swagger-ui/**`). Todo o resto exige token.
- `UsuarioAutenticadoService` extrai o usuário logado a partir da claim `email` do JWT
  (via `SecurityContextHolder`) — é ele que qualquer service usa pra saber "de quem" é o
  dado, nunca confiando em id/email vindo do corpo da requisição.
- CORS restrito à origem do Angular (`http://localhost:4200` em dev).

## 7. Estrutura de pacotes do back-end

```
br.com.fiadoFacil/
  config/       → SecurityConfig, CorsConfig, OpenApiConfig
  controller/   → REST controllers (um por entidade principal)
  domain/       → entidades JPA + enums (StatusVenda, StatusParcela, FormaPagamento)
  dto/
    request/    → DTOs de entrada (nunca expõem campos sensíveis nem aceitam
                  valores calculáveis, ex: valorTotal do item)
    response/   → DTOs de saída
  mapper/       → conversão entidade <-> DTO
  repository/   → Spring Data JPA, com métodos de ownership check
  service/      → regra de negócio
```

Padrão de camadas: `Controller → Service → Repository`, sempre com `Mapper` fazendo a
tradução entidade/DTO. Controllers nunca recebem/retornam entidades JPA diretamente.

## 8. Endpoints da API (todos sob `/api`)

| Método | Rota | Auth | Descrição |
|---|---|---|---|
| POST | `/usuarios` | pública | Cadastro de estabelecimento |
| GET | `/usuarios/{id}` | 🔒 | Busca usuário por id |
| POST | `/auth/login` | pública | Login — retorna JWT |
| POST | `/clientes` | 🔒 | Cadastra cliente |
| GET | `/clientes?busca=` | 🔒 | Lista clientes (busca opcional por nome/telefone) |
| GET | `/clientes/{id}` | 🔒 | Detalhe do cliente |
| PUT | `/clientes/{id}` | 🔒 | Edita cliente |
| DELETE | `/clientes/{id}` | 🔒 | Exclui cliente (bloqueado se tiver venda) |
| POST | `/vendas` | 🔒 | Cria venda (itens + pagamento + parcelas) |
| GET | `/vendas?clienteId=` | 🔒 | Lista vendas (resumo) de um cliente |
| GET | `/vendas/{id}` | 🔒 | Detalhe completo da venda |
| PUT | `/vendas/{id}` | 🔒 | Edita venda (bloqueado se já tem pagamento) |
| DELETE | `/vendas/{id}` | 🔒 | Exclui venda (bloqueado se já tem pagamento) |
| PATCH | `/parcelas/{id}/pagar` | 🔒 | Marca parcela como paga |
| GET | `/painel` | 🔒 | Resumo agregado (total a receber, recebido no mês, clientes em débito, lista de contas em aberto) |

Documentação interativa completa (Swagger UI): `http://localhost:8080/swagger-ui/index.html`

## 9. Front-end (Angular) — status atual

**Ainda em fase inicial.** Decisões já tomadas:

- `ng new fiadoFacil --routing --style=scss --ssr=false`
- Standalone components (padrão atual do Angular, sem NgModule)
- Estrutura de pastas:
  ```
  src/app/
    core/
      guards/
      interceptors/
      models/
      services/
    features/
      home/
      login/
    shared/
  ```
- **Decisão em aberto (ainda não confirmada pelo usuário):** onde ficam os
  models/interfaces TypeScript que espelham os DTOs do back-end — centralizados em
  `core/models`, ou descentralizados (`features/<nome>/models/`, só o realmente
  compartilhado como `Usuario` ficando em `core/models`).
- Ainda **não** criado: `environments/` (Angular CLI recente não gera mais por padrão) —
  será necessário para configurar a URL base da API (`http://localhost:8080/api`).
- Ainda não implementado: nenhuma tela, nenhum service HTTP, nenhum guard/interceptor.

## 10. Wireframes de referência (descrição, arquivos originais não anexados aqui)

Telas mapeadas nos protótipos do usuário: Login, Cadastro de estabelecimento, Home
(painel com cards de totais + lista de clientes em aberto), Clientes (busca, filtros de
status, lista com ver detalhes/nova venda), Perfil do cliente (histórico de compras),
Nova venda (adicionar itens manualmente, escolher forma de pagamento), Detalhes da venda
(itens, parcelas, botão "Efetuando um pagamento" com confirmação).

## 11. O que falta fazer

- [ ] Front-end Angular completo (todas as telas acima)
- [ ] Environments do Angular (URL base da API)
- [ ] HTTP interceptor pra anexar `Authorization: Bearer` automaticamente
- [ ] Guard de rota pra proteger telas que exigem login
- [ ] Documentação Swagger com `@Operation`/descrições em português (opcional, cosmético)
- [ ] Testes automatizados (pasta `test` do Maven ainda vazia)
