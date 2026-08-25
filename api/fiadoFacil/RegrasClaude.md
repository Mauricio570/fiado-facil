# Regras de colaboração — Projeto Fiado Fácil (SafeBook)

## Contexto do projeto
- Sistema web de controle de vendas fiado para pequenos comércios.
- Back-end: Spring Boot 4.1.0, Java 17.
- Banco de dados: PostgreSQL.
- Front-end: Angular (fora do escopo deste back-end).
- Arquitetura de pacotes definida pelo autor:
  ```
  java
  ├── controller
  ├── domain
  ├── dto
  ├── mapper
  ├── repository
  └── service
  ```

## Regras gerais
1. **Não alterar a estrutura de pastas/arquitetura**
2. **Não introduzir vulnerabilidades conhecidas**, por exemplo:
    - Senhas armazenadas em texto puro (usar hashing, ex: BCrypt).
    - Consultas suscetíveis a SQL Injection.
    - Endpoints sem validação de entrada.
    - Exposição de dados sensíveis em DTOs/respostas de API.
3. **Perguntar antes de assumir** qualquer decisão de design (nomes de campo, estratégia de validação, tratamento de erro, regras de negócio não especificadas) — nunca "chutar".
4. **Nada de gambiarra**: sem lógica remendada, sem anotações inconsistentes, sem atalhos que fujam das boas práticas do Spring/Java.
5. Seguir as convenções padrão de Java/Spring:
    - Uma classe pública de nível superior por arquivo, nome do arquivo igual ao nome da classe.
    - Entidades JPA (`@Entity`) sempre como classes de nível superior (nunca como classe interna não-estática).
    - Nomenclatura em PascalCase para classes, camelCase para atributos/métodos.
6.
   - Validação (Bean Validation) nas entidades (@NotBlank, @Email, etc.)


## Decisões já confirmadas no projeto
- Dependências principais (`pom.xml`): `spring-boot-starter-actuator`, `spring-boot-starter-data-jpa`,
  `spring-boot-starter-validation`, `spring-boot-starter-webmvc`, `postgresql`, `lombok`
  (Spring Boot 4 renomeou os starters — ex: `webmvc` no lugar do antigo `web`).

## Pendências em aberto (perguntar antes de prosseguir)
- Hashing de senha (BCrypt): ainda a definir se implementado já na camada de service.
- Ordem de geração das camadas (uma por vez com revisão, ou tudo de uma vez): ainda a definir.