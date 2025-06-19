package com.gomes.assistant.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.gomes.assistant.dto.Event;
import com.gomes.assistant.dto.TimeRange;
import com.gomes.assistant.util.DataUtils;

@Service
public class GoogleCalendarEventService {

    private final GoogleCalendarRestService googleCalendarRestService;
    private String token = null; // This should be set with a valid OAuth token before use
    @Value("${google.api.calendar-id}")
    private String calendarId;

    public GoogleCalendarEventService(GoogleCalendarRestService googleCalendarRestService) {
        this.googleCalendarRestService = googleCalendarRestService;
    }

    public String createEvent(String jsonData) throws Exception {
        return googleCalendarRestService.createEvent(token, calendarId, jsonData);
    }

    public String updateEvent(String eventId, String jsonData) throws Exception {
        return googleCalendarRestService.updateEvent(token, calendarId, eventId, jsonData);
    }

    public String deleteEvent(String eventId) throws Exception {
        return googleCalendarRestService.deleteEvent(token, calendarId, eventId);
    }

    public List<Event> getEvents(TimeRange timeRange) throws Exception {
        String response = googleCalendarRestService.getEvents(token, calendarId, timeRange.start(), timeRange.end());
        String itemsJson = DataUtils.extractField(response, "items");
        
        return DataUtils.parseList(itemsJson, new TypeReference<List<Event>>() {});
    }
}