# Assistant IA Whats

Este projeto é um assistente inteligente para gerenciamento de agendas no Google Calendar, utilizando inteligência artificial (Gemini) para interpretar comandos em linguagem natural e executar ações automaticamente.

## Funcionalidades

- **Criação de eventos** no Google Calendar via comandos em linguagem natural.
- **Busca, atualização e cancelamento** de eventos.
- **Cancelamento em massa** de eventos em um período.
- **Respostas claras e personalizadas** ao usuário.
- **Logs detalhados** para observabilidade e troubleshooting.
- **Tratamento global de exceções** com mensagens amigáveis.

## Como funciona

1. O usuário envia um comando (ex: "Agendar reunião amanhã às 15h") para o endpoint `/chat`.
2. O sistema utiliza IA (Gemini) para interpretar o comando e gerar um JSON estruturado.
3. A ação é executada no Google Calendar via API oficial.
4. O usuário recebe uma resposta clara sobre o resultado da operação.

## Estrutura do Projeto

- `controller/` - Endpoints REST.
- `service/` - Lógica de negócio, integração com IA e Google Calendar.
- `client/` - Clientes HTTP para APIs externas.
- `exception/` - Handler global de exceções.
- `util/` - Utilitários para manipulação de dados.
- `dto/` - Objetos de transferência de dados (DTOs).
- `config/` - Configurações do projeto.
- `schedule/` - Agendamento de tarefas automáticas (ex: renovação de token).

## Pré-requisitos

- Java 17+
- Maven 3.8+
- Conta Google com acesso ao Google Calendar API
- Credenciais do Google OAuth2 (Client ID, Client Secret)
- Chave de API do Gemini (Google AI)

## Configuração

1. **Clone o repositório:**
   ```sh
   git clone https://github.com/seu-usuario/assistant-ia-whats.git
   cd assistant-ia-whats
   ```

2. **Configure as variáveis de ambiente:**

   Crie um arquivo `.env` ou defina as variáveis no seu sistema:

   ```
   OPENAI_API_KEY=SuaChaveGemini
   GOOGLE_CALENDAR_ID=SeuCalendarId
   GOOGLE_CLIENT_ID=SeuClientId
   GOOGLE_CLIENT_SECRET=SeuClientSecret
   ```

   Ou edite o arquivo `src/main/resources/application.properties` conforme necessário.

3. **Habilite a API do Google Calendar** no [Google Cloud Console](https://console.cloud.google.com/).

4. **Configure o OAuth2**:
   - Defina o redirect URI como `http://localhost:8080/api/oauth2/callback` no painel do Google.

## Execução Local

1. **Compile o projeto:**
   ```sh
   mvn clean install
   ```

2. **Execute a aplicação:**
   ```sh
   mvn spring-boot:run
   ```

3. **Autentique com o Google:**
   - Acesse o endpoint de autenticação OAuth2 conforme instruções do seu fluxo (normalmente via navegador).
   - Após autenticar, o token será salvo em memória e renovado automaticamente.

   ```txt
   https://accounts.google.com/o/oauth2/v2/auth?client_id={{GOOGLE_CLIENT_ID}}&redirect_uri=http://localhost:8080/api/oauth2/callback&response_type=code&scope=https://www.googleapis.com/auth/calendar&access_type=offline&prompt=consent
   ````

4. **Utilize o endpoint `/chat`:**
   - Faça uma requisição POST para `http://localhost:8080/chat` com o seguinte corpo:
     ```json
     {
       "message": "Agendar reunião amanhã às 15h"
     }
     ```
   - Você receberá uma resposta clara sobre o resultado da operação.

## Testes

Para rodar os testes automatizados:
```sh
mvn test
```

## Contribuição

Contribuições são bem-vindas! Sinta-se à vontade para abrir issues ou pull requests.

## Licença

Este projeto está sob a licença MIT.

---

**Dúvidas?**  
Abra uma issue ou entre em contato!