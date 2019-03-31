package com.github.freeacs.service;

import com.github.freeacs.cache.UnitCache;
import com.github.freeacs.dao.Unit;
import io.vavr.collection.List;
import io.vavr.control.Option;
import org.springframework.stereotype.Service;

@Service
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
                .flatMap(unit -> profileService.getProfileById(unit.getProfileId())
                        .flatMap(profileDto -> unitTypeService.getUnitTypeById(unit.getUnitTypeId())
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

    public List<UnitDto> searchForUnits(String term, List<Long> profiles, Integer limit) {
        return unitCache.searchForUnits(term, profiles, limit)
                .flatMap(unit -> profileService.getProfileById(unit.getProfileId())
                    .flatMap(profile -> unitTypeService.getUnitTypeById(unit.getUnitTypeId())
                        .map(unitType -> new UnitDto(
                                unit.getUnitId(),
                                profile,
                                unitType
                        ))
                    )
                );
    }
}
