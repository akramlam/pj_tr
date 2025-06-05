package com.example.api.controller;

import com.example.api.service.MatchService;
import com.example.api.service.MatchService.MatchDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/match")
public class MatchController {
    private final MatchService matchService;

    public MatchController(MatchService matchService) {
        this.matchService = matchService;
    }

    /**
     * Get a list of matching profiles for the authenticated user.
     * @param limit maximum number of matches to return (optional, default=10)
     */
    @GetMapping
    public ResponseEntity<List<MatchDto>> getMatches(
            @RequestParam(name = "limit", defaultValue = "10") int limit,
            Principal principal) {
        List<MatchDto> matches = matchService.findMatches(principal.getName(), limit);
        return ResponseEntity.ok(matches);
    }
}