// API Configuration
const API_BASE_URL = 'http://localhost:8080/api';

// DOM Elements
const fileInput = document.getElementById('fileInput');
const uploadArea = document.getElementById('uploadArea');
const loadingState = document.getElementById('loadingState');
const resultsSection = document.getElementById('resultsSection');
const errorMessage = document.getElementById('errorMessage');

// Store resume ID globally for analysis
let currentResumeId = null;

// Event Listeners
uploadArea.addEventListener('click', () => fileInput.click());
uploadArea.addEventListener('dragover', handleDragOver);
uploadArea.addEventListener('dragleave', handleDragLeave);
uploadArea.addEventListener('drop', handleDrop);
fileInput.addEventListener('change', handleFileSelect);

/**
 * Handle file drag over
 */
function handleDragOver(e) {
    e.preventDefault();
    uploadArea.classList.add('dragover');
}

/**
 * Handle file drag leave
 */
function handleDragLeave(e) {
    e.preventDefault();
    uploadArea.classList.remove('dragover');
}

/**
 * Handle file drop
 */
function handleDrop(e) {
    e.preventDefault();
    uploadArea.classList.remove('dragover');
    
    const files = e.dataTransfer.files;
    if (files.length > 0) {
        fileInput.files = files;
        handleFileSelect({ target: { files } });
    }
}

/**
 * Handle file selection
 */
async function handleFileSelect(e) {
    const files = e.target.files;
    
    if (files.length === 0) return;
    
    const file = files[0];
    
    // Validate file
    if (!file.type.includes('pdf')) {
        showError('Please upload a PDF file');
        return;
    }
    
    if (file.size > 10 * 1024 * 1024) {
        showError('File size must be less than 10MB');
        return;
    }
    
    hideError();
    await uploadAndAnalyzeResume(file);
}

/**
 * Upload and analyze resume
 */
async function uploadAndAnalyzeResume(file) {
    try {
        // Show loading state
        uploadArea.style.display = 'none';
        loadingState.style.display = 'block';
        resultsSection.style.display = 'none';
        
        // Step 1: Upload resume
        const formData = new FormData();
        formData.append('file', file);
        
        loadingState.innerHTML = '<div class="spinner"></div><p>Uploading your resume...</p>';
        
        const uploadResponse = await fetch(`${API_BASE_URL}/v1/resume/upload`, {
            method: 'POST',
            body: formData
        });
        
        if (!uploadResponse.ok) {
            throw new Error('Failed to upload resume');
        }
        
        const uploadData = await uploadResponse.json();
        currentResumeId = uploadData.id;
        
        // Step 2: Extract text from PDF
        loadingState.innerHTML = '<div class="spinner"></div><p>Extracting text from PDF...</p>';
        
        const extractResponse = await fetch(`${API_BASE_URL}/v1/resume/extract`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ resume_id: currentResumeId })
        });
        
        if (!extractResponse.ok) {
            throw new Error('Failed to extract resume text');
        }
        
        // Step 3: Analyze resume with AI
        await analyzeResumeWithAI(currentResumeId);
        
    } catch (error) {
        console.error('Error:', error);
        showError(error.message || 'An error occurred while analyzing your resume');
    } finally {
        loadingState.style.display = 'none';
    }
}

/**
 * Analyze resume with AI
 */
async function analyzeResumeWithAI(resumeId) {
    try {
        loadingState.style.display = 'block';
        loadingState.innerHTML = '<div class="spinner"></div><p>Analyzing your resume with AI...</p>';
        resultsSection.style.display = 'none';
        
        // Call analysis endpoint
        const response = await fetch(`${API_BASE_URL}/v1/resume/analyze`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ resume_id: resumeId })
        });
        
        if (!response.ok) {
            throw new Error('Failed to analyze resume');
        }
        
        const analysisData = await response.json();
        displayResults(analysisData);
        
    } catch (error) {
        console.error('Analysis error:', error);
        showError(error.message || 'Failed to analyze resume');
        loadingState.style.display = 'none';
    }
}

