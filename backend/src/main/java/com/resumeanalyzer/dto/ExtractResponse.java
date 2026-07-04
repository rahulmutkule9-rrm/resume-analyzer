package com.resumeanalyzer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExtractResponse {
    @JsonProperty("resume_id")
    private Integer resumeId;
    @JsonProperty("extracted_text")
    private String extractedText;
    @JsonProperty("character_count")
    private Integer characterCount;
    @JsonProperty("word_count")
    private Integer wordCount;
    private String message;
}