package com.resumeanalyzer.service;

import com.resumeanalyzer.model.Resume;
import com.resumeanalyzer.repository.ResumeRepository;
import com.resumeanalyzer.util.FileStorageUtil;
import com.resumeanalyzer.util.PdfExtractor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class ResumeService {
    
    @Autowired
    private ResumeRepository resumeRepository;
    
    @Autowired
    private FileStorageUtil fileStorageUtil;
    
    @Autowired
    private PdfExtractor pdfExtractor;
    
    /**
     * Upload resume file
     * @param file Uploaded file
     * @param userId User ID (for Phase 3)
     * @return Saved resume entity
     */
    public Resume uploadResume(MultipartFile file, Integer userId) throws Exception {
        log.info("Uploading resume: {}", file.getOriginalFilename());
        
        // Validate file
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        
        if (!file.getContentType().equals("application/pdf")) {
            throw new IllegalArgumentException("Only PDF files are allowed");
        }
        
        // Save file to disk
        String filePath = fileStorageUtil.saveFile(file.getBytes(), file.getOriginalFilename());
        
        // Create resume entity
        Resume resume = new Resume();
        resume.setUserId(userId != null ? userId : 1); // Default user ID for Phase 1
        resume.setOriginalFilename(file.getOriginalFilename());
        resume.setFileName(filePath.substring(filePath.lastIndexOf("/") + 1));
        resume.setFilePath(filePath);
        resume.setCreatedAt(LocalDateTime.now());
        resume.setUpdatedAt(LocalDateTime.now());
        
        // Save to database
        Resume savedResume = resumeRepository.save(resume);
        log.info("Resume saved with ID: {}", savedResume.getId());
        
        return savedResume;
    }
    
    /**
     * Extract text from resume PDF
     * @param resumeId Resume ID
     * @return Extracted text
     */
    public String extractResumeText(Integer resumeId) throws Exception {
        log.info("Extracting text from resume ID: {}", resumeId);
        
        Optional<Resume> resume = resumeRepository.findById(resumeId);
        if (resume.isEmpty()) {
            throw new RuntimeException("Resume not found with ID: " + resumeId);
        }
        
        String filePath = resume.get().getFilePath();
        
        // Validate file exists
        if (!fileStorageUtil.fileExists(filePath)) {
            throw new RuntimeException("Resume file not found: " + filePath);
        }
        
        // Extract text using PDFBox
        String extractedText = pdfExtractor.extractTextFromPdf(filePath);
        
        // Update resume with extracted text
        Resume resumeEntity = resume.get();
        resumeEntity.setExtractedText(extractedText);
        resumeEntity.setUpdatedAt(LocalDateTime.now());
        resumeRepository.save(resumeEntity);
        
        log.info("Text extracted successfully. Character count: {}", extractedText.length());
        return extractedText;
    }
    
    /**
     * Get resume by ID
     * @param resumeId Resume ID
     * @return Resume entity
     */
    public Resume getResumeById(Integer resumeId) {
        return resumeRepository.findById(resumeId)
                .orElseThrow(() -> new RuntimeException("Resume not found with ID: " + resumeId));
    }
    
    /**
     * Get all resumes for a user (Phase 3)
     * @param userId User ID
     * @return List of resumes
     */
    public List<Resume> getUserResumes(Integer userId) {
        return resumeRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
    
    /**
     * Delete resume
     * @param resumeId Resume ID
     */
    public void deleteResume(Integer resumeId) throws Exception {
        log.info("Deleting resume ID: {}", resumeId);
        
        Optional<Resume> resume = resumeRepository.findById(resumeId);
        if (resume.isEmpty()) {
            throw new RuntimeException("Resume not found with ID: " + resumeId);
        }
        
        // Delete file from disk
        String filePath = resume.get().getFilePath();
        fileStorageUtil.deleteFile(filePath);
        
        // Delete from database
        resumeRepository.deleteById(resumeId);
        log.info("Resume deleted successfully");
    }
}