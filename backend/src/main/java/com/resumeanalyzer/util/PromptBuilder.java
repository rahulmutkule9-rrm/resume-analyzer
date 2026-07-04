package com.resumeanalyzer.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PromptBuilder {
    
    /**
     * Build prompt for resume analysis
     */
    public String buildAnalysisPrompt(String resumeText) {
        return """Analyze the following resume and provide detailed feedback. Format your response with clear sections.

RESUME:
""" + resumeText + """

Provide analysis in the following format:

**SCORE**: Rate the resume from 0-100 based on overall quality, clarity, and effectiveness.

**STRENGTHS** (List 3-5 key strengths):
- 
- 
- 

**MISSING SKILLS** (List 3-5 important skills that should be added):
- 
- 
- 

**SUGGESTIONS FOR IMPROVEMENT** (List 3-5 actionable suggestions):
- 
- 
- 

**ATS OPTIMIZATION TIPS** (List 3-5 tips to optimize for Applicant Tracking Systems):
- 
- 
- 

Be specific, constructive, and professional in your feedback. Focus on practical improvements that can enhance the resume's impact.
""";
    }
    
    /**
     * Build prompt for skill gap analysis
     */
    public String buildSkillGapPrompt(String resumeText, String jobDescription) {
        return """Compare the resume with the job description to identify skill gaps.

RESUME:
""" + resumeText + """

JOB DESCRIPTION:
""" + jobDescription + """

Identify:
1. Skills in the job description that are missing from the resume
2. Relevant experience that could be better highlighted
3. Recommendations to make the resume more aligned with the job requirements
""";
    }
    
    /**
     * Build prompt for industry-specific analysis
     */
    public String buildIndustryAnalysisPrompt(String resumeText, String industry) {
        return """Analyze this resume specifically for the """ + industry + """ industry.

RESUME:
""" + resumeText + """

Provide feedback on:
1. Whether the resume meets industry standards
2. Industry-specific keywords and terminology that should be included
3. How to better position experience for this industry
4. Relevant certifications or skills commonly required in """ + industry + """
""";
    }
}
