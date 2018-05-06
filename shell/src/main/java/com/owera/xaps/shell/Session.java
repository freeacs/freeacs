package com.owera.xaps.shell;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import com.owera.common.db.ConnectionProperties;
import com.owera.xaps.dbi.DBI;
import com.owera.xaps.dbi.UnitJobs;
import com.owera.xaps.dbi.User;
import com.owera.xaps.dbi.Users;
import com.owera.xaps.dbi.XAPS;
import com.owera.xaps.dbi.XAPSUnit;
import com.owera.xaps.shell.util.FileUtil;

public class Session {

	//	public static final int INTERACTIVE = 0;
	//	public static final int SCRIPT = 1;
	//	public static final int DAEMON = 2;
	//
	private String[] originalOptionArgs = null;

	/* Object holding the Session object and printer-objects */
	private XAPSShell xapsShell;

	/* Information on how to access xAPS database */
	private ConnectionProperties xapsProps = new ConnectionProperties();
	private ConnectionProperties sysProps = null;

	/* Fusion user/pass - can be used instead of running as Admin (default) */
	private String fusionUser;
	private String fusionPass;
	private User verifiedFusionUser; // should always be populated (as soon as login is completed)

	/* Key objects to access/manipulate xAPS database */
	private Users users;
	private DBI dbi;
	private XAPS xaps;
	private XAPSUnit xapsUnit;
	private UnitJobs unitJobs;

	/* Responsible for processing all commands */
	private Processor processor;

	/* The script-stack, containing all commands to be run */
	private Stack<Script> scriptStack = new Stack<Script>();

	/* Batch-storage, to speed up mass-change of unit/unit-parameter/unit-type parameters */
	private BatchStorage batchStorage = new BatchStorage();

	private List<String> commandHistory = new ArrayList<String>();

	// Default mode of a session is Interactive mode
	private SessionMode mode = SessionMode.INTERACTIVE;

	private String databaseName;

	private String migrationFolder = null;

	// Counts number of operations during one command (only for set/del)
	private int counter = 1;

	public Session(String[] args, XAPSShell xapsShell) {
		this.originalOptionArgs = args;
		this.xapsShell = xapsShell;
		Context context = new Context(this);
		this.processor = new Processor(this);

		// default settings
		xapsProps.setMaxConn(5);
		xapsProps.setDriver("com.mysql.jdbc.Driver");
		xapsProps.setMaxAge(600000);

		// read from options
		Map<String, Variable> variables = new HashMap<String, Variable>();
		if (args != null) {
			for (int i = 0; i < args.length - 1; i = i + 2) {
				if (args[i].equals("-script") && FileUtil.exists(args[i + 1])) {
					scriptStack.push(new Script(args[i + 1], context, Script.SCRIPT));
					mode = SessionMode.SCRIPT;
				} else if (args[i].equals("-daemon")) {
					mode = SessionMode.DAEMON;
					i--; // since this argument don't need a 2nd arg.
				} else if (args[i].equals("-export") || args[i].equals("-import")) {
					scriptStack.push(new Script(ScriptMaker.getMigrateScript(i, args), context, Script.SCRIPT));
					mode = SessionMode.SCRIPT;
				} else if (args[i].equals("-delete")) {
					scriptStack.push(new Script(ScriptMaker.getDeleteScript(i, args), context, Script.SCRIPT));
					mode = SessionMode.SCRIPT;
				} else if (args[i].equals("-upgradesystemparameters")) {
					scriptStack.push(new Script(ScriptMaker.getUpgradeSystemparametersScript(i, args), context, Script.SCRIPT));
					mode = SessionMode.SCRIPT;
					i--; // since this argument don't need a 2nd arg.
				} else if (args[i].equals("-url")) {
					xapsProps.setUrl(args[i + 1]);
					if (xapsProps.getUrl().indexOf("oracle") > -1) {
						xapsProps.setDriver("oracle.jdbc.OracleDriver");
					} else if (xapsProps.getUrl().indexOf("mysql") > -1) {
						xapsProps.setDriver("com.mysql.jdbc.Driver");
					} else {
						throw new IllegalArgumentException("The url is not pointing to a MySQL or Oracle database");
					}
					xapsProps.setMaxAge(600000);
					sysProps = xapsProps;
				} else if (args[i].equals("-user")) {
					xapsProps.setUser(args[i + 1]);
				} else if (args[i].equals("-password")) {
					xapsProps.setPassword(args[i + 1]);
				} else if (args[i].equals("-maxconn")) {
					xapsProps.setMaxConn(new Integer(args[i + 1]));
				} else if (args[i].equals("-fusionuser")) {
					fusionUser = args[i + 1];
				} else if (args[i].equals("-fusionpass")) {
					fusionPass = args[i + 1];
				} else if (args[i].startsWith("-") && args[i].length() > 1) {
					try {
						variables.put(args[i].substring(1), new Variable(args[i].substring(1), args[i + 1]));
					} catch (ArrayIndexOutOfBoundsException aioobe) {
						throw new IllegalArgumentException("The option " + args[i] + " is interpreted as a variable, but the variable value is missing");
					}
				}
			}
		}
		getScript().setVariables(variables);
		//		if (scriptStack.size() > 0)
		//			scriptStack.peek().setVariables(variables);
		if (xapsProps.getUrl() != null && (xapsProps.getUser() == null || xapsProps.getPassword() == null)) {
			throw new IllegalArgumentException("Missing password or username for the database logon");
		}
	}

