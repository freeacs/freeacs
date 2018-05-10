package com.owera.xaps.web.app.page.staging;

import com.owera.common.db.ConnectionProperties;
import com.owera.common.db.ConnectionProvider;
import com.owera.common.db.NoAvailableConnectionException;
import com.owera.common.log.Logger;
import com.owera.common.ssl.HTTPSManager;
import com.owera.xaps.dbi.*;
import com.owera.xaps.dbi.JobFlag.JobServiceWindow;
import com.owera.xaps.dbi.JobFlag.JobType;
import com.owera.xaps.dbi.Parameter;
import com.owera.xaps.dbi.Profile;
import com.owera.xaps.dbi.Unit;
import com.owera.xaps.dbi.Unittype;
import com.owera.xaps.dbi.Unittype.ProvisioningProtocol;
import com.owera.xaps.dbi.util.SystemParameters;
import com.owera.xaps.web.app.input.DropDownSingleSelect;
import com.owera.xaps.web.app.input.Input;
import com.owera.xaps.web.app.input.InputSelectionFactory;
import com.owera.xaps.web.app.page.AbstractWebPage;
import com.owera.xaps.web.app.util.SessionCache;
import com.owera.xaps.web.app.util.WebProperties;
import com.owera.xaps.web.app.util.XAPSLoader;
import com.owera.xapsws.*;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;

//import org.safehaus.uuid.UUID;
//import org.safehaus.uuid.UUIDGenerator;

/**
 * The Class StagingActions.
 */
public abstract class StagingActions extends AbstractWebPage {

	/** The Constant IGD_SOFTWARE_VERSION. */
	private static final String IGD_SOFTWARE_VERSION = "InternetGatewayDevice.DeviceInfo.SoftwareVersion";

	/** The logger. */
	private static Logger logger = new Logger();

	/** The warnings. */
	public Map<String, String> warnings = new HashMap<String, String>();

	/** The errors. */
	public Map<String, String> errors = new HashMap<String, String>();

	/**
	 * Generate error list.
	 *
	 * @return the list
	 */
	protected List<String> generateErrorList() {
		List<String> errorList = new ArrayList<String>();
		for (Entry<String, String> entry : errors.entrySet()) {
			errorList.add(entry.getKey() + ": " + entry.getValue());
		}
		return errorList;
	}

	/**
	 * Generate warning list.
	 *
	 * @return the list
	 */
	protected List<String> generateWarningList() {
		List<String> warningList = new ArrayList<String>();
		for (Entry<String, String> entry : warnings.entrySet()) {
			warningList.add(entry.getKey() + ": " + entry.getValue());
		}
		return warningList;
	}

	/** The Constant DEVICE_SOFTWARE_VERSION. */
	private static final String DEVICE_SOFTWARE_VERSION = "Device.DeviceInfo.SoftwareVersion";

	/** The Constant INTERNETGATEWAYDEVICE_SOFTWARE_VERSION. */
	private static final String INTERNETGATEWAYDEVICE_SOFTWARE_VERSION = "InternetGatewayDevice.DeviceInfo.SoftwareVersion";

	//	private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
	/** The tms format. */
	private static SimpleDateFormat tmsFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	/** The date format. */
	private static SimpleDateFormat dateFormat = new SimpleDateFormat("dd");

	/** The year format. */
	private static SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");

	/** The month format. */
	private static SimpleDateFormat monthFormat = new SimpleDateFormat("MM");

	/**
	 * Gets the serial number utp.
	 *
	 * @param unittype the unittype
	 * @return the serial number utp
	 */
	private UnittypeParameter getSerialNumberUtp(Unittype unittype) {
		return unittype.getUnittypeParameters().getByName(SystemParameters.SERIAL_NUMBER);
	}

	/**
	 * Action create distributor.
	 *
	 * @param sessionId the session id
	 * @param distributorName the distributor name
	 * @param unittypeName the unittype name
	 * @throws NoAvailableConnectionException the no available connection exception
	 * @throws SQLException the sQL exception
	 */
	public void actionCreateDistributor(String sessionId, String distributorName, String unittypeName) throws NoAvailableConnectionException, SQLException {
		XAPS xaps = XAPSLoader.getXAPS(sessionId);
		Unittype unittype = xaps.getUnittype(unittypeName + "-" + distributorName);
		if (unittype == null) {
			//			String matcherId = getMatcherId(unittypeName);
			unittype = new Unittype(unittypeName + "-" + distributorName, "PingCom", "Dummy description", ProvisioningProtocol.OPP);
			xaps.getUnittypes().addOrChangeUnittype(unittype, xaps);
		}
		Profile profile = unittype.getProfiles().getByName("Default");
		if (profile == null) {
			profile = new Profile("Default", unittype);
			unittype.getProfiles().addOrChangeProfile(profile, xaps);
		}
		UnittypeParameter serialUtp = getSerialNumberUtp(unittype);
		//		if (serialUtp == null) {
		//			if (unittypeName.equals("NPA201E") || unittypeName.startsWith("RGW")) {
		//				serialUtp = new UnittypeParameter(unittype, "Device.DeviceInfo.SerialNumber", new UnittypeParameterFlag("R"));
		//			} else {
		//				serialUtp = new UnittypeParameter(unittype, "InternetGatewayDevice.DeviceInfo.SerialNumber", new UnittypeParameterFlag("R"));
		//			}
		//		}
		unittype.getUnittypeParameters().addOrChangeUnittypeParameter(serialUtp, xaps);
		UnittypeParameter versionUtp = unittype.getUnittypeParameters().getByName("Device.DeviceInfo.SoftwareVersion");
		if (versionUtp == null) {
			versionUtp = new UnittypeParameter(unittype, "Device.DeviceInfo.SoftwareVersion", new UnittypeParameterFlag("R"));
		}
		unittype.getUnittypeParameters().addOrChangeUnittypeParameter(versionUtp, xaps);
	}

	/*
	 * Will retrieve all necessary information about a shipment, all unit params and all unit job results 
	 */
	/**
	 * Action retrieve shipment data.
	 *
	 * @param unittype the unittype
	 * @param profile the profile
	 * @param sessionId the session id
	 * @param shipmentName the shipment name
	 * @param cp the cp
	 * @return the list
	 * @throws NoAvailableConnectionException the no available connection exception
	 * @throws SQLException the sQL exception
	 * @throws ClassNotFoundException 
	 */
	public List<ShippedUnit> actionRetrieveShipmentData(Unittype unittype, Profile profile, String sessionId, String shipmentName, ConnectionProperties cp) throws NoAvailableConnectionException,
			SQLException, ClassNotFoundException {
		XAPSUnit xapsUnit = XAPSLoader.getXAPSUnit(sessionId);
		List<Parameter> upList = new ArrayList<Parameter>();
		// Konverter shipmentName (2010-03) to UnitParameters (2010, 03)
		String year = shipmentName.substring(0, 4).trim();
		String month = shipmentName.substring(5, 7).trim();
		//		UnittypeParameter shipmentUtp = unittype.getUnittypeParameters().getByName(SystemParameters.STAGING_SHIPMENT);
		UnittypeParameter syUtp = unittype.getUnittypeParameters().getByName(SystemParameters.STAGING_SHIPMENT_YEAR);
		UnittypeParameter smUtp = unittype.getUnittypeParameters().getByName(SystemParameters.STAGING_SHIPMENT_MONTH);
		upList.add(new Parameter(syUtp, year));
		upList.add(new Parameter(smUtp, month));
		Map<String, Unit> unitMap = xapsUnit.getUnits(unittype, profile, upList, Integer.MAX_VALUE);
		List<ShippedUnit> shippedUnits = new ArrayList<ShippedUnit>();
		Connection c = ConnectionProvider.getConnection(cp);
		SQLException sqle = null;
		Statement s = null;
		try {
			for (String unitId : unitMap.keySet()) {
				String sql = "SELECT start_timestamp, status FROM unit_job WHERE unit_id = '" + unitId + "'";
				s = c.createStatement();
				ResultSet rs = s.executeQuery(sql);
				// We expect 1 unit-job or nothing. (We do not consider the possibility of 2 unit jobs)
				String startTimestamp = null;
				String status = null;
				String serialNumber = null;
				String registeredTimestamp = null;
				if (rs.next()) {
					Date tms = rs.getTimestamp("start_timestamp");
					if (tms != null)
						startTimestamp = tmsFormat.format(tms);
					status = rs.getString("status");
				}

				Unit unit = xapsUnit.getUnitById(unitId, unittype, profile);
				for (UnitParameter up : unit.getUnitParameters().values()) {
					if (up.getParameter().getUnittypeParameter().getName().equals(SystemParameters.SERIAL_NUMBER)) {
						serialNumber = up.getValue();
					}
					if (up.getParameter().getUnittypeParameter().getName().equals(SystemParameters.STAGING_SHIPMENT_REGISTERED)) {
						registeredTimestamp = up.getValue();
					}
				}

				shippedUnits.add(new ShippedUnit(unitId, serialNumber, status, registeredTimestamp, startTimestamp));

			}
		} catch (SQLException sqlex) {
			sqle = sqlex;
			throw sqlex;
		} finally {
			if (s != null)
				s.close();
			if (c != null)
				ConnectionProvider.returnConnection(c, sqle);
		}
		return shippedUnits;
	}

