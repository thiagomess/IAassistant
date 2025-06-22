package com.gomes.assistant.client;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.gomes.assistant.dto.TokenResponse;

@Service
public class OAuth2TokenClient {

    private static final Logger logger = LoggerFactory.getLogger(OAuth2TokenClient.class);
    private static final String GRANT_TYPE_CODE = "authorization_code";
    private static final String GRANT_TYPE_REFRESH = "refresh_token";
    private static final String URL = "https://oauth2.googleapis.com/token";

    private final WebClient webClient;
    private final String clientId;
    private final String clientSecret;
    private final String redirectUri;
    private String refreshToken;
    private Optional<TokenResponse> tokenInMemory = Optional.empty();


    public OAuth2TokenClient(WebClient.Builder webClientBuilder,
                             @Value("${google.oauth2.client.id}") String clientId,
                             @Value("${google.oauth2.client.secret}") String clientSecret,
                             @Value("${google.oauth2.redirect.uri}") String redirectUri) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUri = redirectUri;
        this.webClient = webClientBuilder
        .baseUrl(URL)
        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
        .build();
    }

    public void exchangeCodeForToken(String code) {
        MultiValueMap<String, String> requestBody = buildRequestBodyCode(code);
        try {
            TokenResponse response = webClient.post()
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(TokenResponse.class)
                                        .block();
            tokenInMemory = Optional.ofNullable(response);
            if (response != null && response.refresh_token() != null) {
                refreshToken = response.refresh_token(); 
            }
            logger.info("Token trocado com sucesso: {}", tokenInMemory.get());
        } catch (WebClientResponseException e) {
            logger.error("Erro ao trocar código por token - Status: {}, Resposta: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw e;
        }
    }
    
    public void renewToken() {
        MultiValueMap<String, String> requestBody = buildRequestBodyRefreshToken();
        try {
            TokenResponse response = webClient.post()
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(TokenResponse.class)
                                        .block();
            tokenInMemory = Optional.ofNullable(response);
            logger.info("Token renovado com sucesso: {}", tokenInMemory.get());
        } catch (WebClientResponseException e) {
            logger.error("Erro ao renovar token - Status: {}, Resposta: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw e;
        }
    }

    private MultiValueMap<String, String> buildRequestBodyCode(String code) {
        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("code", code);
        requestBody.add("client_id", clientId);
        requestBody.add("client_secret", clientSecret);
        requestBody.add("redirect_uri", redirectUri);
        requestBody.add("grant_type", GRANT_TYPE_CODE);
        return requestBody;
    }
    
    private MultiValueMap<String, String> buildRequestBodyRefreshToken() {
        if (refreshToken == null) {
            logger.warn("Nenhum refresh token disponível. Pulando renovação do token.");
            return new LinkedMultiValueMap<>();
        }
        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("client_id", clientId);
        requestBody.add("client_secret", clientSecret);
        requestBody.add("refresh_token", refreshToken);
        requestBody.add("grant_type", GRANT_TYPE_REFRESH);
        return requestBody;
    }

    public TokenResponse getTokenInMemory() {
        return tokenInMemory.get();
    }
}
