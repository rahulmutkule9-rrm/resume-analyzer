package com.resumeanalyzer.controller;

import com.resumeanalyzer.model.Resume;
import com.resumeanalyzer.service.ResumeService;
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
public class HistoryController {
    
    @Autowired
    private ResumeService resumeService;
    
    /**
     * Get all resumes for a user (analysis history)
     * @param userId User ID from path
     * @return List of user's resumes
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserResumes(@PathVariable Integer userId) {
        try {
            log.info("Fetching resumes for user ID: {}", userId);
            
            List<Resume> resumes = resumeService.getUserResumes(userId);
            
            return ResponseEntity.ok(Map.of(
                "count", resumes.size(),
                "resumes", resumes
            ));
        } catch (Exception e) {
            log.error("Error fetching user resumes", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch resumes: " + e.getMessage()));
        }
    }
    
    /**
     * Get analysis history for a user
     * @param userId User ID
     * @return Analysis history
     */
    @GetMapping("/user/{userId}/analysis-history")
    public ResponseEntity<?> getAnalysisHistory(@PathVariable Integer userId) {
        try {
            log.info("Fetching analysis history for user ID: {}", userId);
            
            List<Resume> resumes = resumeService.getUserResumes(userId);
            
            return ResponseEntity.ok(Map.of(
                "count", resumes.size(),
                "history", resumes
            ));
        } catch (Exception e) {
            log.error("Error fetching analysis history", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch analysis history: " + e.getMessage()));
        }
    }
    
    /**
     * Search resumes by filename
     * @param userId User ID
     * @param query Search query
     * @return Filtered resumes
     */
    @GetMapping("/user/{userId}/search")
    public ResponseEntity<?> searchResumes(
            @PathVariable Integer userId,
            @RequestParam String query) {
        try {
            log.info("Searching resumes for user ID: {} with query: {}", userId, query);
            
            List<Resume> resumes = resumeService.getUserResumes(userId);
            
            // Filter by filename containing query
            List<Resume> filtered = resumes.stream()
                    .filter(r -> r.getOriginalFilename().toLowerCase().contains(query.toLowerCase()))
                    .toList();
            
            return ResponseEntity.ok(Map.of(
                "query", query,
                "count", filtered.size(),
                "resumes", filtered
            ));
        } catch (Exception e) {
            log.error("Error searching resumes", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Search failed: " + e.getMessage()));
        }
    }
}