	// Shipment name must also contain timestamp
	// This method runs SQL directly, in violation with the principle of having all SQL in 
	// DBI. However, this SQL is very Web-specific, thus we allow it.
	//	public List<UnitJobExtended> actionRetrieveShipmentData(Unittype unittype, String sessionId, String shipmentName, ConnectionProperties cp) throws SQLException, NoAvailableConnectionException {
	//		Connection c = ConnectionProvider.getConnection(cp);
	//		SQLException sqle = null;
	//		Statement s = null;
	//		List<UnitJobExtended> unitJobs = new ArrayList<UnitJobExtended>();
	//		try {
	//			UnittypeParameter utp = unittype.getUnittypeParameters().getByName(SystemParameters.STAGING_SHIPMENT);
	//			String sql = "select up.unit_id, job_id, status, start_timestamp, confirmed, ";
	//			sql += "unconfirmed from unit_param up LEFT JOIN unit_job uj ON up.unit_id = uj.unit_id ";
	//			sql += "WHERE unit_type_param_id = " + utp.getId() + " and value = '" + shipmentName + "'";
	//			s = c.createStatement();
	//			ResultSet rs = s.executeQuery(sql);
	//			while (rs.next()) {
	//				String unitId = rs.getString("unit_id");
	//				String jobIdStr = rs.getString("job_id");
	//				String status = rs.getString("status");
	//				Timestamp startTms = rs.getTimestamp("start_timestamp");
	//				//				String processedStr = rs.getString("processed");
	//				String confirmedStr = rs.getString("confirmed");
	//				String unconfirmedStr = rs.getString("unconfirmed");
	//				Integer jobId = null;
	//				if (jobIdStr != null)
	//					jobId = new Integer(jobIdStr);
	//				UnitJob uj = new UnitJob(unitId, jobId);
	//				if (startTms == null)
	//					uj.setStartTimestamp(null);
	//				else
	//					uj.setStartTimestamp(new Date(startTms.getTime()));
	//				uj.setStatus(status);
	//				if (confirmedStr != null)
	//					uj.setConfirmedFailed(new Integer(confirmedStr));
	//				if (unconfirmedStr != null)
	//					uj.setUnconfirmedFailed(new Integer(unconfirmedStr));
	//				unitJobs.add(UnitJobExtended.get(uj));
	//			}
	//		} catch (SQLException sqlex) {
	//			sqle = sqlex;
	//			throw sqlex;
	//		} finally {
	//			if (s != null)
	//				s.close();
	//			if (c != null)
	//				ConnectionProvider.returnConnection(c, sqle);
	//		}
	//		return unitJobs;
	//	}
	//	public class UnitJobComparator implements Comparator<UnitJobExtended> {
	//
	//		public int compare(UnitJobExtended o1, UnitJobExtended o2) {
	//			if (o1.isCompleted() && !o2.isCompleted())
	//				return 1;
	//			else if (o1.isCompleted() && o2.isCompleted())
	//				return 0;
	//			else if (o1.isFailed() && !o2.isFailed())
	//				return 1;
	//			else if (o1.isFailed() && o2.isFailed())
	//				return 0;
	//			return -1;
	//		}
	//
	//	}
	// units-map consists of Key: unitId, Value: OPP-secret (this information can be retrieved directly from a Taiwan-file)
	/**
	 * Action add units to distributor.
	 *
	 * @param unittype the unittype
	 * @param sessionId the session id
	 * @param taiwanFileLines the taiwan file lines
	 * @param version the version
	 * @throws Exception the exception
	 */
	public void actionAddUnitsToDistributor(Unittype unittype, String sessionId, List<String> taiwanFileLines, String version) throws Exception {
		XAPSUnit xapsUnit = XAPSLoader.getXAPSUnit(sessionId);
		Profile profile = unittype.getProfiles().getByName("Default");
		List<UnitParameter> unitParamList = new ArrayList<UnitParameter>();
		List<String> unitList = new ArrayList<String>();
		Map<String, String> unitMap = new HashMap<String, String>();
		for (int i = 0; i < taiwanFileLines.size(); i++) {
			String line = taiwanFileLines.get(i);
			String[] tflArray = StringUtil.split(line);
			if (tflArray.length < 2) {
				warnings.put("Line #" + i, "The entry is not valid. Should contain serialNumber and secret.");
				continue;
			}
			String mac = tflArray[0];
			String secret = tflArray[1];
			String unitId = MAC2UUID(mac);
			if (unitMap.containsKey(unitId))
				continue;
			unitMap.put(unitId, "dummy");
			unitList.add(unitId);
			UnittypeParameter mainSecretUtp = unittype.getUnittypeParameters().getByName(SystemParameters.SECRET);
			UnittypeParameter dVersionUtp = unittype.getUnittypeParameters().getByName(DEVICE_SOFTWARE_VERSION);
			UnittypeParameter igdVersionUtp = unittype.getUnittypeParameters().getByName(INTERNETGATEWAYDEVICE_SOFTWARE_VERSION);
			UnittypeParameter serialUtp = getSerialNumberUtp(unittype);
			if (serialUtp == null || mainSecretUtp == null || (dVersionUtp == null && igdVersionUtp == null)) {
				errors.put(mac, "The unittype does not contain the appropriate unittype parameters (Secret, SerialNumber and SoftwareVersion)");
				break;
			} else {
				if (mainSecretUtp != null) {
					UnitParameter secretUp = new UnitParameter(mainSecretUtp, unitId, secret, profile);
					unitParamList.add(secretUp);
				}
				if (dVersionUtp != null) {
					UnitParameter versionUp = new UnitParameter(dVersionUtp, unitId, version, profile);
					unitParamList.add(versionUp);
				}
				if (igdVersionUtp != null) {
					UnitParameter versionUp = new UnitParameter(igdVersionUtp, unitId, version, profile);
					unitParamList.add(versionUp);
				}
				UnitParameter serialUp = new UnitParameter(serialUtp, unitId, mac, profile);
				unitParamList.add(serialUp);
			}
		}
		if (errors.size() == 0) {
			xapsUnit.addUnits(unitList, profile);
			xapsUnit.addOrChangeUnitParameters(unitParamList, profile);
		}
	}

