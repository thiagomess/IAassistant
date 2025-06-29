# Assistant IA Whats

Este projeto é um assistente inteligente para gerenciamento de agendas no Google Calendar, utilizando inteligência artificial (Gemini) para interpretar comandos em linguagem natural e executar ações automaticamente.

## Funcionalidades

- **Criação de eventos** no Google Calendar via comandos em linguagem natural.
- **Busca, atualização e cancelamento** de eventos.
- **Cancelamento em massa** de eventos em um período.
- **Integração com WhatsApp** via webhook para receber e responder mensagens automaticamente.
- **Processamento assíncrono** de mensagens para melhor performance.
- **Respostas personalizadas** baseadas no nome do usuário e contexto da conversa.
- **Suporte a perguntas gerais** além do gerenciamento de agenda.
- **Respostas claras e personalizadas** ao usuário.
- **Autenticação JWT** para proteger os endpoints da API.
- **Login seguro** com validação de credenciais e tratamento de erros amigável.
- **Fluxo OAuth2** para integração segura com o Google Calendar.
- **Renovação automática do token OAuth2** via agendamento.
- **Tratamento global de exceções** com respostas JSON padronizadas.
- **Logs detalhados** para observabilidade e troubleshooting.

## Como funciona

1. O usuário envia um comando (ex: "Agendar reunião amanhã às 15h") para o endpoint `/chat` ou via WhatsApp.
2. O sistema utiliza IA (Gemini) para interpretar o comando e gerar um JSON estruturado.
3. A ação é executada no Google Calendar via API oficial.
4. O usuário recebe uma resposta clara sobre o resultado da operação.
5. O acesso à API é protegido por autenticação JWT, obtida via login no endpoint `/auth/login`.
6. **Integração WhatsApp**: Mensagens recebidas via webhook são processadas assincronamente e as respostas são enviadas automaticamente.

## Estrutura do Projeto

- `controller/` - Endpoints REST (incluindo autenticação e OAuth2).
- `service/` - Lógica de negócio, integração com IA e Google Calendar.
- `client/` - Clientes HTTP para APIs externas.
- `exception/` - Handler global de exceções.
- `util/` - Utilitários para manipulação de dados.
- `dto/` - Objetos de transferência de dados (DTOs).
- `config/` - Configurações do projeto.
- `schedule/` - Agendamento de tarefas automáticas (ex: renovação de token).
- `security/` - Configuração de autenticação, filtros JWT e utilitários de segurança.

## Pré-requisitos

- Java 17+
- Maven 3.8+
- Conta Google com acesso ao Google Calendar API
- Credenciais do Google OAuth2 (Client ID, Client Secret)
- Chave de API do Gemini (Google AI)
- **Conta Green API** (para integração com WhatsApp) - opcional

## Configuração

1. **Clone o repositório:**
   ```sh
   git clone https://github.com/seu-usuario/assistant-ia-whats.git
   cd assistant-ia-whats
   ```

2. **Configure as variáveis de ambiente:**

   Crie um arquivo `.env` ou defina as variáveis no seu sistema:   ```
   OPENAI_API_KEY=SuaChaveGemini
   GOOGLE_CALENDAR_ID=SeuCalendarId
   GOOGLE_CLIENT_ID=SeuClientId
   GOOGLE_CLIENT_SECRET=SeuClientSecret
   APP_AUTH_USERNAME=SeuUsuario
   APP_AUTH_PASSWORD=SenhaCriptografadaBCrypt
   JWT_SECRET=ChaveJWTSecreta
   GREENAPI_INSTANCE_ID=SeuInstanceId
   GREENAPI_TOKEN=SeuToken
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
   ```

4. **Realize login para obter o JWT:**
   - Faça uma requisição POST para `http://localhost:8080/auth/login` com o seguinte corpo:
     ```json
     {
       "username": "SeuUsuario",
       "password": "SuaSenha"
     }
     ```
   - O token JWT será retornado e deve ser usado no header `Authorization` das próximas requisições.

5. **Utilize o endpoint `/chat`:**
   - Faça uma requisição POST para `http://localhost:8080/chat` com o seguinte corpo:     ```json
     {
       "message": "Agendar reunião amanhã às 15h"
     }
     ```
   - Inclua o header `Authorization: Bearer <seu_token_jwt>`.
   - Você receberá uma resposta clara sobre o resultado da operação.

## Integração WhatsApp

O sistema possui integração completa com WhatsApp através da **[Green API](https://green-api.com/en)**, permitindo:

- **Recebimento automático** de mensagens via webhook
- **Processamento assíncrono** para melhor performance
- **Respostas personalizadas** com o nome do usuário
- **Suporte a todos os comandos** de gerenciamento de agenda

### Configuração do Webhook WhatsApp

1. **Configure o webhook** na Green API apontando para:
   ```
   https://seu-dominio.com/chat/webhook
   ```

2. **Mensagens são processadas automaticamente** e as respostas enviadas diretamente no WhatsApp

3. **Comandos suportados via WhatsApp:**
   - "Agendar reunião amanhã às 15h"
   - "Quais meus compromissos de hoje?"
   - "Cancelar evento Reunião de equipe"
   - "Cancelar todos os eventos de amanhã"
   - Perguntas gerais e saudações

## Tratamento de Erros

Todas as respostas de erro seguem o padrão:
```json
{
  "error": "CÓDIGO_DO_ERRO",
  "message": "Mensagem amigável explicando o problema."
}
```
Exemplos de erros tratados:
- Credenciais inválidas
- Usuário não encontrado
- Dados inválidos na requisição
- Erros de integração com serviços externos
- Operações não suportadas

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