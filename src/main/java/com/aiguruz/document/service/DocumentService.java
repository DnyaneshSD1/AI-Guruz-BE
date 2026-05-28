package com.aiguruz.document.service;

import com.aiguruz.audit.service.AuditService;
import com.aiguruz.common.exception.ResourceNotFoundException;
import com.aiguruz.document.dto.*;
import com.aiguruz.document.model.Document;
import com.aiguruz.document.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentService {

    private final DocumentRepository repo;
    private final S3Service          s3;
    private final TextractService    textract;
    private final AuditService       audit;

    @Value("${aws.s3.bucket}") private String bucket;

    // Threshold: use async Textract for files > 2 MB
    private static final long ASYNC_THRESHOLD_BYTES = 2 * 1024 * 1024;

    public UploadResponse upload(MultipartFile file, String userId,
                                 String tenantId, String ip) throws IOException {
        validateFile(file);

        String s3Key = s3.upload(file, tenantId, userId);

        Document doc = Document.builder()
            .uploadedBy(userId).tenantId(tenantId)
            .originalFilename(file.getOriginalFilename())
            .s3Key(s3Key).s3Bucket(bucket)
            .mimeType(file.getContentType())
            .fileSizeBytes(file.getSize())
            .status("UPLOADED")
            .build();
        doc = repo.save(doc);
        log.info("Document record saved: id={} user={}", doc.getId(), userId);

        audit.log(userId, tenantId, null, "DOCUMENT_UPLOAD", "success", ip,
            Map.of("filename", file.getOriginalFilename(), "size", file.getSize()));

        // kick off extraction in a background thread
        triggerExtraction(doc.getId());

        return UploadResponse.builder()
            .documentId(doc.getId())
            .filename(doc.getOriginalFilename())
            .status(doc.getStatus())
            .build();
    }

    @Async
    public void triggerExtraction(String docId) {
        Document doc = repo.findById(docId).orElse(null);
        if (doc == null) return;
        try {
            doc.setStatus("EXTRACTING");
            repo.save(doc);

            if (doc.getFileSizeBytes() <= ASYNC_THRESHOLD_BYTES) {
                // Sync path — result available immediately
                String text = textract.extractSync(doc.getS3Key());
                doc.setExtractedText(text);
                doc.setStatus("READY");
                doc.setProcessedAt(Instant.now());
            } else {
                // Async path — SQS worker will complete
                String jobId = textract.startAsyncExtract(doc.getS3Key());
                doc.setTextractJobId(jobId);
                // status stays EXTRACTING until SQS worker fires
            }
            repo.save(doc);
        } catch (Exception e) {
            log.error("Extraction failed for doc={}: {}", docId, e.getMessage(), e);
            doc.setStatus("FAILED");
            doc.setFailureReason(e.getMessage());
            repo.save(doc);
        }
    }

    /** Called by SQS worker on Textract async completion. */
    public void handleTextractComplete(String jobId) {
        repo.findByTextractJobId(jobId).ifPresent(doc -> {
            try {
                String text = textract.getAsyncResult(jobId);
                doc.setExtractedText(text);
                doc.setStatus("READY");
                doc.setProcessedAt(Instant.now());
                repo.save(doc);
                log.info("Async extraction complete: docId={}", doc.getId());
            } catch (Exception e) {
                log.error("Async result failed: jobId={} error={}", jobId, e.getMessage());
                doc.setStatus("FAILED");
                doc.setFailureReason(e.getMessage());
                repo.save(doc);
            }
        });
    }

    public List<DocumentDto> listForUser(String userId) {
        return repo.findByUploadedBy(userId).stream()
            .map(this::toDto).collect(Collectors.toList());
    }

    public DocumentDto getById(String id) {
        return toDto(repo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Document not found: " + id)));
    }

    public String presignedUrl(String id) {
        Document doc = repo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Document not found: " + id));
        return s3.presignedUrl(doc.getS3Key());
    }

    public void delete(String id) {
        Document doc = repo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Document not found: " + id));
        s3.delete(doc.getS3Key());
        repo.delete(doc);
        log.info("Document deleted: id={}", id);
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty())
            throw new com.aiguruz.common.exception.BadRequestException("File is empty");
        String ct = file.getContentType();
        if (ct == null || (!ct.equals("application/pdf") &&
                           !ct.equals("application/msword") &&
                           !ct.startsWith("text/")))
            throw new com.aiguruz.common.exception.BadRequestException(
                "Unsupported file type. Allowed: PDF, DOC, TXT");
    }

    private DocumentDto toDto(Document d) {
        return DocumentDto.builder()
            .id(d.getId()).originalFilename(d.getOriginalFilename())
            .status(d.getStatus()).failureReason(d.getFailureReason())
            .mimeType(d.getMimeType()).fileSizeBytes(d.getFileSizeBytes())
            .summaryId(d.getSummaryId())
            .uploadedAt(d.getUploadedAt()).processedAt(d.getProcessedAt())
            .build();
    }
}

