package com.github.freeacs.service;

import com.github.freeacs.cache.UnitCache;
import com.github.freeacs.dao.Unit;
import io.vavr.control.Option;
import org.springframework.stereotype.Component;

@Component
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
                                .map(unitTypeDto -> new UnitDto(
                                        unit.getUnitId(),
                                        profileDto,
                                        unitTypeDto
                                ))
                        )
                );
    }

    public Option<UnitDto> createUnit(UnitDto unitDto) {
        unitCache.createUnit(new Unit(
                unitDto.getUnitId(),
                unitDto.getProfile().getId(),
                unitDto.getUnitType().getId()
        ));
        return getUnit(unitDto.getUnitId());
    }
}