/**
 * Display analysis results
 */
function displayResults(data) {
    // Update Score
    const score = data.score || 0;
    updateScoreDisplay(score);
    
    // Update Strengths
    const strengthsList = document.getElementById('strengthsList');
    strengthsList.innerHTML = '';
    (data.strengths || []).forEach(strength => {
        const li = document.createElement('li');
        li.textContent = strength;
        strengthsList.appendChild(li);
    });
    
    // Update Missing Skills
    const skillsList = document.getElementById('skillsList');
    skillsList.innerHTML = '';
    (data.missing_skills || []).forEach(skill => {
        const li = document.createElement('li');
        li.textContent = skill;
        skillsList.appendChild(li);
    });
    
    // Update Suggestions
    const suggestionsList = document.getElementById('suggestionsList');
    suggestionsList.innerHTML = '';
    (data.suggestions || []).forEach(suggestion => {
        const li = document.createElement('li');
        li.textContent = suggestion;
        suggestionsList.appendChild(li);
    });
    
    // Update ATS Tips
    const atsTipsList = document.getElementById('atsTipsList');
    atsTipsList.innerHTML = '';
    (data.ats_tips || []).forEach(tip => {
        const li = document.createElement('li');
        li.textContent = tip;
        atsTipsList.appendChild(li);
    });
    
    // Show results section
    resultsSection.style.display = 'block';
    uploadArea.style.display = 'none';
    
    // Scroll to results
    resultsSection.scrollIntoView({ behavior: 'smooth' });
}

/**
 * Update score display with animation
 */
function updateScoreDisplay(score) {
    const scoreText = document.getElementById('scoreText');
    const scoreCircle = document.getElementById('scoreCircle');
    const scoreMessage = document.getElementById('scoreMessage');
    
    // Animate score number
    animateValue(scoreText, 0, score, 1000);
    
    // Update circle progress
    const circumference = 282.6;
    const offset = circumference - (score / 100) * circumference;
    scoreCircle.style.strokeDashoffset = offset;
    
    // Update message based on score
    let message = '';
    if (score >= 80) {
        message = '🎉 Excellent! Your resume is well-optimized.';
    } else if (score >= 60) {
        message = '👍 Good! Some improvements recommended.';
    } else if (score >= 40) {
        message = '⚠️ Fair. Several improvements needed.';
    } else {
        message = '❌ Needs significant improvements.';
    }
    scoreMessage.textContent = message;
}

/**
 * Animate number counting
 */
function animateValue(element, start, end, duration) {
    let startTimestamp = null;
    
    const step = (timestamp) => {
        if (!startTimestamp) startTimestamp = timestamp;
        const progress = Math.min((timestamp - startTimestamp) / duration, 1);
        const value = Math.floor(progress * (end - start) + start);
        element.textContent = value;
        
        if (progress < 1) {
            requestAnimationFrame(step);
        }
    };
    
    requestAnimationFrame(step);
}

/**
 * Download report
 */
function downloadReport() {
    // TODO: Implement report download
    alert('Report download feature coming soon!');
}

/**
 * Reset form
 */
function resetForm() {
    fileInput.value = '';
    uploadArea.style.display = 'block';
    resultsSection.style.display = 'none';
    hideError();
}

/**
 * Show error message
 */
function showError(message) {
    errorMessage.textContent = message;
    errorMessage.style.display = 'block';
}

/**
 * Hide error message
 */
function hideError() {
    errorMessage.style.display = 'none';
    errorMessage.textContent = '';
}

/**
 * Scroll to upload section
 */
function scrollToUpload() {
    document.getElementById('upload').scrollIntoView({ behavior: 'smooth' });
}

/**
 * Initialize app
 */
function initializeApp() {
    console.log('Resume Analyzer initialized');
    
    // Check API health
    fetch(`${API_BASE_URL}/health`)
        .then(response => response.text())
        .then(data => console.log('API Status:', data))
        .catch(error => console.warn('API not available:', error));
}

// Initialize when DOM is ready
document.addEventListener('DOMContentLoaded', initializeApp);
