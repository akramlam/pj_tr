package com.example.api.service;

import com.example.api.repository.ProfileRepository;
import com.example.api.repository.UserRepository;
import com.example.core.domain.Profile;
import com.example.core.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class ProfileService {
    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;

    public ProfileService(ProfileRepository profileRepository, UserRepository userRepository) {
        this.profileRepository = profileRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ProfileDto createOrUpdate(String username, ProfileDto request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Profile profile = profileRepository.findByUser(user)
                .orElseGet(Profile::new);
        profile.setUser(user);
        profile.setFormation(request.getFormation());
        profile.setSkills(request.getSkills());
        profile.setProjects(request.getProjects());
        profile.setPreferences(request.getPreferences());
        profile = profileRepository.save(profile);
        return toDto(profile);
    }

    @Transactional(readOnly = true)
    public ProfileDto getByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Profile profile = profileRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Profile not found"));
        return toDto(profile);
    }

    private ProfileDto toDto(Profile profile) {
        ProfileDto dto = new ProfileDto();
        dto.setId(profile.getId());
        dto.setUsername(profile.getUser().getUsername());
        dto.setFormation(profile.getFormation());
        dto.setSkills(profile.getSkills());
        dto.setProjects(profile.getProjects());
        dto.setPreferences(profile.getPreferences());
        return dto;
    }

    public static class ProfileDto {
        private Long id;
        private String username;
        private String formation;
        private java.util.Set<String> skills;
        private java.util.Set<String> projects;
        private String preferences;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getFormation() { return formation; }
        public void setFormation(String formation) { this.formation = formation; }
        public java.util.Set<String> getSkills() { return skills; }
        public void setSkills(java.util.Set<String> skills) { this.skills = skills; }
        public java.util.Set<String> getProjects() { return projects; }
        public void setProjects(java.util.Set<String> projects) { this.projects = projects; }
        public String getPreferences() { return preferences; }
        public void setPreferences(String preferences) { this.preferences = preferences; }
    }
}