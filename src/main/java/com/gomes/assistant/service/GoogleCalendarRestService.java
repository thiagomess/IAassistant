package com.gomes.assistant.service;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Service
public class GoogleCalendarRestService {

    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://www.googleapis.com/calendar/v3")
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();

    public String getEvents(String accessToken, String calendarId, String timeMin, String timeMax) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/calendars/{calendarId}/events")
                        .queryParam("timeMin", timeMin)
                        .queryParam("timeMax", timeMax)
                        .queryParam("singleEvents", true)
                        .queryParam("orderBy", "startTime")
                        .build(calendarId))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
    
    public String createEvent(String accessToken, String calendarId, String eventJson) {
        System.out.println("Request Payload: " + eventJson);
        try {
            return webClient.post()
                    .uri("/calendars/{calendarId}/events", calendarId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .bodyValue(eventJson)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (WebClientResponseException e) {
            System.err.println("Error Status Code: " + e.getStatusCode());
            System.err.println("Error Response Body: " + e.getResponseBodyAsString());
            throw e;
        }
    }


	public String updateEvent(String accessToken, String calendarId, String eventId, String eventJson) {
        System.out.println("Request Payload: " + eventJson);
        try {
        	return webClient.patch()
				.uri("/calendars/{calendarId}/events/{eventId}", calendarId, eventId)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
				.bodyValue(eventJson)
				.retrieve()
				.bodyToMono(String.class)
				.block();
	    } catch (WebClientResponseException e) {
	        System.err.println("Error Status Code: " + e.getStatusCode());
	        System.err.println("Error Response Body: " + e.getResponseBodyAsString());
	        throw e;
	    }
	}

	public String deleteEvent(String accessToken, String calendarId, String eventId) {
        try {
        	return webClient.delete()
				.uri("/calendars/{calendarId}/events/{eventId}", calendarId, eventId)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
				.retrieve()
				.bodyToMono(String.class)
				.block();
	    } catch (WebClientResponseException e) {
	        System.err.println("Error Status Code: " + e.getStatusCode());
	        System.err.println("Error Response Body: " + e.getResponseBodyAsString());
	        throw e;
	    }
	}
}
