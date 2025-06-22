package com.gomes.assistant.dto;

public record TokenResponse(String access_token, String expires_in, String scope, String refresh_token, String token_type, String id_token) {}