	/**
	 * The Class ShipmentCache.
	 */
	public class ShipmentCache {

		/** The shipmentname. */
		private String shipmentname;

		/** The timestamp. */
		private String timestamp;

		/** The unit ids to move. */
		private List<String> unitIdsToMove = new ArrayList<String>();

		/** The unit parameters. */
		private List<UnitParameter> unitParameters = new ArrayList<UnitParameter>();

		/** The units. */
		private List<FoundUnit> units = new ArrayList<FoundUnit>();

		/**
		 * Instantiates a new shipment cache.
		 */
		public ShipmentCache() {

		}

		/**
		 * Sets the unit ids to move.
		 *
		 * @param unitIdsToMove the new unit ids to move
		 */
		protected void setUnitIdsToMove(List<String> unitIdsToMove) {
			this.unitIdsToMove = unitIdsToMove;
		}

		/**
		 * Gets the unit ids to move.
		 *
		 * @return the unit ids to move
		 */
		public List<String> getUnitIdsToMove() {
			return unitIdsToMove;
		}

		/**
		 * Sets the unit parameters.
		 *
		 * @param unitParameters the new unit parameters
		 */
		protected void setUnitParameters(List<UnitParameter> unitParameters) {
			this.unitParameters = unitParameters;
		}

		/**
		 * Gets the unit parameters.
		 *
		 * @return the unit parameters
		 */
		public List<UnitParameter> getUnitParameters() {
			return unitParameters;
		}

		/**
		 * Sets the shipmentname.
		 *
		 * @param shipmentname the new shipmentname
		 */
		protected void setShipmentname(String shipmentname) {
			this.shipmentname = shipmentname;
		}

		/**
		 * Gets the shipmentname.
		 *
		 * @return the shipmentname
		 */
		public String getShipmentname() {
			return shipmentname;
		}

		/**
		 * Sets the timestamp.
		 *
		 * @param l the new timestamp
		 */
		protected void setTimestamp(String l) {
			timestamp = l;
		}

		/**
		 * Gets the timestamp.
		 *
		 * @return the timestamp
		 */
		public String getTimestamp() {
			return timestamp;
		}

		/**
		 * Sets the units.
		 *
		 * @param units the new units
		 */
		public void setUnits(List<FoundUnit> units) {
			this.units = units;
		}

		/**
		 * Gets the found.
		 *
		 * @return the found
		 */
		public List<FoundUnit> getFound() {
			return units;
		}

		/**
		 * Gets the units.
		 *
		 * @return the units
		 */
		public List<Unit> getUnits() {
			List<Unit> list = new ArrayList<Unit>();
			for (FoundUnit unit : units) {
				list.add(unit.getUnit());
			}
			return list;
		}
	}

	/**
	 * The Class FoundUnit.
	 */
	public class FoundUnit {

		/**
		 * Instantiates a new found unit.
		 *
		 * @param m the m
		 * @param u the u
		 */
		public FoundUnit(String m, Unit u) {
			unit = u;
			mac = m;
		}

		/**
		 * Gets the mac.
		 *
		 * @return the mac
		 */
		public String getMac() {
			return mac;
		}

		/**
		 * Sets the mac.
		 *
		 * @param mac the new mac
		 */
		public void setMac(String mac) {
			this.mac = mac;
		}

		/**
		 * Gets the unit.
		 *
		 * @return the unit
		 */
		public Unit getUnit() {
			return unit;
		}

		/**
		 * Sets the unit.
		 *
		 * @param unit the new unit
		 */
		public void setUnit(Unit unit) {
			this.unit = unit;
		}

		/** The mac. */
		private String mac;

		/** The unit. */
		private Unit unit;
	}

	/**
	 * Gets the uTP.
	 *
	 * @param name the name
	 * @param unittype the unittype
	 * @param xaps the xaps
	 * @return the uTP
	 * @throws SQLException the sQL exception
	 * @throws NoAvailableConnectionException the no available connection exception
	 */
	public UnittypeParameter getUTP(String name, Unittype unittype, XAPS xaps) throws SQLException, NoAvailableConnectionException {
		if (name == null)
			return null;
		UnittypeParameter utp = unittype.getUnittypeParameters().getByName(name);
		if (utp == null) {
			utp = new UnittypeParameter(unittype, name, new UnittypeParameterFlag("X"));
			unittype.getUnittypeParameters().addOrChangeUnittypeParameter(utp, xaps);
		}
		return utp;
	}

	/**
	 * Action create return.
	 *
	 * @param unittype the unittype
	 * @param profile the profile
	 * @param sessionId the session id
	 * @param macs the macs
	 * @return the shipment cache
	 * @throws Exception the exception
	 */
	public ShipmentCache actionCreateReturn(Unittype unittype, Profile profile, String sessionId, List<String> macs) throws Exception {
		XAPSUnit xapsUnit = XAPSLoader.getXAPSUnit(sessionId);
		XAPS xaps = XAPSLoader.getXAPS(sessionId);
		Permissions perms = xaps.getSyslog().getIdentity().getUser().getPermissions();
		List<String> unitIdsToMove = new ArrayList<String>();
		List<FoundUnit> foundUnits = new ArrayList<FoundUnit>();

		for (String mac : macs) {
			Unit unit = xapsUnit.getUnitByValue(mac, null, null);
			if (unit == null) {
				errors.put(mac, "The SerialNumber did not correspond to any unit in xAPS");
				continue;
			}
			if (!unit.getUnittype().getName().equals(unittype.getName())) {
				errors.put(mac, "The SerialNumber belongs to another unittype - not allowed to return unit to pool");
				continue;
			}
			if (unit.getProfile().getName().equals("Default")) {
				warnings.put(mac, "The unit is already returned to pool");
				continue;
			}
			if (!unit.getProfile().getName().equals(profile.getName())) {
				if (perms.getByUnittypeProfile(unittype.getId(), unit.getProfile().getId()) == null) {
					errors.put(mac, "The SerialNumber belongs to another Provider (which is not accesible by this login)");
					continue;
				}
			}
			unitIdsToMove.add(unit.getId());
			foundUnits.add(new FoundUnit(mac, unit));
		}
		ShipmentCache shipment = new ShipmentCache();
		shipment.setShipmentname("Return");
		shipment.setUnitIdsToMove(unitIdsToMove);
		shipment.setUnits(foundUnits);
		return shipment;
	}

