package com._Blog.mojebbari.controllers;

import com._Blog.mojebbari.services.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * FileUploadController - Handles file uploads
 * 
 * Endpoints:
 * - POST /api/upload - Upload a file
 */
@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class FileUploadController {

    private final FileStorageService fileStorageService;

    /**
     * Upload a file
     * 
     * POST /api/upload
     * 
     * Request:
     * - Content-Type: multipart/form-data
     * - Body: file (the image file)
     * 
     * Response:
     * {
     *   "url": "/uploads/posts/uuid-filename.jpg"
     * }
     */
    @PostMapping
    public ResponseEntity<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            String fileUrl = fileStorageService.storeFile(file);
            
            Map<String, String> response = new HashMap<>();
            response.put("url", fileUrl);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}
