package com.aiguruz.ai.dto;

import lombok.Data;

@Data
public class ChatRequest {
    private String sessionId;    // null = new session
    private String documentId;   // optional context
    private String message;
}

