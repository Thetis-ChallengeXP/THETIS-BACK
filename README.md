**Thetis** é uma solução **full-stack** para investidores que combina:

- 📱 **App mobile React Native** para buscar ativos em tempo real  
- ☁️ **Azure AI (Text Analytics)** + **Gemini** para analisar notícias  
- 🖥️ **Backend Java 17 / Spring Boot 3** que gerencia usuários, carteiras e alertas  
- 💾 **Oracle ou MySQL** para persistência

O sistema interpreta grandes volumes de texto, gera **nota 0-100 + Positivo/Neutro/Negativo**, destaca palavras-chave e entrega um resumo — tudo direto no celular do investidor. 📊  

---

## 🛠️ Principais Tecnologias

| Stack | Descrição |
|-------|-----------|
| **Java 17** ⚙️ | LTS, performance & segurança |
| **Spring Boot 3** 🌱 | Autoconfiguração ágil |
| **Spring Data JPA** 🗄️ | Persistência fluida |
| **OpenAPI / Swagger** 📜 | Documentação viva |
| **React Native** 📱 | App cross-platform |
| **Azure AI – Text Analytics** ☁️ | NLP em escala |
| **Gemini API** 🤖 | LLM para respostas |

---

## 🎨 Figma  
[🔗 Link do Figma (placeholder)]()

---

## 🏗️ Estrutura de Pastas (backend)

```text
thetis/
 ├─ src/main/java/br/com/fiap/thetis/
 │   ├─ config/          ← Configurações Spring
 │   ├─ controller/      ← Camada REST
 │   ├─ dto/             ← Data-Transfer Objects
 │   │   └─ chatbot/
 │   ├─ model/           ← Entidades JPA
 │   ├─ repository/      ← Spring Data
 │   ├─ service/         ← Regras de negócio
 │   └─ ThetisApplication.java
 ├─ src/test/            ← Testes
 ├─ pom.xml
 └─ README.md
```

---

## 🔄 Fluxo Resumido

1. 📝 **Usuário cria conta** → senha criptografada (`BCrypt`)  
2. 🔑 **Login** → recebe painel da carteira  
3. ➕ **Adiciona ativos** (`/api/wallet/add`)  
4. 📰 **Envia notícia** (`/api/news`) → backend chama Azure AI/Gemini  
5. 📈 **Sentimento salvo** em `asset_sentiments`  
6. 🚨 **Alertas** monitoram ativos da carteira  

---

## 🗂️ Diagrama de Classes

```mermaid
erDiagram
    users ||--o{ wallets : has
    users ||--o{ alerts : creates
    wallets ||--o{ wallet_assets : contains
    assets ||--o{ wallet_assets : includes
    assets ||--o{ asset_sentiments : has
    assets ||--o{ asset_news : has
    assets ||--o{ alerts : triggers

    users {
        UUID id
        string username
        string email
        string password_hash
        string phone
        datetime created_at
        datetime modified_at
    }

    wallets {
        UUID id
        UUID user_id
    }

    assets {
        UUID id
        string symbol
        string type
        decimal current_price
        datetime last_updated
    }

    wallet_assets {
        UUID id
        UUID wallet_id
        UUID asset_id
    }

    asset_sentiments {
        UUID id
        UUID asset_id
        string sentiment
        float confidence_score
        datetime analyzed_at
    }

    asset_news {
        UUID id
        UUID asset_id
        string title
        string summary
        string url
        date published_at
    }

    alerts {
        UUID id
        UUID user_id
        UUID asset_id
        string sentiment_type
        float threshold_percentage
        boolean is_active
        datetime triggered_at
    }
```

---

## 🐳 Subindo MySQL com Docker

**Volátil**  
```bash
docker run -d --name mysql --rm   -e MYSQL_ROOT_PASSWORD=root_pwd   -e MYSQL_USER=new_user   -e MYSQL_PASSWORD=my_pwd   -e MYSQL_DATABASE=thetis   -p 3306:3306 mysql:8
```

**Persistente**  
```bash
docker run -d --name mysql   -v mysql_data:/var/lib/mysql   -e MYSQL_ROOT_PASSWORD=root_pwd   -e MYSQL_USER=new_user   -e MYSQL_PASSWORD=my_pwd   -e MYSQL_DATABASE=thetis   -p 3306:3306 mysql:8
```

---

## 🔑 Variáveis de Ambiente

| Variável | Descrição |
|----------|-----------|
| `spring.datasource.url` | JDBC URL |
| `spring.datasource.username` / `password` | Credenciais BD |
| `GEMINI_API_KEY` | Chave da API Gemini |
| `AZURE_AI_KEY` | Chave Azure Text Analytics |

---

## 📜 Swagger – Exemplos

| Endpoint | Payload |
|----------|---------|
| **POST /api/users** | ```json
{{"username":"joaosilva","email":"joao@email.com","phone":"11999999999","cpf":"12345678909","password":"senha123"}}``` |
| **POST /api/users/login** | ```json
{{"usernameOrEmail":"joaosilva","password":"senha123"}}``` |
| **POST /api/wallet/add** | ```json
{{"userId":"1111...","assetId":"aaaa..."}}``` |
| **POST /api/news** | ```json
{{"title":"Petrobras recorde","summary":"A produção...","url":"https://exemplo.com","assetId":"bbbb..."}}``` |

---

## 🧪 Testes

```bash
mvn test
```

---

## ⚡ Scripts Rápidos

| Comando | Descrição |
|---------|-----------|
| `./mvnw spring-boot:run` | 🚀 Sobe o backend |
| `curl -X POST http://localhost:8080/api/chatbot/message -H "Content-Type: application/json" -d '{"message":"Tipos de renda fixa?"}'` | Teste rápido |

---