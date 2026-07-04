package com.resumeanalyzer.controller;

import com.resumeanalyzer.dto.AnalysisRequest;
import com.resumeanalyzer.dto.ResumeAnalysisResponse;
import com.resumeanalyzer.model.ResumeAnalysis;
import com.resumeanalyzer.service.ResumeAnalysisService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/resume")
@CrossOrigin(origins = "*")
public class AnalysisController {
    
    @Autowired
    private ResumeAnalysisService resumeAnalysisService;
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Analyze resume using AI
     * @param request Analysis request with resume ID
     * @return Analysis results
     */
    @PostMapping("/analyze")
    public ResponseEntity<?> analyzeResume(
            @RequestBody AnalysisRequest request,
            @RequestParam(value = "user_id", required = false) Integer userId) {
        try {
            log.info("Received analysis request for resume ID: {}", request.getResumeId());
            
            if (request.getResumeId() == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Resume ID is required"));
            }
            
            // Perform analysis
            ResumeAnalysis analysis = resumeAnalysisService.analyzeResume(request.getResumeId(), userId);
            
            // Prepare response
            ResumeAnalysisResponse response = new ResumeAnalysisResponse();
            response.setId(analysis.getId());
            response.setResumeId(analysis.getResumeId());
            response.setScore(analysis.getScore());
            response.setSummary(analysis.getSummary());
            
            // Parse JSON strings back to lists
            response.setStrengths(objectMapper.readValue(analysis.getStrengths(), 
                    objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)));
            response.setMissingSkills(objectMapper.readValue(analysis.getMissingSkills(), 
                    objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)));
            response.setSuggestions(objectMapper.readValue(analysis.getSuggestions(), 
                    objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)));
            response.setAtsTips(objectMapper.readValue(analysis.getAtsTips(), 
                    objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)));
            
            log.info("Analysis completed successfully. Score: {}", analysis.getScore());
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            log.warn("Analysis request failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error analyzing resume", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to analyze resume: " + e.getMessage()));
        }
    }
    
    /**
     * Get analysis results by resume ID
     * @param resumeId Resume ID
     * @return Analysis results
     */
    @GetMapping("/{resumeId}/analysis")
    public ResponseEntity<?> getAnalysisByResumeId(@PathVariable Integer resumeId) {
        try {
            ResumeAnalysis analysis = resumeAnalysisService.getAnalysisByResumeId(resumeId);
            
            // Prepare response
            ResumeAnalysisResponse response = new ResumeAnalysisResponse();
            response.setId(analysis.getId());
            response.setResumeId(analysis.getResumeId());
            response.setScore(analysis.getScore());
            response.setSummary(analysis.getSummary());
            
            // Parse JSON strings back to lists
            response.setStrengths(objectMapper.readValue(analysis.getStrengths(), 
                    objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)));
            response.setMissingSkills(objectMapper.readValue(analysis.getMissingSkills(), 
                    objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)));
            response.setSuggestions(objectMapper.readValue(analysis.getSuggestions(), 
                    objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)));
            response.setAtsTips(objectMapper.readValue(analysis.getAtsTips(), 
                    objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)));
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error fetching analysis", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch analysis: " + e.getMessage()));
        }
    }
}
