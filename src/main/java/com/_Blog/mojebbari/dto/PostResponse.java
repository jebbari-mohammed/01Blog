package com._Blog.mojebbari.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PostResponse {
    private Long id;
    private String title;
    private String content;
    private String category;
    private String mediaUrl;
    private String authorUsername;
    private Long authorId;
    private LocalDateTime createdAt;
    
    // Interaction counts
    private long likeCount;
    private long commentCount;
    
    @JsonProperty("isLikedByCurrentUser")  // Force JSON to use this exact name
    private boolean isLikedByCurrentUser;
}