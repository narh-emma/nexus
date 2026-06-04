package com.nexus.authservice.service;

import com.nexus.authservice.model.User;
import com.nexus.authservice.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    
    @Autowired
    private UserRepository userRepository;
    
    public User register(String email, String password, String fullName, String indexNumber) {
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already exists!");
        }
        
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(password);  
        user.setFullName(fullName);
        user.setIndexNumber(indexNumber);
        user.setPreferredLanguage("en");
        user.setSignDialect("ASL");
        user.setMedicalPreferences("{}");  
        
        return userRepository.save(user);
    }
    
    public User login(String email, String password) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found!"));
        
        if (!user.getPasswordHash().equals(password)) {
            throw new RuntimeException("Invalid password!");
        }
        
        return user;
    }
}