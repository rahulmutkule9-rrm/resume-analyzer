package com.resumeanalyzer.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Component
public class FileStorageUtil {
    
    @Value("${app.file.upload-dir:uploads}")
    private String uploadDir;
    
    /**
     * Save uploaded file to disk
     * @param fileBytes File content in bytes
     * @param originalFilename Original filename
     * @return Path where file was saved
     */
    public String saveFile(byte[] fileBytes, String originalFilename) throws Exception {
        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        // Generate unique filename
        String uniqueFileName = UUID.randomUUID() + "_" + sanitizeFileName(originalFilename);
        Path filePath = uploadPath.resolve(uniqueFileName);
        
        // Save file
        Files.write(filePath, fileBytes);
        
        return filePath.toString();
    }
    
    /**
     * Delete file from disk
     * @param filePath Path to file to delete
     * @return true if deletion was successful
     */
    public boolean deleteFile(String filePath) {
        try {
            Path path = Paths.get(filePath);
            if (Files.exists(path)) {
                Files.delete(path);
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Check if file exists
     * @param filePath Path to file
     * @return true if file exists
     */
    public boolean fileExists(String filePath) {
        return Files.exists(Paths.get(filePath));
    }
    
    /**
     * Sanitize filename to prevent security issues
     * @param filename Original filename
     * @return Sanitized filename
     */
    private String sanitizeFileName(String filename) {
        return filename.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
    
    /**
     * Get file size
     * @param filePath Path to file
     * @return File size in bytes
     */
    public long getFileSize(String filePath) throws Exception {
        return Files.size(Paths.get(filePath));
    }
}