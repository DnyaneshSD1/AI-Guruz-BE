package com.aiguruz.document.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3Service {

    private final S3Client    s3;
    private final S3Presigner presigner;

    @Value("${aws.s3.bucket}")                  private String bucket;
    @Value("${aws.s3.presign-expiry-minutes}")  private int    presignExpiry;

    /**
     * Uploads a file to S3.
     * Path: tenants/{tenantId}/users/{userId}/{uuid}-{sanitizedFilename}
     * Returns the S3 key.
     */
    public String upload(MultipartFile file, String tenantId, String userId) throws IOException {
        String sanitized = file.getOriginalFilename()
            .replaceAll("[^a-zA-Z0-9._-]", "_");
        String key = String.format("tenants/%s/users/%s/%s-%s",
            tenantId, userId, UUID.randomUUID(), sanitized);

        log.info("Uploading file to S3: bucket={} key={} size={}",
            bucket, key, file.getSize());

        s3.putObject(
            PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(file.getContentType())
                .contentLength(file.getSize())
                .serverSideEncryption(ServerSideEncryption.AES256)
                .build(),
            RequestBody.fromInputStream(file.getInputStream(), file.getSize())
        );

        log.info("S3 upload complete: key={}", key);
        return key;
    }

    /**
     * Generates a pre-signed GET URL (valid for presignExpiry minutes).
     * Never expose the raw S3 key to the client — always use this.
     */
    public String presignedUrl(String key) {
        String url = presigner.presignGetObject(
            GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(presignExpiry))
                .getObjectRequest(r -> r.bucket(bucket).key(key))
                .build()
        ).url().toString();
        log.debug("Generated presigned URL for key={}", key);
        return url;
    }

    /** Hard-deletes the S3 object. */
    public void delete(String key) {
        s3.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(key).build());
        log.info("Deleted S3 object: key={}", key);
    }
}

