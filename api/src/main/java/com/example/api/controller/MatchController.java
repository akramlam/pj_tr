package com.example.api.controller;

import com.example.api.service.MatchService;
import com.example.api.service.MatchService.MatchDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/matches")
public class MatchController {
    private final MatchService matchService;

    public MatchController(MatchService matchService) {
        this.matchService = matchService;
    }

    /**
     * Get potential matches for the authenticated user
     */
    @GetMapping("/potential")
    public ResponseEntity<List<PotentialMatchDto>> getPotentialMatches(Principal principal) {
        List<MatchDto> matches = matchService.findMatches(principal.getName(), 20);
        
        List<PotentialMatchDto> potentialMatches = matches.stream()
                .map(match -> {
                    PotentialMatchDto dto = new PotentialMatchDto();
                    dto.userId = (long) match.getUsername().hashCode(); // Simple ID generation
                    dto.username = match.getUsername();
                    dto.formation = match.getFormation();
                    
                    // Calculate common skills and compatibility
                    Set<String> commonSkills = new HashSet<>(match.getSkills());
                    dto.commonSkills = commonSkills;
                    dto.compatibilityScore = Math.min(95, match.getScore() * 15 + 40); // Scale to percentage
                    
                    return dto;
                })
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(potentialMatches);
    }

    /**
     * Send a match request (like/pass)
     */
    @PostMapping("/action")
    public ResponseEntity<MatchResponseDto> sendMatchAction(
            @RequestBody MatchRequestDto request,
            Principal principal) {
        
        // For now, simulate match logic
        // In a real app, you'd store the like/pass in database
        MatchResponseDto response = new MatchResponseDto();
        
        if ("LIKE".equals(request.action)) {
            // Simulate 30% chance of mutual match
            response.isMatch = Math.random() < 0.3;
            response.message = response.isMatch ? "It's a match!" : "Like sent successfully";
        } else {
            response.isMatch = false;
            response.message = "Passed";
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * Legacy endpoint for backward compatibility
     */
    @PostMapping("/request")
    public ResponseEntity<MatchResponseDto> sendMatchRequest(
            @RequestBody MatchRequestDto request,
            Principal principal) {
        return sendMatchAction(request, principal);
    }

    // DTOs to match frontend expectations
    public static class PotentialMatchDto {
        public Long userId;
        public String username;
        public String formation;
        public Set<String> commonSkills;
        public int compatibilityScore;
    }

    public static class MatchRequestDto {
        public Long targetUserId;
        public String action; // "LIKE" or "PASS"
    }

    public static class MatchResponseDto {
        public boolean isMatch;
        public String message;
    }
}