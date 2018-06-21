package com.github.freeacs.springshell;

import com.github.freeacs.dbi.Group;
import com.github.freeacs.dbi.Identity;
import com.github.freeacs.dbi.Job;
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
    private Job job;
    private Group group;

    @Autowired
    public ShellContext(Identity identity, @Value("${main.datasource.jdbcUrl}") String jdbcUrl) {
        this.user = identity.getUser().getUsername();
        String cleanedUrl = cleanUrl(jdbcUrl);
        URI uri = URI.create(cleanedUrl);
        this.dbHost = uri.getHost();
    }

    public void setUnitType(Unittype unitType) {
        this.unitType = unitType;
        this.unit = null;
        this.profile = null;
        this.group = null;
        this.job = null;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
        this.group = null;
        this.job = null;
        this.unit = null;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
        this.group = null;
        this.job = null;
    }

    public void setJob(Job job) {
        this.job = job;
        this.unit = null;
        this.profile = null;
        this.group = null;
    }

    public void setGroup(Group group) {
        this.group = group;
        this.unit = null;
        this.profile = null;
        this.job = null;
    }

    public Optional<Unittype> getUnittype() {
        return Optional.ofNullable(unitType);
    }

    public Optional<Profile> getProfile() {
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
        if (group != null) {
            sb.append("(").append(group.getName()).append(":g):");
        }
        if (job != null) {
            sb.append("(").append(job.getName()).append(":j):");
        }
        return sb.toString();
    }

    private String cleanUrl(String jdbcUrl) {
        return (jdbcUrl.contains("?")
                ? jdbcUrl.substring(0, jdbcUrl.indexOf("?"))
                : jdbcUrl).substring(5);
    }
}
