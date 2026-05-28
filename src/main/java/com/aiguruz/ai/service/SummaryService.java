package com.aiguruz.ai.service;

import com.aiguruz.ai.dto.SummaryResponse;
import com.aiguruz.ai.model.Summary;
import com.aiguruz.ai.repository.SummaryRepository;
import com.aiguruz.common.exception.BadRequestException;
import com.aiguruz.common.exception.ResourceNotFoundException;
import com.aiguruz.document.model.Document;
import com.aiguruz.document.repository.DocumentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class SummaryService {

    private final AiService          ai;
    private final SummaryRepository  summaryRepo;
    private final DocumentRepository docRepo;
    private final ObjectMapper       mapper = new ObjectMapper();

    private static final String SYSTEM = """
        You are AIGuruz, an expert academic AI assistant.
        Given document text, produce a structured academic summary as JSON:
        {
          "structuredText": "Markdown-formatted summary with **bold headers**, bullet points, numbered lists",
          "confidenceScore": 0.94,
          "citationCount": 8,
          "sourcePages": 23,
          "mindmapNodes": [
            {"id":"root","label":"Central Topic","x":300,"y":200,"color":"#534AB7","textColor":"#fff","r":40},
            {"id":"n1","label":"Subtopic 1","x":150,"y":100,"color":"#185FA5","textColor":"#fff","r":30}
          ],
          "mindmapEdges": [
            {"from":"root","to":"n1"}
          ]
        }
        Rules:
        - Generate 8-12 mindmap nodes. Root at (300,200). Spread child nodes evenly.
        - Leaf nodes use r=22, mid-level r=30, root r=40.
        - Use a viewBox of 600 x 420 for coordinates.
        - Vary node colors meaningfully.
        - confidenceScore between 0.80-0.98 based on text quality.
        """;

    public SummaryResponse generate(String documentId, String userId) {
        Document doc = docRepo.findById(documentId)
            .orElseThrow(() -> new ResourceNotFoundException("Document not found: " + documentId));

        if (!"READY".equals(doc.getStatus()) && !"SUMMARIZING".equals(doc.getStatus()))
            throw new BadRequestException("Document is not ready. Current status: " + doc.getStatus());

        return summaryRepo.findByDocumentId(documentId)
            .map(this::toDto)
            .orElseGet(() -> createSummary(doc, userId));
    }

    private SummaryResponse createSummary(Document doc, String userId) {
        doc.setStatus("SUMMARIZING");
        docRepo.save(doc);

        String text = doc.getExtractedText();
        if (text == null || text.isBlank())
            throw new BadRequestException("Extracted text is empty");

        String truncated = text.substring(0, Math.min(text.length(), 15_000));
        log.info("Generating summary for docId={} textLen={}", doc.getId(), truncated.length());

        String json = ai.completeJson(SYSTEM, "Summarize this academic document:\n\n" + truncated);

        try {
            Map<?, ?> parsed = mapper.readValue(json, Map.class);

            List<Summary.MindmapNode> nodes = new ArrayList<>();
            for (Map<?, ?> n : (List<Map<?, ?>>) parsed.get("mindmapNodes")) {
                var node = new Summary.MindmapNode();
                node.setId((String) n.get("id"));
                node.setLabel((String) n.get("label"));
                node.setColor((String) n.get("color"));
                node.setTextColor((String) n.get("textColor"));
                node.setX(((Number) n.get("x")).doubleValue());
                node.setY(((Number) n.get("y")).doubleValue());
                node.setR(((Number) n.get("r")).doubleValue());
                nodes.add(node);
            }

            List<Summary.MindmapEdge> edges = new ArrayList<>();
            for (Map<?, ?> e : (List<Map<?, ?>>) parsed.get("mindmapEdges")) {
                edges.add(new Summary.MindmapEdge(
                    (String) e.get("from"), (String) e.get("to")));
            }

            Summary summary = Summary.builder()
                .documentId(doc.getId()).userId(userId)
                .structuredText((String) parsed.get("structuredText"))
                .confidenceScore(((Number) parsed.get("confidenceScore")).doubleValue())
                .citationCount(((Number) parsed.get("citationCount")).intValue())
                .sourcePages(((Number) parsed.get("sourcePages")).intValue())
                .mindmapNodes(nodes).mindmapEdges(edges)
                .build();

            summary = summaryRepo.save(summary);
            doc.setSummaryId(summary.getId());
            doc.setStatus("DONE");
            docRepo.save(doc);

            log.info("Summary saved: id={} docId={}", summary.getId(), doc.getId());
            return toDto(summary);

        } catch (Exception e) {
            log.error("Summary parsing failed for docId={}: {}", doc.getId(), e.getMessage(), e);
            doc.setStatus("FAILED");
            doc.setFailureReason("AI summary parsing failed: " + e.getMessage());
            docRepo.save(doc);
            throw new RuntimeException("Failed to parse AI summary: " + e.getMessage());
        }
    }

    public SummaryResponse getByDocId(String documentId) {
        return summaryRepo.findByDocumentId(documentId)
            .map(this::toDto)
            .orElseThrow(() -> new ResourceNotFoundException("Summary not found for doc: " + documentId));
    }

    private SummaryResponse toDto(Summary s) {
        return SummaryResponse.builder()
            .id(s.getId()).documentId(s.getDocumentId())
            .structuredText(s.getStructuredText())
            .confidenceScore(s.getConfidenceScore())
            .citationCount(s.getCitationCount())
            .sourcePages(s.getSourcePages())
            .mindmapNodes(s.getMindmapNodes())
            .mindmapEdges(s.getMindmapEdges())
            .generatedAt(s.getGeneratedAt())
            .build();
    }
}
