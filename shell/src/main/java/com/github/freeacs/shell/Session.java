package com.github.freeacs.shell;

import com.github.freeacs.dbi.*;
import com.github.freeacs.shell.util.FileUtil;

import javax.sql.DataSource;
import java.util.*;

public class Session {

	//	public static final int INTERACTIVE = 0;
	//	public static final int SCRIPT = 1;
	//	public static final int DAEMON = 2;
	//
	private String[] originalOptionArgs = null;

	/* Object holding the Session object and printer-objects */
	private FreeacsShell freeacsShell;

	/* Information on how to access xAPS database */
	private DataSource xapsProps = null;
	private DataSource sysProps = null;

	/* Fusion user/pass - can be used instead of running as Admin (default) */
	private String fusionUser;
	private String fusionPass;
	private User verifiedFusionUser; // should always be populated (as soon as login is completed)

	/* Key objects to access/manipulate xAPS database */
	private Users users;
	private DBI dbi;
	private ACS acs;
	private ACSUnit acsUnit;
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

    // Counts number of operations during one command (only for set/del)
	private int counter = 1;

	public Session(String[] args, FreeacsShell freeacsShell) {
		this.originalOptionArgs = args;
		this.freeacsShell = freeacsShell;
		Context context = new Context(this);
		this.processor = new Processor(this);

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
				}
				else if (args[i].equals("-fusionuser")) {
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
	}

	public ACSUnit getAcsUnit() {
		return acsUnit;
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

	public void setAcsUnit(ACSUnit xapsU) {
		this.acsUnit = xapsU;
	}

	public DataSource getXapsProps() {
		return xapsProps;
	}

	public int getCounter() {
		return counter;
	}

	public void incCounter() {
		this.counter++;
	}

	public void resetCounter() {
		this.counter = 1;
	}

	public UnitJobs getUnitJobs() {
		return unitJobs;
	}

	public void setUnitJobs(UnitJobs unitJobs) {
		this.unitJobs = unitJobs;
	}

	public DataSource getSysProps() {
		return sysProps;
	}

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

	public ACS getAcs() {
		return acs;
	}

	public void setAcs(ACS acs) {
		this.acs = acs;
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

	public Processor getProcessor() {
		return processor;
	}

	public FreeacsShell getFreeacsShell() {
		return freeacsShell;
	}

	public void println(String s) {
		getFreeacsShell().println(s);
	}

	public void print(String s) {
		getFreeacsShell().print(s);
	}

	public List<String> getCommandHistory() {
		return commandHistory;
	}

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

	public void setXapsProps(DataSource xapsProps) {
		this.xapsProps = xapsProps;
	}

	public void setSysProps(DataSource sysProps) {
		this.sysProps = sysProps;
	}
}
