package com.github.freeacs.shell;

import com.github.freeacs.dbi.ACSUnit;
import com.github.freeacs.dbi.DBI;
import com.github.freeacs.dbi.Identity;
import com.github.freeacs.dbi.Syslog;
import com.github.freeacs.dbi.SyslogConstants;
import com.github.freeacs.dbi.UnitJobs;
import com.github.freeacs.dbi.User;
import com.github.freeacs.dbi.Users;
import com.github.freeacs.dbi.util.ACSVersionCheck;
import com.zaxxer.hikari.HikariDataSource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;

public class ACSShell {
  public static String version = "2.3.41";

  private BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
  private PrintWriter out = new PrintWriter(new OutputStreamWriter(System.out));
  private List<Throwable> throwables = new ArrayList<>();
  private Session session;
  private boolean initialized;

  protected void setReader(BufferedReader reader) {
    if (in == null) {
      throw new IllegalArgumentException("BufferedReader cannot be null");
    }
    in = reader;
  }

  protected BufferedReader getReader() {
    return in;
  }

  public void setPrinter(PrintWriter printer) {
    if (out == null) {
      throw new IllegalArgumentException("PrintWriter cannot be null");
    }
    out = printer;
  }

  public void println(String s) {
    out.println(s);
    out.flush();
  }

  public void print(String s) {
    out.print(s);
    out.flush();
  }

  private Users getUsers() throws Exception {
    if (session.getMode() != SessionMode.DAEMON
        && session.getFusionUser() == null
        && session.getFusionPass() == null) {
      initFusionUser(session);
    }
    Users users = null;
    try {
      users = new Users(session.getXapsProps());
      if (session.getFusionUser() != null) {
        User u = users.getUnprotected(session.getFusionUser());
        if (u == null) {
          throw new IllegalArgumentException(
              "The Fusion Username " + session.getFusionUser() + " was not recognized");
        }
        if (session.getMode() != SessionMode.DAEMON
            && !u.isCorrectSecret(session.getFusionPass())) {
          throw new IllegalArgumentException("The Fusion Password was wrong");
        }
        session.setVerifiedFusionUser(u);
      } else {
        session.setVerifiedFusionUser(
            users.getUnprotected(Users.USER_ADMIN)); // no login - get admin access
      }
    } catch (Throwable t) {
      session.setFusionUser(null);
      session.setFusionPass(null);
      println(
          "\nCould not connect to database, showing the first 150 byte of the error-message:\n");
      String msg = t.getMessage();
      if (t.getMessage().length() > 150) {
        msg = t.getMessage().substring(0, 150) + "....";
      }
      println(
          "\n**********************************\n "
              + msg
              + "\n**********************************\n");
    }
    return users;
  }

  public void init(DataSource xapsDs, DataSource syslogDs) throws Exception {
    session.setXapsProps(xapsDs);
    session.setSysProps(syslogDs);
    ACSVersionCheck.setDatabaseChecked(false); // force another database check
    Users users = getUsers();
    session.setUsers(users);
    Identity id =
        new Identity(SyslogConstants.FACILITY_SHELL, version, session.getVerifiedFusionUser());
    Syslog syslog = new Syslog(session.getSysProps(), id);
    DBI dbi = new DBI(Integer.MAX_VALUE, session.getXapsProps(), syslog);
    session.setDbi(dbi);
    session.setAcs(dbi.getAcs());
    ACSUnit xapsU = new ACSUnit(session.getXapsProps(), session.getAcs(), syslog);
    session.setAcsUnit(xapsU);
    UnitJobs unitJobs = new UnitJobs(session.getXapsProps());
    session.setUnitJobs(unitJobs);
  }