	public XAPSUnit getXapsUnit() {
		return xapsUnit;
	}

	public void exitShell(int status) {
		try {
			if (dbi != null)
				dbi.processOutbox();
		} catch (Throwable t) {
			// Ignore
		}
		if (mode != SessionMode.DAEMON)
			System.exit(status);
	}

	public void setXapsUnit(XAPSUnit xapsU) {
		this.xapsUnit = xapsU;
	}

	//	public boolean isScriptmode() {
	//		return scriptMode;
	//	}

	public String getDatabaseName() {
		if (databaseName != null)
			return databaseName;
		if (xapsProps.getDriver() != null && xapsProps.getDriver().indexOf("Oracle") > -1)
			return "Oracle";
		return null;
	}

	public void setDatabaseName(String databaseName) {
		this.databaseName = databaseName;
	}

	public ConnectionProperties getXapsProps() {
		return xapsProps;
	}

	public void setXapsProps(ConnectionProperties props) {
		this.xapsProps = props;
	}

	public int getCounter() {
		return counter;
	}

	public void setCounter(int counter) {
		this.counter = counter;
	}

	public void incCounter() {
		this.counter++;
	}

	public void resetCounter() {
		this.counter = 1;
	}

	/* Get active/current context */
	//	public Context getContext() {
	//		return getScript().getContext();
	//	}

	//	public void setContext(Context context) {
	//		this.context = context;
	//	}

	public UnitJobs getUnitJobs() {
		return unitJobs;
	}

	public void setUnitJobs(UnitJobs unitJobs) {
		this.unitJobs = unitJobs;
	}

	public String getMigrationFolder() {
		return migrationFolder;
	}

	public ConnectionProperties getSysProps() {
		return sysProps;
	}

	public void setSysProps(ConnectionProperties sysProps) {
		this.sysProps = sysProps;
	}

	//	public boolean isInteractiveMode() {
	//		return interactiveMode;
	//	}

	public Users getUsers() {
		return users;
	}

	public void setUsers(Users users) {
		this.users = users;
	}

	public DBI getDbi() {
		return dbi;
	}

	public void setDbi(DBI dbi) {
		this.dbi = dbi;
	}

	public XAPS getXaps() {
		return xaps;
	}

	public void setXaps(XAPS xaps) {
		this.xaps = xaps;
	}

	public Stack<Script> getScriptStack() {
		return scriptStack;
	}

	/* Get the active/current script of the script stack */
	public Script getScript() {
		if (scriptStack.size() == 0) {
			scriptStack.add(new Script(new ArrayList<String>(), new Context(this), Script.SCRIPT));
		}
		return scriptStack.peek();
	}

	/* Get the active/current context of the session */
	public Context getContext() {
		return getScript().getContext();
	}

	public void setScriptStack(Stack<Script> scriptStack) {
		this.scriptStack = scriptStack;
	}

	public Processor getProcessor() {
		return processor;
	}

	//	public void setScriptMode(boolean scriptMode) {
	//		this.scriptMode = scriptMode;
	//	}

	//	public void setInteractiveMode(boolean interactiveMode) {
	//		this.interactiveMode = interactiveMode;
	//	}
	//
	//	public boolean isDaemonMode() {
	//		return daemonMode;
	//	}

	public XAPSShell getXapsShell() {
		return xapsShell;
	}

	public void setXapsShell(XAPSShell xapsShell) {
		this.xapsShell = xapsShell;
	}

	public void println(String s) {
		getXapsShell().println(s);
	}

	public void print(String s) {
		getXapsShell().print(s);
	}

	public List<String> getCommandHistory() {
		return commandHistory;
	}

	//	public IfElse getIfElse() {
	//		return ifElse;
	//	}
	//
	//	public void setIfElse(IfElse ifElse) {
	//		this.ifElse = ifElse;
	//	}
	//
	//	public While getWhileDone() {
	//		return whileDone;
	//	}
	//
	//	public void setWhileDone(While whileDone) {
	//		this.whileDone = whileDone;
	//	}

	//	public boolean isEcho() {
	//		return echo;
	//	}
	//
	//	public void setEcho(boolean echo) {
	//		this.echo = echo;
	//	}

	public BatchStorage getBatchStorage() {
		return batchStorage;
	}

	public SessionMode getMode() {
		return mode;
	}

	public void setMode(SessionMode mode) {
		this.mode = mode;
	}

	public String getFusionUser() {
		return fusionUser;
	}

	public void setFusionUser(String fusionUser) {
		this.fusionUser = fusionUser;
	}

	public String getFusionPass() {
		return fusionPass;
	}

	public void setFusionPass(String fusionPass) {
		this.fusionPass = fusionPass;
	}

	public User getVerifiedFusionUser() {
		return verifiedFusionUser;
	}

	public void setVerifiedFusionUser(User verifiedFusionUser) {
		this.verifiedFusionUser = verifiedFusionUser;
	}

	public String[] getOriginalOptionArgs() {
		return originalOptionArgs;
	}
}
