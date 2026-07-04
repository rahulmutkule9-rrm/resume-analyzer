package com.resumeanalyzer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResumeUploadResponse {
    private Integer id;
    private String fileName;
    private String originalFilename;
    @JsonProperty("created_at")
    private String createdAt;
    private String message;
}