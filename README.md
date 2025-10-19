## Thetis

Plataforma em Spring Boot com:
- Autenticação de usuário e recuperação de senha por e-mail
- Chatbot integrado ao Gemini
- Módulo financeiro de carteiras, posições e negociações com cálculo de P/L

Esta documentação foi revisada e unificada. Siga as instruções abaixo — destaque para a criação do arquivo .env com os dados do banco e da API do Gemini.

---

## Sumário
- Visão geral e stack
- Estrutura do projeto
- Pré-requisitos
- Configuração do ambiente (.env)
- Banco de dados
- Como executar localmente
- Documentação da API (endpoints)
- Tratamento de erros (padrão de resposta)
- Testes automatizados
- Solução de problemas (FAQ)

---

## Visão geral e stack
- Java 17
- Spring Boot 3.4.x (Web, Validation, Security, Data JPA, Actuator, Mail)
- MySQL (produção/dev) e H2 (testes)
- Springdoc OpenAPI (Swagger UI)
- Lombok
- java-dotenv (carrega variáveis do arquivo .env)

---

## Estrutura do projeto

```
THETIS-BACK/
  ├─ src/
  │  ├─ main/
  │  │  ├─ java/br/com/fiap/thetis/
  │  │  │  ├─ config/ (Security, CORS, RestTemplate, GlobalExceptionHandler)
  │  │  │  ├─ controller/ (UserController, ChatBotController, WalletController)
  │  │  │  ├─ dto/ (users, chatbot, wallet)
  │  │  │  ├─ exception/ (BusinessException, NotFoundException, ErrorResponse)
  │  │  │  ├─ model/ (User, PasswordResetToken, Asset, Wallet, Position, Trade, Alert)
  │  │  │  ├─ repository/ (...Repository)
  │  │  │  ├─ service/ (UserService, EmailService, ChatBotService, WalletService)
  │  │  │  └─ service/market/ (QuoteProvider, HttpQuoteProvider)
  │  │  └─ resources/
  │  │     └─ application.properties
  │  └─ test/
  │     └─ java/br/com/fiap/thetis/ (...Tests)
  ├─ pom.xml
  └─ README.md
```

---

## Pré-requisitos
- JDK 17+
- Maven 3.9+
- MySQL 8+ em execução (ou ajuste a URL para seu ambiente)

---

## Configuração do ambiente (.env) [ESSENCIAL]

Crie um arquivo `.env` na raiz do projeto. O projeto usa `java-dotenv` para carregar pares chave/valor e registrá-los como propriedades do sistema antes do Spring Boot subir.

Para funcionar em todos os cenários, recomendamos definir as propriedades com notação de pontos (para Spring) e, quando indicado, as variações em UPPER_CASE esperadas pelo `application.properties`:

```
# Banco de Dados (Spring lerá diretamente estas chaves)
spring.datasource.url=jdbc:mysql://localhost:3306/thetis?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=seu_usuario
spring.datasource.password=sua_senha

# E-mail (SMTP Gmail) — referenciado em application.properties
MAIL_USER=seu-email@gmail.com
MAIL_PASS=sua-senha-de-aplicativo

# Gemini (obrigatório para o chatbot)
GEMINI_API_KEY=coloque_sua_chave_aqui

# Provedor de cotações (opcional; fallback de preço será usado se indisponível)
MARKET_QUOTES_URL=https://minha-api-de-cotacoes/price?symbol=

# (opcional) CORS do frontend
# Ex.: http://localhost:8081 (ajuste se necessário)
```

Notas:
- As chaves `spring.datasource.*` com ponto garantem leitura direta pelo Spring a partir de System properties.
- Para Gmail, utilize 2FA e crie uma “Senha de app”.
- `GEMINI_API_KEY` é necessário para o endpoint do chatbot.
- `MARKET_QUOTES_URL` é opcional; se ausente/indisponível, o serviço usa um preço fictício apenas para desenvolvimento.

Um arquivo de exemplo está disponível: `.env.example`.

---

## Banco de dados
- O Hibernate usa `spring.jpa.hibernate.ddl-auto=update` para evoluir o schema automaticamente.
- Crie o database antes de rodar a aplicação (ou ajuste a URL):

```
CREATE DATABASE thetis CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

---

## Como executar localmente

1) Instale dependências e gere o pacote:

```bash
mvn clean package
```

2) Suba a aplicação:

```bash
mvn spring-boot:run
```

Endpoints úteis:
- Swagger UI: http://localhost:8080/swagger-ui/index.html
- Actuator (parcial): http://localhost:8080/actuator

---

## Documentação da API (endpoints)

### Usuários
- POST `/api/users` — cadastro
  - body: `{ "username": "...", "email": "...", "phone": "...", "cpf": "...", "password": "..." }`
- POST `/api/users/login` — login por username OU e-mail
  - body: `{ "usernameOrEmail": "...", "password": "..." }`
- POST `/api/users/reset/request` — solicita reset de senha
  - body: `{ "email": "..." }` (e-mail com link é enviado)
- POST `/api/users/reset/confirm` — confirma reset
  - body: `{ "token": "uuid", "newPassword": "..." }`

### Chatbot (Gemini)
- POST `/api/chatbot/message`
  - body: `{ "message": "pergunta do usuário" }`
  - requer `GEMINI_API_KEY` válido no `.env`

### Mercado Financeiro
- POST `/api/wallet/{userId}` — cria carteira para o usuário
  - body: `{ "name": "Minha carteira" }`
- GET `/api/wallet/{walletId}` — consulta carteira (posições, valor de mercado, P/L)
- POST `/api/wallet/trade` — executa trade (compra/venda)
  - body: `{ "walletId": "uuid", "symbol": "AAPL", "side": "BUY|SELL", "quantity": 10, "price": 100.00 }`

Observações:
- As cotações são obtidas via `QuoteProvider`. Se o provedor externo falhar, um valor padrão é usado para desenvolvimento.
- A segurança atual permite `/api/wallet/**` sem autenticação (pode ser endurecida conforme a necessidade).

---

## Tratamento de erros

Respostas padronizadas via `GlobalExceptionHandler`:

```
{
  "timestamp": "2025-10-19T21:00:00Z",
  "status": 422,
  "error": "Business Rule",
  "message": "Quantidade para venda excede a posição",
  "path": "/api/wallet/trade",
  "fieldErrors": [
    { "field": "quantity", "message": "must be greater than 0" }
  ]
}
```

Mapeamentos principais:
- 400 — validações/argumentos inválidos
- 404 — não encontrado (`NotFoundException`)
- 422 — regras de negócio (`BusinessException`)
- 500 — erro inesperado

---

## Testes automatizados

- O perfil de testes usa H2 em memória (sem dependência de MySQL).
- Para executar:

```bash
mvn test
```

---

## Solução de problemas (FAQ)

1) Erro de conexão MySQL
- Verifique se o MySQL está rodando e se `spring.datasource.*` no `.env` estão corretos.
- Confirme firewall/porta e se o database `thetis` existe.

2) E-mail não enviado (SMTP)
- Use `MAIL_USER` e `MAIL_PASS` (senha de app do Gmail).
- Logs úteis: `logging.level.org.springframework.mail=DEBUG` (já habilitado).

3) Chatbot falha
- Garanta que `GEMINI_API_KEY` está presente e válido no `.env`.
- Verifique limites de uso e conectividade.

4) Cotações sempre iguais
- Configure `MARKET_QUOTES_URL` para um provedor real.
- Em desenvolvimento, o serviço usa fallback de preço quando a API externa não responde.

---
