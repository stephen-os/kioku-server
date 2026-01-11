package com.kioku.api.model;

/**
 * Enum representing supported programming languages for syntax highlighting.
 *
 * <p>This enum defines the languages that can be used when a card's content type
 * is {@link ContentType#CODE}. The frontend uses this to apply appropriate
 * syntax highlighting.
 *
 * <p>The enum values are designed to match common syntax highlighter identifiers
 * (e.g., Prism.js, highlight.js) for easy frontend integration.
 *
 * @author Stephen Watson
 * @version 1.0
 * @since 1.0
 */
public enum CodeLanguage {

    // Plain text (no highlighting)
    PLAINTEXT("Plain Text"),

    // Web
    JAVASCRIPT("JavaScript"),
    TYPESCRIPT("TypeScript"),
    HTML("HTML"),
    CSS("CSS"),

    // Systems Programming
    C("C"),
    CPP("C++"),
    RUST("Rust"),
    GO("Go"),

    // JVM Languages
    JAVA("Java"),
    KOTLIN("Kotlin"),
    SCALA("Scala"),

    // Scripting
    PYTHON("Python"),
    RUBY("Ruby"),
    PHP("PHP"),
    PERL("Perl"),

    // Mobile
    SWIFT("Swift"),
    DART("Dart"),

    // .NET
    CSHARP("C#"),
    FSHARP("F#"),

    // Functional
    HASKELL("Haskell"),
    ELIXIR("Elixir"),
    CLOJURE("Clojure"),

    // Data & Query
    SQL("SQL"),
    GRAPHQL("GraphQL"),
    R("R"),

    // Config & Data Formats
    JSON("JSON"),
    YAML("YAML"),
    XML("XML"),
    TOML("TOML"),
    MARKDOWN("Markdown"),

    // Shell
    BASH("Bash"),
    POWERSHELL("PowerShell"),

    // Other
    DOCKER("Dockerfile"),
    REGEX("Regex");

    private final String displayName;

    CodeLanguage(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Gets the human-readable display name for this language.
     *
     * @return the display name
     */
    public String getDisplayName() {
        return displayName;
    }
}
