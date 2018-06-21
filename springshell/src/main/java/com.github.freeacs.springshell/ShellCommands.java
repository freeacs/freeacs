package com.github.freeacs.springshell;

import com.github.freeacs.dbi.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.sql.SQLException;

@ShellComponent
public class ShellCommands {

    private final DBI dbi;
    private final ACSUnit acsUnit;
    private final UnitJobs unitJobs;

    @Autowired
    public ShellCommands(DBI dbi, ACSUnit acsUnit, UnitJobs unitJobs) {
        this.dbi = dbi;
        this.acsUnit = acsUnit;
        this.unitJobs = unitJobs;
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
            return "Created " + unittype;
        }
        return "Unittype " + unittype + " exists";
    }
}
