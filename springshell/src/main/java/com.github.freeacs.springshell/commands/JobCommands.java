package com.github.freeacs.springshell.commands;

import com.github.freeacs.dbi.Job;
import com.github.freeacs.springshell.ShellContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.sql.SQLException;

@ShellComponent
@ShellCommandGroup("Job commands")
public class JobCommands extends ShellCommands {

    @Autowired
    public JobCommands(ShellContext shellContext) {
        super(shellContext);
    }

    @ShellMethod("Set job context")
    public String setJob(@ShellOption String jobName) throws SQLException {
        return doOnUnittype((unittype) -> {
            Job job = unittype.getJobs().getByName(jobName);
            if (job == null) {
                throw new IllegalArgumentException("Job " + jobName + " does not exist");
            }
            shellContext.setJob(job);
            return "Changed job to " + jobName;
        });
    }
}
