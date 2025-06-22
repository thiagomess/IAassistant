package com.gomes.assistant.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.gomes.assistant.config.GoogleCalendarConfig;

@Service
public class GoogleCalendarClient {

    private static final Logger logger = LoggerFactory.getLogger(GoogleCalendarClient.class);
    private static final String BASE_URL = "https://www.googleapis.com/calendar/v3";
    private static final String EVENTS_PATH = "/calendars/{calendarId}/events";

    private final WebClient webClient;
    private final OAuth2TokenClient oAuth2TokenClient;

    public GoogleCalendarClient(WebClient.Builder webClientBuilder, OAuth2TokenClient oAuth2TokenClient, GoogleCalendarConfig googleCalendarConfig) {
    	this.oAuth2TokenClient = oAuth2TokenClient;
        this.webClient = webClientBuilder
                .baseUrl(BASE_URL)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public String getEvents(String calendarId, String timeMin, String timeMax) {
        try {
            return webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(EVENTS_PATH)
                            .queryParam("timeMin", timeMin)
                            .queryParam("timeMax", timeMax)
                            .queryParam("singleEvents", true)
                            .queryParam("orderBy", "startTime")
                            .build(calendarId))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + oAuth2TokenClient.getTokenInMemory().access_token())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (WebClientResponseException e) {
            logError("Erro ao buscar eventos", e);
            throw e;
        }
    }

    public String createEvent(String calendarId, String eventJson) {
        logger.info("Criando evento com payload: {}", eventJson);
        try {
            return webClient.post()
                    .uri(EVENTS_PATH, calendarId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + oAuth2TokenClient.getTokenInMemory().access_token())
                    .bodyValue(eventJson)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (WebClientResponseException e) {
            logError("Erro ao criar evento", e);
            throw e;
        }
    }

    public String updateEvent(String calendarId, String eventId, String eventJson) {
        logger.info("Atualizando evento com payload: {}", eventJson);
        try {
            return webClient.patch()
                    .uri(EVENTS_PATH + "/{eventId}", calendarId, eventId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + oAuth2TokenClient.getTokenInMemory().access_token())
                    .bodyValue(eventJson)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (WebClientResponseException e) {
            logError("Erro ao atualizar evento", e);
            throw e;
        }
    }

    public String deleteEvent(String calendarId, String eventId) {
        try {
            return webClient.delete()
                    .uri(EVENTS_PATH + "/{eventId}", calendarId, eventId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + oAuth2TokenClient.getTokenInMemory().access_token())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (WebClientResponseException e) {
            logError("Erro ao deletar evento", e);
            throw e;
        }
    }

    private void logError(String message, WebClientResponseException e) {
        logger.error("{} - Status: {}, Resposta: {}", message, e.getStatusCode(), e.getResponseBodyAsString());
    }
}
