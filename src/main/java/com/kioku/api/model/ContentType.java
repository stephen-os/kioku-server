package com.kioku.api.model;

/**
 * Enum representing the content type of a card side (front or back).
 *
 * <p>This determines how the frontend should render the card content:
 * <ul>
 *   <li>{@link #TEXT} - Plain text, rendered as-is</li>
 *   <li>{@link #CODE} - Code block, rendered with syntax highlighting</li>
 * </ul>
 *
 * @author Stephen Watson
 * @version 1.0
 * @since 1.0
 */
public enum ContentType {

    /**
     * Plain text content. This is the default type.
     * Content is rendered as regular text without special formatting.
     */
    TEXT,

    /**
     * Code content. Content is rendered as a code block with syntax highlighting.
     * When this type is used, a corresponding language should be specified.
     */
    CODE
}
