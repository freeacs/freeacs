package com.github.freeacs.service;

import com.github.freeacs.cache.ProfileCache;
import com.github.freeacs.dao.Profile;
import io.vavr.collection.List;
import io.vavr.control.Option;
import org.springframework.stereotype.Service;

@Service
public class ProfileService {
    private final ProfileCache profileCache;
    private final UnitTypeService unitTypeService;

    public ProfileService(ProfileCache profileCache, UnitTypeService unitTypeService) {
        this.profileCache = profileCache;
        this.unitTypeService = unitTypeService;
    }

    public Option<ProfileDto> getProfileById(Long id) {
        return profileCache.getProfileById(id)
                .flatMap(profile -> unitTypeService.getUnitTypeById(profile.getUnitTypeId())
                        .map(unitTypeDto -> new ProfileDto(
                                profile.getId(),
                                profile.getName(),
                                unitTypeDto
                        ))
                );
    }

    public Option<ProfileDto> getProfileByIdWithoutUnitType(Long id) {
        return profileCache.getProfileById(id)
                .map(profile -> new ProfileDto(
                        profile.getId(),
                        profile.getName(),
                        null
                ));
    }

    public Option<ProfileDto> createProfile(ProfileDto profileDto) {
        Long newId = profileCache.createProfile(new Profile(
                null,
                profileDto.getName(),
                profileDto.getUnitType().getId()
        ));
        return getProfileById(newId);
    }

    public List<ProfileDto> getProfiles(Long unitTypeId) {
        return profileCache.getProfiles(unitTypeId)
                .flatMap(profile -> getProfileById(profile.getId()));
    }

    public List<ProfileDto> getProfilesWithoutUnitType(Long unitTypeId) {
        return profileCache.getProfiles(unitTypeId)
                .flatMap(profile -> getProfileByIdWithoutUnitType(profile.getId()));
    }
}
