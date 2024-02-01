package com.github.freeacs.common.util;

import org.apache.ibatis.jdbc.ScriptRunner;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;

public class DBScriptUtility {
    public static void runScript(
            String path,
            Connection connection
    ) {
        InputStream inputStream = ClassLoader.getSystemClassLoader().getResourceAsStream(path);
        if (inputStream == null) {
            System.out.println("File " + path + " does not exist");
            return;
        }
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        ScriptRunner scriptRunner = new ScriptRunner(connection);
        scriptRunner.setSendFullScript(false);
        scriptRunner.setStopOnError(true);
        scriptRunner.setLogWriter(null);
        scriptRunner.runScript(inputStreamReader);
    }
}
