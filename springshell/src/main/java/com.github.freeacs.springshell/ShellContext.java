package com.github.freeacs.springshell;

import com.github.freeacs.dbi.Identity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;

@Component
public class ShellContext {

    private final String db;
    private final String user;

    private String unitType;
    private String profile;
    private String unit;

    @Autowired
    public ShellContext(Identity identity, @Value("${main.datasource.jdbcUrl}") String db) {
        this.user = identity.getUser().getUsername();
        String cleanedUrl = (db.contains("?") ? db.substring(0, db.indexOf("?")) : db).substring(5);
        URI uri = URI.create(cleanedUrl);
        this.db = uri.getHost();
    }

    void setUnitType(String unitType) {
        this.unitType = unitType;
    }

    void setProfile(String profile) {
        this.profile = profile;
    }

    void setUnit(String unit) {
        this.unit = unit;
    }

    public String getUser() {
        return user;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("shell(").append(user).append("@" + db + "):");
        if (unitType != null) {
            sb.append("(").append(unitType).append(":ut):");
        }
        if (profile != null) {
            sb.append("(").append(profile).append(":pr):");
        }
        if (unit != null) {
            sb.append("(").append(unit).append(":u):");
        }
        return sb.toString();
    }
}
