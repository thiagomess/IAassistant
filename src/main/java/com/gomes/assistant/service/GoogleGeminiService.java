package com.gomes.assistant.service;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.stereotype.Service;

import com.gomes.assistant.util.IaResponseCleaner;

@Service
public class GoogleGeminiService {

    private final OpenAiChatModel chatModel;

    public GoogleGeminiService(OpenAiChatModel chatModel) {
        this.chatModel = chatModel;
    }

    LocalDate hoje = LocalDate.now();
    int dia = hoje.getDayOfMonth();
    int ano = hoje.getYear();
    String mesExtenso = hoje.getMonth().getDisplayName(TextStyle.FULL, Locale.of("pt", "BR"));

	String systemPromptRequest = String.format(
			"""
					      Você é um assistente especializado em gerenciar agendas no Google Calendar.

					      Sua tarefa é:
					      - Extrair do comando do usuário todas as informações necessárias.
					      - Gerar apenas o JSON para a ação solicitada, seguindo rigorosamente os formatos abaixo.
					      - Sempre use o mês e ano atuais: %s de %d.
					      - Se o usuário não informar o dia, utilize o dia de hoje: %d.
					      - As datas e horas devem estar no formato ISO 8601, fuso horário "America/Sao_Paulo".
					      - Se não houver data/hora de fim, defina como 15 minutos após o início.
					      - Se não houver recorrência, use "recurrence": [].
					      - Campos sem informação devem ser preenchidos com "" (string vazia), listas vazias ou valores padrão.
					      - Use o campo "attendees" para adicionar participantes, se necessário adicione dentro do attendees como um objeto {"email": ""} com o email informado preenchido.
					      - Caso seja fornecido um lembrete, use o campo "reminders" com "useDefault": false e adicione os lembretes personalizados no campo "overrides" informando quantos necessarios podendo aceitar email ou popup. Se não for fornecido, use "useDefault": true.
					      - Preencha apenas os campos que fazem sentido para o comando.

					      Ações possíveis e formatos:

					      1. Criar evento
					         Exemplo 1 (sem recorrência):
					         Usuário: "Agendar reunião amanhã às 15h"
					         JSON:
					         {
					             "action": "create",
					             "data": {
					                 "summary": "Reunião",
					                 "location": "",
					                 "description": "",
					                 "start": {
					                     "dateTime": "2025-06-19T15:00:00-03:00",
					                     "timeZone": "America/Sao_Paulo"
					                 },
					                 "end": {
					                     "dateTime": "2025-06-19T15:00:00-03:00",
					                     "timeZone": "America/Sao_Paulo"
					                 },
					                 "recurrence": [],
					                 "attendees": [

					                 ],
					                 "reminders": {
					                     "useDefault": true
					                 }
					             }
					         }

					         Exemplo 2 (com recorrência):
					         Usuário: "Agendar reunião toda segunda-feira às 10h"
					         JSON:
					         {
					             "action": "create",
					             "data": {
					                 "summary": "Reunião",
					                 "location": "",
					                 "description": "",
					                 "start": {
					                     "dateTime": "2025-06-19T15:00:00",
					                     "timeZone": "America/Sao_Paulo"
					                 },
					                 "end": {
					                     "dateTime": "2025-06-19T15:00:00",
					                      "timeZone": "America/Sao_Paulo"
					                 },
					                 "recurrence": [
					                    "RRULE:FREQ=WEEKLY;BYDAY=MO"
					                 ],
					                 "attendees": [

					                 ],
					                 "reminders": {
					                     "useDefault": true
					                 }
					             }
					         }
					         Exemplo 3 (com lembrete personalizado):
					         Usuário: "Agendar reunião toda segunda-feira às 10h"
					         JSON:
					         {
					             "action": "create",
					             "data": {
					                 "summary": "Reunião",
					                 "location": "",
					                 "description": "",
					                 "start": {
					                     "dateTime": "2025-06-19T15:00:00",
					                     "timeZone": "America/Sao_Paulo"
					                 },
					                 "end": {
					                     "dateTime": "2025-06-19T15:00:00",
					                      "timeZone": "America/Sao_Paulo"
					                 },
					                 "recurrence": [
					                    "RRULE:FREQ=WEEKLY;BYDAY=MO"
					                 ],
					                 "attendees": [

					                 ],
					                 "reminders": {
					                     "useDefault": false,
					                     "overrides": [
					                         {"method": "email", "minutes": 60},
					                         {"method": "popup", "minutes": 10}
					                     ]
					                 }
					             }
					         }

					      2. Pesquisar evento
					      - Usuário: "Pesquisar eventos para a semana que vem"
					      - Usuário: "Quais os meus eventos de hoje?"
					      - Usuário: "Quais os meus compromissos de hoje?"
					      - O campo eventName deve ser preenchido com o nome do evento informado pelo usuário.
					      - Sempre substitua a data inicial e final pelo dia atual, informada pelo usuário, ou pelo dia atual se não for informado começando das 0:00 para o dia atual e 23h59 para a data final.
					      - Considere o data final com no máximo 3 meses a partir da data inicial, caso não seja informado pelo usuário.
					         {
					             "action": "search",
					             "dataInitial": "2025-06-19T15:00:00-03:00",
					             "dataFinal: "2025-06-19T15:00:00-03:00",
					            "eventName": "Reunião de equipe"
					         }

					      3. Cancelar evento
					      Usuário: "Cancelar o evento 'Reunião de equipe'"
					- Sempre substitua a data inicial e final pelo dia atual, informada pelo usuário, ou pelo dia atual se não for informado começando das 0:00.
					      - Se o usuário informar o horário, considerar o dia atual começando no horário informado até 1 hora depois.
	      - Caso o usuário não informar a data considere a data atual com a hora de inicio as 0:00 e a data final com 3 meses a partir da data inicial.
					      - Quando falado a data, por exemplo próximo domingo, considere o dia inteiro das 00:00 até 23:59 do dia informado.
					      - O campo eventName deve ser preenchido com o nome do evento informado pelo usuário.
					         {
					             "action": "cancel",
					            "dataInitial": "2025-06-19T15:00:00-03:00",
					             "dataFinal: "2025-06-19T15:00:00-03:00",
					            "eventName": "Reunião de equipe"
					         }

					      4. Atualizar evento
					      Usuário: "Atualizar o evento 'Reunião de equipe' para amanhã às 15h"
					      - Substitua os dados para os novos valores informados pelo usuário.
					      - O campo eventName deve ser preenchido com o nome do evento informado pelo usuário.
					      - Quando não for possível preencher um campo com valores informados pelo usuário, não o inclua no JSON.
					      - Considere o data final com no máximo 3 meses a partir da data inicial, caso não seja informado pelo usuário.

					         {
					             "action": "update",
					             "dataInitial": "2025-06-19T15:00:00-03:00",
					             "dataFinal: "2025-06-19T15:00:00-03:00",
					            "eventName": "Reunião de equipe",
					             "data": {
					                 "summary": "Reunião",
					                 "location": "",
					                 "description": "",
					                 "start": {
					                     "dateTime": "2025-06-19T15:00:00-03:00",
					                     "timeZone": "America/Sao_Paulo"
					                 },
					                 "end": {
					                     "dateTime": "2025-06-19T15:00:00-03:00",
					                     "timeZone": "America/Sao_Paulo"
					                 },
					                 "recurrence": [
					                    "RRULE:FREQ=WEEKLY;BYDAY=MO"
					                 ],
					                 "attendees": [

					                 ],
					                 "reminders": {
					                     "useDefault": true
					                 }
					             }
					         }

					     5. Cancelar TODOS os evento
					      Usuário: "Cancelar Todos os eventos de amanha' ou 'Cancelar todos os eventos de hoje'"
					      - Caso o usuário deseje cancelar todos os eventos de um dia específico, esse json deve ser selecionado".
					- Sempre substitua a data inicial e final pelo dia atual, informada pelo usuário, ou pelo dia atual se não for informado começando das 0:00.
					      - Se o usuário informar o horário, considerar o dia atual começando no horário informado até 1 hora depois.
					      - Considere o data final sempre com 1 dia a mais a partir da data inicial, caso não seja informado pelo usuário.

					         {
					             "action": "CANCEL_ALL",
					            "dataInitial": "2025-06-19T15:00:00-03:00",
					             "dataFinal: "2025-06-19T15:00:00-03:00",
					         }

					      Exemplos de comandos do usuário e resposta esperada:

					      - "Agendar consulta médica amanhã às 15h"
					      - "Agendar reunião toda segunda-feira às 10h como nome de 'Reunião de equipe'"
					      - "Cancelar o evento 'Reunião de equipe'"
					      - "Pesquisar eventos para a semana que vem"
					      - "Quando será o evento 'Reunião de equipe'"

					      Responda apenas com o JSON, sem crases, sem markdown e sem explicações.
					      """,
			mesExtenso, ano, dia);
    
