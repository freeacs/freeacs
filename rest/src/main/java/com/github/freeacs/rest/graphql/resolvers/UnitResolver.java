package com.github.freeacs.rest.graphql.resolvers;

import com.coxautodev.graphql.tools.GraphQLQueryResolver;
import com.github.freeacs.dbi.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;

@Component
public class UnitResolver implements GraphQLQueryResolver {
    private static final Logger log = LoggerFactory.getLogger(UnitResolver.class);

    private final ACSUnit acsUnit;

    @Autowired
    public UnitResolver(DBI dbi) throws SQLException {
        this.acsUnit = new ACSUnit(dbi.getAcs().getDataSource(), dbi.getAcs(), dbi.getAcs().getSyslog());
    }

    @SuppressWarnings("unused")
    public Collection<Unit> getUnits(String search, String unittypeName, String profileName, Integer number) throws SQLException {
        Unittype unittype = null;
        Profile profile = null;
        if (unittypeName != null) {
            unittype = this.acsUnit.getAcs().getUnittype(unittypeName);
            if (unittype == null) {
                log.warn("Unittype " + unittypeName + " does not exist");
                return Collections.emptyList();
            } else if (profileName != null) {
                profile = unittype.getProfiles().getByName(profileName);
                if (profile == null) {
                    log.warn("Profile " + profileName + " does not exist in Unittype " + unittypeName);
                    return Collections.emptyList();
                }
            }
        }
        return acsUnit.getUnits(search, unittype, profile, number).values();
    }
}
