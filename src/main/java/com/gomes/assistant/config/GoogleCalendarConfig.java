package com.gomes.assistant.config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;

@Configuration
public class GoogleCalendarConfig {

    @Value("${google.api.application-name}")
    private String applicationName;

    @Bean
    NetHttpTransport netHttpTransport() {
        return new NetHttpTransport();
    }

    @Bean
    JsonFactory jsonFactory() {
        return GsonFactory.getDefaultInstance();
    }

}
