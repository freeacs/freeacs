package com.github.freeacs.controllers;

import com.github.freeacs.service.UnitTypeDto;
import com.github.freeacs.service.UnitTypeService;
import io.vavr.collection.List;
import io.vavr.control.Option;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/unittype")
public class UnitTypeController {

    private final UnitTypeService unitTypeService;

    public UnitTypeController(UnitTypeService unitTypeService) {
        this.unitTypeService = unitTypeService;
    }

    @GetMapping
    public List<UnitTypeDto> getUnitTypes() {
        return this.unitTypeService.getUnitTypes();
    }

    @GetMapping("/{id}")
    public Option<UnitTypeDto> getUnitTypeById(@PathVariable Long id) {
        return this.unitTypeService.getUnitTypeById(id);
    }

    @PostMapping
    public Option<UnitTypeDto> createUnitType(@RequestBody UnitTypeDto unitTypeDto) {
        return this.unitTypeService.createUnitType(unitTypeDto);
    }
}