  private boolean help(String[] args) {
    if (args != null) {
      for (String arg : args) {
        if ("-help".equals(arg) || "-?".equals(arg)) {
          println("Usage:\n");
          println("xapsshell");
          println("\tInteractive session. Choose DB in application");
          println("xapsshell <LOGIN>");
          println("\tInteractive session. DB chosen using command line arguments");
          println("xapsshell <COMMAND-OPTION> [<LOGIN>]");
          println("\tSession runs according to command-option. May be interactive.");
          println("\tDATABASE can be omitted only if using the script, help and ? command");
          println("\toptions.\n");
          println("COMMAND-OPTIONS (choose one):");
          println("\t-script <filename>");
          println("\t\tThe shell will execute the commands in the file. The commands ");
          println("\t\twould be the same as used in an interactive session.");
          println("\t-export ALL|<unittype-name> [-dir <directory>]");
          println("\t\tThe shell wil export ALL or a specific unit type to files. If you");
          println("\t\twant to, you can specify a directory to place the files, otherwise");
          println("\t\tit will place the files in a directory with the same name as the");
          println("\t\tunittype.");
          println("\t-import ALL|<unittype-name> [-dir <directory>]");
          println("\t\tThe shell will import ALL or a specific unit type from files. If you");
          println("\t\twant to, you can specify a directory to read the files, otherwise");
          println("\t\tit will read the files in a directory with the same name as the");
          println("\t\tunittype.");
          println("\t-delete ALL|<unittype-name>");
          println("\t\tThe shell will delete ALL or a specific unit type. Be careful.");
          println("\t-help");
          println("\t\tShows this message");
          println("\t-?");
          println("\t\tShows this message");
          println("\t-<unrecognized option> <value>");
          println("\t\tThe shell will translate any unrecognized option into a variable. ");
          println("\t\tOption name will be the variable name and option will be the variable ");
          println("\t\tvalue.");
          println("\nLOGIN:");
          println("\t-user <username>");
          println("\t\tThe database username, can be taken from xaps-shell.properties");
          println("\t-password <password>");
          println("\t\tThe database password, can be taken from xaps-shell.properties");
          println("\t-url <jdbc-url>");
          println("\t\tThe database url, can be taken from xaps-shell.properties.");
          println("\t-fusionuser <username>");
          println("\t\tThe Fusion user, with a specified set of access permissions within ");
          println("\t\tFreeACS user database");
          println("\t-fusionpass <password>");
          println("\t\tThe FreeACS password");
          return true;
        }
      }
    }
    return false;
  }

  public void mainImpl(String[] args, DataSource xapsDs, DataSource syslogDs) {
    try {
      if (help(args)) {
        System.exit(1);
        return;
      }
      session = new Session(args, this);
      init(xapsDs, syslogDs);
      if (session.getMode() != SessionMode.SCRIPT) {
        println("  ____ _  _ ____ _ ____ _  _    ____ _  _ ____ _    _    ");
        println("  |___ |  | [__  | |  | |\\ |    [__  |__| |___ |    |");
        println("  |    |__| ___] | |__| | \\|    ___] |  | |___ |___ |___ v" + version + "\n\n");
        println("  Fusion Shell provdes a variety of commands and possibilities for the advanced ");
        println("  user of Fusion. It is possible to make scripts to automate tedious tasks, and ");
        println("  the scripts can be made fairly advanced by the use of file input/output. ");
        println("  Furthermore it is possible to combine several scripts so the user can build ");
        println("  script libraries for optimal reuse. To learn all about Fusion Shell you need ");
        println("  to read Fusion Shell User Manual, but 4 commands is a must at this point:\n");
        println("  - Type 'help' to get context-sensitive help.");
        println("  - Type 'help generic' to get generic help.");
        println("  - Type 'help <name-of-command>' to get detailed command help.");
        println("  - Type 'exit' to exit.\n");
        println("  By using help the user should be able to learn most of the commands in Fusion ");
        println("  Shell, which will enable him to create/update/delete/inspect all parts of ");
        println("  the Fusion database. The argument in the 'help' command can be shortened as ");
        println("  long as it's not ambigious.\n\n");
      }
      initialized = true;
      do {
        try {
          session.getProcessor().promptProcessing();
        } catch (IOException e) {
          addThrowable(e);
          println("An I/O-error occurred: " + e);
          if (session.getMode() == SessionMode.SCRIPT) {
            session.exitShell(1);
          }
        } catch (SQLException sqle) {
          addThrowable(sqle);
          session.getContext().resetXAPS(session.getDbi().getAcs());
          println("An SQL-error occurred: " + sqle);
          if (session.getMode() == SessionMode.SCRIPT) {
            session.exitShell(1);
          }
        } catch (IllegalArgumentException iae) {
          addThrowable(iae);
          println("Error: " + iae.getMessage());
          if (session.getMode() == SessionMode.SCRIPT) {
            session.exitShell(1);
          }
        } catch (Throwable tInner) {
          addThrowable(tInner);
          println(
              "An unexpected error occurred: ("
                  + tInner.getClass().getName()
                  + "): "
                  + tInner.getMessage());
          if (session.getMode() == SessionMode.SCRIPT) {
            session.exitShell(1);
          }
        }
      } while (true);
    } catch (Throwable t) {
      addThrowable(t);
      println("\n\nAn unexpected error occurred:" + t);
      t.printStackTrace();
      out.flush();
    }
    session.exitShell(0);
  }