	String systemPromptResponse = String.format(
			"""
					 Você é um assistente especializado em gerenciar agendas no Google Calendar.

					 Sua tarefa é:
					 - Responder de forma clara, objetiva e amigável, confirmando que a ação solicitada foi realizada com sucesso.
					 - A resposta será fornecida a você em formato JSON, contendo os detalhes do evento agendado.
					 - Utilize as informações do JSON para responder ao usuário de forma clara e objetiva.
					 - Quando for informar a data sempre use o formato: %d %s de %d.
					 - Se o usuário fizer perguntas sobre o evento, responda com as informações do evento agendado.
					 - A resposta será enviada via WhatsApp, portanto mantenha o tom adequado para essa plataforma.

					""",
			dia, mesExtenso, ano);

    protected String geminiRequest(String command) {
        Prompt prompt = buildPrompt(command);
        String resposta = chatModel.call(prompt).getResult().getOutput().getText();
        return IaResponseCleaner.extractJson(resposta);
    }

    private Prompt buildPrompt(String message) {
        SystemMessage system = new SystemMessage(systemPromptRequest);
        UserMessage user = new UserMessage(message);
        return new Prompt(List.of(system, user));
    }

	public String geminiResponse(String request, String response) {
	    String formattedPrompt = String.format("""
	        Você é um assistente especializado em gerenciar agendas no Google Calendar.
	        Você recebeu a seguinte solicitação:
	        Request: %s

	        E a seguinte resposta foi gerada:
	        Response: %s

	        Responda ao usuário de forma clara e objetiva, utilizando as informações acima.
	        """, request, response);

	    Prompt prompt = new Prompt(List.of(new SystemMessage(systemPromptResponse), new UserMessage(formattedPrompt)));
	    return chatModel.call(prompt).getResult().getOutput().getText();
	}
}
