package com.github.freeacs.rest.controllers;

import com.github.freeacs.rest.dtos.ProfileDto;
import com.github.freeacs.rest.dtos.UnitDto;
import com.github.freeacs.rest.dtos.UnitTypeDto;
import org.jdbi.v3.core.Jdbi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@RequestMapping("/search")
public class SearchController {

    @Autowired
    private Jdbi jdbi;

    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public List<UnitDto> doSearch() {
        return jdbi.withHandle(handle -> handle.createQuery(
                "select unit.unit_id, profile.id, profile.name, unit_type.id, unit_type.name " +
                "from unit " +
                "inner join profile on unit.profile_id = profile.profile_id " +
                "inner join unit_type on unit.unit_type_id = unit_type.unit_type_id "
        ).map((rs, ctx) -> new UnitDto(
                rs.getString("unit_id"),
                new ProfileDto(rs.getLong("profile_id"), rs.getString("name")),
                new UnitTypeDto(rs.getLong("unit_type_id"), rs.getString("name")))
        ).list());
    }
}
