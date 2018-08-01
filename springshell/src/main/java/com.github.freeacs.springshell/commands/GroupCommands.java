package com.github.freeacs.springshell.commands;

import com.github.freeacs.dbi.Group;
import com.github.freeacs.springshell.ShellContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.sql.SQLException;

@ShellComponent
@ShellCommandGroup("Group commands")
public class GroupCommands extends ShellCommands {

    @Autowired
    public GroupCommands(ShellContext shellContext) {
        super(shellContext);
    }

    @ShellMethod("Set group context")
    public String setGroup(@ShellOption String groupName) throws SQLException {
        return doOnUnittype((unittype) -> {
            Group group = unittype.getGroups().getByName(groupName);
            if (group == null) {
                throw new IllegalArgumentException("Group " + groupName + " does not exist");
            }
            shellContext.setGroup(group);
            return "Changed group to " + groupName;
        });
    }

}
