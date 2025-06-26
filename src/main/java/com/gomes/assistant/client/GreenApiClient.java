package com.gomes.assistant.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.gomes.assistant.dto.ResponseWhatsApp;

import reactor.core.publisher.Mono;

@Service
public class GreenApiClient {

	private static final Logger logger = LoggerFactory.getLogger(GreenApiClient.class);

	private final String instanceId;
	private final String token;
	private final WebClient webClient;

	public GreenApiClient(
			WebClient.Builder webClientBuilder,
			@Value("${greenapi.instance-id}") String instanceId,
			@Value("${greenapi.token}") String token) {
		this.instanceId = instanceId;
		this.token = token;
		String baseUrl = "https://7105.api.greenapi.com/waInstance" + instanceId;
		this.webClient = webClientBuilder
				.baseUrl(baseUrl)
				.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
				.build();
	}

	public String sendMessage(ResponseWhatsApp responseWhatsApp) {
		String sendMessagePath = "/sendMessage/" + token;
		try {
			logger.info("Sending message with payload: {}", responseWhatsApp);
			return webClient.post()
					.uri(sendMessagePath)
					.body(Mono.just(responseWhatsApp), ResponseWhatsApp.class)
					.retrieve()
					.bodyToMono(String.class)
					.block();
		} catch (WebClientResponseException e) {
			logger.error("Error response from API: Status {}, Body: {}", e.getStatusCode(),
					e.getResponseBodyAsString());
			throw e;
		}
	}
}
