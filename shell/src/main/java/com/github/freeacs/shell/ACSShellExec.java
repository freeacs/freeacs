package com.github.freeacs.shell;

import com.github.freeacs.dbi.util.SyslogClient;
import java.io.InputStreamReader;
import javax.sql.DataSource;
import jline.ConsoleReader;

public class ACSShellExec {
  public static void main(String[] args) throws Exception {
    SyslogClient.SYSLOG_SERVER_HOST = Properties.pr.getProperty("syslog.server.host");
    ConsoleReader reader = new ConsoleReader();
    reader.setBellEnabled(false);
    ACSShell acsShell = new ACSShell();
    ACSShellReader acsShellReader = new ACSShellReader(new InputStreamReader(System.in), reader);
    acsShellReader.setACSShell(acsShell);
    acsShell.setReader(acsShellReader);
    DataSource ds = ACSShell.getHikariDataSource("main");
    acsShell.mainImpl(args, ds, ds);
  }
}
