// Store authentication token in localStorage
let authToken = localStorage.getItem('authToken');
let currentUserId = localStorage.getItem('userId');
let currentUserEmail = localStorage.getItem('userEmail');

/**
 * Handle user signup
 */
async function handleSignup(email, password, firstName, lastName) {
    try {
        const response = await fetch(`${API_BASE_URL}/v1/auth/signup`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                email,
                password,
                firstName,
                lastName
            })
        });

        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.error || 'Signup failed');
        }

        const data = await response.json();
        const token = response.headers.get('Authorization')?.split(' ')[1];
        
        // Store credentials
        if (token) {
            localStorage.setItem('authToken', token);
            localStorage.setItem('userId', data.id);
            localStorage.setItem('userEmail', data.email);
            authToken = token;
            currentUserId = data.id;
            currentUserEmail = data.email;
        }
        
        console.log('Signup successful');
        return data;
    } catch (error) {
        console.error('Signup error:', error);
        throw error;
    }
}

/**
 * Handle user login
 */
async function handleLogin(email, password) {
    try {
        const response = await fetch(`${API_BASE_URL}/v1/auth/login`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ email, password })
        });

        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.error || 'Login failed');
        }

        const data = await response.json();
        
        // Store credentials
        localStorage.setItem('authToken', data.token);
        localStorage.setItem('userId', data.id);
        localStorage.setItem('userEmail', data.email);
        authToken = data.token;
        currentUserId = data.id;
        currentUserEmail = data.email;
        
        console.log('Login successful');
        return data;
    } catch (error) {
        console.error('Login error:', error);
        throw error;
    }
}

/**
 * Logout user
 */
function handleLogout() {
    localStorage.removeItem('authToken');
    localStorage.removeItem('userId');
    localStorage.removeItem('userEmail');
    authToken = null;
    currentUserId = null;
    currentUserEmail = null;
    console.log('Logged out');
}

/**
 * Get analysis history for current user
 */
async function getAnalysisHistory() {
    try {
        if (!currentUserId) {
            throw new Error('User not authenticated');
        }
        
        const response = await fetch(`${API_BASE_URL}/v1/resume/user/${currentUserId}`, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${authToken}`
            }
        });

        if (!response.ok) {
            throw new Error('Failed to fetch analysis history');
        }

        return await response.json();
    } catch (error) {
        console.error('Error fetching history:', error);
        throw error;
    }
}

/**
 * Check if user is authenticated
 */
function isAuthenticated() {
    return !!authToken && !!currentUserId;
}

/**
 * Get current user info
 */
async function getCurrentUserInfo() {
    try {
        const response = await fetch(`${API_BASE_URL}/v1/auth/me`, {
            headers: {
                'Authorization': `Bearer ${authToken}`
            }
        });

        if (!response.ok) {
            throw new Error('Failed to fetch user info');
        }

        return await response.json();
    } catch (error) {
        console.error('Error fetching user info:', error);
        return null;
    }
}

/**
 * Update upload and analyze with authentication
 */
async function uploadAndAnalyzeResumeWithAuth(file) {
    try {
        uploadArea.style.display = 'none';
        loadingState.style.display = 'block';
        resultsSection.style.display = 'none';
        
        const formData = new FormData();
        formData.append('file', file);
        if (currentUserId) {
            formData.append('user_id', currentUserId);
        }
        
        loadingState.innerHTML = '<div class="spinner"></div><p>Uploading your resume...</p>';
        
        const uploadResponse = await fetch(`${API_BASE_URL}/v1/resume/upload`, {
            method: 'POST',
            body: formData,
            headers: authToken ? { 'Authorization': `Bearer ${authToken}` } : {}
        });
        
        if (!uploadResponse.ok) {
            throw new Error('Failed to upload resume');
        }
        
        const uploadData = await uploadResponse.json();
        currentResumeId = uploadData.id;
        
        loadingState.innerHTML = '<div class="spinner"></div><p>Extracting text from PDF...</p>';
        
        const extractResponse = await fetch(`${API_BASE_URL}/v1/resume/extract`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                ...(authToken && { 'Authorization': `Bearer ${authToken}` })
            },
            body: JSON.stringify({ resume_id: currentResumeId })
        });
        
        if (!extractResponse.ok) {
            throw new Error('Failed to extract resume text');
        }
        
        await analyzeResumeWithAI(currentResumeId);
        
    } catch (error) {
        console.error('Error:', error);
        showError(error.message || 'An error occurred while analyzing your resume');
    } finally {
        loadingState.style.display = 'none';
    }
}
