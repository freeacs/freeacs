package com.github.freeacs.dbi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.Map;


public class Certificates {
	private static Logger logger = LoggerFactory.getLogger(Certificates.class);
	private Map<String, Certificate> nameMap;
	private Map<Integer, Certificate> idMap;

	public Certificates(Map<Integer, Certificate> idMap, Map<String, Certificate> nameMap) {
		this.idMap = idMap;
		this.nameMap = nameMap;
	}

	public Certificate getById(Integer id) {
		return idMap.get(id);
	}

	public Certificate getByName(String name) {
		return nameMap.get(name);
	}

	public Certificate[] getCertificates() {
		Certificate[] Certificates = nameMap.values().toArray(new Certificate[] {});
		return Certificates;
	}

	@Override
	public String toString() {
		return "Contains " + nameMap.size() + " Certificates";
	}

	public Certificate getCertificate(String certType) {
		for (Certificate cert : nameMap.values()) {
			if (cert.getCertType().equals(certType))
				return cert;
		}
		return null;
	}

	/**
	 * This method is only to be used by UnitQuery2 - because that class needs a
	 * structure of Unittype and Certificates to store it's query parameters
	 * 
	 * @param certificate
	 */
	protected void addCertificate(Certificate certificate) {
		idMap.put(certificate.getId(), certificate);
		nameMap.put(certificate.getName(), certificate);
	}

	public void addOrChangeCertificate(Certificate certificate, XAPS xaps) throws SQLException {
		if (!xaps.getUser().isAdmin())
			throw new IllegalArgumentException("Not allowed action for this user");
		Certificate replaceCert = null;
		for (Certificate cert : nameMap.values()) {
			if (cert.getCertType().equals(certificate.getCertType())) {
				if (!cert.isTrial() && certificate.isTrial())
					throw new IllegalArgumentException("Not allowed to replace a production certificate with a trial certificate");
				replaceCert = cert;
			}
		}
		if (replaceCert != null) {
			deleteCertificateImpl(replaceCert, xaps);
			nameMap.remove(replaceCert.getName());
			idMap.remove(replaceCert.getId());
		}
		addOrChangeCertificateImpl(certificate, xaps);
		nameMap.put(certificate.getName(), certificate);
		idMap.put(certificate.getId(), certificate);
		if (certificate.getOldName() != null) {
			nameMap.remove(certificate.getOldName());
			certificate.setOldName(null);
		}
	}

	private int deleteCertificateImpl(Certificate certificate, XAPS xaps) throws SQLException {
		Statement s = null;
		String sql = null;
		Connection c = xaps.getDataSource().getConnection();
		SQLException sqlex = null;
		try {
			s = c.createStatement();
			sql = "DELETE FROM certificate WHERE ";
			sql += "id = " + certificate.getId();
			s.setQueryTimeout(60);
			int rowsDeleted = s.executeUpdate(sql);
			
			logger.info("Deleted Certificate " + certificate.getName());
			if (xaps.getDbi() != null)
				xaps.getDbi().publishCertificate(certificate);
			return rowsDeleted;
		} catch (SQLException sqle) {
			sqlex = sqle;
			throw sqle;
		} finally {
			if (s != null)
				s.close();
			c.close();
		}
	}

	/**
	 * The first time this method is run, the flag is set. The second time this
	 * method is run, the parameter is removed from the name- and id-Map.
	 * 
	 * @throws SQLException
	 */
	public int deleteCertificate(Certificate certificate, XAPS xaps) throws SQLException {
		if (!xaps.getUser().isAdmin())
			throw new IllegalArgumentException("Not allowed action for this user");
		int rowsDeleted = deleteCertificateImpl(certificate, xaps);
		nameMap.remove(certificate.getName());
		idMap.remove(certificate.getId());
		return rowsDeleted;
	}

	private void addOrChangeCertificateImpl(Certificate certificate, XAPS xaps) throws SQLException {
		PreparedStatement ps = null;
		Connection c = xaps.getDataSource().getConnection();
		SQLException sqlex = null;
		try {
			DynamicStatement ds = new DynamicStatement();
			try {
				ds.setSql("INSERT INTO certificate (name, certificate) VALUES (?, ?)");
				ds.addArguments(certificate.getName());
				ds.addArguments(certificate.getCertificate());
				ps = ds.makePreparedStatement(c, "id");
				ps.setQueryTimeout(60);
				ps.executeUpdate();
				ResultSet gk = ps.getGeneratedKeys();
				if (gk.next())
					certificate.setId(gk.getInt(1));
				
				logger.info("Inserted Certificate " + certificate.getName());
				if (xaps.getDbi() != null)
					xaps.getDbi().publishCertificate(certificate);
			} catch (SQLException sqlex2) {
				ds.setSql("UPDATE certificate SET name = ?, certificate = ? WHERE id = ?");
				ds.addArguments(certificate.getName());
				ds.addArguments(certificate.getCertificate());
				ds.addArguments(certificate.getId());
				ps = ds.makePreparedStatement(c);
				ps.setQueryTimeout(60);
				int rowsUpdated = ps.executeUpdate();
				if (rowsUpdated == 0)
					throw sqlex2;
				
				logger.info("Updated Certificate " + certificate.getName());
				if (xaps.getDbi() != null)
					xaps.getDbi().publishCertificate(certificate);
			}
		} catch (SQLException sqle) {
			sqlex = sqle;
			throw sqle;
		} finally {
			if (ps != null)
				ps.close();
			c.close();
		}
	}

	protected Map<String, Certificate> getNameMap() {
		return nameMap;
	}

	protected Map<Integer, Certificate> getIdMap() {
		return idMap;
	}
}
