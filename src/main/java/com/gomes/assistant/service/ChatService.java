package com.gomes.assistant.service;

import org.springframework.stereotype.Service;

import com.gomes.assistant.dto.ChatResponse;
import com.gomes.assistant.dto.Event;
import com.gomes.assistant.dto.TimeRange;
import com.gomes.assistant.enums.ChatAction;
import com.gomes.assistant.util.DataUtils;

@Service
public class ChatService {

    private final GoogleGeminiService googleGeminiService;
    private final GoogleCalendarEventService googleCalendarEventService;

    public ChatService(GoogleGeminiService googleGeminiService, GoogleCalendarEventService googleCalendarEventService) {
        this.googleGeminiService = googleGeminiService;
        this.googleCalendarEventService = googleCalendarEventService;
    }

    public String generate(String message) throws Exception {
        String geminiResponse = googleGeminiService.geminiRequest(message);
        ChatResponse chatResponse = DataUtils.parse(geminiResponse, ChatResponse.class);
        TimeRange timeRange = DataUtils.extractTimeRange(chatResponse);

        ChatAction action = ChatAction.fromString(chatResponse.action());
        return switch (action) {
            case CREATE -> googleCalendarEventService.createEvent(DataUtils.convertMapToJsonString(chatResponse.data()));
            case UPDATE -> updateEvent(chatResponse, timeRange);
            case CANCEL -> cancelEvent(chatResponse, timeRange);
            case CANCEL_ALL -> cancelAllEvents(chatResponse, timeRange);
            case SEARCH -> googleCalendarEventService.getEvents(timeRange).toString();
        };
    }

    private String cancelAllEvents(ChatResponse chatResponse, TimeRange timeRange) throws Exception {
        var events = googleCalendarEventService.getEvents(timeRange);
        for (var event : events) {
            googleCalendarEventService.deleteEvent(event.id());
        }
        return "All events canceled.";
    }

    private String cancelEvent(ChatResponse chatResponse, TimeRange timeRange) throws Exception {
        var events = googleCalendarEventService.getEvents(timeRange);
        var eventId = events.stream()
                .filter(event -> event.summary().equalsIgnoreCase(chatResponse.eventName()))
                .map(Event::id)
                .findFirst()
                .orElse(null);
        return eventId != null ? googleCalendarEventService.deleteEvent(eventId) : "Event not found.";
    }

    private String updateEvent(ChatResponse chatResponse, TimeRange timeRange) throws Exception {
        var events = googleCalendarEventService.getEvents(timeRange);
        var eventId = events.stream()
                .filter(event -> event.summary().equalsIgnoreCase(chatResponse.eventName()))
                .map(Event::id)
                .findFirst()
                .orElse(null);
        return eventId != null ? googleCalendarEventService.updateEvent(eventId, DataUtils.convertMapToJsonString(chatResponse.data())) : "Event not found.";
    }
}