	/**
	 * Action create shipment.
	 *
	 * @param unittype the unittype
	 * @param profile the profile
	 * @param sessionId the session id
	 * @param shipmentName the shipment name
	 * @param macs the macs
	 * @param forceMove the force move
	 * @return the shipment cache
	 * @throws Exception the exception
	 */
	public ShipmentCache actionCreateShipment(Unittype unittype, Profile profile, String sessionId, String shipmentName, List<String> macs, boolean forceMove) throws Exception {
		Map<String, Unit> unitMap = new HashMap<String, Unit>();
		List<UnitParameter> unitParameters = new ArrayList<UnitParameter>();
		List<String> unitIdsToMove = new ArrayList<String>();
		List<FoundUnit> foundUnits = new ArrayList<FoundUnit>();
		XAPSUnit xapsUnit = XAPSLoader.getXAPSUnit(sessionId);
		Date now = new Date();

		UnittypeParameter serialNumberUtp = unittype.getUnittypeParameters().getByName(SystemParameters.SERIAL_NUMBER);
		//		if (serialNumberUtp == null)
		//			serialNumberUtp = unittype.getUnittypeParameters().getByName("Device.DeviceInfo.SerialNumber");
		UnittypeParameter secretUtp = unittype.getUnittypeParameters().getByName(SystemParameters.SECRET);
		//		if (secretUtp == null) {
		//			if (unittype.getProtocol() == ProvisioningProtocol.OPP) {
		//				secretUtp = unittype.getUnittypeParameters().getByName(SystemParameters.OPP_SECRET);
		//				//			} else if (unittype.getProtocol().equals(SystemConstants.TR069)) {
		//				//				secretUtp = unittype.getUnittypeParameters().getByName(SystemParameters.TR069_SECRET);
		//			}
		//		}

		Profile defaultProfile = unittype.getProfiles().getByName("Default");

		List<Unit> units = xapsUnit.getLimitedUnitsByValues(macs);
		units = xapsUnit.getUnitsWithParameters(unittype, null, units);

		Map<String, Unit> sn2UnitMap = new HashMap<>();
		for (Unit unit : units) {
			//			String mac = unit.getUnitParameters().get(SystemParameters.MAC) == null ? null : unit.getUnitParameters().get(SystemParameters.MAC).getValue();
			//			if (mac == null)
			String sn = unit.getUnitParameters().get(SystemParameters.SERIAL_NUMBER) == null ? null : unit.getUnitParameters().get(SystemParameters.SERIAL_NUMBER).getValue();
			//			if (sn == null)
			//				sn = unit.getUnitParameters().get("InternetGatewayDevice.DeviceInfo.SerialNumber") == null ? null : unit.getUnitParameters().get("InternetGatewayDevice.DeviceInfo.SerialNumber")
			//						.getValue();
			if (sn != null)
				sn2UnitMap.put(sn.toUpperCase(), unit);
		}
		for (String mac : macs) {
			Unit unit = sn2UnitMap.get(mac.toUpperCase());
			if (unit == null) {
				errors.put(mac, "The SerialNumber did not correspond to a unit in this unittype.");
				continue;
			}

			//			else if (!unit.getProfile().equals(profile)) {
			//				errors.put(mac, "The unit has already been shipped to another provider.");
			//				continue;
			//			}

			String serialNumber = unit.getParameters().get(serialNumberUtp.getName());
			String secret = unit.getParameters().get(secretUtp.getName());

			if (serialNumber == null) {
				errors.put(unit.getId(), "Missing SerialNumber unit parameter. Advise: The unit must be corrected.");
				continue;
			}
			if (secret == null) {
				errors.put(unit.getId(), "Missing Secret unit parameter. Advise: The unit must be corrected.");
				continue;
			}
			if (unitMap.containsKey(mac)) {
				errors.put(mac, "The SerialNumber/MAC is already added. Advise: Check your SerialNumber/MAC list for double scans");
				continue;
			}
			if (unit.getParameters().get(SystemParameters.LAST_CONNECT_TMS) != null && unit.getParameters().get(SystemParameters.DESIRED_SOFTWARE_VERSION) != null) {
				String msg = "The unit has already connected and may have staged. If so it will not stage.";
				warnings.put(mac, msg);
			}
			if (unit.getProfile() != defaultProfile && unit.getProfile().getId() != profile.getId())
				warnings.put(mac, "The SerialNumber was moved from another provider (" + unit.getProfile().getName() + "), but since it has not been staged, provider can be changed");
			if (unit.getProfile() == defaultProfile && unit.getUnitParameters().get(SystemParameters.STAGING_SHIPMENT_MONTH) != null && warnings.get(mac) == null)
				warnings.put(mac, "The SerialNumber is registered on a unit which has previously been shipped and then cancelled/returned  - will change to shipped state.");
			//			if (unit.getUnitParameters().get(SystemParameters.STAGING_SHIPMENT_MONTH) != null && warnings.get(mac) == null)
			//				warnings.put(mac, "The SerialNumber is registered on a unit which has previously been shipped and then cancelled/returned  - will change to shipped state.");

			unitMap.put(mac, unit);
			unitIdsToMove.add(unit.getId());
			foundUnits.add(new FoundUnit(mac, unit));

			//			UnittypeParameter shipmentUtp = unittype.getUnittypeParameters().getByName(SystemParameters.STAGING_SHIPMENT);
			//			if (shipmentUtp == null) {
			//				shipmentUtp = new UnittypeParameter(SystemParameters.STAGING_SHIPMENT, new UnittypeParameterFlag("X"));
			//				unittype.getUnittypeParameters().addOrChangeUnittypeParameter(shipmentUtp, XAPSLoader.getXAPS(sessionId));
			//			}
			//			UnitParameter snUp = new UnitParameter(shipmentUtp, unit.getId(), shipmentName /*+ ":" + timestamp*/, unit.getProfile());
			//			unitParameters.add(snUp);

			UnittypeParameter srUtp = unittype.getUnittypeParameters().getByName(SystemParameters.STAGING_SHIPMENT_REGISTERED);
			UnitParameter srUp = new UnitParameter(srUtp, unit.getId(), tmsFormat.format(now), unit.getProfile());
			unitParameters.add(srUp);

			UnittypeParameter syUtp = unittype.getUnittypeParameters().getByName(SystemParameters.STAGING_SHIPMENT_YEAR);
			UnitParameter syUp = new UnitParameter(syUtp, unit.getId(), yearFormat.format(now), unit.getProfile());
			unitParameters.add(syUp);

			UnittypeParameter smUtp = unittype.getUnittypeParameters().getByName(SystemParameters.STAGING_SHIPMENT_MONTH);
			UnitParameter smUp = new UnitParameter(smUtp, unit.getId(), monthFormat.format(now), unit.getProfile());
			unitParameters.add(smUp);

			UnittypeParameter sdUtp = unittype.getUnittypeParameters().getByName(SystemParameters.STAGING_SHIPMENT_DATE);
			UnitParameter sdUp = new UnitParameter(sdUtp, unit.getId(), dateFormat.format(now), unit.getProfile());
			unitParameters.add(sdUp);

		}

		ShipmentCache shipment = new ShipmentCache();
		shipment.setShipmentname(shipmentName);
		//shipment.setTimestamp(timestamp);
		shipment.setUnitIdsToMove(unitIdsToMove);
		shipment.setUnitParameters(unitParameters);
		shipment.setUnits(foundUnits);

		return shipment;
	}

	/**
	 * MA c2 uuid.
	 *
	 * @param MAC the mAC
	 * @return the string
	 * @throws Exception the exception
	 */
	public static String MAC2UUID(String MAC) throws Exception {
		String fixmac = "";
		for (int i = 0; i < 6; i++)
			fixmac += MAC.substring(i * 2, i * 2 + 2) + ":";
		MAC = fixmac.substring(0, 12 + 5);
		MAC = MAC.toLowerCase(); // Note!!!  UUID is generated from lowercase MAC
		java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-1");
		String name = "opp://owera.com/;mac=" + MAC;
		UUID uuid = UUID.nameUUIDFromBytes(name.getBytes());
		String uuid_str = uuid.toString();
		// Replace with version 5
		uuid_str = uuid_str.substring(0, 14) + '5' + uuid_str.substring(15, 36);
		return uuid_str;
	}

	public static void main(String[] args) throws Exception {
		System.out.println(MAC2UUID("00:A0:C9:14:C8:29"));
	}

