package com.aiguruz.ai.controller;

import com.aiguruz.ai.dto.ChatRequest;
import com.aiguruz.ai.dto.ChatResponse;
import com.aiguruz.ai.model.AiSession;
import com.aiguruz.ai.repository.AiSessionRepository;
import com.aiguruz.ai.service.AiService;
import com.aiguruz.ai.service.SummaryService;
import com.aiguruz.audit.service.AuditService;
import com.aiguruz.common.exception.ResourceNotFoundException;
import com.aiguruz.common.model.ApiResponse;
import com.aiguruz.common.util.SecurityUtils;
import com.aiguruz.document.repository.DocumentRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "AI Chat", description = "Conversational AI with optional document context")
public class AiController {

    private final AiService            aiService;
    private final AiSessionRepository  sessionRepo;
    private final DocumentRepository   docRepo;
    private final AuditService         audit;
    private final SummaryService       summaryService;

    @PostMapping("/chat")
    public ResponseEntity<ApiResponse<ChatResponse>> chat(
            @RequestBody ChatRequest req, HttpServletRequest httpReq) {

        String userId = SecurityUtils.currentUserId();

        AiSession session = (req.getSessionId() != null)
            ? sessionRepo.findById(req.getSessionId())
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"))
            : AiSession.builder().userId(userId).documentId(req.getDocumentId())
                .messages(new ArrayList<>()).build();

        session.getMessages().add(
            new AiSession.ChatMessage("user", req.getMessage(), Instant.now()));

        String reply = aiService.complete(buildSystem(req.getDocumentId()),
            session.getMessages().stream()
                .map(m -> new AiService.Msg(m.getRole(), m.getContent()))
                .toList());

        session.getMessages().add(
            new AiSession.ChatMessage("assistant", reply, Instant.now()));
        session = sessionRepo.save(session);

        log.debug("AI chat: userId={} sessionId={}", userId, session.getId());
        audit.log(userId, SecurityUtils.currentTenantId(), null,
            "AI_CHAT", "success",
            httpReq.getRemoteAddr(),
            Map.of("sessionId", session.getId()));

        return ResponseEntity.ok(ApiResponse.ok(ChatResponse.builder()
            .sessionId(session.getId()).reply(reply).timestamp(Instant.now())
            .build()));
    }

    @GetMapping("/sessions")
    public ResponseEntity<ApiResponse<List<AiSession>>> sessions() {
        return ResponseEntity.ok(ApiResponse.ok(
            sessionRepo.findByUserIdOrderByUpdatedAtDesc(SecurityUtils.currentUserId())));
    }

    @GetMapping("/sessions/{id}")
    public ResponseEntity<ApiResponse<AiSession>> session(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok(sessionRepo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Session not found"))));
    }

    @DeleteMapping("/sessions/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSession(@PathVariable String id) {
        sessionRepo.deleteById(id);
        return ResponseEntity.ok(ApiResponse.ok(null, "Session deleted"));
    }

    @PostMapping("/summaries/{id}")
    public ResponseEntity<ApiResponse<Object>> summarize(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok(summaryService.generate(id, SecurityUtils.currentUserId())));
    }

    private String buildSystem(String documentId) {
        var sb = new StringBuilder(
            "You are AIGuruz, an AI academic assistant. " +
            "Be concise, educational, and accurate. " +
            "Keep responses under 150 words unless detailed explanation is requested.");
        if (documentId != null) {
            docRepo.findById(documentId).ifPresent(doc -> {
                if (doc.getExtractedText() != null) {
                    sb.append("\n\nDocument context (use this to answer questions):\n")
                      .append(doc.getExtractedText(), 0,
                          Math.min(doc.getExtractedText().length(), 8_000));
                }
            });
        }
        return sb.toString();
    }
}
