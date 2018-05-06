package com.owera.xaps.web.app.page.staging;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import com.owera.xaps.dbi.Profile;
import com.owera.xaps.dbi.ProfileParameter;
import com.owera.xaps.dbi.Unittype;
import com.owera.xaps.dbi.util.SystemParameters;
import com.owera.xaps.web.app.input.Input;
import com.owera.xaps.web.app.input.InputData;

/**
 * The Class StagingProviderData.
 */
public class StagingProviderData extends InputData {

	/** The provider wsurl. */
	private Input providerWsurl = Input.getStringInput("provider_wsurl");

	/** The provider wsuser. */
	private Input providerWsuser = Input.getStringInput("provider_wsuser");

	/** The provider wspass. */
	private Input providerWspass = Input.getStringInput("provider_wspass");

	/** The provider email. */
	private Input providerEmail = Input.getEmailInput("provider_email");

	/** The provider unittype. */
	private Input providerUnittype = Input.getStringInput("provider_unittype");

	/** The provider profile. */
	private Input providerProfile = Input.getStringInput("provider_profile");

	/** The provider serial. */
	//	private Input providerSerial = Input.getStringInput("provider_serialnumber");

	/** The provider protocol. */
	//	private Input providerProtocol = Input.getStringInput("provider_protocol");

	/** The provider secret. */
	//	private Input providerSecret = Input.getStringInput("provider_secret");

	/**
	 * Gets the provider secret.
	 *
	 * @return the provider secret
	 */
	//	public Input getProviderSecret() {
	//		return providerSecret;
	//	}

	/**
	 * Sets the provider secret.
	 *
	 * @param providerSecret the new provider secret
	 */
//	public void setProviderSecret(Input providerSecret) {
//		this.providerSecret = providerSecret;
//	}

	/** The provider name. */
	private Input providerName = Input.getStringInput("providername");

	/** The from software. */
	private Input fromSoftware = Input.getStringInput("fromsoftware");

	/** The to software. */
	private Input toSoftware = Input.getStringInput("tosoftware");

	/** The shipment. */
	private Input shipment = Input.getStringInput("shipment");

	/**
	 * Instantiates a new staging provider data.
	 */
	public StagingProviderData() {
		super.CMD_ADD = "Add new provider";
		super.CMD_UPDATE = "Update provider";
		super.CMD_DELETE = "Delete provider";
	}

	/**
	 * Checks if is adds the upgrade job operation.
	 *
	 * @return true, if is adds the upgrade job operation
	 */
	public boolean isAddUpgradeJobOperation() {
		return getFormSubmit().isValue("Add new upgrade job");
	}

	/* (non-Javadoc)
	 * @see com.owera.xaps.web.app.input.InputData#bindForm(java.util.Map)
	 */
	@Override
	protected void bindForm(Map<String, Object> root) {
		root.put(providerName.getKey(), providerName.getString());
		root.put(providerWsurl.getKey(), providerWsurl.getString());
		root.put(providerWsuser.getKey(), providerWsuser.getString());
		root.put(providerWspass.getKey(), providerWspass.getString());
		root.put(providerUnittype.getKey(), providerUnittype.getString());
		root.put(providerProfile.getKey(), providerProfile.getString());
//		root.put(providerSerial.getKey(), providerSerial.getString());
		root.put(providerEmail.getKey(), providerEmail.getEmail());
//		root.put(providerProtocol.getKey(), providerProtocol.getString());
//		root.put(providerSecret.getKey(), providerSecret.getString());
	}

	/* (non-Javadoc)
	 * @see com.owera.xaps.web.app.input.InputData#validateForm()
	 */
	@Override
	protected boolean validateForm() {
		boolean valid = true;

		if (providerEmail.getError() != null)
			return false;

		//		if (providerProtocol.getError() != null)
		//			return false;

		if (isAddOperation())
			valid = validateAddProvider();
		else if (isUpdateOperation())
			valid = validateUpdateProvider();

		return valid;
	}

	/**
	 * Validate add provider.
	 *
	 * @return true, if successful
	 */
	private boolean validateAddProvider() {
		boolean valid = validateUpdateProvider();

		if (providerName.getError() != null)
			valid = false;
		else if (providerName.isNullOrValue("")) {
			providerName.setError("Provider name is required");
			valid = false;
		}

		//		if (toSoftware.getError() != null)
		//			valid = false;
		//		else if (toSoftware.isNullOrValue("")) {
		//			toSoftware.setError("To software is required");
		//			valid = false;
		//		}

		return valid;
	}

