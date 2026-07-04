package com.resumeanalyzer.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.LocalDateTime;

@Entity
@Table(name = "resume_analyses")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResumeAnalysis {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "resume_id", nullable = false)
    private Integer resumeId;
    
    @Column(name = "user_id", nullable = false)
    private Integer userId;
    
    @Column(name = "score")
    private Integer score;
    
    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;
    
    @Column(name = "strengths", columnDefinition = "JSON")
    private String strengths;
    
    @Column(name = "missing_skills", columnDefinition = "JSON")
    private String missingSkills;
    
    @Column(name = "suggestions", columnDefinition = "JSON")
    private String suggestions;
    
    @Column(name = "ats_tips", columnDefinition = "JSON")
    private String atsTips;
    
    @Column(name = "raw_analysis", columnDefinition = "JSON")
    private String rawAnalysis;
    
    @Column(name = "analyzed_at", nullable = false, updatable = false)
    private LocalDateTime analyzedAt;
    
    @PrePersist
    protected void onCreate() {
        analyzedAt = LocalDateTime.now();
    }
}