package com.github.freeacs.service;

import com.github.freeacs.cache.ProfileCache;
import io.vavr.control.Option;
import org.springframework.stereotype.Component;

@Component
public class ProfileService {
    private final ProfileCache profileCache;
    private final UnitTypeService unitTypeService;

    public ProfileService(ProfileCache profileCache, UnitTypeService unitTypeService) {
        this.profileCache = profileCache;
        this.unitTypeService = unitTypeService;
    }

    public Option<ProfileDto> getProfile(Long id) {
        return profileCache.getProfile(id)
                .flatMap(profile -> unitTypeService.getUnitType(profile.getUnitTypeId())
                        .map(unitTypeDto -> new ProfileDto(
                                profile.getId(),
                                profile.getName(),
                                unitTypeDto
                        ))
                );
    }
}