  public static DataSource getHikariDataSource(String prefix) {
    HikariDataSource ds = new HikariDataSource();
    String jdbcUrl = prefix + ".datasource.jdbcUrl";
    ds.setJdbcUrl(
        Optional.ofNullable(System.getProperty(jdbcUrl))
            .orElseGet(() -> Properties.pr.getProperty(jdbcUrl)));
    String className = prefix + ".datasource.driverClassName";
    ds.setDriverClassName(
        Optional.ofNullable(System.getProperty(className))
            .orElseGet(() -> Properties.pr.getProperty(className)));
    String user = prefix + ".datasource.username";
    ds.setUsername(
        Optional.ofNullable(System.getProperty(user))
            .orElseGet(() -> Properties.pr.getProperty(user)));
    String password = prefix + ".datasource.password";
    ds.setPassword(
        Optional.ofNullable(System.getProperty(password))
            .orElseGet(() -> Properties.pr.getProperty(password)));
    String poolSize = prefix + ".datasource.maximum-pool-size";
    ds.setMaximumPoolSize(
        Integer.parseInt(
            Optional.ofNullable(System.getProperty(poolSize))
                .orElseGet(() -> Properties.pr.getProperty(poolSize))));
    String minimumIdle = prefix + ".datasource.minimum-idle";
    ds.setMinimumIdle(
        Integer.parseInt(
            Optional.ofNullable(System.getProperty(minimumIdle))
                .orElseGet(() -> Properties.pr.getProperty(minimumIdle))));
    String poolName = prefix + ".datasource.poolName";
    ds.setPoolName(
        Optional.ofNullable(System.getProperty(poolName))
            .orElseGet(() -> Properties.pr.getProperty(poolName)));
    return ds;
  }

  private void initFusionUser(Session session) throws IOException {
    session.println("=== FUSION LOGIN === ");
    if (!Properties.isRestricted()) {
      session.println("You may skip login to become Admin, just press ENTER");
    }
    session.print("Username: ");
    String fusionUser = session.getProcessor().retrieveInput();
    if (Properties.isRestricted()
        || (fusionUser != null
            && !fusionUser.trim().isEmpty()
            && !"admin".equals(fusionUser.toLowerCase()))) {
      session.setFusionUser(fusionUser);
      session.print("Password: ");
      session.setFusionPass(session.getProcessor().retrieveInput());
    }
  }

  public Session getSession() {
    return session;
  }

  public synchronized void addThrowable(Throwable t) {
    this.throwables.add(t);
  }

  public synchronized List<Throwable> getThrowables() {
    return throwables;
  }

  public synchronized void setThrowables(List<Throwable> throwables) {
    this.throwables = throwables;
  }

  public void setSession(Session session) {
    this.session = session;
  }

  public boolean isInitialized() {
    return initialized;
  }
}
