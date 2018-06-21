package com.github.freeacs.springshell;

import com.github.freeacs.dbi.Identity;
import com.github.freeacs.dbi.Profile;
import com.github.freeacs.dbi.Unit;
import com.github.freeacs.dbi.Unittype;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.Optional;

@Component
public class ShellContext {

    private final String dbHost;
    private final String user;

    private Unittype unitType;
    private Profile profile;
    private Unit unit;

    @Autowired
    public ShellContext(Identity identity, @Value("${main.datasource.jdbcUrl}") String jdbcUrl) {
        this.user = identity.getUser().getUsername();
        String cleanedUrl = cleanUrl(jdbcUrl);
        URI uri = URI.create(cleanedUrl);
        this.dbHost = uri.getHost();
    }

    void setUnitType(Unittype unitType) {
        this.unitType = unitType;
    }

    void setProfile(Profile profile) {
        this.profile = profile;
    }

    void setUnit(Unit unit) {
        this.unit = unit;
    }

    Optional<Unittype> getUnittype() {
        return Optional.ofNullable(unitType);
    }

    Optional<Profile> getProfile() {
        return Optional.ofNullable(profile);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("shell(").append(user).append("@").append(dbHost).append("):");
        if (unitType != null) {
            sb.append("(").append(unitType.getName()).append(":ut):");
        }
        if (profile != null) {
            sb.append("(").append(profile.getName()).append(":pr):");
        }
        if (unit != null) {
            sb.append("(").append(unit.getId()).append(":u):");
        }
        return sb.toString();
    }

    private String cleanUrl(String jdbcUrl) {
        return (jdbcUrl.contains("?")
                ? jdbcUrl.substring(0, jdbcUrl.indexOf("?"))
                : jdbcUrl).substring(5);
    }
}
