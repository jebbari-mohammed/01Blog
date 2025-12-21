package com._Blog.mojebbari.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreatePostRequest {

    @NotBlank(message = "Post title cannot be empty")
    private String title;

    @NotBlank(message = "Post content cannot be empty")
    private String content;

    private String category; // Optional: TECH, LIFESTYLE, FOOD, etc.

    private String mediaUrl; // Optional: URL to image or video
}