	/**
	 * This code should be copied directly into xAPS Web.
	 *
	 * @param provider the provider
	 * @param units the units
	 * @throws Exception the exception
	 */
	private void createShipment(com.owera.xaps.dbi.Profile provider, List<com.owera.xaps.dbi.Unit> units, String user) throws Exception {
		XAPSWSProxy tp = new XAPSWSProxy();
		ProfileParameters pps = provider.getProfileParameters();
		ProfileParameter urlpp = pps.getByName(SystemParameters.STAGING_PROVIDER_WSURL);
		ProfileParameter emailpp = pps.getByName(SystemParameters.STAGING_PROVIDER_EMAIL);
		//		ProfileParameter protocolpp = pps.getByName(SystemParameters.STAGING_PROVIDER_PROTOCOL);
		//		if (protocolpp == null || protocolpp.getValue() == null || (!protocolpp.getValue().equals(SystemConstants.OPP) && !protocolpp.getValue().equals(SystemConstants.TR069))) {
		//			logger.error("The provider does not specify a correct protocol (OPP or TR-069)");
		//			errors.put("All units", "The provider does not specify a correct protocol (OPP or TR-069)");
		//			return;
		//		}
		//		String protocol = protocolpp.getValue();
		String url = null;
		if (urlpp != null && urlpp.getValue() != null && urlpp.getValue().trim().length() > 0) {
			url = urlpp.getValue();
			String unittypeName = pps.getByName(SystemParameters.STAGING_PROVIDER_UNITTYPE).getValue();
			String profileName = pps.getByName(SystemParameters.STAGING_PROVIDER_PROFILE).getValue();
			tp.setEndpoint(url);
			if (url.startsWith("https")) {
				logger.debug("Provider URL is HTTPS, will check certificates and install if needed");
				HTTPSManager.installCertificate(url, WebProperties.getString("keystore.pass", "changeit"));
			}
			com.owera.xapsws.Profile p = new com.owera.xapsws.Profile();
			p.setName(profileName);
			com.owera.xapsws.Unittype ut = new com.owera.xapsws.Unittype();
			ut.setName(unittypeName);
			com.owera.xaps.dbi.Unittype unittypeXAPS = provider.getUnittype();
			UnittypeParameter snUtp = unittypeXAPS.getUnittypeParameters().getByName(SystemParameters.SERIAL_NUMBER);
			//			if (snUtp == null)
			//				snUtp = unittypeXAPS.getUnittypeParameters().getByName("Device.DeviceInfo.SerialNumber");
			UnittypeParameter secUtp = unittypeXAPS.getUnittypeParameters().getByName(SystemParameters.SECRET);
			//			if (secUtp == null) {
			//				if (unittypeXAPS.getProtocol().equals(SystemConstants.OPP)) {
			//					secUtp = unittypeXAPS.getUnittypeParameters().getByName(SystemParameters.OPP_SECRET);
			//				} else if (unittypeXAPS.getProtocol().equals(SystemConstants.TR069)) {
			//					secUtp = unittypeXAPS.getUnittypeParameters().getByName(SystemParameters.TR069_SECRET);
			//				}
			//			}

			String wsuser = null;
			ProfileParameter wsuserpp = pps.getByName(SystemParameters.STAGING_PROVIDER_WSUSER);
			if (wsuserpp != null)
				wsuser = wsuserpp.getValue();

			String wspass = null;
			ProfileParameter wspasspp = pps.getByName(SystemParameters.STAGING_PROVIDER_WSPASSWORD);
			if (wspasspp != null)
				wspass = wspasspp.getValue();

			logger.debug("All parmeters and information is ready, will now copy units to Provider's xAPS Server");
			for (com.owera.xaps.dbi.Unit unitXAPS : units) {
				com.owera.xapsws.Unit unit = new com.owera.xapsws.Unit();
				String serialNumber = unitXAPS.getParameters().get(snUtp.getName());
				if (serialNumber == null || serialNumber.trim().length() != 12) {
					logger.error(unitXAPS.getId() + " copy process failed because serialNumber was not found on the unit, which we need to generate correct unitid");
					errors.put(unitXAPS.getId(), "Failed to copy unit -> serialNumber was not found on the unit, which we need to generate correct unitid");
					return;
				}
				//				if (protocol.equals(SystemConstants.OPP)) {
				//					unit.setUnitId(MAC2UUID(serialNumber.trim()));
				//				} else {
				if (unittypeXAPS.getName().contains("NPA201E"))
					unit.setUnitId("002194-NPA201E-" + serialNumber.trim());
				else if (unittypeXAPS.getName().contains("RGW208EN") || unittypeXAPS.getName().contains("IAD208AN"))
					unit.setUnitId("002194-RGW208EN-" + serialNumber.trim());
				else
					unit.setUnitId(serialNumber.substring(0, 6) + "-" + unittypeXAPS.getName() + "-" + serialNumber.trim());
				//				}
				String secret = unitXAPS.getParameters().get(secUtp.getName());
				//				String providerSerialNumber = getSerialNumberFromProvider(provider);
				//				String providerSecret = getSecretFromProvider(provider);
				com.owera.xapsws.Parameter[] parameterArr = new com.owera.xapsws.Parameter[2];
				//				parameterArr[0] = new com.owera.xapsws.Parameter(providerSerialNumber, serialNumber, null);
				parameterArr[0] = new com.owera.xapsws.Parameter(SystemParameters.SERIAL_NUMBER, serialNumber, null);
				//				parameterArr[2] = new com.owera.xapsws.Parameter(providerSecret, secret, null);
				parameterArr[1] = new com.owera.xapsws.Parameter(SystemParameters.SECRET, secret, null);
				ParameterList parameters = new ParameterList(new ArrayOfParameter(parameterArr));
				unit.setParameters(parameters);
				unit.setUnittype(ut);
				unit.setProfile(p);

				Login login = new Login(wsuser, wspass);

				AddOrChangeUnitRequest auReq = new AddOrChangeUnitRequest(login, unit);
				try {
					AddOrChangeUnitResponse auRes = tp.addOrChangeUnit(auReq);
					logger.debug(auRes.getUnit().getUnitId() + " was copied successfully");
				} catch (Exception e) {
					logger.error(unitXAPS.getId() + " copy process failed: " + e.getMessage(), e);
					errors.put(unitXAPS.getId(), "Failed to copy unit -> " + e.getLocalizedMessage());
					return;
				}
			}
		}
		if (emailpp != null && emailpp.getValue() != null && emailpp.getValue().trim().length() > 0) {
			String emailaddr = emailpp.getValue();
			com.owera.xaps.dbi.Unittype unittypeXAPS = provider.getUnittype();
			StringBuffer msg = new StringBuffer("This mail is autogenerated from Fusion Staging Server\n\n");
			msg.append("A shipment has been set up in Staging Server by user " + user + ". This mail is a confirmation\n");
			msg.append("of a successful execution.\n\n");
			if (url == null) {
				msg.append("Through the shipment process we have detected that no automatic xAPS shipment data\n");
				msg.append("tranfer/copying to your local provisioning system has been set up. Therefore we\n");
				msg.append("expect you to take good care of this email, since it contains all necessary\n");
				msg.append("information to set up your local provisioning system. A staging of the firmware may\n");
				msg.append("have been set up, so that the new firmware will point to your local provisioning\n");
				msg.append("system, but this is not mandatory, it depends on the agreement. In case it has not been\n");
				msg.append("set up, keep in mind that you will need to do a manual firmware upgrade of your devices.\n\n");
			}
			msg.append(String.format("%1$-40s%2$-15s%3$-45s\n", "ACS-Username", "MAC Address", "ACS-Password"));
			msg.append("====================================================================================================\n");

			for (com.owera.xaps.dbi.Unit unitXAPS : units) {
				Map<String, UnitParameter> unitParams = unitXAPS.getUnitParameters();
				UnitParameter snUp = unitParams.get(SystemParameters.SERIAL_NUMBER);
				//				if (snUp == null)
				//					snUp = unitParams.get("InternetGatewayDevice.DeviceInfo.SerialNumber");
				UnitParameter secUp = unitParams.get(SystemParameters.SECRET);
				com.owera.xapsws.Unit unit = new com.owera.xapsws.Unit();
				String serialNumber = snUp.getValue();
				String secValue = secUp.getValue();
				if (url != null)
					secValue = "(Set securely through Fusion Web Service)";
				else {
					if ((unittypeXAPS.getName().contains("NPA201E") || unittypeXAPS.getName().contains("RGW208EN") || unittypeXAPS.getName().contains("IAD208AN")) && secValue != null
							&& secValue.length() > 16)
						secValue = secValue.substring(0, 16);
				}
				if (unittypeXAPS.getName().contains("NPA201E"))
					unit.setUnitId("002194-NPA201E-" + serialNumber.trim());
				else if (unittypeXAPS.getName().contains("RGW208EN") || unittypeXAPS.getName().contains("IAD208AN"))
					unit.setUnitId("002194-RGW208EN-" + serialNumber.trim());
				else
					unit.setUnitId(serialNumber.substring(0, 6) + "-" + unittypeXAPS.getName() + "-" + serialNumber);

				//				}
				msg.append(String.format("%1$-40s%2$-15s%3$-45s\n", unit.getUnitId(), serialNumber, secValue));
			}
			postMail("xAPS Staging Server has registered a shipment of " + units.size() + " units", msg.toString(), emailaddr);
		}
	}

