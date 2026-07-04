package com.resumeanalyzer.service;

import com.resumeanalyzer.dto.LoginRequest;
import com.resumeanalyzer.dto.SignupRequest;
import com.resumeanalyzer.model.User;
import com.resumeanalyzer.repository.UserRepository;
import com.resumeanalyzer.security.JwtTokenProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
public class AuthService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    
    /**
     * Register new user
     */
    public User signup(SignupRequest request) throws Exception {
        log.info("Registering new user with email: {}", request.getEmail());
        
        // Validate email
        if (request.getEmail() == null || request.getEmail().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
        
        if (request.getPassword() == null || request.getPassword().length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }
        
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }
        
        // Create user
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(encodePassword(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        
        User savedUser = userRepository.save(user);
        log.info("User registered successfully with ID: {}", savedUser.getId());
        
        return savedUser;
    }
    
    /**
     * Login user
     */
    public User login(LoginRequest request) throws Exception {
        log.info("Attempting login for email: {}", request.getEmail());
        
        // Find user by email
        Optional<User> user = userRepository.findByEmail(request.getEmail());
        if (user.isEmpty()) {
            throw new IllegalArgumentException("Invalid email or password");
        }
        
        // Validate password
        User foundUser = user.get();
        if (!validatePassword(request.getPassword(), foundUser.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
        }
        
        log.info("User logged in successfully: {}", foundUser.getId());
        return foundUser;
    }
    
    /**
     * Generate JWT token for user
     */
    public String generateToken(User user) {
        return jwtTokenProvider.generateToken(user.getId(), user.getEmail());
    }
    
    /**
     * Validate JWT token
     */
    public boolean validateToken(String token) {
        return jwtTokenProvider.validateToken(token);
    }
    
    /**
     * Get user from token
     */
    public User getUserFromToken(String token) {
        Integer userId = jwtTokenProvider.getUserIdFromToken(token);
        if (userId != null) {
            return userRepository.findById(userId).orElse(null);
        }
        return null;
    }
    
    /**
     * Get user by ID
     */
    public User getUserById(Integer userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
    }
    
    /**
     * Encode password (for Phase 3, using basic encoding)
     */
    private String encodePassword(String password) {
        // For now, use Base64 encoding for demo
        // In production, use BCrypt or similar
        return java.util.Base64.getEncoder().encodeToString(password.getBytes());
    }
    
    /**
     * Validate password
     */
    private boolean validatePassword(String rawPassword, String encodedPassword) {
        return encodePassword(rawPassword).equals(encodedPassword);
    }
}
