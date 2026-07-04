package com.resumeanalyzer.service;

import com.resumeanalyzer.model.Resume;
import com.resumeanalyzer.model.ResumeAnalysis;
import com.resumeanalyzer.repository.ResumeAnalysisRepository;
import com.resumeanalyzer.repository.ResumeRepository;
import com.resumeanalyzer.util.AnalysisResponseParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
public class ResumeAnalysisService {
    
    @Autowired
    private ResumeRepository resumeRepository;
    
    @Autowired
    private ResumeAnalysisRepository resumeAnalysisRepository;
    
    @Autowired
    private LlmAnalysisService llmAnalysisService;
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Analyze resume using LLM and save results
     */
    public ResumeAnalysis analyzeResume(Integer resumeId, Integer userId) throws Exception {
        log.info("Analyzing resume ID: {}", resumeId);
        
        // Get resume
        Optional<Resume> resume = resumeRepository.findById(resumeId);
        if (resume.isEmpty()) {
            throw new RuntimeException("Resume not found with ID: " + resumeId);
        }
        
        Resume resumeEntity = resume.get();
        
        // Get extracted text
        String extractedText = resumeEntity.getExtractedText();
        if (extractedText == null || extractedText.isEmpty()) {
            throw new RuntimeException("Resume text not extracted. Please extract text first.");
        }
        
        // Analyze using LLM
        AnalysisResponseParser.AnalysisResult analysisResult = llmAnalysisService.analyzeResume(extractedText);
        
        // Create analysis entity
        ResumeAnalysis analysis = new ResumeAnalysis();
        analysis.setResumeId(resumeId);
        analysis.setUserId(userId != null ? userId : 1);
        analysis.setScore(analysisResult.getScore());
        analysis.setSummary(generateSummary(analysisResult));
        
        // Convert lists to JSON strings
        analysis.setStrengths(objectMapper.writeValueAsString(analysisResult.getStrengths()));
        analysis.setMissingSkills(objectMapper.writeValueAsString(analysisResult.getMissingSkills()));
        analysis.setSuggestions(objectMapper.writeValueAsString(analysisResult.getSuggestions()));
        analysis.setAtsTips(objectMapper.writeValueAsString(analysisResult.getAtsTips()));
        analysis.setRawAnalysis(analysisResult.getRawAnalysis());
        analysis.setAnalyzedAt(LocalDateTime.now());
        
        // Save to database
        ResumeAnalysis savedAnalysis = resumeAnalysisRepository.save(analysis);
        log.info("Resume analysis saved with ID: {}", savedAnalysis.getId());
        
        return savedAnalysis;
    }
    
    /**
     * Get analysis by resume ID
     */
    public ResumeAnalysis getAnalysisByResumeId(Integer resumeId) {
        Optional<ResumeAnalysis> analysis = resumeAnalysisRepository.findByResumeId(resumeId);
        if (analysis.isEmpty()) {
            throw new RuntimeException("Analysis not found for resume ID: " + resumeId);
        }
        return analysis.get();
    }
    
    /**
     * Get analysis by ID
     */
    public ResumeAnalysis getAnalysisById(Integer analysisId) {
        return resumeAnalysisRepository.findById(analysisId)
                .orElseThrow(() -> new RuntimeException("Analysis not found with ID: " + analysisId));
    }
    
    /**
     * Generate summary based on score
     */
    private String generateSummary(AnalysisResponseParser.AnalysisResult result) {
        int score = result.getScore();
        if (score >= 85) {
            return "Excellent resume! Your document is well-structured with strong content and should perform well in ATS screening.";
        } else if (score >= 70) {
            return "Good resume with solid fundamentals. Some improvements in formatting and keyword optimization could enhance effectiveness.";
        } else if (score >= 55) {
            return "Fair resume that needs several improvements. Focus on clarity, organization, and relevant keyword inclusion.";
        } else {
            return "Resume needs significant improvements. Consider restructuring, adding metrics, and improving keyword alignment.";
        }
    }
}
