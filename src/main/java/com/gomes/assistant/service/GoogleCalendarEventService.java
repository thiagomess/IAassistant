package com.gomes.assistant.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.gomes.assistant.client.GoogleCalendarClient;
import com.gomes.assistant.dto.Event;
import com.gomes.assistant.dto.TimeRange;
import com.gomes.assistant.util.DataUtils;

@Service
public class GoogleCalendarEventService {

    private static final Logger logger = LoggerFactory.getLogger(GoogleCalendarEventService.class);

    private final GoogleCalendarClient googleCalendarRestService;
    @Value("${google.api.calendar-id}")
    private String calendarId;

    public GoogleCalendarEventService(GoogleCalendarClient googleCalendarRestService) {
        this.googleCalendarRestService = googleCalendarRestService;
    }

    public String createEvent(String jsonData) throws Exception {
        logger.info("Criando evento no Google Calendar");
        return googleCalendarRestService.createEvent(calendarId, jsonData);
    }

    public String updateEvent(String eventId, String jsonData) throws Exception {
        logger.info("Atualizando evento no Google Calendar: {}", eventId);
        return googleCalendarRestService.updateEvent(calendarId, eventId, jsonData);
    }

    public String deleteEvent(String eventId) throws Exception {
        logger.info("Deletando evento no Google Calendar: {}", eventId);
        return googleCalendarRestService.deleteEvent(calendarId, eventId);
    }

    public List<Event> getEvents(TimeRange timeRange) throws Exception {
        logger.info("Buscando eventos no intervalo: {}", timeRange);
        String response = googleCalendarRestService.getEvents(calendarId, timeRange.start(), timeRange.end());
        String itemsJson = DataUtils.extractField(response, "items");
        return DataUtils.parseList(itemsJson, new TypeReference<List<Event>>() {});
    }
}