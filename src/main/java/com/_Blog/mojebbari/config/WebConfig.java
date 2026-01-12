package com._Blog.mojebbari.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * WebConfig - Configure static resources
 * 
 * What it does:
 * - Maps /uploads/** URLs to the file system directory
 * - Allows uploaded images to be accessed via HTTP
 * 
 * Example:
 * - File stored at: /Users/username/Downloads/01Blog/uploads/posts/uuid.jpg
 * - Accessible at: http://localhost:8080/uploads/posts/uuid.jpg
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        // Convert relative path to absolute path
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        String uploadLocation = "file:" + uploadPath.toString() + "/";

        // Map /uploads/** URLs to the file system directory
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadLocation);
    }
}
