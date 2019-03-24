package com.github.freeacs.controllers;

import com.github.freeacs.service.ProfileDto;
import com.github.freeacs.service.ProfileService;
import io.vavr.control.Option;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/profile")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping("/{id}")
    public Option<ProfileDto> getProfile(@PathVariable Long id) {
        return this.profileService.getProfile(id);
    }

    @PostMapping
    public Option<ProfileDto> createProfile(@RequestBody ProfileDto profileDto) {
        return this.profileService.createProfile(profileDto);
    }
}
