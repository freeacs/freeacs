package com.github.freeacs.springshell.commands;

import com.github.freeacs.dbi.ACS;
import com.github.freeacs.dbi.DBI;
import com.github.freeacs.dbi.Unittype;
import com.github.freeacs.springshell.ShellContext;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.sql.SQLException;

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
            @ShellOption Unittype.ProvisioningProtocol protocol) throws SQLException {
        ACS acs = dbi.getAcs();
        if (acs.getUnittype(unittype) == null) {
            Unittype newUnitType = new Unittype(unittype, vendor, description, protocol);
            acs.getUnittypes().addOrChangeUnittype(newUnitType, acs);
            return null;
        }
        return "Unittype " + unittype + " exists";
    }
}
