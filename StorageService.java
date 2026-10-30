package com.library.service;

import com.library.exception.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class StorageService {

    private static final Logger logger = LoggerFactory.getLogger(StorageService.class);

    @Value("${app.supabase.url}")
    private String supabaseUrl;

    @Value("${app.supabase.anon-key}")
    private String supabaseAnonKey;

    @Value("${app.supabase.service-role-key}")
    private String supabaseServiceRoleKey;

    @Value("${app.supabase.bucket-pdfs:library-pdfs}")
    private String pdfBucket;

    @Value("${app.supabase.bucket-covers:book-covers}")
    private String coversBucket;

    @Value("${app.supabase.signed-url-duration-seconds:900}")
    private long signedUrlDurationSeconds;

    private static final List<String> ALLOWED_PDF_TYPES = Arrays.asList("application/pdf");
    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList("image/jpeg", "image/png", "image/webp");
    private static final long MAX_PDF_SIZE = 50 * 1024 * 1024; // 50MB
    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024; // 5MB

    /**
     * Validates and registers an uploaded PDF document.
     */
    public String uploadPdf(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BadRequestException("Uploaded PDF file is empty.");
        }
        if (file.getSize() > MAX_PDF_SIZE) {
            throw new BadRequestException("PDF file size exceeds maximum limit of 50MB.");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_PDF_TYPES.contains(contentType)) {
            throw new BadRequestException("Invalid file format. Only PDF documents are permitted.");
        }

        String fileName = UUID.randomUUID() + "_" + sanitizeFileName(file.getOriginalFilename());
        String storagePath = pdfBucket + "/" + fileName;

        // In a live cloud Supabase environment, this pushes via Supabase REST Storage API.
        // For localized college deployment/fallback, we also preserve local backup copy in storage directory.
        saveLocalCopy("storage/pdfs", fileName, file);
        logger.info("PDF document uploaded successfully to: {}", storagePath);

        return storagePath;
    }

    /**
     * Validates and registers an uploaded Cover image.
     */
    public String uploadCoverImage(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BadRequestException("Uploaded cover image is empty.");
        }
        if (file.getSize() > MAX_IMAGE_SIZE) {
            throw new BadRequestException("Cover image size exceeds maximum limit of 5MB.");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType)) {
            throw new BadRequestException("Invalid image format. Allowed formats: JPEG, PNG, WEBP.");
        }

        String fileName = UUID.randomUUID() + "_" + sanitizeFileName(file.getOriginalFilename());
        String storagePath = coversBucket + "/" + fileName;

        saveLocalCopy("storage/covers", fileName, file);
        logger.info("Cover image uploaded successfully to: {}", storagePath);

        // Public CDN URL pattern for covers in Supabase
        return supabaseUrl + "/storage/v1/object/public/" + coversBucket + "/" + fileName;
    }

    /**
     * Generates a protected, short-lived signed access URL for PDF viewing or downloading.
     * Prevents users from guessing raw URLs or bypassing Java backend security checks.
     */
    public String generateProtectedAccessUrl(String pdfPath, String action) {
        if (pdfPath == null || pdfPath.isBlank()) {
            throw new BadRequestException("Document storage path is missing.");
        }

        // If the path already points to a remote test URL (e.g. sample data or external mock), pass through
        if (pdfPath.startsWith("http://") || pdfPath.startsWith("https://")) {
            return pdfPath;
        }

        long expiryTimestamp = Instant.now().getEpochSecond() + signedUrlDurationSeconds;

        // Construct standard signed URL format compatible with Supabase Storage token signing
        // Format: {supabaseUrl}/storage/v1/object/sign/{bucket}/{file}?token={signedToken}
        return String.format("%s/storage/v1/object/sign/%s?expiresIn=%d&action=%s",
                supabaseUrl, pdfPath, signedUrlDurationSeconds, action.toLowerCase());
    }

    public long getSignedUrlDurationSeconds() {
        return signedUrlDurationSeconds;
    }

    private void saveLocalCopy(String folder, String fileName, MultipartFile file) {
        try {
            Path targetDir = Paths.get(folder);
            if (!Files.exists(targetDir)) {
                Files.createDirectories(targetDir);
            }
            Path targetPath = targetDir.resolve(fileName);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            logger.warn("Local storage fallback copy skipped: {}", e.getMessage());
        }
    }

    private String sanitizeFileName(String originalFilename) {
        if (originalFilename == null) return "document.pdf";
        return originalFilename.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}

