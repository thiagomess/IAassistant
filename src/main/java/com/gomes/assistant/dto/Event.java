package com.gomes.assistant.dto;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Event(
        String id,
        String summary,
        Map<String, Object> start,
        Map<String, Object> end
    ) {}
