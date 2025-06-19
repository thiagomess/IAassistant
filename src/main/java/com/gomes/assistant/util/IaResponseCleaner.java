package com.gomes.assistant.util;

public class IaResponseCleaner {
    public static String extractJson(String resposta) {
        if (resposta == null) return null;
        resposta = resposta.trim();
        if (resposta.contains("```")) {
            int start = resposta.indexOf("```");
            int end = resposta.lastIndexOf("```");
            if (start != -1 && end != -1 && end > start) {
                resposta = resposta.substring(start + 3, end).trim();
                // Remove o "json" do início, se houver
                if (resposta.startsWith("json")) {
                    resposta = resposta.substring(4).trim();
                }
            }
        }
        return resposta;
    }
}