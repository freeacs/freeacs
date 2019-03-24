package com.github.freeacs.controllers;

import com.github.freeacs.service.UnitTypeDto;
import com.github.freeacs.service.UnitTypeService;
import io.vavr.control.Option;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/unittype")
public class UnitTypeController {

    private final UnitTypeService unitTypeService;

    public UnitTypeController(UnitTypeService unitTypeService) {
        this.unitTypeService = unitTypeService;
    }

    @GetMapping("/{id}")
    public Option<UnitTypeDto> getUnitType(@PathVariable Long id) {
        return this.unitTypeService.getUnitType(id);
    }

    @PostMapping
    public Option<UnitTypeDto> createUnitType(@RequestBody UnitTypeDto unitTypeDto) {
        return this.unitTypeService.createUnitType(unitTypeDto);
    }
}
