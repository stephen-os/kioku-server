package com.kioku.api.dto;

import com.kioku.api.entity.Tag;

public class TagResponse {

    private Long id;
    private String name;

    // Constructors
    public TagResponse() {}

    public TagResponse(Tag tag) {
        this.id = tag.getId();
        this.name = tag.getName();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}