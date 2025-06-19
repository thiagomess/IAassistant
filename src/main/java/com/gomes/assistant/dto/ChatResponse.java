package com.gomes.assistant.dto;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ChatResponse(
	    String action,
	    String dataInitial,
	    String dataFinal,
	    String eventName,
	    Map<String, Object> data
	) {}