	/**
	 * Gets the serial number from provider.
	 *
	 * @param profile the profile
	 * @return the serial number from provider
	 */
	//	private String getSerialNumberFromProvider(Profile profile) {
	//		ProfileParameter pp = profile.getProfileParameters().getByName(SystemParameters.STAGING_PROVIDER_SNPARAMETER);
	//		if (pp != null && pp.getValue() != null) {
	//			return pp.getValue();
	//		} else {
	//			throw new RuntimeException("Provider parameter " + SystemParameters.STAGING_PROVIDER_SNPARAMETER + " is NULL. Giving up.");
	//		}
	//	}

	/**
	 * Gets the secret from provider.
	 *
	 * @param profile the profile
	 * @return the secret from provider
	 */
	//	private String getSecretFromProvider(Profile profile) {
	//		ProfileParameter pp = profile.getProfileParameters().getByName(SystemParameters.STAGING_PROVIDER_SECPARAMETER);
	//		if (pp != null && pp.getValue() != null) {
	//			return pp.getValue();
	//		} else {
	//			throw new RuntimeException("Provider parameter " + SystemParameters.STAGING_PROVIDER_SECPARAMETER + " is NULL. Giving up.");
	//		}
	//	}

	/**
	 * Action confirm return.
	 *
	 * @param unittype the unittype
	 * @param profile the profile
	 * @param sessionId the session id
	 * @return the shipment cache
	 * @throws Exception the exception
	 */
	public ShipmentCache actionConfirmReturn(Unittype unittype, Profile profile, String sessionId) throws Exception {
		XAPSUnit xapsUnit = XAPSLoader.getXAPSUnit(sessionId);
		ShipmentCache shipment = SessionCache.getSessionData(sessionId).getShipmentCache();
		xapsUnit.moveUnits(shipment.getUnitIdsToMove(), profile);
		return shipment;
	}

	/**
	 * Action confirm shipment.
	 *
	 * @param unittype the unittype
	 * @param profile the profile
	 * @param sessionId the session id
	 * @return the shipment cache
	 * @throws Exception the exception
	 */
	public ShipmentCache actionConfirmShipment(Unittype unittype, Profile profile, String sessionId) throws Exception {
		XAPSUnit xapsUnit = XAPSLoader.getXAPSUnit(sessionId);
		ShipmentCache shipment = SessionCache.getSessionData(sessionId).getShipmentCache();
		xapsUnit.moveUnits(shipment.getUnitIdsToMove(), profile);
		xapsUnit.addOrChangeUnitParameters(shipment.getUnitParameters(), profile);
		XAPS xaps = XAPSLoader.getXAPS(sessionId);
		List<UnitParameter> unitParams = shipment.getUnitParameters();
		String year = "YYYY";
		Group yearGroup = null;
		String month = "MM";
		Group monthGroup = null;
		for (UnitParameter up : unitParams) {
			if (up.getParameter().getUnittypeParameter().getName().equals(SystemParameters.STAGING_SHIPMENT_YEAR)) {
				String groupName = up.getValue() + ":" + profile.getName();
				Group group = unittype.getGroups().getByName(groupName);
				if (group == null) {
					group = new Group(groupName, "All shipped units in " + up.getValue(), null, unittype, profile);
					unittype.getGroups().addOrChangeGroup(group, xaps);
					GroupParameter gp = new GroupParameter(up.getParameter(), group);
					group.getGroupParameters().addOrChangeGroupParameter(gp, xaps);
				}
				year = up.getValue();
				yearGroup = group;
				break;
			}
		}
		for (UnitParameter up : unitParams) {
			if (up.getParameter().getUnittypeParameter().getName().equals(SystemParameters.STAGING_SHIPMENT_MONTH)) {
				String groupName = year + "-" + up.getValue() + ":" + profile.getName();
				Group group = unittype.getGroups().getByName(groupName);
				if (group == null) {
					group = new Group(groupName, "All shipped units in " + year + "-" + up.getValue(), yearGroup, unittype, profile);
					unittype.getGroups().addOrChangeGroup(group, xaps);
					GroupParameter gp = new GroupParameter(up.getParameter(), group);
					group.getGroupParameters().addOrChangeGroupParameter(gp, xaps);
				}
				month = up.getValue();
				monthGroup = group;
				break;
			}
		}
		for (UnitParameter up : unitParams) {
			if (up.getParameter().getUnittypeParameter().getName().equals(SystemParameters.STAGING_SHIPMENT_DATE)) {
				String date = year + "-" + month + "-" + up.getValue();
				String groupName = date + ":" + profile.getName();
				Group group = unittype.getGroups().getByName(groupName);
				if (group == null) {
					group = new Group(groupName, "All shipped units in " + date, monthGroup, unittype, profile);
					unittype.getGroups().addOrChangeGroup(group, xaps);
					GroupParameter gp = new GroupParameter(up.getParameter(), group);
					group.getGroupParameters().addOrChangeGroupParameter(gp, xaps);
				}
				break;
			}
		}
		//		String groupName = "shipment:" + shipment.getShipmentname();
		//		Group group = unittype.getGroups().getByName(groupName);
		//		if (group == null) {
		//			group = new Group(groupName /*+ ":" + shipment.getTimestamp()*/, "All units in shipment " + shipment.getShipmentname(), null, unittype, profile);
		//			unittype.getGroups().addOrChangeGroup(group, xaps);
		//			UnittypeParameter utp = unittype.getUnittypeParameters().getByName(SystemParameters.STAGING_SHIPMENT);
		//			GroupParameter gp = new GroupParameter(group, true, utp, shipment.getShipmentname() /*+ ":" + shipment.getTimestamp()*/);
		//			group.getGroupParameters().addOrChangeGroupParameter(gp, xaps);
		//		}
		createShipment(profile, shipment.getUnits(), SessionCache.getSessionData(sessionId).getUser().getUsername());
		return shipment;
	}

	/**
	 * The Class WebServiceParams.
	 */
	class WebServiceParams {

		/** The serial. */
		//		private String serial;

		/** The wsuser. */
		private String wsuser;

		/** The wsurl. */
		private String wsurl;

		/** The wspass. */
		private String wspass;

		/** The email. */
		private String email;

		/** The unittype. */
		private String unittype;

		/** The profile. */
		private String profile;

		/** The protocol. */
		//		private String protocol;
		//
		//		/** The secret. */
		//		private String secret;

		/**
		 * Gets the secret.
		 *
		 * @return the secret
		 */
		//		public String getSecret() {
		//			return secret;
		//		}

		/**
		 * Sets the secret.
		 *
		 * @param secret the new secret
		 */
		//		public void setSecret(String secret) {
		//			this.secret = secret;
		//		}

