package com.github.freeacs.service;

import com.github.freeacs.cache.UnitTypeCache;
import io.vavr.control.Option;

public class UnitTypeService {
    private final UnitTypeCache unitTypeCache;

    public UnitTypeService(UnitTypeCache unitTypeCache) {
        this.unitTypeCache = unitTypeCache;
    }

    public Option<UnitTypeDto> getUnitType(Long id) {
        return unitTypeCache.getUnitType(id)
                .map(unitType -> new UnitTypeDto(
                        unitType.getId(),
                        unitType.getName(),
                        unitType.getVendor(),
                        unitType.getDescription(),
                        unitType.getProtocol()
                ));
    }
}
