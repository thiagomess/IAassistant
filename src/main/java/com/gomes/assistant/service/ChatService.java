package com.gomes.assistant.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.gomes.assistant.dto.ChatResponse;
import com.gomes.assistant.dto.Event;
import com.gomes.assistant.dto.TimeRange;
import com.gomes.assistant.enums.ChatAction;
import com.gomes.assistant.util.DataUtils;

@Service
public class ChatService {

    private static final Logger logger = LoggerFactory.getLogger(ChatService.class);

    private final GoogleGeminiService googleGeminiService;
    private final GoogleCalendarEventService googleCalendarEventService;

    public ChatService(GoogleGeminiService googleGeminiService, GoogleCalendarEventService googleCalendarEventService) {
        this.googleGeminiService = googleGeminiService;
        this.googleCalendarEventService = googleCalendarEventService;
    }

    public String generate(String message) throws Exception {
        logger.info("Processando mensagem: {}", message);
        String geminiResponse = googleGeminiService.geminiRequest(message);
        logger.debug("Resposta do Gemini: {}", geminiResponse);
        ChatResponse chatResponse = DataUtils.parse(geminiResponse, ChatResponse.class);
        TimeRange timeRange = DataUtils.extractTimeRange(chatResponse);

        ChatAction action = ChatAction.fromString(chatResponse.action());
        String response;
        try {
            response = switch (action) {
                case CREATE -> googleCalendarEventService.createEvent(DataUtils.convertMapToJsonString(chatResponse.data()));
                case UPDATE -> updateEvent(chatResponse, timeRange);
                case CANCEL -> cancelEvent(chatResponse, timeRange);
                case CANCEL_ALL -> cancelAllEvents(chatResponse, timeRange);
                case SEARCH -> googleCalendarEventService.getEvents(timeRange).toString();
                default -> {
					logger.warn("Ação não reconhecida: {}", action);
					yield "Ação não reconhecida. Por favor, tente novamente.";
				}
            };
        } catch (Exception e) {
            logger.error("Erro ao executar ação {}: {}", action, e.getMessage(), e);
            throw e;
        }

        String geminiResponseFinal = googleGeminiService.geminiResponse(message, response);
        logger.info("Processamento finalizado para mensagem: {}", message);

        return geminiResponseFinal;
    }

    private String cancelAllEvents(ChatResponse chatResponse, TimeRange timeRange) throws Exception {
        var events = googleCalendarEventService.getEvents(timeRange);
        logger.info("Cancelando todos os eventos no intervalo: {} - Quantidade: {}", timeRange, events.size());
        for (var event : events) {
            googleCalendarEventService.deleteEvent(event.id());
        }
        return "All events canceled.";
    }

    private String cancelEvent(ChatResponse chatResponse, TimeRange timeRange) throws Exception {
        var events = googleCalendarEventService.getEvents(timeRange);
        var eventId = events.stream()
                .filter(event -> DataUtils.isSimilar(event.summary(), chatResponse.eventName()))
                .map(Event::id)
                .findFirst()
                .orElse(null);
        if (eventId != null) {
            logger.info("Cancelando evento: {}", eventId);
            googleCalendarEventService.deleteEvent(eventId);
            return "Event successfully canceled.";
        }
        logger.warn("Evento não encontrado para cancelamento: {}", chatResponse.eventName());
        return "Event not found.";
    }

    private String updateEvent(ChatResponse chatResponse, TimeRange timeRange) throws Exception {
        var events = googleCalendarEventService.getEvents(timeRange);
        var eventId = events.stream()
                .filter(event -> DataUtils.isSimilar(event.summary(), chatResponse.eventName()))
                .map(Event::id)
                .findFirst()
                .orElse(null);
        if (eventId != null) {
            logger.info("Atualizando evento: {}", eventId);
            return googleCalendarEventService.updateEvent(eventId, DataUtils.convertMapToJsonString(chatResponse.data()));
        }
        logger.warn("Evento não encontrado para atualização: {}", chatResponse.eventName());
        return "Event not found.";
    }
}