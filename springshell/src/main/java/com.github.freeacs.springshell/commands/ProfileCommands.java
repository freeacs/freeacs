package com.github.freeacs.springshell.commands;

import com.github.freeacs.dbi.DBI;
import com.github.freeacs.dbi.Profile;
import com.github.freeacs.springshell.ShellContext;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.sql.SQLException;

@ShellComponent
@ShellCommandGroup("Profile commands")
public class ProfileCommands extends ShellCommands {
    private final DBI dbi;

    public ProfileCommands(DBI dbi, ShellContext shellContext) {
        super(shellContext);
        this.dbi = dbi;
    }

    @ShellMethod("Set profile context")
    public String setProfile(@ShellOption String profileName) {
        return doOnUnittype((unittype) -> {
            Profile profile = dbi.getAcs().getProfile(unittype.getName(), profileName);
            if (profile == null) {
                throw new IllegalArgumentException("Profile does not exist");
            }
            shellContext.setProfile(profile);
            return "Changed profile to " + profileName;
        });
    }

    @ShellMethod("Create profile")
    public String createProfile(@ShellOption String profileName) throws SQLException {
        return doOnUnittype((unittype) -> {
            Profile newProfile = new Profile(profileName, unittype);
            try {
                unittype.getProfiles().addOrChangeProfile(newProfile, dbi.getAcs());
            } catch (SQLException e) {
                throw new IllegalArgumentException("Failed to create profile", e);
            }
            return null;
        });
    }
}
