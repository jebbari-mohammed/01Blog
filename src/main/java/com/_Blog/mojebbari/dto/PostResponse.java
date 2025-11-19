package com._Blog.mojebbari.dto;

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
    private String text;
    private String mediaUrl;
    private String authorUsername; // Just the name, not the whole User object
    private LocalDateTime timestamp;
}