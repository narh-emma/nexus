package com.nexus.authservice.service;

import com.nexus.authservice.dto.ProfileRequest;
import com.nexus.authservice.dto.ProfileResponse;
import com.nexus.authservice.model.User;
import com.nexus.authservice.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;

@Service
public class ProfileService {

    @Autowired
    private UserRepository userRepository;

    // ===== GET PROFILE =====
    public ProfileResponse getProfile(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
        return convertToProfileResponse(user);
    }

    // ===== UPDATE PROFILE =====
    public ProfileResponse updateProfile(String email, ProfileRequest request) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));

        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getDateOfBirth() != null) {
            user.setDateOfBirth(request.getDateOfBirth());
        }
        if (request.getBio() != null) {
            user.setBio(request.getBio());
        }
        if (request.getPreferredLanguage() != null) {
            user.setPreferredLanguage(request.getPreferredLanguage());
        }
        if (request.getSignDialect() != null) {
            user.setSignDialect(request.getSignDialect());
        }
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new RuntimeException("Email already exists!");
            }
            user.setEmail(request.getEmail());
        }

        user.setUpdatedAt(LocalDateTime.now());
        User updatedUser = userRepository.save(user);
        return convertToProfileResponse(updatedUser);
    }

    // ===== UPLOAD PROFILE PICTURE =====
    public String uploadProfilePicture(String email, MultipartFile file) throws IOException {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));

        // Validate file
        if (file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new RuntimeException("Only image files are allowed");
        }

        if (file.getSize() > 5 * 1024 * 1024) { // 5MB
            throw new RuntimeException("File size exceeds 5MB limit");
        }

        // Create directory if not exists
        String uploadDir = "./uploads/profile-pictures/";
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Generate filename
        String fileName = user.getId() + "_" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path filePath = uploadPath.resolve(fileName);

        // Save file
        Files.write(filePath, file.getBytes());

        // Update user
        String fileUrl = "/uploads/profile-pictures/" + fileName;
        user.setProfilePictureUrl(fileUrl);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        return fileUrl;
    }

    // ===== DELETE PROFILE PICTURE =====
    public void deleteProfilePicture(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));

        user.setProfilePictureUrl(null);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    // ===== HELPER METHOD =====
    private ProfileResponse convertToProfileResponse(User user) {
        return new ProfileResponse(
            user.getId().toString(),
            user.getEmail(),
            user.getFullName(),
            user.getIndexNumber(),
            user.getPhoneNumber(),
            user.getDateOfBirth(),
            user.getBio(),
            user.getProfilePictureUrl(),
            user.getPreferredLanguage(),
            user.getSignDialect(),
            user.getRole(),
            user.getCreatedAt(),
            user.getUpdatedAt()
        );
    }
}