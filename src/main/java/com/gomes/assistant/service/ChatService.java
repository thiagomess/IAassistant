package com.gomes.assistant.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.gomes.assistant.client.GreenApiClient;
import com.gomes.assistant.dto.ChatResponse;
import com.gomes.assistant.dto.Event;
import com.gomes.assistant.dto.ResponseWhatsApp;
import com.gomes.assistant.dto.TimeRange;
import com.gomes.assistant.dto.WebhookPayload;
import com.gomes.assistant.enums.ChatAction;
import com.gomes.assistant.util.DataUtils;

@Service
public class ChatService {

    private static final Logger logger = LoggerFactory.getLogger(ChatService.class);

    private static final String EVENT_NOT_FOUND = "Event not found.";
    private static final String EVENT_CANCELED = "Event successfully canceled.";
    private static final String ALL_EVENTS_CANCELED = "All events canceled.";
    private static final String UNKNOWN_ACTION = "Ação não reconhecida. Por favor, tente novamente.";

    private final GoogleGeminiService googleGeminiService;
    private final GoogleCalendarEventService googleCalendarEventService;
    private final GreenApiClient greenApiClient;

    public ChatService(GoogleGeminiService googleGeminiService, GoogleCalendarEventService googleCalendarEventService, GreenApiClient greenApiClient) {
        this.googleGeminiService = googleGeminiService;
        this.googleCalendarEventService = googleCalendarEventService;
        this.greenApiClient = greenApiClient;
    }

    @Async
    public void generateAsync(WebhookPayload webhookPayload) {
        String message = webhookPayload.messageData().textMessageData().textMessage();
        String name = webhookPayload.senderData().chatName();
        String idMessage = webhookPayload.idMessage();
        String sender = webhookPayload.senderData().sender();

        logger.info("Iniciando processamento assíncrono para mensagem: {}", message);

        try {
            String result = execute(message);
            String geminiResponse = googleGeminiService.geminiResponse(message, result, name);
            logger.info("Resposta do Gemini: {}", geminiResponse);
            ResponseWhatsApp responseWhatsApp = new ResponseWhatsApp(sender, geminiResponse, idMessage);
            String response = greenApiClient.sendMessage(responseWhatsApp);
            logger.info("Mensagem enviada com sucesso: {}", response);

        } catch (Exception e) {
            logger.error("Erro ao processar mensagem assíncrona: {}", e.getMessage(), e);
        }
    }

    public String generateSync(String message) throws Exception {
        String response = execute(message);
        String geminiResponseFinal = googleGeminiService.geminiResponse(message, response, null);
        logger.info("Processamento finalizado para mensagem: {}", message);
        return geminiResponseFinal;
    }

    private String execute(String message) throws Exception {
        logger.info("Processando mensagem: {}", message);
        String geminiResponse = googleGeminiService.geminiRequest(message);
        logger.debug("Resposta do Gemini: {}", geminiResponse);
        ChatResponse chatResponse = DataUtils.parse(geminiResponse, ChatResponse.class);
        TimeRange timeRange = DataUtils.extractTimeRange(chatResponse);

        ChatAction action = ChatAction.fromString(chatResponse.action());
        try {
            return switch (action) {
                case CREATE -> googleCalendarEventService.createEvent(DataUtils.convertMapToJsonString(chatResponse.data()));
                case UPDATE -> updateEvent(chatResponse, timeRange);
                case CANCEL -> cancelEvent(chatResponse, timeRange);
                case CANCEL_ALL -> cancelAllEvents(timeRange);
                case SEARCH -> googleCalendarEventService.getEvents(timeRange).toString();
                case QUESTION -> message;
                default -> {
                    logger.warn("Ação não reconhecida: {}", action);
                    yield UNKNOWN_ACTION;
                }
            };
        } catch (Exception e) {
            logger.error("Erro ao executar ação {}: {}", action, e.getMessage(), e);
            throw e;
        }
    }

    private String cancelAllEvents(TimeRange timeRange) throws Exception {
        var events = googleCalendarEventService.getEvents(timeRange);
        logger.info("Cancelando todos os eventos no intervalo: {} - Quantidade: {}", timeRange, events.size());
        for (var event : events) {
            googleCalendarEventService.deleteEvent(event.id());
        }
        return ALL_EVENTS_CANCELED;
    }

    private String cancelEvent(ChatResponse chatResponse, TimeRange timeRange) throws Exception {
        var eventId = findSimilarEventId(chatResponse, timeRange);
        if (eventId != null) {
            logger.info("Cancelando evento: {}", eventId);
            googleCalendarEventService.deleteEvent(eventId);
            return EVENT_CANCELED;
        }
        logger.warn("Evento não encontrado para cancelamento: {}", chatResponse.eventName());
        return EVENT_NOT_FOUND;
    }

    private String updateEvent(ChatResponse chatResponse, TimeRange timeRange) throws Exception {
        var eventId = findSimilarEventId(chatResponse, timeRange);
        if (eventId != null) {
            logger.info("Atualizando evento: {}", eventId);
            return googleCalendarEventService.updateEvent(eventId, DataUtils.convertMapToJsonString(chatResponse.data()));
        }
        logger.warn("Evento não encontrado para atualização: {}", chatResponse.eventName());
        return EVENT_NOT_FOUND;
    }

    private String findSimilarEventId(ChatResponse chatResponse, TimeRange timeRange) throws Exception {
        var events = googleCalendarEventService.getEvents(timeRange);
        return events.stream()
                .filter(event -> DataUtils.isSimilar(event.summary(), chatResponse.eventName()))
                .map(Event::id)
                .findFirst()
                .orElse(null);
    }
}