package com.github.freeacs.springshell.commands;

import com.github.freeacs.dbi.*;
import com.github.freeacs.springshell.ShellContext;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

@ShellComponent
@ShellCommandGroup("Unittype commands")
public class UnittypeCommands extends ShellCommands {

    private final DBI dbi;

    public UnittypeCommands(DBI dbi, ShellContext shellContext) {
        super(shellContext);
        this.dbi = dbi;
    }

    @ShellMethod("Set unittype context")
    public String setUnittype(@ShellOption String unittypeName) {
        Unittype unittype = dbi.getAcs().getUnittype(unittypeName);
        if (unittype != null) {
            shellContext.setUnitType(unittype);
            return "Changed unittype to " + unittypeName;
        } else {
            return "Unittype " + unittypeName + " does not exist";
        }
    }

    @ShellMethod("Create unittype")
    public String createUnittype(
            @ShellOption String unittype,
            @ShellOption String vendor,
            @ShellOption String description,
            @ShellOption Unittype.ProvisioningProtocol protocol) {
        ACS acs = dbi.getAcs();
        if (acs.getUnittype(unittype) == null) {
            Unittype newUnitType = new Unittype(unittype, vendor, description, protocol);
            try {
                acs.getUnittypes().addOrChangeUnittype(newUnitType, acs);
            } catch (SQLException e) {
                return "Unittype " + unittype + " could not be created: " + e.getLocalizedMessage();
            }
            return null;
        }
        return "Unittype " + unittype + " exists";
    }

    @ShellMethod("List unittypes")
    public List<Unittype> listUnittypes() {
        return Arrays.asList(dbi.getAcs().getUnittypes().getUnittypes());
    }

    @ShellMethod("Delete unittype")
    public String deleteUnittype(@ShellOption String unittype, @ShellOption boolean cascade) {
        ACS acs = dbi.getAcs();
        Unittype unittypeToDelete = acs.getUnittype(unittype);
        try {
            acs.getUnittypes().deleteUnittype(unittypeToDelete, acs, cascade);
        } catch (SQLException ex) {
            return "Unittype " + unittype + " could not be deleted: " + ex.getLocalizedMessage();
        }
        return "Unittype " + unittype + " was deleted";
    }

    @ShellMethod("Add or change unittype parameter")
    public String addUnittypeParam(
            @ShellOption String name,
            @ShellOption UnittypeParameterFlag flag) {
        ACS acs = dbi.getAcs();
        return doOnUnittype(unittype -> {
            UnittypeParameter utp = unittype.getUnittypeParameters().getByName(name);
            if (utp == null) {
                utp = new UnittypeParameter(unittype, name, flag);
            } else {
                utp.setFlag(flag);
            }
            try {
                unittype.getUnittypeParameters().addOrChangeUnittypeParameter(utp, acs);
                return "Succesfully updated unittype parameter " + name;
            } catch (SQLException e) {
                return "Failed to add or change unittype parameter " + name;
            }
        });
    }
}
