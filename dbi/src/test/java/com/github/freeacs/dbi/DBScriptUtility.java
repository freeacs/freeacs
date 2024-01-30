package com.github.freeacs.dbi;

import org.apache.ibatis.jdbc.ScriptRunner;

import java.sql.Connection;

public class DBScriptUtility {
    static void runScript(
            String path,
            Connection connection
    ) throws Exception {
        ScriptRunner scriptRunner = new ScriptRunner(connection);
        scriptRunner.setSendFullScript(false);
        scriptRunner.setStopOnError(true);
        scriptRunner.setLogWriter(null);
        scriptRunner.runScript(new java.io.FileReader(path));
    }
}
