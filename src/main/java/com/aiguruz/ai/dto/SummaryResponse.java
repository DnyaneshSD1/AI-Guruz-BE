package com.aiguruz.ai.dto;

import com.aiguruz.ai.model.Summary;
import lombok.Builder;
import lombok.Data;
import java.time.Instant;
import java.util.List;

@Data
@Builder
public class SummaryResponse {
    private String id;
    private String documentId;
    private String structuredText;
    private double confidenceScore;
    private int citationCount;
    private int sourcePages;
    private List<Summary.MindmapNode> mindmapNodes;
    private List<Summary.MindmapEdge> mindmapEdges;
    private Instant generatedAt;
}
