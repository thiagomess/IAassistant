package com.gomes.assistant.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.gomes.assistant.client.OAuth2TokenClient;

@RestController
@RequestMapping("/api/oauth2")
public class OAuth2CallbackController {

	private OAuth2TokenClient oAuth2TokenClient;

	public OAuth2CallbackController(OAuth2TokenClient oAuth2TokenClient) {
		this.oAuth2TokenClient = oAuth2TokenClient;
	}

	@GetMapping("/callback")
	public ResponseEntity<Void> callback(@RequestParam("code") String code,
			@RequestParam(value = "state", required = false) String state,
			@RequestParam(value = "error", required = false) String error) {
		if (error != null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Erro de autorização: " + error);
		}

		oAuth2TokenClient.exchangeCodeForToken(code);

		return ResponseEntity.ok().build();

	}

}
