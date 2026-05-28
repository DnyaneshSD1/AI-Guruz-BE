package com.aiguruz.document.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.textract.TextractClient;
import software.amazon.awssdk.services.textract.model.*;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TextractService {

    private final TextractClient textract;

    @Value("${aws.s3.bucket}")              private String bucket;
    @Value("${aws.textract.sns-topic-arn}") private String snsTopicArn;
    @Value("${aws.textract.role-arn}")      private String roleArn;

    /**
     * Synchronous extraction — fast, suitable for single-page PDFs or images.
     */
    public String extractSync(String s3Key) {
        log.info("Starting sync Textract extraction for key={}", s3Key);
        var resp = textract.detectDocumentText(
            DetectDocumentTextRequest.builder()
                .document(Document.builder()
                    .s3Object(S3Object.builder().bucket(bucket).name(s3Key).build())
                    .build())
                .build()
        );
        String text = resp.blocks().stream()
            .filter(b -> b.blockType() == BlockType.LINE)
            .map(Block::text)
            .collect(Collectors.joining("\n"));
        log.info("Sync extraction complete: key={} chars={}", s3Key, text.length());
        return text;
    }

    /**
     * Asynchronous extraction — for multi-page PDFs.
     * Textract notifies SNS → SQS when done. Returns the jobId.
     */
    public String startAsyncExtract(String s3Key) {
        log.info("Starting async Textract extraction for key={}", s3Key);
        var resp = textract.startDocumentTextDetection(
            StartDocumentTextDetectionRequest.builder()
                .documentLocation(DocumentLocation.builder()
                    .s3Object(S3Object.builder().bucket(bucket).name(s3Key).build())
                    .build())
                .notificationChannel(NotificationChannel.builder()
                    .snsTopicArn(snsTopicArn)
                    .roleArn(roleArn)
                    .build())
                .build()
        );
        log.info("Async Textract job started: jobId={}", resp.jobId());
        return resp.jobId();
    }

    /**
     * Retrieves results of a completed async job (called by SQS worker).
     * Paginates through all result pages.
     */
    public String getAsyncResult(String jobId) {
        log.info("Retrieving async Textract result: jobId={}", jobId);
        StringBuilder sb = new StringBuilder();
        String nextToken = null;
        do {
            var builder = GetDocumentTextDetectionRequest.builder().jobId(jobId);
            if (nextToken != null) builder.nextToken(nextToken);
            var resp = textract.getDocumentTextDetection(builder.build());
            resp.blocks().stream()
                .filter(b -> b.blockType() == BlockType.LINE)
                .forEach(b -> sb.append(b.text()).append("\n"));
            nextToken = resp.nextToken();
        } while (nextToken != null);
        log.info("Async result retrieved: jobId={} chars={}", jobId, sb.length());
        return sb.toString();
    }
}

