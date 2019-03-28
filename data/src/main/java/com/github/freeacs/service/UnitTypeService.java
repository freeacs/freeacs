package com.github.freeacs.service;

import com.github.freeacs.cache.UnitTypeCache;
import com.github.freeacs.dao.UnitType;
import io.vavr.collection.List;
import io.vavr.control.Option;
import org.springframework.stereotype.Component;

@Component
public class UnitTypeService {
    private final UnitTypeCache unitTypeCache;

    public UnitTypeService(UnitTypeCache unitTypeCache) {
        this.unitTypeCache = unitTypeCache;
    }

    public Option<UnitTypeDto> getUnitTypeById(Long id) {
        return unitTypeCache.getUnitTypeById(id)
                .map(unitType -> new UnitTypeDto(
                        unitType.getId(),
                        unitType.getName(),
                        unitType.getVendor(),
                        unitType.getDescription(),
                        unitType.getProtocol()
                ));
    }

    public Option<UnitTypeDto> getUnitTypeByName(String name) {
        return unitTypeCache.getUnitTypeByName(name)
                .map(unitType -> new UnitTypeDto(
                        unitType.getId(),
                        unitType.getName(),
                        unitType.getVendor(),
                        unitType.getDescription(),
                        unitType.getProtocol()
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
