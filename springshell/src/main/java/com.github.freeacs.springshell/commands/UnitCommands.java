package com.github.freeacs.springshell.commands;

import com.github.freeacs.dbi.ACSUnit;
import com.github.freeacs.dbi.Unit;
import com.github.freeacs.springshell.ShellContext;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.sql.SQLException;
import java.util.Collections;

@ShellComponent
@ShellCommandGroup("Unit commands")
public class UnitCommands extends ShellCommands {

    private final ACSUnit acsUnit;

    public UnitCommands(ACSUnit acsUnit, ShellContext shellContext) {
        super(shellContext);
        this.acsUnit = acsUnit;
    }

    @ShellMethod("Set unit context")
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
}