	/**
	 * Validate update provider.
	 *
	 * @return true, if successful
	 */
	private boolean validateUpdateProvider() {
		boolean valid = true;

		boolean providerEmailBlank = (providerEmail.getError() == null && providerEmail.isNullOrValue(""));
		boolean providerWsurlBlank = (providerWsurl.getError() == null && providerWsurl.isNullOrValue(""));
		if (providerEmailBlank && providerWsurlBlank) {
			providerEmail.setError("Email or web service url is required");
			valid = false;
		}

		valid = isProtocolValid();

//		if (providerSecret.getError() != null)
//			valid = false;
//		else if (providerSecret.isNullOrValue("")) {
//			providerSecret.setError("Secret parameter name is required");
//			valid = false;
//		}

//		if (providerSerial.getError() != null)
//			valid = false;
//		else if (providerSerial.isNullOrValue("")) {
//			providerSerial.setError("Serial number parameter name is required");
//			valid = false;
//		}

		return valid;
	}

	/**
	 * Checks if is protocol valid.
	 * 
	 * Refactored, protocol doesn't matter anymore, always using TR-069
	 *
	 * @return true, if is protocol valid
	 */
	private boolean isProtocolValid() {
		return true;
		//		boolean providerProtcolBlank = (providerProtocol.getError()==null && providerProtocol.isNullOrValue(""));
		//		if (providerProtcolBlank){
		//			providerProtocol.setError("Protocol is required.");
		//			return false;
		//		}else if (!providerProtocol.getValue().equals(SystemConstants.OPP) && !providerProtocol.getValue().equals(SystemConstants.TR069)){ 
		//			providerProtocol.setError("Protocol must be " + SystemConstants.OPP + " or " +SystemConstants.TR069);
		//			return false;
		//		}else
		//			return true;
	}

	/**
	 * Clear inputs.
	 *
	 * @throws IllegalArgumentException the illegal argument exception
	 * @throws SecurityException the security exception
	 * @throws IllegalAccessException the illegal access exception
	 * @throws InvocationTargetException the invocation target exception
	 * @throws NoSuchMethodException the no such method exception
	 */
	protected void clearInputs() throws IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		getProfile().setValue(null);
		getProviderWsurl().setValue(null);
		getProviderWsuser().setValue(null);
		getProviderWspass().setValue(null);
		getProviderEmail().setValue(null);
		getProviderUnittype().setValue(null);
		getProviderProfile().setValue(null);
		getFromSoftware().setValue(null);
		getToSoftware().setValue(null);
//		getProviderSerial().setValue(null);
//		getProviderProtocol().setValue(null);
		//		getProviderSecret().setValue(null);
	}

	/**
	 * Sets input value if parameter is set and input value is null or "".
	 *
	 * @param unittype the unittype
	 * @param profile the profile
	 * @throws Exception the exception
	 */
	protected void populateFieldData(Unittype unittype, Profile profile) throws Exception {
		ProfileParameter provider_wsurl = profile.getProfileParameters().getByName(SystemParameters.STAGING_PROVIDER_WSURL);
		if (provider_wsurl != null && !getProviderWsurl().notNullNorValue(""))
			getProviderWsurl().setValue(provider_wsurl.getValue());
		ProfileParameter provider_unittype = profile.getProfileParameters().getByName(SystemParameters.STAGING_PROVIDER_UNITTYPE);
		if (provider_unittype != null && !getProviderUnittype().notNullNorValue(""))
			getProviderUnittype().setValue(provider_unittype.getValue());
		ProfileParameter provider_profile = profile.getProfileParameters().getByName(SystemParameters.STAGING_PROVIDER_PROFILE);
		if (provider_profile != null && !getProviderProfile().notNullNorValue(""))
			getProviderProfile().setValue(provider_profile.getValue());
		ProfileParameter provider_wspass = profile.getProfileParameters().getByName(SystemParameters.STAGING_PROVIDER_WSPASSWORD);
		if (provider_wspass != null && !getProviderWspass().notNullNorValue(""))
			getProviderWspass().setValue(provider_wspass.getValue());
		ProfileParameter provider_wsuser = profile.getProfileParameters().getByName(SystemParameters.STAGING_PROVIDER_WSUSER);
		if (provider_wsuser != null && !getProviderWsuser().notNullNorValue(""))
			getProviderWsuser().setValue(provider_wsuser.getValue());
//		ProfileParameter provider_serial = profile.getProfileParameters().getByName(SystemParameters.STAGING_PROVIDER_SNPARAMETER);
//		if (provider_serial != null && !getProviderSerial().notNullNorValue(""))
//			getProviderSerial().setValue(provider_serial.getValue());
		ProfileParameter provider_email = profile.getProfileParameters().getByName(SystemParameters.STAGING_PROVIDER_EMAIL);
		if (provider_email != null && !getProviderEmail().notNullNorValue(""))
			getProviderEmail().setValue(provider_email.getValue());
//		ProfileParameter provider_protocol = profile.getProfileParameters().getByName(SystemParameters.STAGING_PROVIDER_PROTOCOL);
//		if (provider_protocol != null && !getProviderProtocol().notNullNorValue(""))
//			getProviderProtocol().setValue(provider_protocol.getValue());
//		ProfileParameter provider_secret = profile.getProfileParameters().getByName(SystemParameters.STAGING_PROVIDER_SECPARAMETER);
//		if (provider_secret != null && !getProviderSecret().notNullNorValue(""))
//			getProviderSecret().setValue(provider_secret.getValue());
	}

	/**
	 * Gets the provider wsurl.
	 *
	 * @return the provider wsurl
	 */
	public Input getProviderWsurl() {
		return providerWsurl;
	}

	/**
	 * Sets the provider wsurl.
	 *
	 * @param providerWsurl the new provider wsurl
	 */
	public void setProviderWsurl(Input providerWsurl) {
		this.providerWsurl = providerWsurl;
	}

	/**
	 * Gets the provider wsuser.
	 *
	 * @return the provider wsuser
	 */
	public Input getProviderWsuser() {
		return providerWsuser;
	}

	/**
	 * Sets the provider wsuser.
	 *
	 * @param providerWsuser the new provider wsuser
	 */
	public void setProviderWsuser(Input providerWsuser) {
		this.providerWsuser = providerWsuser;
	}

	/**
	 * Gets the provider wspass.
	 *
	 * @return the provider wspass
	 */
	public Input getProviderWspass() {
		return providerWspass;
	}

	/**
	 * Sets the provider wspass.
	 *
	 * @param providerWspass the new provider wspass
	 */
	public void setProviderWspass(Input providerWspass) {
		this.providerWspass = providerWspass;
	}

	/**
	 * Gets the provider email.
	 *
	 * @return the provider email
	 */
	public Input getProviderEmail() {
		return providerEmail;
	}

	/**
	 * Sets the provider email.
	 *
	 * @param providerEmail the new provider email
	 */
	public void setProviderEmail(Input providerEmail) {
		this.providerEmail = providerEmail;
	}

	/**
	 * Gets the provider unittype.
	 *
	 * @return the provider unittype
	 */
	public Input getProviderUnittype() {
		return providerUnittype;
	}

	/**
	 * Sets the provider unittype.
	 *
	 * @param providerUnittype the new provider unittype
	 */
	public void setProviderUnittype(Input providerUnittype) {
		this.providerUnittype = providerUnittype;
	}

	/**
	 * Gets the provider profile.
	 *
	 * @return the provider profile
	 */
	public Input getProviderProfile() {
		return providerProfile;
	}

	/**
	 * Sets the provider profile.
	 *
	 * @param providerProfile the new provider profile
	 */
	public void setProviderProfile(Input providerProfile) {
		this.providerProfile = providerProfile;
	}

	/**
	 * Gets the provider serial.
	 *
	 * @return the provider serial
	 */
	//	public Input getProviderSerial() {
	//		return providerSerial;
	//	}

	/**
	 * Sets the provider serial.
	 *
	 * @param providerSerial the new provider serial
	 */
