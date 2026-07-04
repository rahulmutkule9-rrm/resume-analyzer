package com.resumeanalyzer.util;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;
import java.io.File;
import java.io.IOException;

@Component
public class PdfExtractor {
    
    /**
     * Extract text from a PDF file
     * @param filePath Path to the PDF file
     * @return Extracted text content
     * @throws IOException if PDF reading fails
     */
    public String extractTextFromPdf(String filePath) throws IOException {
        try (PDDocument document = PDDocument.load(new File(filePath))) {
            if (document.isEncrypted()) {
                throw new IOException("PDF is encrypted and cannot be processed");
            }
            
            PDFTextStripper pdfStripper = new PDFTextStripper();
            return pdfStripper.getText(document).trim();
        }
    }
    
    /**
     * Extract text with pagination info
     * @param filePath Path to the PDF file
     * @return Text with page separators
     * @throws IOException if PDF reading fails
     */
    public String extractTextWithPages(String filePath) throws IOException {
        try (PDDocument document = PDDocument.load(new File(filePath))) {
            if (document.isEncrypted()) {
                throw new IOException("PDF is encrypted and cannot be processed");
            }
            
            StringBuilder text = new StringBuilder();
            PDFTextStripper pdfStripper = new PDFTextStripper();
            
            for (int page = 0; page < document.getNumberOfPages(); page++) {
                pdfStripper.setStartPage(page + 1);
                pdfStripper.setEndPage(page + 1);
                String pageText = pdfStripper.getText(document);
                text.append("--- Page ").append(page + 1).append(" ---\n");
                text.append(pageText).append("\n");
            }
            
            return text.toString().trim();
        }
    }
    
    /**
     * Validate if file is a PDF
     * @param filePath Path to the file
     * @return true if file is a valid PDF
     */
    public boolean isValidPdf(String filePath) {
        try (PDDocument document = PDDocument.load(new File(filePath))) {
            return !document.isEncrypted();
        } catch (IOException e) {
            return false;
        }
    }
}