package com.github.freeacs.controllers;

import com.github.freeacs.service.UnitDto;
import com.github.freeacs.service.UnitService;
import io.vavr.collection.List;
import io.vavr.control.Option;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/unit")
public class UnitController {

    private final UnitService unitService;

    public UnitController(UnitService unitService) {
        this.unitService = unitService;
    }

    @GetMapping("/{unitId}")
    public Option<UnitDto> getUnit(@PathVariable String unitId) {
        return unitService.getUnit(unitId);
    }

    @PostMapping
    public Option<UnitDto>  createUnit(@RequestBody UnitDto payload) {
        return unitService.createUnit(payload);
    }

    @GetMapping("/search")
    public List<UnitDto> searchForUnits(@RequestParam String term,
                                        @RequestParam List<Long> profiles,
                                        @RequestParam Integer limit) {
        return unitService.searchForUnits(term, profiles, limit);
    }
}
