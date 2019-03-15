package com.github.freeacs.rest.controllers;

import com.github.freeacs.rest.dtos.UnitDto;
import com.github.freeacs.rest.repositories.UnitRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Service
@RequestMapping("/search")
public class SearchController {

    private final UnitRepository unitRepository;

    @Autowired
    public SearchController(UnitRepository unitRepository) {
        this.unitRepository = unitRepository;
    }

    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public List<UnitDto> doSearch() {
        return unitRepository.getAllUnits();
    }
}
