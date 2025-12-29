package com.kioku.api.dto;

import com.kioku.api.entity.Card;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

public class CardResponse {

    private Long id;
    private String front;
    private String back;
    private String notes;
    private String frontAudioUrl;
    private String backAudioUrl;
    private Set<TagResponse> tags;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors
    public CardResponse() {}

    public CardResponse(Card card) {
        this.id = card.getId();
        this.front = card.getFront();
        this.back = card.getBack();
        this.notes = card.getNotes();
        this.frontAudioUrl = card.getFrontAudioUrl();
        this.backAudioUrl = card.getBackAudioUrl();
        this.tags = card.getTags().stream()
                .map(TagResponse::new)
                .collect(Collectors.toSet());
        this.createdAt = card.getCreatedAt();
        this.updatedAt = card.getUpdatedAt();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFront() {
        return front;
    }

    public void setFront(String front) {
        this.front = front;
    }

    public String getBack() {
        return back;
    }

    public void setBack(String back) {
        this.back = back;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getFrontAudioUrl() {
        return frontAudioUrl;
    }

    public void setFrontAudioUrl(String frontAudioUrl) {
        this.frontAudioUrl = frontAudioUrl;
    }

    public String getBackAudioUrl() {
        return backAudioUrl;
    }

    public void setBackAudioUrl(String backAudioUrl) {
        this.backAudioUrl = backAudioUrl;
    }

    public Set<TagResponse> getTags() {
        return tags;
    }

    public void setTags(Set<TagResponse> tags) {
        this.tags = tags;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}