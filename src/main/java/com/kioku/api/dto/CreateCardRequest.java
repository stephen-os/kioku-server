package com.kioku.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateCardRequest {

    @NotBlank(message = "Front is required")
    @Size(max = 500, message = "Front must not exceed 500 characters")
    private String front;

    @NotBlank(message = "Back is required")
    @Size(max = 500, message = "Back must not exceed 500 characters")
    private String back;

    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;

    // Constructors
    public CreateCardRequest() {}

    public CreateCardRequest(String front, String back, String notes) {
        this.front = front;
        this.back = back;
        this.notes = notes;
    }

    // Getters and Setters
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
}