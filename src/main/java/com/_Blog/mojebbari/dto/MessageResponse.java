package com._Blog.mojebbari.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Generic message response for simple API responses
 * Used when we just need to return a message string
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageResponse {
    private String message;
}
