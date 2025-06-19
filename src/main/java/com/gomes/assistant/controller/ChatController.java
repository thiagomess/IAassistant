package com.gomes.assistant.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.gomes.assistant.dto.Request;
import com.gomes.assistant.service.ChatService;

@RestController
public class ChatController {

    private final ChatService chatService;
    

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/chat")
    public String generate(@RequestBody Request message) throws Exception {

        return chatService.generate(message.message());
    }
    

}