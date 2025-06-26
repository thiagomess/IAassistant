package com.gomes.assistant.dto;

public record WebhookPayload(
	    String typeWebhook,
	    InstanceData instanceData,
	    long timestamp,
	    String idMessage,
	    SenderData senderData,
	    MessageData messageData
	) {

	    public record InstanceData(
	        long idInstance,
	        String wid,
	        String typeInstance
	    ) {}

	    public record SenderData(
	        String chatId,
	        String chatName,
	        String sender,
	        String senderName,
	        String senderContactName
	    ) {}

	    public record MessageData(
	        String typeMessage,
	        TextMessageData textMessageData
	    ) {
	        public record TextMessageData(
	            String textMessage
	        ) {}
	    }
	}

