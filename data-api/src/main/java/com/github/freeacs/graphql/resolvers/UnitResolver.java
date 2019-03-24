package com.github.freeacs.graphql.resolvers;

import com.coxautodev.graphql.tools.GraphQLQueryResolver;
import com.github.freeacs.service.ProfileService;
import com.github.freeacs.service.UnitDto;
import com.github.freeacs.service.UnitService;
import com.github.freeacs.service.UnitTypeService;
import io.vavr.collection.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UnitResolver implements GraphQLQueryResolver {
    private final UnitService unitService;
    private final ProfileService profileService;
    private final UnitTypeService unitTypeService;

    @Autowired
    public UnitResolver(UnitService unitService, ProfileService profileService, UnitTypeService unitTypeService) {
        this.unitService = unitService;
        this.profileService = profileService;
        this.unitTypeService = unitTypeService;
    }

    @SuppressWarnings("unused")
    public List<UnitDto> getUnits(String search, String unittypeName, String profileName, Integer limit) {
        return List.empty();
    }

/*    private Tuple2<UnitTypeDto, ProfileDto> getUnittypeAndProfile(String unittypeName, String profileName) {
        return Optional.ofNullable(unittypeName)
                .map(this.unitTypeService::getUnitTypeByName)
                .flatMap(unittype -> {
                    Optional<Tuple<Unittype, Profile>> tuple = Optional.ofNullable(profileName)
                            .map(unittype.getProfiles()::getByName)
                            .map(profile -> new Tuple<>(unittype, profile));
                    if (tuple.isPresent()) {
                        return tuple;
                    }
                    return Optional.of(new Tuple<>(unittype, null));
                })
                .orElseGet(() -> new Tuple<>(null, null));
    }*/
}
