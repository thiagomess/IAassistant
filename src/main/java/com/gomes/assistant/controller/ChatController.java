package com.gomes.assistant.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.gomes.assistant.dto.Request;
import com.gomes.assistant.service.ChatService;

@RestController
public class ChatController {

    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);
    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/chat")
    public String generate(@RequestBody Request message) throws Exception {
        logger.info("Recebida requisição /chat: {}", message.message());
        String response = chatService.generate(message.message());
        logger.info("Resposta gerada para /chat");
        return response;
    }
}