		/**
		 * Instantiates a new web service params.
		 *
		 * @param wsurl the wsurl
		 * @param wsuser the wsuser
		 * @param wspass the wspass
		 * @param email the email
		 * @param unittype the unittype
		 * @param profile the profile
		 */
		public WebServiceParams(String wsurl, String wsuser, String wspass, String email, String unittype, String profile/*, String serial, String protocol, String secret*/) {
			this.wsurl = wsurl != null ? wsurl : new String();
			this.unittype = unittype != null ? unittype : new String();
			this.profile = profile != null ? profile : new String();
			this.wsuser = wsuser != null ? wsuser : new String();
			this.wspass = wspass != null ? wspass : new String();
			//			this.serial = serial != null ? serial : new String();
			this.email = email != null ? email : new String();
			//			this.protocol = protocol != null ? protocol : new String();
			//			this.secret = secret != null ? secret : new String();
		}

		/**
		 * Gets the wsurl.
		 *
		 * @return the wsurl
		 */
		public String getWsurl() {
			return wsurl;
		}

		/**
		 * Sets the wsurl.
		 *
		 * @param wsurl the new wsurl
		 */
		public void setWsurl(String wsurl) {
			this.wsurl = wsurl;
		}

		/**
		 * Gets the unittype.
		 *
		 * @return the unittype
		 */
		public String getUnittype() {
			return unittype;
		}

		/**
		 * Sets the unittype.
		 *
		 * @param unittype the new unittype
		 */
		public void setUnittype(String unittype) {
			this.unittype = unittype;
		}

		/**
		 * Gets the profile.
		 *
		 * @return the profile
		 */
		public String getProfile() {
			return profile;
		}

		/**
		 * Sets the profile.
		 *
		 * @param profile the new profile
		 */
		public void setProfile(String profile) {
			this.profile = profile;
		}

		/**
		 * Gets the wspass.
		 *
		 * @return the wspass
		 */
		public String getWspass() {
			return wspass;
		}

		/**
		 * Sets the wspass.
		 *
		 * @param wspass the new wspass
		 */
		public void setWspass(String wspass) {
			this.wspass = wspass;
		}

		/**
		 * Sets the wsuser.
		 *
		 * @param wsuser the new wsuser
		 */
		public void setWsuser(String wsuser) {
			this.wsuser = wsuser;
		}

		/**
		 * Gets the wsuser.
		 *
		 * @return the wsuser
		 */
		public String getWsuser() {
			return wsuser;
		}

		/**
		 * Gets the serial.
		 *
		 * @return the serial
		 */
		//		public String getSerial() {
		//			return serial;
		//		}
		//
		//		/**
		//		 * Sets the serial.
		//		 *
		//		 * @param serial the new serial
		//		 */
		//		public void setSerial(String serial) {
		//			this.serial = serial;
		//		}

		/**
		 * Gets the email.
		 *
		 * @return the email
		 */
		public String getEmail() {
			return email;
		}

		/**
		 * Sets the email.
		 *
		 * @param email the new email
		 */
		public void setEmail(String email) {
			this.email = email;
		}

		/**
		 * Gets the protocol.
		 *
		 * @return the protocol
		 */
		//		public String getProtocol() {
		//			return protocol;
		//		}
		//
		//		/**
		//		 * Sets the protocol.
		//		 *
		//		 * @param protocol the new protocol
		//		 */
		//		public void setProtocol(String protocol) {
		//			this.protocol = protocol;
		//		}

	}

	/**
	 * Action create provider.
	 *
	 * @param unittype the unittype
	 * @param sessionId the session id
	 * @param providerName the provider name
	 * @param params the params
	 * @param fromFwVersion the from fw version
	 * @param toFwVersion the to fw version
	 * @throws SQLException the sQL exception
	 * @throws NoAvailableConnectionException the no available connection exception
	 */
	public void actionCreateProvider(Unittype unittype, String sessionId, String providerName, WebServiceParams params, String fromFwVersion, String toFwVersion) throws SQLException,
			NoAvailableConnectionException {
		String jobAndGroupName = "Upgrade from " + (fromFwVersion != null ? fromFwVersion : "any") + " for " + providerName;

		XAPS xaps = XAPSLoader.getXAPS(sessionId);
		Profile profile = unittype.getProfiles().getByName(providerName);
		if (profile == null) {
			profile = new Profile(providerName, unittype);
			unittype.getProfiles().addOrChangeProfile(profile, xaps);
		}

		setProfileParameter(SystemParameters.STAGING_PROVIDER_WSURL, params.getWsurl(), unittype, profile, xaps);
		setProfileParameter(SystemParameters.STAGING_PROVIDER_WSUSER, params.getWsuser(), unittype, profile, xaps);
		setProfileParameter(SystemParameters.STAGING_PROVIDER_WSPASSWORD, params.getWspass(), unittype, profile, xaps);
		setProfileParameter(SystemParameters.STAGING_PROVIDER_EMAIL, params.getEmail(), unittype, profile, xaps);
		setProfileParameter(SystemParameters.STAGING_PROVIDER_UNITTYPE, params.getUnittype(), unittype, profile, xaps);
		setProfileParameter(SystemParameters.STAGING_PROVIDER_PROFILE, params.getProfile(), unittype, profile, xaps);
		//		setProfileParameter(SystemParameters.STAGING_PROVIDER_SNPARAMETER, params.getSerial(), unittype, profile, xaps);
		//		setProfileParameter(SystemParameters.STAGING_PROVIDER_PROTOCOL, params.getProtocol(), unittype, profile, xaps);
		//		setProfileParameter(SystemParameters.STAGING_PROVIDER_SECPARAMETER, params.getSecret(), unittype, profile, xaps);

		Group group = unittype.getGroups().getByName(jobAndGroupName);
		if (group == null) {
			group = new Group(jobAndGroupName, "All units for provider " + providerName, null, unittype, profile);
			unittype.getGroups().addOrChangeGroup(group, xaps);
			UnittypeParameter versionUtp = unittype.getUnittypeParameters().getByName(DEVICE_SOFTWARE_VERSION);
			if (versionUtp == null)
				versionUtp = unittype.getUnittypeParameters().getByName(IGD_SOFTWARE_VERSION);
			if (versionUtp == null) {
				errors.put("Trying to add SoftwareVersion to " + jobAndGroupName, "Could not find any matching keyroot on " + unittype.getName());
				return;
			}
			Parameter param = new Parameter(versionUtp, fromFwVersion);
			GroupParameter gParam = new GroupParameter(param, group);
			group.getGroupParameters().addOrChangeGroupParameter(gParam, xaps);
		}

		if (toFwVersion != null && toFwVersion.trim().length() > 0) {
			Jobs xapsJobs = unittype.getJobs();
			Job job = xapsJobs.getByName(jobAndGroupName);
			if (job == null) {
				JobFlag jf = new JobFlag(JobType.SOFTWARE, JobServiceWindow.DISRUPTIVE);
				String desc = "Upgrade from " + (fromFwVersion != null ? fromFwVersion : "any") + " to " + toFwVersion;
				File f = unittype.getFiles().getByVersionType(fromFwVersion, FileType.SOFTWARE);
				job = new Job(unittype, jobAndGroupName, jf, desc, group, 60, null, f, null, null, null);
				xapsJobs.add(job, xaps);
				List<JobParameter> jobParameters = new ArrayList<JobParameter>();
				UnittypeParameter utp = unittype.getUnittypeParameters().getByName(SystemParameters.DESIRED_SOFTWARE_VERSION);
				JobParameter jp = new JobParameter(job, Job.ANY_UNIT_IN_GROUP, new Parameter(utp, toFwVersion));
				jobParameters.add(jp);
				xapsJobs.addOrChangeJobParameters(jobParameters, xaps);
				job.setStatus(JobStatus.STARTED);
				xapsJobs.changeStatus(job, xaps);
			} else if (job.getFile().getVersion().equals(fromFwVersion)) {
				List<JobParameter> jobParameters = new ArrayList<JobParameter>();
				UnittypeParameter utp = unittype.getUnittypeParameters().getByName(SystemParameters.DESIRED_SOFTWARE_VERSION);
				JobParameter jp = new JobParameter(job, Job.ANY_UNIT_IN_GROUP, new Parameter(utp, toFwVersion));
				jobParameters.add(jp);
				xapsJobs.addOrChangeJobParameters(jobParameters, xaps);
				warnings.put(providerName, "Upgrade job from " + fromFwVersion + " is now upgrading to " + toFwVersion);
			}
		}
	}

