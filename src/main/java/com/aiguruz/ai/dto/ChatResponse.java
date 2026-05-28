package com.aiguruz.ai.dto;

import lombok.Builder;
import lombok.Data;
import java.time.Instant;

@Data
@Builder
public class ChatResponse {
    private String sessionId;
    private String reply;
    private Instant timestamp;
}
