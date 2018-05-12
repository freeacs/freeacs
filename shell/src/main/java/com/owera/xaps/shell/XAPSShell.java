package com.owera.xaps.shell;

import com.owera.common.db.ConnectionProperties;
import com.owera.common.db.ConnectionProvider;
import com.owera.common.util.PropertyReader;
import com.owera.xaps.dbi.*;
import com.owera.xaps.dbi.util.XAPSVersionCheck;
import com.owera.xaps.shell.util.ValidateInput;
import com.owera.xaps.shell.util.ValidateInteger;

import java.io.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class XAPSShell {

	public static String version = "2.3.41";

	private BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
	private PrintWriter out = new PrintWriter(new OutputStreamWriter(System.out));
	private List<Throwable> throwables = new ArrayList<Throwable>();
	private Session session;
	private boolean initialized = false;

	protected void setReader(BufferedReader reader) {
		if (in == null)
			throw new IllegalArgumentException("BufferedReader cannot be null");
		in = reader;
	}

	protected BufferedReader getReader() {
		return in;
	}

	public void setPrinter(PrintWriter printer) {
		if (out == null)
			throw new IllegalArgumentException("PrintWriter cannot be null");
		out = printer;
	}

	public PrintWriter getPrinter() {
		return out;
	}

	public void println(String s) {
		out.println(s);
		out.flush();
	}

	public void print(String s) {
		out.print(s);
		out.flush();
	}

	private Users getUsers(boolean failedLastTime) throws Exception {
		if (!Properties.isRestricted() && (failedLastTime || (session.getXapsProps().getUrl() == null && session.getMode() == SessionMode.INTERACTIVE)))
			initConnectionProperties(session);
		if (session.getMode() != SessionMode.DAEMON && session.getFusionUser() == null && session.getFusionPass() == null)
			initFusionUser(session);
		Users users = null;
		try {
			users = new Users(session.getXapsProps());
			if (session.getFusionUser() != null) {
				User u = users.getUnprotected(session.getFusionUser());
				if (u == null)
					throw new IllegalArgumentException("The Fusion Username " + session.getFusionUser() + " was not recognized");
				if (session.getMode() != SessionMode.DAEMON && !u.isCorrectSecret(session.getFusionPass())) // In Daemon-mode, we skip the password
					throw new IllegalArgumentException("The Fusion Password was wrong");
				session.setVerifiedFusionUser(u);
			} else {
				session.setVerifiedFusionUser(users.getUnprotected(Users.USER_ADMIN)); // no login - get admin access
			}
		} catch (Throwable t) {
			session.setFusionUser(null);
			session.setFusionPass(null);
			println("\nCould not connect to database, showing the first 150 byte of the error-message:\n");
			String msg = t.getMessage();
			if (t.getMessage().length() > 150)
				msg = t.getMessage().substring(0, 150) + "....";
			println("\n**********************************\n " + msg + "\n**********************************\n");
			return getUsers(true);
		}
		return users;
	}

	public void init() throws Exception {
		XAPSVersionCheck.setDatabaseChecked(false); // force another database check
		Users users = getUsers(false);
		session.setUsers(users);
		Identity id = new Identity(SyslogConstants.FACILITY_SHELL, XAPSShell.version, session.getVerifiedFusionUser());
		Syslog syslog = new Syslog(session.getSysProps(), id);
		DBI dbi = new DBI(Integer.MAX_VALUE, session.getXapsProps(), syslog);
		session.setDbi(dbi);
		session.setXaps(dbi.getXaps());
		XAPSUnit xapsU = new XAPSUnit(session.getXapsProps(), session.getXaps(), syslog);
		session.setXapsUnit(xapsU);
		UnitJobs unitJobs = new UnitJobs(session.getXapsProps());
		session.setUnitJobs(unitJobs);
	}

	private boolean help(String[] args) {
		if (args != null) {
			for (String arg : args) {
				if (arg.equals("-help") || arg.equals("-?")) {
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

	public void mainImpl(String[] args) {
		try {
			if (help(args)) {
				System.exit(1);
				return;
			}
			session = new Session(args, this);
			init();
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
			while (true) {
				try {
					session.getProcessor().promptProcessing();
				} catch (IOException e) {
					addThrowable(e);
					println("An I/O-error occurred: " + e);
					if (session.getMode() == SessionMode.SCRIPT)
						session.exitShell(1);
				} catch (SQLException sqle) {
					addThrowable(sqle);
					session.getContext().resetXAPS(session.getDbi().getXaps());
					println("An SQL-error occurred: " + sqle);
					if (session.getMode() == SessionMode.SCRIPT)
						session.exitShell(1);
				} catch (IllegalArgumentException iae) {
					addThrowable(iae);
					println("Error: " + iae.getMessage());
					if (session.getMode() == SessionMode.SCRIPT)
						session.exitShell(1);
				} catch (Throwable tInner) {
					addThrowable(tInner);
					println("An unexpected error occurred: (" + tInner.getClass().getName() + "): " + tInner.getMessage());
					if (session.getMode() == SessionMode.SCRIPT)
						session.exitShell(1);
				}
			}
		} catch (Throwable t) {
			addThrowable(t);
			println("\n\nAn unexpected error occurred:" + t);
			t.printStackTrace();
			out.flush();
		}
		session.exitShell(0);
	}

	public static void main(String[] args) {
		Locale.setDefault(new Locale("en", "US"));
		XAPSShell xapsShell = new XAPSShell();
		xapsShell.mainImpl(args);
	}

	private void initConnectionProperties(Session session) throws Exception {
		String question = "Choose one of the following database-connections by entering\n";
		question += "the number.\n";
		String propFile = "xaps-shell.properties";
		PropertyReader pr = new PropertyReader(propFile);
		int i = 1;
		question += "\t0. Custom.\n";
		while (true) {
			String db = pr.getProperty("db." + i);
			if (db != null) {
				question += "\t" + i + ". " + db + "\n";
				i++;
			} else
				break;
		}
		ValidateInput valInput = new ValidateInteger(0, i);
		int chosen = Integer.parseInt(session.getProcessor().questionProcessing(question, valInput));
		ConnectionProperties xapsProps = null;
		ConnectionProperties sysProps = null;
		if (chosen > 0 && chosen < i) {
			String xapsDb = "db." + chosen;
			String syslogDb = "sysdb." + chosen;
			xapsProps = ConnectionProvider.getConnectionProperties(propFile, xapsDb);
			sysProps = ConnectionProvider.getConnectionProperties(propFile, syslogDb);
			if (sysProps == null)
				sysProps = xapsProps;
		} else if (chosen == 0) {
			xapsProps = getConnectionPropertiesCustom(session);
			sysProps = xapsProps;
		}
		session.setXapsProps(xapsProps);
		session.setSysProps(sysProps);
	}

	private void initFusionUser(Session session) throws IOException {
		session.println("=== FUSION LOGIN === ");
		if (!Properties.isRestricted())
			session.println("You may skip login to become Admin, just press ENTER");
		session.print("Username: ");
		String fusionUser = session.getProcessor().retrieveInput();
//		String fusionUser = "Admin";
		if (Properties.isRestricted() || (fusionUser != null && fusionUser.trim().length() > 0 && !fusionUser.toLowerCase().equals("admin"))) {
			session.setFusionUser(fusionUser);
			session.print("Password: ");
			session.setFusionPass(session.getProcessor().retrieveInput());
		}
	}

	private static ConnectionProperties getConnectionPropertiesCustom(Session session) throws Exception {
		ConnectionProperties props = new ConnectionProperties();
		Processor processor = session.getProcessor();
		props.setUrl(processor.questionProcessing("What is the url to the database?", null));
		if (props.getUrl().indexOf("mysql") > -1)
			props.setDriver("com.mysql.jdbc.Driver");
		else
			props.setDriver("oracle.jdbc.driver.OracleDriver");
		props.setUser(processor.questionProcessing("What is the username to the database?", null));
		props.setPassword(processor.questionProcessing("What is the password to the database?", null));
		props.setMaxAge(600000);
		props.setMaxConn(5);
		return props;
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
