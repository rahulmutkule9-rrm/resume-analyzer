package com.resumeanalyzer.service;

import com.resumeanalyzer.dto.OpenAiRequest;
import com.resumeanalyzer.dto.OpenAiResponse;
import com.resumeanalyzer.util.AnalysisResponseParser;
import com.resumeanalyzer.util.PromptBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.util.Arrays;

@Slf4j
@Service
public class LlmAnalysisService {
    
    @Autowired
    private WebClient openaiWebClient;
    
    @Autowired
    private PromptBuilder promptBuilder;
    
    @Autowired
    private AnalysisResponseParser analysisResponseParser;
    
    @Value("${openai.api.key}")
    private String openaiApiKey;
    
    @Value("${openai.model:gpt-3.5-turbo}")
    private String model;
    
    /**
     * Analyze resume using OpenAI API
     */
    public AnalysisResponseParser.AnalysisResult analyzeResume(String resumeText) throws Exception {
        log.info("Starting resume analysis with LLM");
        
        // Check if API key is configured
        if (openaiApiKey == null || openaiApiKey.isEmpty() || openaiApiKey.equals("${OPENAI_API_KEY:}")) {
            log.warn("OpenAI API key not configured, using mock analysis");
            return getMockAnalysis();
        }
        
        try {
            // Build prompt
            String prompt = promptBuilder.buildAnalysisPrompt(resumeText);
            
            // Create request
            OpenAiRequest request = new OpenAiRequest();
            request.setModel(model);
            request.setMessages(Arrays.asList(
                new OpenAiRequest.Message("system", "You are an expert resume reviewer and career coach. Provide detailed, actionable feedback."),
                new OpenAiRequest.Message("user", prompt)
            ));
            request.setTemperature(0.7);
            request.setMaxTokens(2000);
            
            // Call OpenAI API
            OpenAiResponse response = openaiWebClient.post()
                    .uri("/chat/completions")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(OpenAiResponse.class)
                    .block();
            
            if (response == null || response.getChoices().isEmpty()) {
                throw new RuntimeException("Empty response from OpenAI API");
            }
            
            // Extract response content
            String analysisContent = response.getChoices().get(0).getMessage().getContent();
            log.info("Received analysis from LLM");
            
            // Parse response
            AnalysisResponseParser.AnalysisResult result = analysisResponseParser.parseAnalysisResponse(analysisContent);
            return result;
            
        } catch (Exception e) {
            log.error("Error calling OpenAI API: {}", e.getMessage());
            log.warn("Using mock analysis as fallback");
            return getMockAnalysis();
        }
    }
    
    /**
     * Mock analysis for testing without API key
     */
    private AnalysisResponseParser.AnalysisResult getMockAnalysis() {
        AnalysisResponseParser.AnalysisResult result = new AnalysisResponseParser.AnalysisResult();
        result.setScore(78);
        result.setStrengths(Arrays.asList(
            "Clear professional summary highlighting key achievements",
            "Well-organized work experience with quantifiable results",
            "Good use of industry-relevant keywords and technical skills",
            "Strong educational background and certifications"
        ));
        result.setMissingSkills(Arrays.asList(
            "Cloud platforms (AWS, Azure, GCP)",
            "Leadership and project management experience",
            "Data analysis and visualization tools",
            "Soft skills section (communication, problem-solving)"
        ));
        result.setSuggestions(Arrays.asList(
            "Add quantifiable metrics to job achievements",
            "Include a brief 'Technical Skills' section with relevant technologies",
            "Highlight any volunteer work or side projects",
            "Use consistent formatting and consistent bullet point style"
        ));
        result.setAtsTips(Arrays.asList(
            "Use standard section headers (Experience, Education, Skills)",
            "Avoid graphics, tables, and unusual formatting",
            "Use common fonts like Arial or Calibri",
            "Include relevant keywords from job postings",
            "Save resume as PDF to preserve formatting"
        ));
        result.setRawAnalysis("Mock analysis generated for testing purposes");
        return result;
    }
}
