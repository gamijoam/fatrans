package com.tufondo.creditos.infrastructure.security;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class XssSanitizer {

    private static final Pattern HTML_SCRIPT_PATTERN =
        Pattern.compile("<script[^>]*>.*?</script>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern HTML_STYLE_PATTERN =
        Pattern.compile("<style[^>]*>.*?</style>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern HTML_TAG_PATTERN =
        Pattern.compile("<[^>]+>", Pattern.CASE_INSENSITIVE);
    private static final Pattern HTML_ENTITY_PATTERN =
        Pattern.compile("&[a-zA-Z]+;|&#\\d+;");
    private static final Pattern JAVASCRIPT_PROTOCOL_PATTERN =
        Pattern.compile("javascript\\s*:", Pattern.CASE_INSENSITIVE);
    private static final Pattern ON_EVENT_PATTERN =
        Pattern.compile("\\bon\\w+\\s*=", Pattern.CASE_INSENSITIVE);

    public String sanitize(String input) {
        if (input == null || input.isBlank()) {
            return input;
        }

        String sanitized = input;

        sanitized = HTML_SCRIPT_PATTERN.matcher(sanitized).replaceAll("");
        sanitized = HTML_STYLE_PATTERN.matcher(sanitized).replaceAll("");
        sanitized = JAVASCRIPT_PROTOCOL_PATTERN.matcher(sanitized).replaceAll("");
        sanitized = ON_EVENT_PATTERN.matcher(sanitized).replaceAll("");
        sanitized = HTML_ENTITY_PATTERN.matcher(sanitized).replaceAll("");
        sanitized = HTML_TAG_PATTERN.matcher(sanitized).replaceAll("");

        return sanitized.trim();
    }

    public String sanitizeDescription(String description) {
        if (description == null) {
            return null;
        }
        String sanitized = sanitize(description);
        return sanitized.replaceAll("['\"\\\\]", "");
    }

    public String sanitizeUsername(String username) {
        if (username == null) {
            return null;
        }
        return sanitize(username).toLowerCase().trim();
    }
}