	/**
	 * Sets the profile parameter.
	 *
	 * @param utp the utp
	 * @param value the value
	 * @param unittype the unittype
	 * @param profile the profile
	 * @param xaps the xaps
	 * @throws SQLException the sQL exception
	 * @throws NoAvailableConnectionException the no available connection exception
	 */
	public void setProfileParameter(String utp, String value, Unittype unittype, Profile profile, XAPS xaps) throws SQLException, NoAvailableConnectionException {
		UnittypeParameter unittypeParameter = getUTP(utp, unittype, xaps);
		ProfileParameter profileParameter = profile.getProfileParameters().getByName(utp);
		if (profileParameter == null)
			profileParameter = new ProfileParameter(profile, unittypeParameter, value);
		else
			profileParameter.setValue(value);
		profile.getProfileParameters().addOrChangeProfileParameter(profileParameter, xaps);
	}

	/**
	 * Action delete provider.
	 *
	 * @param unittype the unittype
	 * @param profile the profile
	 * @param sessionId the session id
	 * @throws NoAvailableConnectionException the no available connection exception
	 * @throws SQLException the sQL exception
	 */
	public void actionDeleteProvider(Unittype unittype, Profile profile, String sessionId) throws NoAvailableConnectionException, SQLException {
		XAPS xaps = XAPSLoader.getXAPS(sessionId);
		XAPSUnit xapsUnit = XAPSLoader.getXAPSUnit(sessionId);
		Map<String, Unit> units = xapsUnit.getUnits(unittype, profile, new ArrayList<Parameter>(), Integer.MAX_VALUE);

		if (units.size() > 0) {
			for (Unit u : units.values()) {
				errors.put(u.getId(), "Unit found in provider " + profile.getName() + " - cancel shipment if possible");
			}
		} else {
			Jobs jobs = unittype.getJobs();
			for (Job job : jobs.getJobs()) {
				Profile groupProfile = job.getGroup().getProfile();
				if (groupProfile != null && groupProfile.getId() == profile.getId()) {
					job.setStatus(JobStatus.PAUSED);
					jobs.changeStatus(job, xaps);
					job.setStatus(JobStatus.COMPLETED);
					jobs.changeStatus(job, xaps);
					jobs.delete(job, xaps);
					Group group = job.getGroup();
					group.setProfile(null);
					unittype.getGroups().deleteGroup(group, xaps);
				}
			}
			Group[] groups = unittype.getGroups().getGroups();
			for (Group group : groups) {
				Profile groupProfile = group.getProfile();
				if (groupProfile != null && groupProfile.getId() == profile.getId())
					unittype.getGroups().deleteGroup(group, xaps);
			}
			unittype.getProfiles().deleteProfile(profile, xaps, true);
		}
	}

	/**
	 * Post mail.
	 *
	 * @param subjectStr the subject str
	 * @param messageStr the message str
	 * @param receiversStr the receivers str
	 * @throws MessagingException the messaging exception
	 */
	private static void postMail(String subjectStr, String messageStr, String receiversStr) throws MessagingException {
		try {
			String host = "smtp.googlemail.com";
			int port = 587; // must in some cases be set 587 (gmail)
			String from = "xaps-staging@pingcom.net";
			String pass = "ja0gnei2010"; // not in use when auth is false
			boolean auth = true;
			boolean tls = true;
			String[] replyToArr = new String[] { "support@pingcom.net" };

			Properties props = System.getProperties();
			props.put("mail.smtp.starttls.enable", tls); // added this line
			props.put("mail.smtp.host", host);
			props.put("mail.smtp.user", from);
			props.put("mail.smtp.password", pass);
			props.put("mail.smtp.port", port);
			props.put("mail.smtp.auth", auth);

			String[] to = receiversStr.split(",");
			Session session = Session.getDefaultInstance(props, null);
			MimeMessage message = new MimeMessage(session);
			message.setFrom(new InternetAddress(from));
			InternetAddress[] toAddress = new InternetAddress[to.length];
			for (int i = 0; i < to.length; i++)
				toAddress[i] = new InternetAddress(to[i]);
			for (int i = 0; i < toAddress.length; i++)
				message.addRecipient(Message.RecipientType.TO, toAddress[i]);
			InternetAddress[] replyToAddr = new InternetAddress[replyToArr.length];
			for (int i = 0; i < replyToArr.length; i++)
				replyToAddr[i] = new InternetAddress(replyToArr[i]);
			message.setSubject(subjectStr);
			message.setReplyTo(replyToAddr);
			message.setText(messageStr);
			Transport transport = session.getTransport("smtp");
			transport.connect(host, from, pass);
			transport.sendMessage(message, message.getAllRecipients());
			transport.close();
		} catch (MessagingException me) {
			logger.error("StagingActions.postMail(): No email was sent, messaging exception occurred", me);
			me.printStackTrace();
			throw me;
		} catch (Throwable t) {
			logger.error("StagingActions.postMail(): No email was sent, unexpected rror occurred in ", t);
			t.printStackTrace();
		}
	}

	/**
	 * Gets the shipments.
	 *
	 * @param unittype the unittype
	 * @param profile the profile
	 * @param shipmentInput the shipment input
	 * @param xaps the xaps
	 * @return the shipments
	 * @throws Exception the exception
	 */
	protected DropDownSingleSelect<Shipment> getShipments(Unittype unittype, Profile profile, Input shipmentInput, XAPS xaps) throws Exception {
		List<Shipment> shipments = new ArrayList<Shipment>();
		Shipment selected = null;
		Group[] groups = unittype.getGroups().getGroups();
		for (Group g : groups) {
			Profile groupProfile = g.getTopParent().getProfile();
			boolean canceled = false;
			if (groupProfile != null) {
				canceled = groupProfile.getName().equals("Default");
				if (!canceled && !groupProfile.getName().equals(profile.getName()))
					continue;
			}
			if (g.getName().equals(profile.getName()))
				continue;
			GroupParameter[] gParams = g.getGroupParameters().getGroupParameters();
			for (GroupParameter gp : gParams) {
				if (gp.getName().contains(SystemParameters.STAGING_SHIPMENT_MONTH) && gp.getParameter().getValue() != null) {
					String shipmentName = g.getName();
					Shipment shipment = new Shipment(shipmentName, canceled);
					shipments.add(shipment);
					if (shipment.getName().equals(shipmentInput.getString()))
						selected = shipment;
				}
			}
		}
		Collections.sort(shipments, new ShipmentComparator());
		return InputSelectionFactory.getDropDownSingleSelect(shipmentInput, selected, shipments);
	}

	/**
	 * Gets the allowed profiles except default.
	 *
	 * @param selected the selected
	 * @param sessionId the session id
	 * @return the allowed profiles except default
	 * @throws NoAvailableConnectionException the no available connection exception
	 * @throws SQLException the sQL exception
	 */
	protected List<Profile> getAllowedProfilesExceptDefault(Unittype selected, String sessionId) throws NoAvailableConnectionException, SQLException {
		List<Profile> allowedProfiles = new ArrayList<Profile>();
		for (Profile p : getAllowedProfiles(sessionId, selected)) {
			if (!p.getName().equals("Default"))
				allowedProfiles.add(p);
		}
		return allowedProfiles;
	}
}
