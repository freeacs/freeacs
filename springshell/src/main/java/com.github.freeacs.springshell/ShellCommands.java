package com.github.freeacs.springshell;

import com.github.freeacs.dbi.*;
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
        Unittype unittype = shellContext.getUnittype();
        if (unittype == null) {
            return "Unittype is not set";
        }
        Profile newProfile = new Profile(profileName, unittype);
        unittype.getProfiles().addOrChangeProfile(newProfile, dbi.getAcs());
        return null;
    }

    @ShellMethod("Create unit")
    public String createUnit(@ShellOption String unitId) throws SQLException {
        Unittype unittype = shellContext.getUnittype();
        if (unittype == null) {
            return "Unittype is not set";
        }
        Profile profile = shellContext.getProfile();
        if (profile == null) {
            return "Profile is not set";
        }
        acsUnit.addUnits(Collections.singletonList(unitId), profile);
        return null;
    }

    @ShellMethod("Set unittype")
    public String setUnittype(@ShellOption String unittypeName) {
        Unittype unittype = dbi.getAcs().getUnittype(unittypeName);
        if (unittype != null) {
            shellContext.setUnitType(unittype);
            return null;
        } else {
            return "Unittype " + unittypeName + " does not exist";
        }
    }

    @ShellMethod("Set profile")
    public String setProfile(@ShellOption String profileName) {
        Unittype unittype = shellContext.getUnittype();
        if (unittype == null) {
            return "Unittype is not set";
        }
        Profile profile = dbi.getAcs().getProfile(unittype.getName(), profileName);
        if (profile == null) {
            return "Profile does not exist";
        }
        shellContext.setProfile(profile);
        return null;
    }

    @ShellMethod("Set unit")
    public String setUnit(@ShellOption String unitId) throws SQLException {
        Unittype unittype = shellContext.getUnittype();
        if (unittype == null) {
            return "Unittype is not set";
        }
        Profile profile = shellContext.getProfile();
        if (profile == null) {
            return "Profile is not set";
        }
        Unit unit = acsUnit.getUnitById(unitId, unittype, profile);
        if  (unit == null) {
            return "Unit " + unitId + " does not exist";
        }
        shellContext.setUnit(unit);
        return null;
    }

}
