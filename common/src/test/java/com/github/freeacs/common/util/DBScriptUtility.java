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
        if (!new File(path).exists()) {
            System.out.println("File " + path + " does not exist");
            return;
        }
        InputStream inputStream = ClassLoader.getSystemClassLoader().getResourceAsStream(path);
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        ScriptRunner scriptRunner = new ScriptRunner(connection);
        scriptRunner.setSendFullScript(false);
        scriptRunner.setStopOnError(true);
        scriptRunner.setLogWriter(null);
        scriptRunner.runScript(inputStreamReader);
    }
}
