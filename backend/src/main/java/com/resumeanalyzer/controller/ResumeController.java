package com.resumeanalyzer.controller;

import com.resumeanalyzer.dto.ExtractRequest;
import com.resumeanalyzer.dto.ExtractResponse;
import com.resumeanalyzer.dto.ResumeUploadResponse;
import com.resumeanalyzer.model.Resume;
import com.resumeanalyzer.service.ResumeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/resume")
@CrossOrigin(origins = "*")
public class ResumeController {
    
    @Autowired
    private ResumeService resumeService;
    
    /**
     * Upload resume PDF file
     * @param file PDF file to upload
     * @return Upload response with resume details
     */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadResume(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "user_id", required = false) Integer userId) {
        try {
            log.info("Received resume upload request: {}", file.getOriginalFilename());
            
            // Upload resume
            Resume resume = resumeService.uploadResume(file, userId);
            
            // Prepare response
            ResumeUploadResponse response = new ResumeUploadResponse();
            response.setId(resume.getId());
            response.setFileName(resume.getFileName());
            response.setOriginalFilename(resume.getOriginalFilename());
            response.setCreatedAt(resume.getCreatedAt().toString());
            response.setMessage("Resume uploaded successfully");
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (IllegalArgumentException e) {
            log.warn("Invalid file upload: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error uploading resume", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to upload resume: " + e.getMessage()));
        }
    }
    
    /**
     * Extract text from resume PDF
     * @param request Extract request containing resume ID
     * @return Extracted text response
     */
    @PostMapping("/extract")
    public ResponseEntity<?> extractResume(@RequestBody ExtractRequest request) {
        try {
            log.info("Extracting text from resume ID: {}", request.getResumeId());
            
            if (request.getResumeId() == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Resume ID is required"));
            }
            
            // Extract text
            String extractedText = resumeService.extractResumeText(request.getResumeId());
            
            // Calculate word and character count
            int wordCount = extractedText.trim().split("\\s+").length;
            int characterCount = extractedText.length();
            
            // Prepare response
            ExtractResponse response = new ExtractResponse();
            response.setResumeId(request.getResumeId());
            response.setExtractedText(extractedText);
            response.setCharacterCount(characterCount);
            response.setWordCount(wordCount);
            response.setMessage("Text extracted successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            log.warn("Resume not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error extracting resume text", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to extract text: " + e.getMessage()));
        }
    }
    
    /**
     * Get resume details
     * @param resumeId Resume ID
     * @return Resume details
     */
    @GetMapping("/{resumeId}")
    public ResponseEntity<?> getResume(@PathVariable Integer resumeId) {
        try {
            Resume resume = resumeService.getResumeById(resumeId);
            return ResponseEntity.ok(resume);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Delete resume
     * @param resumeId Resume ID
     * @return Success message
     */
    @DeleteMapping("/{resumeId}")
    public ResponseEntity<?> deleteResume(@PathVariable Integer resumeId) {
        try {
            resumeService.deleteResume(resumeId);
            return ResponseEntity.ok(Map.of("message", "Resume deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error deleting resume", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to delete resume: " + e.getMessage()));
        }
    }
    
    /**
     * Health check endpoint
     * @return Health status
     */
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "Resume Upload Service",
            "timestamp", System.currentTimeMillis()
        ));
    }
}