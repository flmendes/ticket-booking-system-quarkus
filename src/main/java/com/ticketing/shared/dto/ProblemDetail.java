package com.ticketing.shared.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Problem Details for HTTP APIs - RFC 7807
 * https://datatracker.ietf.org/doc/html/rfc7807
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProblemDetail {

    /**
     * A URI reference that identifies the problem type
     */
    @JsonProperty("type")
    private URI type;

    /**
     * A short, human-readable summary of the problem type
     */
    @JsonProperty("title")
    private String title;

    /**
     * The HTTP status code
     */
    @JsonProperty("status")
    private Integer status;

    /**
     * A human-readable explanation specific to this occurrence of the problem
     */
    @JsonProperty("detail")
    private String detail;

    /**
     * A URI reference that identifies the specific occurrence of the problem
     */
    @JsonProperty("instance")
    private URI instance;

    /**
     * Timestamp when the error occurred
     */
    @JsonProperty("timestamp")
    private LocalDateTime timestamp;

    /**
     * Additional properties/extensions
     */
    @JsonProperty("extensions")
    private Map<String, Object> extensions;

    /**
     * Add an extension property
     */
    public void addExtension(String key, Object value) {
        if (this.extensions == null) {
            this.extensions = new HashMap<>();
        }
        this.extensions.put(key, value);
    }

    /**
     * Create a ProblemDetail with basic information
     */
    public static ProblemDetail forStatus(int status, String title, String detail) {
        return ProblemDetail.builder()
            .status(status)
            .title(title)
            .detail(detail)
            .timestamp(LocalDateTime.now())
            .type(URI.create("about:blank"))
            .build();
    }

    /**
     * Create a ProblemDetail with type URI
     */
    public static ProblemDetail forStatusAndType(int status, URI type, String title, String detail) {
        return ProblemDetail.builder()
            .status(status)
            .type(type)
            .title(title)
            .detail(detail)
            .timestamp(LocalDateTime.now())
            .build();
    }
}
