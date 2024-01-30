package com.github.freeacs.dbi;

import org.apache.ibatis.jdbc.ScriptRunner;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;

public class DBScriptUtility {
    static void runScript(
            String path,
            Connection connection
    ) {
        InputStream inputStream = ClassLoader.getSystemClassLoader().getResourceAsStream(path);
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        ScriptRunner scriptRunner = new ScriptRunner(connection);
        scriptRunner.setSendFullScript(false);
        scriptRunner.setStopOnError(true);
        scriptRunner.setLogWriter(null);
        scriptRunner.runScript(inputStreamReader);
    }
}
