package com.messagesservice.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FeedItemResponse {

    private Long id;
    private String text;
    private AuthorDto author;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    public FeedItemResponse() {
    }

    public FeedItemResponse(Long id, String text, AuthorDto author, LocalDateTime createdAt) {
        this.id = id;
        this.text = text;
        this.author = author;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public AuthorDto getAuthor() {
        return author;
    }

    public void setAuthor(AuthorDto author) {
        this.author = author;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
