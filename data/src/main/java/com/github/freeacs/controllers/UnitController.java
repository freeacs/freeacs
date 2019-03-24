package com.github.freeacs.controllers;

import com.github.freeacs.service.UnitDto;
import com.github.freeacs.service.UnitService;
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
    @ResponseBody
    public Option<UnitDto> getUnit(@PathVariable String unitId) {
        return unitService.getUnit(unitId);
    }
}
