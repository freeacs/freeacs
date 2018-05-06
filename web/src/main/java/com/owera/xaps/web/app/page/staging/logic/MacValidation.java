package com.owera.xaps.web.app.page.staging.logic;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.owera.common.db.ConnectionProperties;
import com.owera.common.db.ConnectionProvider;
import com.owera.xaps.dbi.DynamicStatement;


/**
 * The Class MacValidation.
 */
public class MacValidation {

	/** The USER. */
	private static String USER = "pingcom";
	
	/** The PASSWORD. */
	private static String PWOD = "pingcom123";

	/** The errors. */
	public Map<String, String> errors = new HashMap<String, String>();

	/** The cp. */
	private ConnectionProperties cp;

	/**
	 * Instantiates a new mac validation.
	 */
	public MacValidation() {
		cp = ConnectionProvider.getConnectionProperties("xaps-web.properties", "db.xaps");
		int lastSlashPos = cp.getUrl().lastIndexOf("/");
		cp.setUrl(cp.getUrl().substring(0, lastSlashPos + 1) + "lotfiles");
		cp.setPassword(PWOD);
		cp.setUser(USER);
	}

	/**
	 * Process mac list.
	 *
	 * @param is the is
	 * @return the string
	 */
	public String processMacList(InputStream is) {
		Connection c = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			c = ConnectionProvider.getConnection(cp);
		} catch (Throwable t) {
			errors.put("", "This function cannot run on this server: " + t);
			return "";
		}
		try {
			DynamicStatement ds = new DynamicStatement();
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			String line = null;
			Set<String> macSet = new HashSet<String>();
			while ((line = br.readLine()) != null) {
				ds.addSql("SELECT WAN_MAC FROM lotfiles WHERE WAN_MAC = '" + line + "'");
				ps = ds.makePreparedStatement(c);
				rs = ps.executeQuery();
				if (!rs.next()) 
					errors.put(line, "MAC address not valid or does not exist");
				if (macSet.contains(line))
					errors.put(line, "MAC address is added twice");
				macSet.add(line);
			}
			if (errors.size() > 0)
				return "";
			else
				return "Validation complete and successful. Found " + macSet.size() + " units ";
		} catch (Throwable t) {
			errors.put("", "An error ocurred: " + t);
			return "";
		}
	}

	/**
	 * Gets the errors.
	 *
	 * @return the errors
	 */
	public Map<String, String> getErrors() {
		return errors;
	}

}
