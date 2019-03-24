package com.github.freeacs.service;

import com.github.freeacs.cache.UnitCache;
import io.vavr.control.Option;

public class UnitService {

    private final UnitCache unitCache;
    private final ProfileService profileService;
    private final UnitTypeService unitTypeService;

    public UnitService(UnitCache unitCache, ProfileService profileService, UnitTypeService unitTypeService) {
        this.unitCache = unitCache;
        this.profileService = profileService;
        this.unitTypeService = unitTypeService;
    }

    public Option<UnitDto> getUnit(String unitId) {
        return unitCache.getUnit(unitId)
                .flatMap(unit -> profileService.getProfile(unit.getProfileId())
                        .flatMap(profileDto -> unitTypeService.getUnitType(unit.getUnitTypeId())
                                .map(unitTypeDto -> new UnitDto(unit.getUnitId(), profileDto, unitTypeDto))
                        )
                );
    }
}
