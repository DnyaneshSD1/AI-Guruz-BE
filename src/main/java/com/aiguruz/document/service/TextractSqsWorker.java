package com.aiguruz.document.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class TextractSqsWorker {

    private final SqsClient       sqs;
    private final DocumentService docService;
    private final ObjectMapper    mapper = new ObjectMapper();

    @Value("${aws.sqs.textract-queue-url:}") private String queueUrl;

    @Scheduled(fixedDelay = 10_000)     // poll every 10 seconds
    public void poll() {
        if (queueUrl == null || queueUrl.isBlank()) return;

        var messages = sqs.receiveMessage(
            ReceiveMessageRequest.builder()
                .queueUrl(queueUrl)
                .maxNumberOfMessages(10)
                .waitTimeSeconds(5)     // long-polling
                .build()
        ).messages();

        for (var msg : messages) {
            try {
                // SQS body is SNS envelope wrapping Textract notification JSON
                JsonNode root  = mapper.readTree(msg.body());
                JsonNode inner = mapper.readTree(root.path("Message").asText());
                String jobId   = inner.path("JobId").asText();
                String status  = inner.path("Status").asText();

                log.info("Textract SQS notification: jobId={} status={}", jobId, status);

                if ("SUCCEEDED".equals(status)) {
                    docService.handleTextractComplete(jobId);
                } else {
                    log.warn("Textract job did not succeed: jobId={} status={}", jobId, status);
                }

                sqs.deleteMessage(DeleteMessageRequest.builder()
                    .queueUrl(queueUrl).receiptHandle(msg.receiptHandle()).build());

            } catch (Exception e) {
                log.error("Error processing SQS message: {}", e.getMessage(), e);
            }
        }
    }
}

