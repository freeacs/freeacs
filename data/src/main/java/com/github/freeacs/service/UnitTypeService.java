package com.github.freeacs.service;

import com.github.freeacs.cache.ProfileCache;
import com.github.freeacs.cache.UnitTypeCache;
import com.github.freeacs.dao.UnitType;
import io.vavr.collection.List;
import io.vavr.control.Option;
import org.springframework.stereotype.Service;

@Service
public class UnitTypeService {
    private final UnitTypeCache unitTypeCache;
    private final ProfileService profileService;

    public UnitTypeService(UnitTypeCache unitTypeCache, ProfileCache profileCache) {
        this.unitTypeCache = unitTypeCache;
        this.profileService = new ProfileService(profileCache, this);
    }

    public Option<UnitTypeDto> getUnitTypeById(Long id) {
        return unitTypeCache.getUnitTypeById(id)
                .map(unitType -> new UnitTypeDto(
                        unitType.getId(),
                        unitType.getName(),
                        unitType.getVendor(),
                        unitType.getDescription(),
                        unitType.getProtocol(),
                        profileService.getProfilesWithoutUnitType(unitType.getId())
                ));
    }

    public Option<UnitTypeDto> createUnitType(UnitTypeDto unitTypeDto) {
        Long newId = unitTypeCache.createUnitType(new UnitType(
                null,
                unitTypeDto.getName(),
                unitTypeDto.getVendor(),
                unitTypeDto.getDescription(),
                unitTypeDto.getProtocol()
        ));
        return getUnitTypeById(newId);
    }

    public List<UnitTypeDto> getUnitTypes() {
        return unitTypeCache.getUnitTypes()
                .flatMap(unitType -> getUnitTypeById(unitType.getId()));
    }
}
