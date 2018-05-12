package com.github.freeacs.web.app.page.search;

import com.github.freeacs.web.app.input.Input;
import com.github.freeacs.web.app.input.InputData;

import java.util.Map;


/**
 * The Class SearchData.
 */
public class SearchData extends InputData {
	
	/** The unit param value. */
	private Input unitParamValue = Input.getStringInput("unitparamvalue");
	
	/** The limit. */
	private Input limit = Input.getIntegerInput("limit");
	
	/** The mark box. */
	private Input markBox = Input.getStringInput("markbox");
	
	/** The cmd. */
	private Input cmd = Input.getStringInput("cmd");
	
	/** The url. */
	private Input url = Input.getStringInput("url");
	
	/** The advanced. */
	private Input advanced = Input.getBooleanInput("advancedView");
	
	/**
	 * Gets the limit.
	 *
	 * @return the limit
	 */
	public Input getLimit() {
		return limit;
	}

	/**
	 * Sets the limit.
	 *
	 * @param limit the new limit
	 */
	public void setLimit(Input limit) {
		this.limit = limit;
	}

	/**
	 * Sets the mark box.
	 *
	 * @param markBox the new mark box
	 */
	public void setMarkBox(Input markBox) {
		this.markBox = markBox;
	}

	/**
	 * Gets the mark box.
	 *
	 * @return the mark box
	 */
	public Input getMarkBox() {
		return markBox;
	}

	/* (non-Javadoc)
	 * @see com.owera.xaps.web.app.input.InputData#setCmd(com.owera.xaps.web.app.input.Input)
	 */
	public void setCmd(Input cmd) {
		this.cmd = cmd;
	}

	/* (non-Javadoc)
	 * @see com.owera.xaps.web.app.input.InputData#getCmd()
	 */
	public Input getCmd() {
		return cmd;
	}

	/**
	 * Sets the url.
	 *
	 * @param history the new url
	 */
	public void setUrl(Input history) {
		this.url = history;
	}

	/**
	 * Gets the url.
	 *
	 * @return the url
	 */
	public Input getUrl() {
		return url;
	}

	/* (non-Javadoc)
	 * @see com.owera.xaps.web.app.input.InputData#bindForm(java.util.Map)
	 */
	@Override
	public void bindForm(Map<String, Object> root) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see com.owera.xaps.web.app.input.InputData#validateForm()
	 */
	@Override
	public boolean validateForm() {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * Gets the unit param value.
	 *
	 * @return the unit param value
	 */
	public Input getUnitParamValue() {
		return unitParamValue;
	}

	/**
	 * Sets the unit param value.
	 *
	 * @param unitParamValue the new unit param value
	 */
	public void setUnitParamValue(Input unitParamValue) {
		this.unitParamValue = unitParamValue;
	}

	/**
	 * Sets the advanced.
	 *
	 * @param advanced the new advanced
	 */
	public void setAdvanced(Input advanced) {
		this.advanced = advanced;
	}

	/**
	 * Gets the advanced.
	 *
	 * @return the advanced
	 */
	public Input getAdvanced() {
		return advanced;
	}

}