//	public void setProviderSerial(Input providerSerial) {
//		this.providerSerial = providerSerial;
//	}

	/**
	 * Gets the from software.
	 *
	 * @return the from software
	 */
	public Input getFromSoftware() {
		return fromSoftware;
	}

	/**
	 * Sets the from software.
	 *
	 * @param fromSoftware the new from software
	 */
	public void setFromSoftware(Input fromSoftware) {
		this.fromSoftware = fromSoftware;
	}

	/**
	 * Gets the to software.
	 *
	 * @return the to software
	 */
	public Input getToSoftware() {
		return toSoftware;
	}

	/**
	 * Sets the to software.
	 *
	 * @param toSoftware the new to software
	 */
	public void setToSoftware(Input toSoftware) {
		this.toSoftware = toSoftware;
	}

	/**
	 * Gets the provider name.
	 *
	 * @return the provider name
	 */
	public Input getProviderName() {
		return providerName;
	}

	/**
	 * Sets the provider name.
	 *
	 * @param providerName the new provider name
	 */
	public void setProviderName(Input providerName) {
		this.providerName = providerName;
	}

	/**
	 * Gets the shipment.
	 *
	 * @return the shipment
	 */
	public Input getShipment() {
		return shipment;
	}

	/**
	 * Sets the shipment.
	 *
	 * @param shipment the new shipment
	 */
	public void setShipment(Input shipment) {
		this.shipment = shipment;
	}

	/**
	 * Gets the provider protocol.
	 *
	 * @return the provider protocol
	 */
//	public Input getProviderProtocol() {
//		return providerProtocol;
//	}

	/**
	 * Sets the provider protocol.
	 *
	 * @param providerProtocol the new provider protocol
	 */
	//	public void setProviderProtocol(Input providerProtocol) {
	//		this.providerProtocol = providerProtocol;
	//	}

}
