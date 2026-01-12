package com._Blog.mojebbari.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * FileStorageService - Handles file upload and storage
 * 
 * What it does:
 * 1. Validates file type (images only)
 * 2. Generates unique filename
 * 3. Stores file in uploads directory
 * 4. Returns file URL
 */
@Service
public class FileStorageService {

    private final Path fileStorageLocation;

    public FileStorageService(@Value("${file.upload-dir}") String uploadDir) {
        this.fileStorageLocation = Paths.get(uploadDir)
                .toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    /**
     * Store a file and return its URL
     * 
     * @param file The uploaded file
     * @return The file URL (e.g., "/uploads/posts/uuid-filename.jpg")
     */
    public String storeFile(MultipartFile file) {
        // Validate file
        if (file.isEmpty()) {
            throw new RuntimeException("Failed to store empty file.");
        }

        // Get original filename
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new RuntimeException("Invalid filename");
        }
        originalFilename = StringUtils.cleanPath(originalFilename);

        // Validate file type (only images)
        if (!isImageFile(originalFilename)) {
            throw new RuntimeException("Only image files are allowed (jpg, jpeg, png, gif)");
        }

        try {
            // Check if the filename contains invalid characters
            if (originalFilename.contains("..")) {
                throw new RuntimeException("Filename contains invalid path sequence " + originalFilename);
            }

            // Generate unique filename: UUID + extension
            String fileExtension = getFileExtension(originalFilename);
            String newFilename = UUID.randomUUID().toString() + fileExtension;

            // Copy file to the target location
            Path targetLocation = this.fileStorageLocation.resolve(newFilename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // Return the URL path (relative to server)
            return "/uploads/posts/" + newFilename;
        } catch (IOException ex) {
            throw new RuntimeException("Could not store file " + originalFilename + ". Please try again!", ex);
        }
    }

    /**
     * Check if file is an image
     */
    private boolean isImageFile(String filename) {
        String extension = getFileExtension(filename).toLowerCase();
        return extension.equals(".jpg") || 
               extension.equals(".jpeg") || 
               extension.equals(".png") || 
               extension.equals(".gif") ||
               extension.equals(".webp");
    }

    /**
     * Get file extension from filename
     */
    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        return filename.substring(lastDotIndex);
    }

    /**
     * Delete a file
     * 
     * @param fileUrl The file URL (e.g., "/uploads/posts/uuid-filename.jpg")
     */
    public void deleteFile(String fileUrl) {
        try {
            // Extract filename from URL
            String filename = fileUrl.substring(fileUrl.lastIndexOf('/') + 1);
            Path filePath = this.fileStorageLocation.resolve(filename).normalize();
            Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            // Log error but don't throw exception (file might not exist)
            System.err.println("Could not delete file: " + fileUrl);
        }
    }
}
