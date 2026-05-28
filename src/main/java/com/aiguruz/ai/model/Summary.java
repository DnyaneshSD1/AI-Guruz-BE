package com.aiguruz.ai.model;

import lombok.*;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Document(collection = "summaries")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Summary {
    @Id private String id;
    private String documentId;
    private String userId;
    private String structuredText;
    private double confidenceScore;
    private int    citationCount;
    private int    sourcePages;
    private List<MindmapNode> mindmapNodes;
    private List<MindmapEdge> mindmapEdges;
    @CreatedDate private Instant generatedAt;

    @Data @AllArgsConstructor @NoArgsConstructor
    public static class MindmapNode {
        private String id, label, color, textColor;
        private double x, y, r;
    }
    @Data @AllArgsConstructor @NoArgsConstructor
    public static class MindmapEdge {
        private String from, to;
    }
}

