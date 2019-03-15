package com.github.freeacs.rest.repositories;

import com.github.freeacs.rest.dtos.UnitDto;
import org.jdbi.v3.sqlobject.config.RegisterFieldMapper;
import org.jdbi.v3.sqlobject.statement.SqlQuery;

import java.util.List;

public interface UnitRepository{

    String BASIC_UNIT_QUERY =
            "select unit.unit_id, profile.id as profile_id, profile.name as profile_name, unit_type.id as unit_type_id, unit_type.name as unit_type_name " +
                    "from unit " +
                    "inner join profile on unit.profile_id = profile.profile_id " +
                    "inner join unit_type on unit.unit_type_id = unit_type.unit_type_id ";

    @SqlQuery(BASIC_UNIT_QUERY)
    @RegisterFieldMapper(UnitDto.class)
    List<UnitDto> getAllUnits();

}
