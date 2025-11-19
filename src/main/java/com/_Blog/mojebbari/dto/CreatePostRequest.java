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

    @NotBlank(message = "Post content cannot be empty")
    private String text;

    // Optional: For now, this is just a URL string. 
    // Later, we can handle real file uploads.
    private String mediaUrl; 
}