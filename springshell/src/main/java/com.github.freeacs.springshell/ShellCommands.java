package com.github.freeacs.springshell;

import com.github.freeacs.dbi.ACS;
import com.github.freeacs.dbi.ACSUnit;
import com.github.freeacs.dbi.DBI;
import com.github.freeacs.dbi.Group;
import com.github.freeacs.dbi.Job;
import com.github.freeacs.dbi.Profile;
import com.github.freeacs.dbi.Unit;
import com.github.freeacs.dbi.UnitJobs;
import com.github.freeacs.dbi.Unittype;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.sql.SQLException;
import java.util.Collections;

@ShellComponent
public class ShellCommands {

    private final DBI dbi;
    private final ACSUnit acsUnit;
    private final UnitJobs unitJobs;
    private final ShellContext shellContext;

    @Autowired
    public ShellCommands(DBI dbi, ACSUnit acsUnit, UnitJobs unitJobs, ShellContext shellContext) {
        this.dbi = dbi;
        this.acsUnit = acsUnit;
        this.unitJobs = unitJobs;
        this.shellContext = shellContext;
    }

    @ShellMethod("Create unittype")
    public String createUnittype(
            @ShellOption String unittype,
            @ShellOption String vendor,
            @ShellOption String description,
            @ShellOption Unittype.ProvisioningProtocol protocol) throws SQLException {
        ACS acs = dbi.getAcs();
        if (acs.getUnittype(unittype) == null) {
            Unittype newUnitType = new Unittype(unittype, vendor, description, protocol);
            acs.getUnittypes().addOrChangeUnittype(newUnitType, acs);
            return null;
        }
        return "Unittype " + unittype + " exists";
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

    @ShellMethod("Create unit")
    public String createUnit(@ShellOption String unitId) throws SQLException {
        return doOnProfile((unittype, profile) -> {
            try {
                acsUnit.addUnits(Collections.singletonList(unitId), profile);
            } catch (SQLException e) {
                throw new IllegalArgumentException("Failed to create unit", e);
            }
            return null;
        });
    }

    @ShellMethod("Set unittype")
    public String setUnittype(@ShellOption String unittypeName) {
        Unittype unittype = dbi.getAcs().getUnittype(unittypeName);
        if (unittype != null) {
            shellContext.setUnitType(unittype);
            return "Changed unittype to " + unittypeName;
        } else {
            return "Unittype " + unittypeName + " does not exist";
        }
    }

    @ShellMethod("Set profile")
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

    @ShellMethod("Set unit")
    public String setUnit(@ShellOption String unitId) throws SQLException {
        return doOnProfile((unittype, profile) -> {
            try {
                Unit unit = acsUnit.getUnitById(unitId, unittype, profile);
                if (unit == null) {
                    throw new IllegalArgumentException("Unit " + unitId + " does not exist");
                }
                shellContext.setUnit(unit);
                return "Changed unit to " + unitId;
            } catch (SQLException e) {
                throw new IllegalStateException("Failed to get unit " + unitId, e);
            }
        });
    }

    @ShellMethod("Set group")
    public String setGroup(@ShellOption String groupName) throws SQLException {
        return doOnUnittype((unittype) -> {
            Group group = unittype.getGroups().getByName(groupName);
            if (group == null) {
                throw new IllegalArgumentException("Group " + groupName + " does not exist");
            }
            shellContext.setGroup(group);
            shellContext.setUnit(null);
            shellContext.setProfile(null);
            return "Changed group to " + groupName;
        });
    }

    @ShellMethod("Set job")
    public String setJob(@ShellOption String jobName) throws SQLException {
        return doOnUnittype((unittype) -> {
            Job job = unittype.getJobs().getByName(jobName);
            if (job == null) {
                throw new IllegalArgumentException("Job " + jobName + " does not exist");
            }
            shellContext.setJob(job);
            shellContext.setGroup(null);
            shellContext.setUnit(null);
            shellContext.setProfile(null);
            return "Changed job to " + jobName;
        });
    }

    private String doOnProfile(CheckedBiFunction<Unittype, Profile, String> func) {
        return shellContext.getUnittype()
                .map(unittype -> shellContext.getProfile()
                        .map(profile -> func.apply(unittype, profile))
                        .orElseGet(() -> "Profile is not set"))
                .orElseGet(() -> "Unittype is not set");
    }

    private String doOnUnittype(CheckedFunction<Unittype, String> func) {
        return shellContext.getUnittype()
                .map(func::apply)
                .orElseGet(() -> "Unittype is not set");
    }

    @FunctionalInterface
    public interface CheckedFunction<T1, R> {
        R apply(T1 t1) throws IllegalArgumentException;
    }

    @FunctionalInterface
    public interface CheckedBiFunction<T1, T2, R> {
        R apply(T1 t1, T2 t2) throws IllegalArgumentException;
    }
}
