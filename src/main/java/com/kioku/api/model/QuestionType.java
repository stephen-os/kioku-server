package com.kioku.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The kind of answer a question expects.
 *
 * <p>Stored as the Java constant name, but serialised in the snake_case form
 * the desktop app already uses, so the wire format stays unchanged.
 */
public enum QuestionType {

    @JsonProperty("multiple_choice")
    MULTIPLE_CHOICE,

    @JsonProperty("fill_in_blank")
    FILL_IN_BLANK
}
