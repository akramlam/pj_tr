package com.example.api.service;

import com.example.api.repository.ProfileRepository;
import com.example.core.domain.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class MatchService {
    private final ProfileRepository profileRepository;

    public MatchService(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    /**
     * Find matching profiles for the given username, limited by count.
     * Matching is based on shared skills and same formation.
     */
    @Transactional(readOnly = true)
    public List<MatchDto> findMatches(String username, int limit) {
        // load all profiles
        List<Profile> all = profileRepository.findAll();
        // find current user's profile
        Profile me = all.stream()
                .filter(p -> username.equals(p.getUser().getUsername()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Profile not found for user: " + username));

        List<MatchDto> matches = new ArrayList<>();
        Set<String> mySkills = new HashSet<>(me.getSkills());
        String myFormation = me.getFormation();

        for (Profile p : all) {
            if (username.equals(p.getUser().getUsername())) {
                continue;
            }
            
            // compute shared skills
            Set<String> common = new HashSet<>(mySkills);
            common.retainAll(p.getSkills());
            int skillScore = common.size();
            
            // Calculate compatibility score based on shared skills and formation
            int compatibilityScore = skillScore;
            
            // Users with same formation get a base compatibility score even without shared skills
            if (myFormation.equals(p.getFormation()) && skillScore == 0) {
                compatibilityScore = 1; // Base score for same formation
            }
            
            // Only include matches with some compatibility
            if (compatibilityScore <= 0) {
                continue;
            }
            
            matches.add(new MatchDto(
                    p.getUser().getId(),
                    p.getUser().getUsername(),
                    p.getFormation(),
                    p.getSkills(),
                    p.getPreferences(),
                    compatibilityScore
            ));
        }
        // sort by descending score
        return matches.stream()
                .sorted(Comparator.comparingInt(MatchDto::getScore).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * DTO for match results.
     */
    public static class MatchDto {
        private Long userId;
        private String username;
        private String formation;
        private Set<String> skills;
        private String preferences;
        private int score;

        public MatchDto(Long userId, String username, String formation, Set<String> skills, String preferences, int score) {
            this.userId = userId;
            this.username = username;
            this.formation = formation;
            this.skills = skills;
            this.preferences = preferences;
            this.score = score;
        }

        public Long getUserId() { return userId; }
        public String getUsername() { return username; }
        public String getFormation() { return formation; }
        public Set<String> getSkills() { return skills; }
        public String getPreferences() { return preferences; }
        public int getScore() { return score; }
    }
}