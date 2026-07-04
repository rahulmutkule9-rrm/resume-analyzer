package com.resumeanalyzer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResumeAnalysisResponse {
    private Integer id;
    private Integer resumeId;
    private Integer score;
    private String summary;
    private List<String> strengths;
    @JsonProperty("missing_skills")
    private List<String> missingSkills;
    private List<String> suggestions;
    @JsonProperty("ats_tips")
    private List<String> atsTips;
}