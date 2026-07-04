package com.resumeanalyzer.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class AnalysisResponseParser {
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Parse LLM response and extract analysis components
     */
    public AnalysisResult parseAnalysisResponse(String response) {
        log.info("Parsing analysis response");
        AnalysisResult result = new AnalysisResult();
        
        try {
            // Extract score (0-100)
            result.setScore(extractScore(response));
            
            // Extract strengths
            result.setStrengths(extractSection(response, "strength"));
            
            // Extract missing skills
            result.setMissingSkills(extractSection(response, "skill"));
            
            // Extract suggestions
            result.setSuggestions(extractSection(response, "suggestion"));
            
            // Extract ATS tips
            result.setAtsTips(extractSection(response, "ats"));
            
            // Store raw analysis
            result.setRawAnalysis(response);
            
            log.info("Analysis response parsed successfully. Score: {}", result.getScore());
        } catch (Exception e) {
            log.error("Error parsing analysis response", e);
        }
        
        return result;
    }
    
    /**
     * Extract score from response (0-100)
     */
    private Integer extractScore(String response) {
        try {
            // Look for patterns like "Score: 85", "score: 85", "85/100", etc.
            Pattern patterns[] = {
                Pattern.compile("[Ss]core[:\\s]+([0-9]{1,3})(?:[/\\s]out[\\s]of[\\s]100)?"),
                Pattern.compile("([0-9]{1,3})/100"),
                Pattern.compile("([0-9]{1,3})%")
            };
            
            for (Pattern pattern : patterns) {
                Matcher matcher = pattern.matcher(response);
                if (matcher.find()) {
                    int score = Integer.parseInt(matcher.group(1));
                    if (score >= 0 && score <= 100) {
                        return score;
                    }
                }
            }
            
            // Default score if not found
            return 50;
        } catch (Exception e) {
            log.warn("Could not parse score from response", e);
            return 50;
        }
    }
    
    /**
     * Extract list items from a section
     */
    private List<String> extractSection(String response, String sectionKeyword) {
        List<String> items = new ArrayList<>();
        
        try {
            // Split response into lines
            String[] lines = response.split("\n");
            boolean inSection = false;
            
            for (String line : lines) {
                String lowerLine = line.toLowerCase();
                
                // Check if we're entering a relevant section
                if (lowerLine.contains(sectionKeyword)) {
                    inSection = true;
                    continue;
                }
                
                // Exit section if we hit another header
                if (inSection && (lowerLine.contains(":") && !line.startsWith("-") && !line.startsWith("•") && !line.startsWith("*"))) {
                    if (items.size() > 0) {
                        break;
                    }
                }
                
                // Extract items from section
                if (inSection) {
                    String trimmed = line.replaceAll("^[\\s\\-•*]+", "").trim();
                    if (!trimmed.isEmpty() && trimmed.length() > 2) {
                        items.add(trimmed);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Error extracting section: {}", sectionKeyword, e);
        }
        
        // Return at most 8 items per section
        return items.subList(0, Math.min(8, items.size()));
    }
    
    /**
     * Result class for analysis parsing
     */
    public static class AnalysisResult {
        private Integer score;
        private List<String> strengths = new ArrayList<>();
        private List<String> missingSkills = new ArrayList<>();
        private List<String> suggestions = new ArrayList<>();
        private List<String> atsTips = new ArrayList<>();
        private String rawAnalysis;
        
        // Getters and Setters
        public Integer getScore() { return score; }
        public void setScore(Integer score) { this.score = score; }
        
        public List<String> getStrengths() { return strengths; }
        public void setStrengths(List<String> strengths) { this.strengths = strengths; }
        
        public List<String> getMissingSkills() { return missingSkills; }
        public void setMissingSkills(List<String> missingSkills) { this.missingSkills = missingSkills; }
        
        public List<String> getSuggestions() { return suggestions; }
        public void setSuggestions(List<String> suggestions) { this.suggestions = suggestions; }
        
        public List<String> getAtsTips() { return atsTips; }
        public void setAtsTips(List<String> atsTips) { this.atsTips = atsTips; }
        
        public String getRawAnalysis() { return rawAnalysis; }
        public void setRawAnalysis(String rawAnalysis) { this.rawAnalysis = rawAnalysis; }
    }
}
