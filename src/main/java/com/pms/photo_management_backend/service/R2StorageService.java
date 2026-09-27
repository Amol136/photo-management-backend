package com.pms.photo_management_backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.time.Duration;
import java.util.UUID;

@Service
public class R2StorageService {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${cloudflare.r2.bucket}")
    private String bucketName;

    public R2StorageService(
            S3Client s3Client,
            S3Presigner s3Presigner
    ) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
    }

    // ==========================================
    // UPLOAD FILE TO R2
    // ==========================================

    public String uploadFile(
            MultipartFile file,
            String folder
    ) {

        if (file == null || file.isEmpty()) {
            throw new RuntimeException(
                    "Upload करण्यासाठी file आवश्यक आहे."
            );
        }

        String contentType = file.getContentType();

        if (
                contentType == null ||
                        !contentType.startsWith("image/")
        ) {
            throw new RuntimeException(
                    "फक्त image file upload करू शकता."
            );
        }

        String originalFilename =
                file.getOriginalFilename();

        String extension = "";

        if (
                originalFilename != null &&
                        originalFilename.contains(".")
        ) {

            extension =
                    originalFilename.substring(
                            originalFilename.lastIndexOf(".")
                    );
        }

        String objectKey =
                folder +
                        "/" +
                        UUID.randomUUID() +
                        extension;

        try {

            PutObjectRequest putObjectRequest =
                    PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(objectKey)
                            .contentType(contentType)
                            .build();

            s3Client.putObject(
                    putObjectRequest,
                    RequestBody.fromBytes(
                            file.getBytes()
                    )
            );

            return objectKey;

        } catch (IOException exception) {

            throw new RuntimeException(
                    "Photo read करताना error आला.",
                    exception
            );

        } catch (Exception exception) {

            throw new RuntimeException(
                    "Cloudflare R2 वर photo upload झाला नाही: "
                            + exception.getMessage(),
                    exception
            );
        }
    }

    // ==========================================
    // CREATE PRIVATE SIGNED URL
    // Valid for 10 minutes
    // ==========================================

    public String generateSignedUrl(
            String objectKey
    ) {

        if (
                objectKey == null ||
                        objectKey.isBlank()
        ) {
            throw new RuntimeException(
                    "Photo object key उपलब्ध नाही."
            );
        }

        try {

            GetObjectRequest getObjectRequest =
                    GetObjectRequest.builder()
                            .bucket(bucketName)
                            .key(objectKey)
                            .build();

            GetObjectPresignRequest presignRequest =
                    GetObjectPresignRequest.builder()
                            .signatureDuration(
                                    Duration.ofMinutes(10)
                            )
                            .getObjectRequest(
                                    getObjectRequest
                            )
                            .build();

            PresignedGetObjectRequest
                    presignedRequest =
                    s3Presigner.presignGetObject(
                            presignRequest
                    );

            return presignedRequest
                    .url()
                    .toString();

        } catch (Exception exception) {

            throw new RuntimeException(
                    "Photo Signed URL तयार झाला नाही: "
                            + exception.getMessage(),
                    exception
            );
        }
    }
    // ==========================================
// CREATE DOWNLOAD SIGNED URL
// Valid for 10 minutes
// ==========================================

    public String generateDownloadSignedUrl(
            String objectKey,
            String fileName
    ) {

        if (objectKey == null || objectKey.isBlank()) {
            throw new RuntimeException(
                    "Photo object key उपलब्ध नाही."
            );
        }

        try {

            GetObjectRequest getObjectRequest =
                    GetObjectRequest.builder()
                            .bucket(bucketName)
                            .key(objectKey)
                            .responseContentDisposition(
                                    "attachment; filename=\"" + fileName + "\""
                            )
                            .build();

            GetObjectPresignRequest presignRequest =
                    GetObjectPresignRequest.builder()
                            .signatureDuration(
                                    Duration.ofMinutes(10)
                            )
                            .getObjectRequest(
                                    getObjectRequest
                            )
                            .build();

            PresignedGetObjectRequest presignedRequest =
                    s3Presigner.presignGetObject(
                            presignRequest
                    );

            return presignedRequest
                    .url()
                    .toString();

        } catch (Exception exception) {

            throw new RuntimeException(
                    "Download URL तयार झाला नाही: "
                            + exception.getMessage(),
                    exception
            );
        }
    }

    // ==========================================
    // DELETE FILE FROM R2
    // ==========================================

    public void deleteFile(
            String objectKey
    ) {

        if (
                objectKey == null ||
                        objectKey.isBlank()
        ) {
            return;
        }

        try {

            DeleteObjectRequest deleteObjectRequest =
                    DeleteObjectRequest.builder()
                            .bucket(bucketName)
                            .key(objectKey)
                            .build();

            s3Client.deleteObject(
                    deleteObjectRequest
            );

        } catch (Exception exception) {

            throw new RuntimeException(
                    "R2 मधून photo delete करताना error आला: "
                            + exception.getMessage(),
                    exception
            );
        }
    }
}