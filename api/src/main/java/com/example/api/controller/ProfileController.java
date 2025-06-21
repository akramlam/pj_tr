package com.example.api.controller;

import com.example.api.service.ProfileService;
import com.example.api.service.ProfileService.ProfileDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {
    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @PostMapping
    public ResponseEntity<ProfileDto> createProfile(@RequestBody ProfileDto request,
                                                   Principal principal) {
        ProfileDto dto = profileService.createOrUpdate(principal.getName(), request);
        return ResponseEntity.ok(dto);
    }

    @PutMapping
    public ResponseEntity<ProfileDto> updateProfile(@RequestBody ProfileDto request,
                                                   Principal principal) {
        ProfileDto dto = profileService.createOrUpdate(principal.getName(), request);
        return ResponseEntity.ok(dto);
    }

    @GetMapping
    public ResponseEntity<ProfileDto> getProfile(Principal principal) {
        ProfileDto dto = profileService.getByUsername(principal.getName());
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/current")
    public ResponseEntity<ProfileDto> getCurrentProfile(Principal principal) {
        ProfileDto dto = profileService.getByUsername(principal.getName());
        return ResponseEntity.ok(dto);
    }
}