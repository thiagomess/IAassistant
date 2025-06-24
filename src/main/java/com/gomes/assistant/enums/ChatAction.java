package com.gomes.assistant.enums;

public enum ChatAction {
    CREATE, UPDATE, CANCEL, CANCEL_ALL, SEARCH, QUESTION;

    public static ChatAction fromString(String action) {
        try {
            return ChatAction.valueOf(action.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new UnsupportedOperationException("Unsupported action: " + action);
        }
    }
}