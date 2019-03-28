package com.github.freeacs.controllers;

import com.github.freeacs.service.ProfileDto;
import com.github.freeacs.service.ProfileService;
import io.vavr.collection.List;
import io.vavr.control.Option;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/profile")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping("/byUnitTypeId/{unitTypeId}")
    public List<ProfileDto> getProfiles(@PathVariable Long unitTypeId) {
        return this.profileService.getProfiles(unitTypeId);
    }

    @GetMapping("/{id}")
    public Option<ProfileDto> getProfileById(@PathVariable Long id) {
        return this.profileService.getProfileById(id);
    }

    @PostMapping
    public Option<ProfileDto> createProfile(@RequestBody ProfileDto profileDto) {
        return this.profileService.createProfile(profileDto);
    }
}
