package com.github.freeacs.rest.graphql.resolvers;

import com.coxautodev.graphql.tools.GraphQLQueryResolver;
import com.github.freeacs.dbi.*;
import com.github.freeacs.rest.util.Tuple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Optional;

@Component
public class UnitResolver implements GraphQLQueryResolver {
    private final ACSUnit acsUnit;

    @Autowired
    public UnitResolver(DBI dbi) throws SQLException {
        this.acsUnit = new ACSUnit(dbi.getAcs().getDataSource(), dbi.getAcs(), dbi.getAcs().getSyslog());
    }

    @SuppressWarnings("unused")
    public Collection<Unit> getUnits(String search, String unittypeName, String profileName, Integer limit) throws SQLException {
        Tuple<Unittype, Profile> tuple = getUnittypeAndProfile(unittypeName, profileName);
        return acsUnit.getUnits(search, tuple.getFirst(), tuple.getSecond(), limit).values();
    }

    private Tuple<Unittype, Profile> getUnittypeAndProfile(String unittypeName, String profileName) {
        return Optional.ofNullable(unittypeName)
                .map(this.acsUnit.getAcs()::getUnittype)
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
    }
}
