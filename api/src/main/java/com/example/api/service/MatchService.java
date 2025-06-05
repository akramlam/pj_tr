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
     * Matching is based on number of shared skills.
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

        for (Profile p : all) {
            if (username.equals(p.getUser().getUsername())) {
                continue;
            }
            // compute shared skills
            Set<String> common = new HashSet<>(mySkills);
            common.retainAll(p.getSkills());
            int score = common.size();
            if (score <= 0) {
                continue;
            }
            matches.add(new MatchDto(
                    p.getUser().getUsername(),
                    p.getFormation(),
                    p.getSkills(),
                    p.getPreferences(),
                    score
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
        private String username;
        private String formation;
        private Set<String> skills;
        private String preferences;
        private int score;

        public MatchDto(String username, String formation, Set<String> skills, String preferences, int score) {
            this.username = username;
            this.formation = formation;
            this.skills = skills;
            this.preferences = preferences;
            this.score = score;
        }

        public String getUsername() { return username; }
        public String getFormation() { return formation; }
        public Set<String> getSkills() { return skills; }
        public String getPreferences() { return preferences; }
        public int getScore() { return score; }
    }
}