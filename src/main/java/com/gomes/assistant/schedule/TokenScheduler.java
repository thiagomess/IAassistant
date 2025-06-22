package com.gomes.assistant.schedule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.gomes.assistant.client.OAuth2TokenClient;

@Service
public class TokenScheduler {
	
    private static final Logger logger = LoggerFactory.getLogger(TokenScheduler.class);
    private final OAuth2TokenClient oAuth2TokenClient;

    public TokenScheduler(OAuth2TokenClient oAuth2TokenClient) {
        this.oAuth2TokenClient = oAuth2TokenClient;
    }

    @Scheduled(fixedRate = 50 * 60 * 1000, initialDelay = 50 * 60 * 1000) 
    public void renewTokenPeriodically() {
        oAuth2TokenClient.renewToken();
        logger.info("Token renovado com sucesso em {}", System.currentTimeMillis());
    }
}
