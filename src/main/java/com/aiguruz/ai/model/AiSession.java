package com.aiguruz.ai.model;

import lombok.*;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Document(collection = "ai_sessions")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class AiSession {
    @Id private String id;
    private String userId;
    private String documentId;      // optional context
    private List<ChatMessage> messages;
    @CreatedDate      private Instant createdAt;
    @LastModifiedDate private Instant updatedAt;

    @Data @AllArgsConstructor @NoArgsConstructor
    public static class ChatMessage {
        private String  role;       // "user" | "assistant"
        private String  content;
        private Instant timestamp;
    }
}

